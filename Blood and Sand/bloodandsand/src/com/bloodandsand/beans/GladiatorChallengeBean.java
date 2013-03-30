/**
 *  Created by Andy Hayward
 *  Dec 23, 2012
 */
package com.bloodandsand.beans;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.bloodandsand.utilities.CoreBean;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;


public class GladiatorChallengeBean extends CoreBean implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1240080988276326974L;
	/*
	 * This bean contains the data required for managing challenges between gladiators
	 * When a gladiator challenges another gladiator, this bean is created, storing the challenger's 
	 * id, the date of the challenge, and any wager amounts.
	 * 
	 * If the incumbant gladiator accepts the challenge, the status of the challenge is set to confirmed
	 * and will be included in the next tournament
	 */
	
	boolean logEnabled = false;
	
	public Date challengeDate;
	private GladiatorDataBean challenger;
	private GladiatorDataBean incumbant;
	
	public LudusDataBean challengerLudus;
	public LudusDataBean incumbantLudus;
	
	public Key challengerKey;//always store these using the keytostring function
	public Key incumbantKey;
	
	private Key gladiatorChallengeKey;
	
	private long wager;
	private Status status = Status.INITIATED;
	
	protected static final Logger log = Logger.getLogger(GladiatorChallengeBean.class.getName());	
	
	private Entity thisEntity = new Entity(challengeEntity);
	
	public GladiatorChallengeBean(){
		/*
		 * TODO
		 */
	}
	
	public GladiatorChallengeBean(Entity challenge, boolean getGladiators){
		thisEntity = challenge;

		this.challengeDate = (Date) challenge.getProperty("challengeDate");
		
		this.challengerKey = (Key)challenge.getProperty("challengerKey");
		this.incumbantKey = (Key)challenge.getProperty("incumbantKey");
		
		
		this.gladiatorChallengeKey = challenge.getKey();
		
		
		this.wager = (Long)challenge.getProperty("wager");
		if (getGladiators){
			findChallenger();
			findIncumbant();
		}
		
		this.status = Status.valueOf((String)challenge.getProperty("status"));
		if (logEnabled) {log.info("challenge bean created from entity");}
	}
	
	public GladiatorChallengeBean(GladiatorDataBean challenger, GladiatorDataBean incumbant, Long wager){
		//sets up the variables
		challengeDate = new Date();
		this.challenger = challenger;
		this.incumbant = incumbant;
		this.challengerKey = challenger.getDataStoreKey();
		this.incumbantKey = incumbant.getDataStoreKey();
		
		this.wager = wager;
		this.status = Status.INITIATED;
		TournamentDataBean tourn = getTournament();
		Entity temp = new Entity(challengeEntity, tourn.getDataStoreKey());
		thisEntity = temp;
		setUpEntity();
		if (logEnabled) {log.info("challenge bean created with gladiators and wager");}
	}
	
	private void setUpEntity(){
		
		thisEntity.setProperty("challengeDate",this.challengeDate);
		thisEntity.setProperty("challengerKey", this.challengerKey);
		thisEntity.setProperty("incumbantKey", this.incumbantKey);
		thisEntity.setProperty("wager", this.wager);
		thisEntity.setProperty("status",this.status.toString());

	}
	
	public TournamentDataBean getTournament(){
		List<Entity> tournaments = null;
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query trn = new Query(tournamentEntity);
		Filter pnding = new FilterPredicate("status", FilterOperator.EQUAL, "Pending");
		trn.setFilter(pnding);
		trn.addSort("eventDate", SortDirection.DESCENDING);
		
		tournaments = datastore.prepare(trn).asList(FetchOptions.Builder.withDefaults().limit(MAX_TOURNAMENTS));
		if (tournaments == null || tournaments.size() == 0){//for those odd situations where there are no tournaments
			
			TournamentDataBean tournament = new TournamentDataBean();
			tournament.saveTournament();
			if (logEnabled) {log.info("No existing tournament. Created new tournament: " + tournament.getKey());}
			return tournament;
		} else {
			if (tournaments.size() > 1){
				log.warning("Multiple Pending tournaments in system.");
			}
			return new TournamentDataBean (tournaments.get(0), false);
		}
	}
	
	public void findLuduses(){
		//this simply populates the bean's ludus fields
		if (challenger == null || incumbant == null){
			findIncumbant();
			findChallenger();
		}
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query findLud = new Query(ludusEntity).setAncestor(challenger.getOwnerKey());
		Entity temp = datastore.prepare(findLud).asSingleEntity();
		challengerLudus = new LudusDataBean(temp);
		findLud = new Query(ludusEntity).setAncestor(incumbant.getOwnerKey());
		temp = datastore.prepare(findLud).asSingleEntity();
		incumbantLudus = new LudusDataBean(temp);
		if (logEnabled) {log.info("findluduses executed");}
	}
	
	public GladiatorChallengeBean createDummyChallenge(){
		GladiatorChallengeBean c = new GladiatorChallengeBean();
		GladiatorDataBean g = new GladiatorDataBean();
		
		c.challengeDate = new Date();
		c.challenger = g.getDummyGladiator();
		c.incumbant = g.getDummyGladiator();

		c.challenger.setName("challenger");
		c.challenger.setOwner("owner2", null);
		c.incumbant.setName("incumbant");
		c.incumbant.setOwner("owner1", null);
		c.wager = 10;
		setUpEntity();
		if (logEnabled) {log.info("Dummy challenge created");}
		return c;
	}
	
	public boolean acceptChallenge() {
		//check both gladiators to see if they have accepted a challenge. If not,
		// update this challenge and all of the gladiators' challenges to declined
		
		boolean successful = false;
		if (!challenger.isGladiatorAvailableToChallenge() || !incumbant.isGladiatorAvailableToChallenge()){
			log.warning("One of the gladiators was not available");
			return successful;
		} else {
			for (GladiatorChallengeBean chall: challenger.getChallenges()){
				if (!chall.getGladiatorChallengeKey().equals(this.getGladiatorChallengeKey())){
					chall.declineChallenge();
					chall.saveChallenge();
					if (logEnabled) {log.info("Wrong challenge");}
				} else {
					successful = true;
					if (logEnabled) {log.info("Found challenge");}				}
			}
			for (GladiatorChallengeBean chall: incumbant.getChallenges()){
				if (!chall.getGladiatorChallengeKey().equals(this.getGladiatorChallengeKey())){
					chall.declineChallenge();
					chall.saveChallenge();
					if (logEnabled) {log.info("Wrong challenge");}
				} else {
					successful = true;
					if (logEnabled) {log.info("Found challenge");}
				}
			}			
			if (successful){
				//first need to apply the wager to the incumbant. It was already set for the challenger when the challenge was created
				//but only if there is a wager
				
				this.status = Status.ACCEPTED;
				this.saveChallenge();
				if (logEnabled) {log.info("New status is " + this.status.toString());}
			}
			
			return successful;
		}			
	}
	
	public void saveChallenge() {
				
		//which looks the challenge up in the ds and updates it rather than saving a new one		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
				
		Transaction txn = datastore.beginTransaction();
		setUpEntity();

		try {					
		    datastore.put(thisEntity);
		    txn.commit();
		} finally {
		    if (txn.isActive()) {
		        txn.rollback();
		        log.warning("Save Existing Challenge transaction failed: rolled back");
		    }
		}		
	}

	public void expireChallenge() {
		status = Status.EXPIRED;
		if (wager > 0){
			refundWager();
		}

		thisEntity.setProperty("status",this.status.toString());
		if (logEnabled) {log.info("Challenge expired");}
	}
	
	public void declineChallenge() {
		this.status = Status.DECLINED;
		//need to update wager if exists
		if (wager > 0){
			refundWager();
		}
		thisEntity.setProperty("status",this.status.toString());
		if (logEnabled) {log.info("Challenge declined");}
	}
	
	public void rescindChallenge() {
		status = Status.CANCELED;
		
		if (wager > 0){
			refundWager();
		}

		thisEntity.setProperty("status",this.status.toString());
		if (logEnabled) {log.info("Challenge canceled");}
	}
	
	public Status getStatusEnum(){
		return this.status;
	}
	
	public String getStatusString(){
		return this.status.toString();
	}
	
	public void setStatus(Status sts){
		this.status = sts;	

		thisEntity.setProperty("status",this.status.toString());
	}
	
	private void refundWager() {
		//used when challenges are declined, expired or canceled
		if (wager > 0){
			if (challengerLudus == null){
				//go get challenger				
				this.findLuduses();
			} 
			challengerLudus.setWager(-(this.wager));
			challengerLudus.saveLudus();
			if (logEnabled) {log.info("refunded wager of : " + wager);}
			//

		}
	}
	
	public void findChallenger(){
		if (challenger != null){
			log.info("GladiatorChallenge.java: Found Challenger without database check");
			return;
		} else {
			//attempt to get from the key value
			if (challengerKey != null){
				GladiatorDataBean ch = new GladiatorDataBean();
				Entity challenger = ch.findGladiatorEntityByKey(challengerKey);
				this.challenger = new GladiatorDataBean(challenger);
			}
			 if (challenger == null){
				 log.warning("GladiatorChallengeBean: unable to resolve challenger");
				 return;
			 }
		}
	}
	
	public void findIncumbant(){
		
		if (incumbant != null){
			 	log.info("GladiatorChallenge.java: Found Incumbant without database check");
			return ;
		} else {
			//attempt to get from the key value
			if (incumbantKey != null){
				GladiatorDataBean ch = new GladiatorDataBean();
				Entity incumb = ch.findGladiatorEntityByKey(incumbantKey);
				this.incumbant = new GladiatorDataBean(incumb);
			}
			 if (incumbant == null){
				 log.warning("GladiatorChallengeBean: unable to resolve incumbant");
				 return;
			 }
		}
	}
	
	
	public void saveNewChallenge(){
		//ensure that all required fields are completed before initiating this method
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

		Transaction txn = datastore.beginTransaction();

		try {					
		    datastore.put(thisEntity);
		    txn.commit();
		} finally {
		    if (txn.isActive()) {
		        txn.rollback();
		        log.warning("Save New User transaction failed: rolled back");
		    }
		}
	}	

	public String getChallengerKey() {
		if (this.challengerKey != null){
			return KeyFactory.keyToString(this.challengerKey);
		} else {
			return null;
		}
	}
	
	public String getIncumbantKey() {
		if (this.incumbantKey != null){
			return KeyFactory.keyToString(this.incumbantKey);
		} else {
			return null;
		}
	}

	public void setChallenger(GladiatorDataBean challenger) {
		this.challenger = challenger;
		
	}
	
	public void setIncumbant(GladiatorDataBean incumb) {
		this.incumbant = incumb;
		
	}
	
	public GladiatorDataBean getChallenger() {
		return this.challenger ;
		
	}
	
	public GladiatorDataBean getIncumbant() {
		return this.incumbant;
		
	}
	
	public long getWager(){
		return this.wager;
	}
	
	public String getStatus(){
		return this.status.toString();
	}
	
	public void setWager(long wagr){
		this.wager=wagr;

		thisEntity.setProperty("wager", this.wager);

	}
	
	public String getGladiatorChallengeKey(){
		return thisEntity.getKey().toString();
	}

	public void applyTie() {
		// IF there is a wager, 
		//must get the luduses and 'move' the wager amount from wagered gold to available gold
		if (wager > 0){
			if (incumbantLudus == null || challengerLudus == null){
				findLuduses();
			}			
			incumbantLudus.setWager(-wager);//returns the wagers to both fighters
			challengerLudus.setWager(-wager);//moving the wager from the wageredGold to availableGold
			incumbantLudus.saveLudus();
			challengerLudus.saveLudus();
			if (logEnabled){log.info("refunded wagers to available gold: " + wager);}
		}
		this.status = Status.COMPLETED;
		
	}

	public void applyChallengerWin() {
		// If there was a wager, add double the wager amount to the challenger and remove the wager amount from 
		// each ludus' wagered gold
		if (incumbantLudus == null || challengerLudus == null){
			findLuduses();
		}
		if (wager > 0){
			
			incumbantLudus.updateWageredGold(-wager);
			challengerLudus.setWager(-wager);//moves the wagered gold from wageredGold to available gold
			
		}
		this.status = Status.COMPLETED;
		challengerLudus.updateAvailableGold((wager) + STANDARD_WIN_AMOUNT);//removes the wagered gold from the wageredGold store
		if (logEnabled) {log.info("Awarded challenger: " + wager + " and " + STANDARD_WIN_AMOUNT);}//awards the wager and the standard fight win reward
		incumbantLudus.saveLudus();
		challengerLudus.saveLudus();
	}

	public void applyIncumbantWin() {
		// If there was a wager, add double the wager amount to the incumbant and remove the wager amount from 
		// each ludus' wagered gold
		if (incumbantLudus == null || challengerLudus == null){
			findLuduses();
		}
		if (wager > 0){			
			challengerLudus.updateWageredGold(-wager);//removes the wagered gold from the wageredGold store
			incumbantLudus.setWager(-wager);//moves the wagered gold from wageredGold to available gold
		}
		this.status = Status.COMPLETED;
		incumbantLudus.updateAvailableGold((wager) + STANDARD_WIN_AMOUNT);//awards the wager and the standard fight win reward
		if (logEnabled){log.info("Awarded incumbant: " + wager + " and " + STANDARD_WIN_AMOUNT);}
		incumbantLudus.saveLudus();
		challengerLudus.saveLudus();

	}
}
