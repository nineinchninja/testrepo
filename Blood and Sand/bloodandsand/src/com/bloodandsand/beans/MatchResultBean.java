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

import org.mortbay.log.Log;

import com.bloodandsand.core.GladiatorCombat;
import com.google.appengine.labs.repackaged.com.google.common.collect.ArrayTable;

public class MatchResultBean implements java.io.Serializable {
	/**
	 * 
	 */
	protected static final Logger log = Logger.getLogger(MatchResultBean.class.getName());
	
	private static final long serialVersionUID = 5687618169542410632L;
	public GladiatorDataBean challenger;
	public GladiatorDataBean incumbant;
	
	public String incumbantName;
	public String challengerName;
	public String challengerPossessive;
	public String incumbantPossessive;
	
	public Date matchDate;
	
	public String challengerAction;
	public String incumbantAction;
	
	public String challengerRiposte;
	public String incumbantRiposte;
	
	public String challengerResult;
	public String incumbantResult;
	
	public String challengerLocation;
	public String challengerDamage;
	public String incumbantLocation;
	public String incumbantDamage;
	
	public String challengerState;
	public String incumbantState;
		
	private int round;
	public String roundDescription;
	public ArrayList<String> roundsDetails;//contains a simple string with all details for each round that can be parsed later if I want to get additional details
	private String fightDescription;
	
	
	public MatchResultBean (GladiatorDataBean challenger, GladiatorDataBean incumbant){
		this.challenger = challenger;
		this.incumbant = incumbant;
		
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
		
		Date matchDate = new Date();
		
		round = 0;		
	}
	
	public void initializeNewRound (int round){
		this.round = round;
		roundDescription = "";
		
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
		
		roundsDetails.add(round, "ChallengerAction:"+ challengerAction + "::" +
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
	

}
