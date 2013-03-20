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
	
	private Date matchDate;
	
	private String challengerAction;
	private String incumbantAction;
	
	private String challengerRiposte;
	private String incumbantRiposte;
	
	private String challengerResult;
	private String incumbantResult;
	
	private String challengerLocation;
	private String challengerDamage;
	private String incumbantLocation;
	private String incumbantDamage;
	
	private String challengerState;
	private String incumbantState;
		
	private long round;
	private String roundDescription;
	private ArrayList<String> roundsDetails;//contains a simple string with all details for each round that can be parsed later if I want to get additional details
	private String fightDescription;
	
	private Entity thisEntity = new Entity(matchResultEntity);
	
	public MatchResultBean(Entity e){
		thisEntity = e;
		setUpBean();
		
	}
	
	public MatchResultBean (GladiatorDataBean challenger, GladiatorDataBean incumbant){
		
		this.challenger = challenger;
		this.incumbant = incumbant;
		
		this.challengerKey = challenger.getDataStoreKey();
		this.incumbantKey = incumbant.getDataStoreKey();
		
		this.challengerName = challenger.getName();
		this.incumbantName = incumbant.getName();
		
		challengerPossessive = challenger.getPossessive();
		incumbantPossessive = incumbant.getPossessive();
		
		roundDescription = "";
		roundsDetails = new ArrayList<String>();
		fightDescription = ""; 
		
		challengerAction = "";
		incumbantAction = "";
		
		challengerRiposte = "";
		incumbantRiposte = "";
		
		challengerResult = "";
		incumbantResult = "";
		
		challengerState = "";
		incumbantState = "";		
		
		matchDate = new Date();
		
		round = 0;		
		winner = "";
		setUpEntity();
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

	public String getChallengerAction() {
		return challengerAction;
	}

	public void setChallengerAction(String challengerAction) {
		this.challengerAction = challengerAction;
	}

	public String getIncumbantAction() {
		return incumbantAction;
	}

	public void setIncumbantAction(String incumbantAction) {
		this.incumbantAction = incumbantAction;
	}

	public String getChallengerRiposte() {
		return challengerRiposte;
	}

	public void setChallengerRiposte(String challengerRiposte) {
		this.challengerRiposte = challengerRiposte;
	}

	public String getIncumbantRiposte() {
		return incumbantRiposte;
	}

	public void setIncumbantRiposte(String incumbantRiposte) {
		this.incumbantRiposte = incumbantRiposte;
	}

	public String getChallengerResult() {
		return challengerResult;
	}

	public void setChallengerResult(String challengerResult) {
		this.challengerResult = challengerResult;
	}

	public String getIncumbantResult() {
		return incumbantResult;
	}

	public void setIncumbantResult(String incumbantResult) {
		this.incumbantResult = incumbantResult;
	}

	public String getChallengerLocation() {
		return challengerLocation;
	}

	public void setChallengerLocation(String challengerLocation) {
		this.challengerLocation = challengerLocation;
	}

	public String getChallengerDamage() {
		return challengerDamage;
	}

	public void setChallengerDamage(String challengerDamage) {
		this.challengerDamage = challengerDamage;
	}

	public String getIncumbantLocation() {
		return incumbantLocation;
	}

	public void setIncumbantLocation(String incumbantLocation) {
		this.incumbantLocation = incumbantLocation;
	}

	public String getIncumbantDamage() {
		return incumbantDamage;
	}

	public void setIncumbantDamage(String incumbantDamage) {
		this.incumbantDamage = incumbantDamage;
	}

	public String getChallengerState() {
		return challengerState;
	}

	public void setChallengerState(String challengerState) {
		this.challengerState = challengerState;
	}

	public String getIncumbantState() {
		return incumbantState;
	}

	public void setIncumbantState(String incumbantState) {
		this.incumbantState = incumbantState;
	}

	public long getRound() {
		return round;
	}

	public void setRound(long round) {
		this.round = round;
		thisEntity.setProperty("totalRounds", round);
	}

	public String getRoundDescription() {
		return roundDescription;
	}

	public void setRoundDescription(String roundDescription) {
		this.roundDescription = roundDescription;
	}

	public ArrayList<String> getRoundsDetails() {
		return roundsDetails;
	}

	public void setRoundsDetails(ArrayList<String> roundsDetails) {
		this.roundsDetails = roundsDetails;
	}

	public String getFightDescription() {
		return fightDescription;
	}

	public void setFightDescription(String fightDescription) {
		this.fightDescription = fightDescription;
		thisEntity.setProperty("fightDescription", new Text(fightDescription));
	}

	public void initializeNewRound (int round){
		this.round = round;
		setRoundDescription ("");
		
		challengerAction = "";
		incumbantAction = "";
		
		challengerRiposte = "";
		incumbantRiposte = "";
		
		challengerResult = "";
		incumbantResult = "";
		
		challengerLocation = "";
		challengerDamage = "";
		
		incumbantLocation = "";
		incumbantDamage = "";
		
		challengerState = "";
		incumbantState = "";	
		
	}	
	
	public void recordActions (String challengerAction, String incumbantAction){
		this.challengerAction = challengerAction;
		this.incumbantAction= incumbantAction;
	}
	
	public void recordRiposte (String riposte, String name){
		if (name.equals(challengerName)){
			challengerRiposte= riposte;
			log.info(name + ": recorded riposte: " + riposte);
		} else {
			incumbantRiposte= riposte;
			log.info(name + ": recorded riposte: " + riposte);
		}
	}
	
	public void recordResults (String name, String outcome){//records taunts, positions, defend and rests
		if (name.equals(challengerName)){
			challengerResult = outcome;
			
		} else {
			incumbantResult = outcome;
		}
		log.info(name + " outcome: " + outcome );
	}
	
	public void recordResults (String name, String outcome, String location, String damage){//records attacks
		if (name.equals(challengerName)){
			challengerResult = outcome;
			if (outcome.equals("HIT")){
				challengerLocation = location;
				challengerDamage = damage;
				challengerResult += location+damage;
			}

		} else {
			incumbantResult = outcome;
			if (outcome.equals("HIT")){
				incumbantLocation = location;
				incumbantDamage = damage;
				incumbantResult += location+damage;
			}
		}
	}
	
	public void recordStatus(String name, String status){
		if (name.equals(challengerName)){
			challengerState = status;
		} else {
			incumbantState = status;
		}
	}
	
	public String getRoundDetails(int round){ //TODO returns a semi-formatted string of the details for the round
		return "";
	}
	
	public String completeRound(){
		
		roundDescription = "Round " + round  + "\n";
		roundDescription += getAction(challenger, challengerAction) + "\n";
		roundDescription += getAction(incumbant, incumbantAction) + "\n";
		
		if (challengerRiposte != null && challengerRiposte != ""){
			roundDescription += getRiposte(challenger, challengerRiposte) + "\n";
		}
		if (incumbantRiposte != null && incumbantRiposte != ""){
			roundDescription += getRiposte(incumbant, incumbantRiposte) + "\n";
		}
		
		roundDescription += getResult(challenger, challengerAction, challengerRiposte, challengerResult, challengerLocation, challengerDamage) + "\n";
		roundDescription += getResult(incumbant, incumbantAction, incumbantRiposte, incumbantResult, incumbantLocation, incumbantDamage) + "\n";
		
		roundDescription += getState(challenger, challengerState) + "\n";
		roundDescription += getState(incumbant, incumbantState);
			
		roundDescription += "\n";
		
		roundsDetails.add((int) round, "ChallengerAction:"+ challengerAction + "::" +
								"IncumbantAction:" + incumbantAction + "::" +
								"ChallengerRiposte:" + challengerRiposte + "::" +
								"IncumbantRiposte:" + incumbantRiposte + "::" +
								"ChallengerResult:" + challengerResult + "::" +
								"IncumbantResult:" + incumbantResult + "::" +
								"ChallengerState:" + challengerState + "::" +
								"IncumbantState:" + incumbantState); 
		log.info("ChallengerAction:"+ challengerAction + "::" +
				"IncumbantAction:" + incumbantAction + "::" +
				"ChallengerRiposte:" + challengerRiposte + "::" +
				"IncumbantRiposte:" + incumbantRiposte + "::" +
				"ChallengerResult:" + challengerResult + "::" +
				"IncumbantResult:" + incumbantResult + "::" +
				"ChallengerState:" + challengerState + "::" +
				"IncumbantState:" + incumbantState);
		fightDescription += roundDescription;
		setUpEntity();
		return roundDescription; //this is a hack to get things moving. It should be revamped for formatting etc
	}
	
	private String getState(GladiatorDataBean glad, String state) {
		
		String name = glad.name;
		return name + ": " + state;
	}

	private String getResult(GladiatorDataBean glad,
			String action, String riposte, String outcome,
			String location, String damage) {
		
		String name = glad.name;
		String poss = glad.getPossessive();
		String roundAction = "";
		
		if (riposte != null && riposte != ""){
			roundAction = riposte;
		} else {
			roundAction = action;
		}
		if (location == null){
			location = "error";
		}
		
		if (roundAction.equals("Defend") || roundAction.equals("Rest") ){
			return name + " stands idle";
		} else {
			if (roundAction.equals("Taunt") ){
				if (outcome.equals("HIT")){
					return name + "'s opponent looks angered by the insult";
				} else {
					return name + "'s opponent ignores the insult";
				}
			} else {
				if (roundAction.equals("Position") ){
					if (outcome.equals("HIT")){
						return name + " moves into a better position";
					} else {
						return name + " can't seem to flank " + poss + " oppoonent";
					}
				} else {
					if (roundAction.equals("Attack")){
						if (outcome.equals("DODGED")){
							return name + "'s opponent dodged " + poss + " attack";
						} else {
							if (outcome.equals("BLOCKED")){
								return name + "'s opponent dodged " + poss + " attack";
							} else {
								if (outcome.equals("MISS")){
									return name + " missed!";
								} else {
									if (damage.equals("broken")){
										return name + " hit " + poss + " opponent in the " + location + " and it appears to be a serious injury!";
									} else {
										if (damage.equals("damaged")){
											return name + " hit " + poss + " opponent in the " + location + "!";
											
										} else {
											return name + " hit " + poss + " opponent in the " + location + " but didn't cause any serious damage";
										}
										
									}
										
								}
							}
						}
					}
				}
			}
		}
	
		return "";
	}

	private String getRiposte(GladiatorDataBean glad, String action){
		String poss = glad.getPossessive();
		String name = glad.getName();
		
		if (action.equals("Attack")){
			return name + " quickly changes " + poss + " tactics and moves to attack.";
		} else {
			if (action.equals("Position")){
				return name + " quickly changes " + poss + " tactics and attempts to out manouever " + poss + " opponent.";
			} else {
				if (action.equals("Defend")){
					return name + " quickly changes " + poss + " tactics and moves into a defensive stance.";
				} else {
					if (action.equals("Taunt")){
						return name + " throws insults at " + poss + " opponent, trying to get " + 
								poss + " opponent angry.";
					} else {
						if (action.equals("Rest")){
							return name + " sees an opportunity to regain some energy and changes tactics to a quick breather.";
						} else {
							log.info(name + ": No valid action recieved for printing round details: " + action);
							return "Error: no valid action received";
						}
					}
				}
			}
		}
		
	}

	private String getAction(GladiatorDataBean glad, String action){
		String poss = glad.getPossessive();
		String name = glad.getName();
		
		if (action.equals("Attack")){
			return name + " moves to attack.";
		} else {
			if (action.equals("Position")){
				return name + " attempts to out manouever " + poss + " opponent.";
			} else {
				if (action.equals("Defend")){
					return name + " moves into a defensive stance.";
				} else {
					if (action.equals("Taunt")){
						return name + " throws insults at " + poss + " opponent, trying to get " + 
								poss + " opponent angry.";
					} else {
						if (action.equals("Rest")){
							return name + " tries to regain some energy by grabbing a quick breather.";
						} else {
							log.info(name + ": No valid action recieved for printing round details: " + action);
							return "Error: no valid action received";
						}
					}
				}
			}
		}
		
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
	

}
