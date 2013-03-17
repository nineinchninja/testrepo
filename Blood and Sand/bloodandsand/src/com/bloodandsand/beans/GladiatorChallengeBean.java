/**
 *  Created by Andy Hayward
 *  Dec 23, 2012
 */
package com.bloodandsand.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.bloodandsand.utilities.CoreBean;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;


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
	
	
	public Date challengeDate;
	private GladiatorDataBean challenger;
	private GladiatorDataBean incumbant;
	
	public Key challengerKey;//always store these using the keytostring function
	public Key incumbantKey;
	
	private Key gladiatorChallengeKey;
	
	private long wager;
	private Status status = Status.INITIATED;
	
	protected static final Logger log = Logger.getLogger(GladiatorChallengeBean.class.getName());	
	
	public GladiatorChallengeBean(){
		/*
		 * TODO
		 */
	}
	
	public GladiatorChallengeBean(Entity challenge, boolean getGladiators){
		this.challengeDate = (Date) challenge.getProperty("challengeDate");
		String temp = (String) challenge.getProperty("challengerKey");
		this.challengerKey = KeyFactory.stringToKey(temp);
		temp = (String) challenge.getProperty("incumbantKey");
		this.incumbantKey = KeyFactory.stringToKey(temp);
		
		this.gladiatorChallengeKey = challenge.getKey();
		
		
		this.wager = (Long)challenge.getProperty("wager");
		if (getGladiators){
			findChallenger();
			findIncumbant();
		}
		
		this.status = Status.valueOf((String)challenge.getProperty("status"));
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
	}
	
	public GladiatorChallengeBean createDummyChallenge(){
		GladiatorChallengeBean c = new GladiatorChallengeBean();
		GladiatorDataBean g = new GladiatorDataBean();
		
		c.challengeDate = new Date();
		c.challenger = g.getDummyGladiator();
		c.incumbant = g.getDummyGladiator();
		//this.challengerKey = KeyFactory.stringToKey("gladiatorchallenger");
		//this.incumbantKey = KeyFactory.stringToKey("gladiatorincumbant");
		c.challenger.setName("challenger");
		c.challenger.setOwner("owner2");
		c.incumbant.setName("incumbant");
		c.incumbant.setOwner("owner1");
		c.wager = 10;
		
		return c;
	}
	
	public boolean acceptChallenge(){
		//check both gladiators to see if they have accepted a challenge. If not,
		// update this challenge and all of the gladiators' challenges to declined
		
		boolean successful = false;
		if (!challenger.isGladiatorAvailableToChallenge() || !incumbant.isGladiatorAvailableToChallenge()){
			log.info("One of the gladiators was not available");
			return successful;
		} else {
			for (GladiatorChallengeBean chall: challenger.getChallenges()){
				if (!chall.getGladiatorChallengeKey().equals(this.getGladiatorChallengeKey())){
					chall.declineChallenge();
					chall.saveChallenge();
					log.info("Wrong challenge");
				} else {
					successful = true;
					log.info("Matched challenges");
				}
			}
			for (GladiatorChallengeBean chall: incumbant.getChallenges()){
				if (!chall.getGladiatorChallengeKey().equals(this.getGladiatorChallengeKey())){
					chall.declineChallenge();
					chall.saveChallenge();
					log.info("Wrong challenge");
				} else {
					successful = true;
					log.info("Matched challenges");
				}
			}			
			if (successful){
				this.status = Status.ACCEPTED;
				this.saveChallenge();
				log.info("New status is " + this.status.toString());
			}
			
			return successful;
		}			
	}
	
	public void saveChallenge() {
				
		//which looks the challenge up in the ds and updates it rather than saving a new one		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity challEnt;
		
		try {			
			challEnt = datastore.get(this.gladiatorChallengeKey);
		} catch (EntityNotFoundException e) {
			log.info("GladiatorChallengeBean.java: Didn't find the challenge when using key search");
			e.printStackTrace();
			return;
		}
				
		Transaction txn = datastore.beginTransaction();
		challEnt = populateChallengeEntity(challEnt);
		try {					
		    datastore.put(challEnt);
		    txn.commit();
		} finally {
		    if (txn.isActive()) {
		        txn.rollback();
		        log.warning("Save Existing Challenge transaction failed: rolled back");
		    }
		}		
	}

	public void expireChallenge(){
		status = Status.EXPIRED;		
	}
	
	public void declineChallenge(){
		this.status = Status.DECLINED;
	}
	
	public void rescindChallenge(){
		status = Status.CANCELED;
	}
	
	public Status getStatusEnum(){
		return this.status;
	}
	
	public String getStatusString(){
		return this.status.toString();
	}
	
	public void setStatus(Status sts){
		this.status = sts;
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
				 log.info("GladiatorChallengeBean: unable to resolve challenger");
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
				 log.info("GladiatorChallengeBean: unable to resolve incumbant");
				 return;
			 }
		}
	}
	
	private Entity populateChallengeEntity(Entity ent){
		ent.setProperty("challengeDate", challengeDate);
		ent.setProperty("challengerKey", challenger.getKey());
				
		ent.setProperty("incumbantKey", incumbant.getKey());
		ent.setProperty("wager", wager);
		
		ent.setProperty("status", status.toString());
		return ent;
	}
	
	public void saveNewChallenge(){
		//ensure that all required fields are completed before initiating this method
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity c = new Entity(challengeEntity);
		Transaction txn = datastore.beginTransaction();
		c = populateChallengeEntity(c);
		try {					
		    datastore.put(c);
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
	}
	
	public String getGladiatorChallengeKey(){
		return this.gladiatorChallengeKey.toString();
	}

	
	

}
