/**
 *  Created by Andy Hayward
 *  Jan 4, 2013
 */
package com.bloodandsand.beans;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Logger;


import com.bloodandsand.utilities.CoreBean;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.labs.repackaged.com.google.common.collect.ArrayTable;
import com.google.appengine.api.datastore.Text;

public class MatchResultBean extends CoreBean implements java.io.Serializable {
	/**
	 * 
	 */
	protected static final Logger log = Logger.getLogger(MatchResultBean.class.getName());
	
	
	private static final long serialVersionUID = 5687618169542410632L;
	private GladiatorDataBean challenger;
	private GladiatorDataBean incumbant;
	
	private String winner;
	
	private String incumbantName;
	private String challengerName;
	private Key incumbantKey;
	private Key challengerKey;
	
	private String challengerPossessive;
	private String incumbantPossessive;
	private String challengerWeapon;
	private String incumbantWeapon;
	
	private Date matchDate;	
	private long round;
	
	private String fightDescription;
	
	private Entity thisEntity = new Entity(matchResultEntity);
	
	public MatchResultBean(Entity e){
		thisEntity = e;
		setUpBean();		
	}
	
	public MatchResultBean (GladiatorDataBean challenger, GladiatorDataBean incumbant, String challengerWeapon, String incumbantWeapon){
		
		this.challenger = challenger;
		this.incumbant = incumbant;
		
		this.challengerKey = challenger.getDataStoreKey();
		this.incumbantKey = incumbant.getDataStoreKey();
		
		this.challengerName = challenger.getName();
		this.incumbantName = incumbant.getName();
		
		this.challengerWeapon = challengerWeapon;
		this.incumbantWeapon = incumbantWeapon;
		
		challengerPossessive = challenger.getPossessive();
		incumbantPossessive = incumbant.getPossessive();
		
		fightDescription = ""; 
		
		matchDate = new Date();
		
		round = 0;	
		winner = "";

		setUpEntity();
	}
	
	public void describeMatchStart() {
		fightDescription = "The bout between " + challengerName + " and " + incumbantName + " is about to begin. ";
		fightDescription += "The gladiators enter the arena from opposite sides. " + challengerName + ", the challenger, is wielding a " + challengerWeapon +
				" while " + incumbantName + " wields " + incumbantWeapon + ". ";
		fightDescription += "The fighters meet face to face in the middle, and the announcer hits the starting bell. The match has begun!\n";
		
	}

	private void setUpBean(){
		challengerKey = (Key) this.thisEntity.getProperty("challengerKey");
		incumbantKey = (Key) this.thisEntity.getProperty("incumbantKey");
		challengerName = (String) this.thisEntity.getProperty("challengerName");
		incumbantName = (String) this.thisEntity.getProperty("incumbantName");
		matchDate = (Date) this.thisEntity.getProperty("matchDate");
		fightDescription = ((Text) this.thisEntity.getProperty("fightDescription")).getValue();
		round = (Long) this.thisEntity.getProperty("totalRounds");	
		winner = (String) this.thisEntity.getProperty("winner");
	}
	
	private void setUpEntity(){
		//ensure all properties are set up
		
		this.thisEntity.setProperty("challengerKey", challengerKey);
		this.thisEntity.setProperty("incumbantKey", incumbantKey);
		this.thisEntity.setProperty("challengerName", challengerName);
		this.thisEntity.setProperty("incumbantName", incumbantName);
		this.thisEntity.setProperty("matchDate", matchDate);
		this.thisEntity.setProperty("fightDescription", new Text(fightDescription));
		this.thisEntity.setProperty("totalRounds", round);	
		this.thisEntity.setProperty("winner", winner);
	}
	
	public GladiatorDataBean getChallenger() {
		return challenger;
	}

	public void setChallenger(GladiatorDataBean challenger) {
		this.challenger = challenger;
	}

	public GladiatorDataBean getIncumbant() {
		return incumbant;
	}

	public void setIncumbant(GladiatorDataBean incumbant) {
		this.incumbant = incumbant;
	}

	public String getIncumbantName() {
		return incumbantName;
	}

	public void setIncumbantName(String incumbantName) {
		this.incumbantName = incumbantName;
		thisEntity.setProperty("incumbantName", incumbantName);
	}

	public String getChallengerName() {
		return challengerName;
	}

	public void setChallengerName(String challengerName) {
		this.challengerName = challengerName;
		thisEntity.setProperty("challengerName", challengerName);
	}

	public Key getIncumbantKey() {
		return incumbantKey;
	}

	public void setIncumbantKey(Key incumbantKey) {
		this.incumbantKey = incumbantKey;
		thisEntity.setProperty("incumbantKey", incumbantKey);
	}

	public Key getChallengerKey() {
		return challengerKey;
	}

	public void setChallengerKey(Key challengerKey) {
		this.challengerKey = challengerKey;
		thisEntity.setProperty("challengerKey", challengerKey);
	}

	public String getChallengerPossessive() {
		return challengerPossessive;
	}

	public void setChallengerPossessive(String challengerPossessive) {
		this.challengerPossessive = challengerPossessive;
	}

	public String getIncumbantPossessive() {
		return incumbantPossessive;
	}

	public void setIncumbantPossessive(String incumbantPossessive) {
		this.incumbantPossessive = incumbantPossessive;
	}

	public Date getMatchDate() {
		return matchDate;
	}

	public void setMatchDate(Date matchDate) {
		this.matchDate = matchDate;
		thisEntity.setProperty("matchDate", matchDate);
	}

	
	public long getRound() {
		return round;
	}

	public void setRound(long round) {
		this.round = round;
		thisEntity.setProperty("totalRounds", round);
	}

	public String getFightDescription() {
		return fightDescription;
	}

	public void setFightDescription(String fightDescription) {
		this.fightDescription = fightDescription;
		thisEntity.setProperty("fightDescription", new Text(fightDescription));
	}

	public void initializeNewRound (long round){
		this.round = round;
		if (TESTTOGGLE){fightDescription += "\nStart of round " + round +"\n";}
	}	
	
	
	public void completeRound(){
		//TODO
	
		setUpEntity();
	}

	public void saveNewResults(TournamentDataBean tourney) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();	
		thisEntity = new Entity(matchResultEntity, tourney.getDataStoreKey());
		
		setUpEntity();
		
		Transaction txn = datastore.beginTransaction();

		try {			
			datastore.put(thisEntity);
			txn.commit();
		} finally  {
			if (txn.isActive()) {
		        txn.rollback();
		        log.warning("Save match result transaction failed: rolled back");
		    }
		}		
	}

	public void setWinner(String winner) {
		this.winner = winner;
		thisEntity.setProperty("winner", winner);		
	}
	
	public String getWinner(){
		return this.winner;
	}

	public void recordHit(String fighterName) {
		//TODO
		fightDescription += fighterName + " hits his opponent. \n";
		
	}

	public void recordBlock(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " blocks the attack with his weapon. \n";
		
	}

	public void recordDodge(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " dodges the attack. \n";
		
	}

	public void recordMiss(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " misses completely. \n";
		
	}

	public void recordRest(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " is tiring and tries to catch his breath. \n";
	}

	public void recordRiposte(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " finds an opening and tries to counterattack! \n";
		
	}

	public void recordAvoidRiposte(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " avoids the counterattack. \n";
		
	}

	public void recordCriticalHit(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " has scored a good hit! \n";
		
	}

	public void recordReceivedCritical(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " looks to be critically wounded. \n";
		
	}

	public void recordResistedCritical(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " shrugs off the hit. \n";
		
	}

	public void recordNormalWound(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " has received a minor wound.  \n";
		
	}

	public void recordNormalHit(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " has just managed to hit. \n";
		
	}

	public void writeSummaryStats() {
		// TODO Auto-generated method stub
		fightDescription += "summary stats will go here,\n";
		
	}

	public void recordNonCriticalInjury(String fighterName, String location) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " has been scratched in the " + location.toLowerCase() + ". \n";
		
	}

	public void recordCriticalInjury(String fighterName, String location) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " has been critically wounded in the " + location.toLowerCase() + ".\n";
		
	}

	public void recordDeath(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " has died! Pity!";
		
	}

	public void recordExhausted(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " looks exhausted. \n";
		
	}

	public void recordTired(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " looks tired. \n";
	}

	public void recordTiring(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " seems to be tiring. \n";
	}

	public void recordConcede(String fighterName) {
		// TODO Auto-generated method stub
		fightDescription += fighterName + " is forced to concede. \n";
		
	}

}
