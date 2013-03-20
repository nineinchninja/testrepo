/**
 *  Created by Andy Hayward
 *  Dec 23, 2012
 */
package com.bloodandsand.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.swing.SortOrder;


import com.bloodandsand.beans.GladiatorChallengeBean;
import com.bloodandsand.beans.GladiatorDataBean;
import com.bloodandsand.beans.MatchResultBean;
import com.bloodandsand.beans.TournamentDataBean;
import com.bloodandsand.utilities.BaseServlet;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class GladiatorCombat extends BaseServlet{
	/* this is where the magic happens. This class will take two gladiators and 
	 * run them through a combat sequence and record the outcome. At the end, 
	 * the details of the combat will be available for the owners to read, and
	 * some summary statistics will be available.
	 */
	public static final String[] ACTIONS = {"Attack", "Defend", "Taunt", "Position", "Rest"};
	
	public static final String[] NON_CRITICAL_BODY_PARTS = {"MAIN HAND", "OFF HAND", "MAIN ARM", "OFF ARM", 
															"MAIN LEG", "OFF LEG", "MAIN SHOULDER", "OFF SHOULDER", 
															"MAIN ARM", "OFF ARM", 	"MAIN LEG", "OFF LEG", 
															"MAIN SHOULDER", "OFF SHOULDER"}; //values are repeated as a hack for increasing odds of hitting larger areas
	public static final String[] CRITICAL_BODY_PARTS = {"ABDOMEN", "CHEST", "ABDOMEN", "CHEST", "NECK", "HEAD"};
	
	public static final int CRITICAL_LOCATION_BASE = 0;//the value used as centre point for determining whether the glad targeted a critical area or not
	
	public static final int CRITICAL_LOCATION_INJURY_CHANCE = 20;
	public static final int NON_CRITICAL_LOCATION_INJURY_CHANCE = 50;	

	protected static final Logger log = Logger.getLogger(GladiatorCombat.class.getName());
	private static final long REST_HIT_MODIFIER = 15;
	private static final long DODGE_STAMINA_MODIFIER = 10;
	private static final long BLOCK_STAMINA_MODIFIER = 10;	
	private static final long POSITION_STAMINA_MODIFIER = 5;
	
	private static final int MAXIMUM_MATCH_LENGTH = 40;
	
	boolean combatComplete;//test boolean used to end the while-loop
	int round;//tracks the number of 6 second turns in the combat
	String roundDescription;
	String result;
	
	List<GladiatorChallengeBean> matches = new ArrayList<GladiatorChallengeBean>();
	Fighter challenger;
	Fighter incumbant;
	
	public MatchResultBean results_bean;
	private TournamentDataBean tournament;
		
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException{
		long startTime = + System.currentTimeMillis();
		log.info("Start of getting matchups: " + startTime);
		
		getGladiatorMatches();//gets all of the matches that are set to 'accepted' changes all other matches to expired
		
		long startMatchesTime = System.currentTimeMillis();
		log.info("Total Time for getting matchups: " + (startTime - startMatchesTime));
		Iterator<GladiatorChallengeBean> it = matches.iterator();
		log.info("GladiatorCombat Class: Starting matches");
		while (it.hasNext()){
			GladiatorChallengeBean currentMatch = it.next();
			//creating the fighters obj by sending the gladiator bean populates all the initial variables
			challenger = new Fighter(currentMatch.getChallenger());//this also calculates all initial variables and sets up the action table
			incumbant = new Fighter(currentMatch.getIncumbant());
			results_bean = new MatchResultBean(currentMatch.getChallenger(), currentMatch.getIncumbant());

			combatComplete = false;
			round = 0;			
			while (!combatComplete){
				results_bean.initializeNewRound(round); //sets up the variables. Note that round 1 = round 0 in the bean
				round +=1;
				log.info("Start of round:" + round);
				resetFighters();
				determineActions(); //fighters 
				determineRiposte();
				
				calculateOutcome();
				calculateRoundStats();
				combatComplete = isCombatComplete();
				write_line(req, resp, results_bean.completeRound());
				log.info("End of round: " + round);
				write_line(req, resp, getFighterStats());
			}
			//when combat is complete, declare winner, loser, store resultbean, update ludus and gladiator stats
			closeMatch();
		}
		long endTime = + System.currentTimeMillis();
		log.info("End of matchups: " + endTime);
		log.info("Total elapsed time: " + (endTime - startTime));
		log.info("Total time for matches: " + (endTime - startMatchesTime));		
	}
		
	private void closeMatch() {
		//update the total matches for each gladiator:
		
		// declare the winner
		if (challenger.status.equals("Dead") || challenger.status.equals("Concedes") ){
			//incumbant wins
			incumbant.gldtr.addWin(); //adds a match to the total and increments total matches
			log.info(incumbant.fighterName + " declared winner!");
			challenger.gldtr.addLoss();
			results_bean.setWinner("incumbant.fighterName");
			if (challenger.status.equals("Dead")){
				challenger.gldtr.setStatus("DEAD");
			}
		} else {
			if ( incumbant.status.equals("Dead") || incumbant.status.equals("Concedes") ){
				//challenger wins
				challenger.gldtr.addWin(); //adds a match to the total and increments total matches
				incumbant.gldtr.addLoss();
				log.info(challenger.fighterName + " declared winner!");
				results_bean.setWinner("challenger.fighterName");
				if (incumbant.status.equals("Dead")){
					incumbant.gldtr.setStatus("DEAD");
				}
			} else {
				//tie
				challenger.gldtr.addTie();
				incumbant.gldtr.addTie();
				results_bean.setWinner("Tie");
				log.info("Match declared a draw");
			}
		}
		//store all data
		challenger.gldtr.saveGladiator();
		incumbant.gldtr.saveGladiator();
		results_bean.saveNewResults(tournament);
	}

	private void resetFighters() {
		challenger.resetForNewRound();
		incumbant.resetForNewRound();		
	}

	private String getFighterStats() {
		// this is a debug function and should be turned off when going live
		String fighterDetails = challenger.getCurrentStats() + "\n" + incumbant.getCurrentStats();
		return fighterDetails;

	}

	private boolean isCombatComplete() {

		if (challenger.status.equals("Dead") || incumbant.status.equals("Dead") || 
				challenger.status.equals("Concedes") || incumbant.status.equals("Concedes") ){
			return true;
		}
		if (round > MAXIMUM_MATCH_LENGTH){
			log.info("The match officials end the match due to it being well over the alloted time. ");
			return true;
		}
		else
			return false;
	}
	
	private void calculateRoundStats() {
		// apply modifiers
		challenger.applyNewRoundModifiers();
		incumbant.applyNewRoundModifiers();	
		results_bean.recordStatus(challenger.fighterName, challenger.status);
		results_bean.recordStatus(incumbant.fighterName, incumbant.status);
	}

	private void calculateOutcome() {
		// compare the actions and base results on the pairing
		// If both attack, there are no adjustments and both do attack rolls
		// If one attacks and the other defends, the defender gets a bonus to defense rolls
		// etc
		//possible combinations: 
		//	AttackAttack, AttackDefend/DefendAttack, AttackTaunt/TauntAttack, AttackPosition/PositionAttack, AttackRest/RestAttack
		//	DefendDefend, DefendTaunt/TauntDefend, DefendPosition/PositionDefend, DefendRest/RestDefend,
		//	TauntTaunt, TauntPosition/PositionTaunt, TauntRest/RestTaunt,
		//	PositionPosition, PositionRest/RestPosition, 
		//	RestRest
		//
		//First deal with the non-events - RestRest, DefendDefend, DefendRest, RestDefend. This enables us to skip 
		//	some decisions and reduce processing when not required 
		//Next, position and taunts, as they may modify the current round's attacks and defenses
		//Then break out the variations on attack
		log.info("Round: " + round + "\n" + "incumbant action: " + incumbant.nextAction + "\nchallenger action: " + challenger.nextAction);
		if ((challenger.nextAction.equals("Rest") && incumbant.nextAction.equals("Rest")) || //Deal with those with least processing first
				(challenger.nextAction.equals("Defend") && incumbant.nextAction.equals("Defend")) ||
				(challenger.nextAction.equals("Defend") && incumbant.nextAction.equals("Rest")) ||
				(challenger.nextAction.equals("Rest") && incumbant.nextAction.equals("Defend"))){
			log.info("Fighters did nothing of interest here");
			results_bean.recordResults(challenger.fighterName, "MISS");
			results_bean.recordResults(incumbant.fighterName, "MISS");
			
		} //start adding modifiers
		// taunt - a taunting player can reduce the opposing fighter's chance to hit. 
		if (challenger.nextAction.equals("Taunt") || incumbant.nextAction.equals("Taunt")) {
			if (challenger.nextAction.equals("Taunt")){
				long c = challenger.getTauntRoll();
				long i = incumbant.defendTauntRoll();
				if (c > i) {
					incumbant.applyTauntModifier(c - i);
					
					results_bean.recordResults(challenger.fighterName, "HIT");
				} else {
					incumbant.applyTauntModifier((int)((i - c)/2));//failed taunts reduce effects to a small extent. Otherwise the player's attack just keeps rising
					results_bean.recordResults(challenger.fighterName, "MISS");
				}
			}
			
			if (incumbant.nextAction.equals("Taunt")){
				long c = challenger.defendTauntRoll();
				long i = incumbant.getTauntRoll();
				if (i > c) {
					challenger.applyTauntModifier(i-c);
					
					results_bean.recordResults(incumbant.fighterName, "HIT");
				} else {
					challenger.applyTauntModifier((int)((c - i)/2));
					results_bean.recordResults(incumbant.fighterName, "MISS");
				}
			}
		}
		// position. A successful position roll improves the chance to defend this round and hit next round
		if (challenger.nextAction.equals("Position") || incumbant.nextAction.equals("Position")) {
			if (challenger.nextAction.equals("Position")){
				long c = challenger.getPositionRoll();
				long i = incumbant.defendPositionRoll();
				if (c > i) {
					challenger.applyPositionModifier(c-i);
					
					results_bean.recordResults(challenger.fighterName, "HIT");
				} else {
					results_bean.recordResults(challenger.fighterName, "MISS");
				}
			}
			
			if (incumbant.nextAction.equals("Position")){
				log.info("Incumbant attempting position" );
				long c = challenger.defendPositionRoll();
				long i = incumbant.getPositionRoll();
				if (i > c) {
					incumbant.applyPositionModifier(i - c);
					log.info("Incumbant Position modifier applied: " + (i-c));
					
					results_bean.recordResults(incumbant.fighterName, "HIT");
				} else {
					results_bean.recordResults(incumbant.fighterName, "MISS");
				}
			}
		}
		//rest. The resting fighter gets a bonus to stamina, but the attacker gets a bonus to hit
		if (challenger.nextAction.equals("Rest") || incumbant.nextAction.equals("Rest")) {
			if (challenger.nextAction.equals("Rest")){
				challenger.applyRestModifier();
				incumbant.applyRestingOpponentModifier();
				results_bean.recordResults(challenger.fighterName, "REST");
			}
			
			if (incumbant.nextAction.equals("Rest")){
				incumbant.applyRestModifier();
				challenger.applyRestingOpponentModifier();
				results_bean.recordResults(incumbant.fighterName, "REST");
			}
		}
		//attack and defend. In summary, each attacker rolls to hit. If they hit successfully, a defender has a chance to dodge and block
		//TODO: CURRENTLY: The attacked fighter can only dodge if he or she chose to defend this round. Otherwise a hit roll is a hit. This should be changed
		//to enable fighters to dodge with reduced chance when not defending actively.
		if (challenger.nextAction.equals("Attack") || incumbant.nextAction.equals("Attack")) {
			if (challenger.nextAction.equals("Attack")){
				//determine if hit
				boolean hit = false;
				long hitRoll = challenger.getAttackRoll();
				log.info("challenger attack roll: " + hitRoll);
				//give chance to dodge
				if (hitRoll > 0 && !incumbant.nextAction.equals("Defend")){
					hit = true;
					
				}
				if (hitRoll > 0 && incumbant.nextAction.equals("Defend")){
					if (incumbant.tryDodge(hitRoll)){
						log.info("incumbant dodged");
						hit = false;
						results_bean.recordResults(challenger.fighterName, "DODGED", "", "");
						
					} else {
						log.info("incumbant failed to dodge.");
						if (incumbant.tryBlock(hitRoll)){//give chance to block
							hit = false;
							log.info("incumbant blocked");
							results_bean.recordResults(challenger.fighterName, "BLOCKED", "", "");
						} else {
							log.info("incumbant failed blocking");
							hit = true;
						}
					}

					if (hit){
						challenger.calculateDamage(incumbant);
						results_bean.recordResults(challenger.fighterName, "HIT", incumbant.damagedLocation, incumbant.damageType);
						log.info("Challenger hit incumbant!");
					}
					
				} else {		
						results_bean.recordResults(challenger.fighterName, "MISS", "", "");
					}
				}
			}			
			if (incumbant.nextAction.equals("Attack")){
				//determine if hit
				boolean hit = false;
				long hitRoll = incumbant.getAttackRoll();
				log.info("incumbant hitRoll: " + hitRoll);//give chance to dodge
				if (hitRoll > 0 && !challenger.nextAction.equals("Defend")){
					hit = true;					
				}

				if (hitRoll > 0 && challenger.nextAction.equals("Defend")){
					if (challenger.tryDodge(hitRoll)){
						log.info("challenger dodged");
						hit = false;
						results_bean.recordResults(incumbant.fighterName, "DODGED", "", "");
					} else {
						log.info("Challenger failed to dodge");
						if (challenger.tryBlock(hitRoll)){//give chance to block
							hit = false;
							log.info("challenger blocked");
							results_bean.recordResults(incumbant.fighterName, "BLOCKED", "", "");
						} else {
							log.info("challenger failed to block");
							hit = true;
						}
					}					
					if (hit){
						incumbant.calculateDamage(challenger);
						results_bean.recordResults(incumbant.fighterName, "HIT", challenger.damagedLocation, challenger.damageType);
						log.info("incumbant hit challenger!");
					}
					
				} else {
					results_bean.recordResults(incumbant.fighterName, "MISS", "", "");
				}
			}
	}

	private void determineRiposte()  {
			//Both fighters roll against 100. If their roll is below their chance to riposte, they have succeeded
			//then the highest of the two rolls gets to riposte
			//NOTE that riposte isn't really a riposte. It's more of a response to the other fighter's action
			long c = challenger.attemptRiposte();
			long i = incumbant.attemptRiposte();
			if (c > i && c != -1){
				challenger.chooseRiposte(incumbant.nextAction);
				results_bean.recordRiposte(challenger.nextAction, challenger.fighterName);
			} else {
				if (i > c && i != -1){
					incumbant.chooseRiposte(challenger.nextAction);
					results_bean.recordRiposte(incumbant.nextAction, incumbant.fighterName);
				}
			}
	}

	private void determineActions(){
		challenger.chooseAction();
		incumbant.chooseAction();
		results_bean.recordActions(challenger.nextAction, incumbant.nextAction);		
	}

	private void getGladiatorMatches(){
		//clears the list to be sure to start fresh, then queries the db for accepted matches.
		//
		matches.clear();
		//first get all the matches that are ready to go
		// get the next tournament and all related matches
		
		GladiatorChallengeBean temp = new GladiatorChallengeBean();
		
		tournament = temp.getTournament();
		
		for (GladiatorChallengeBean match: tournament.executeTournament()){
			matches.add(match);
		}		
        
	}
		
	
//******************************************************************************************************************
	private class Fighter{
		
		private static final long DAMAGED_STAMINA_COST = 5;
		private static final long BROKEN_STAMINA_COST = 15;
		private static final long DAMAGED_PAIN_COST = 5;
		private static final int BROKEN_PAIN_COST = 10;
		
		public String fighterName = "";
		public long basePower;
		public long baseStamina;
		public double baseRiposteChance;
		public double baseHitChance;
		public double baseDodgeChance;
		public double baseBlockChance;
		public long baseInjuryResistance;
		public long restBonus;
		public String status;
		public GladiatorDataBean gldtr;
		
		public long currentPower;
		public long currentStamina;
		public double currentRiposteChance;
		public double currentHitChance;
		public double currentDodgeChance;
		public double currentBlockChance;
		public long currentInjuryResistance;
		public long determination;
		
		public long hitModifier;
		public long powerModifier;
		public long defendModifier;
		public long staminaModifier;
				
		//used as an action table
		public double attack;
		public double defend;
		public double taunt;
		public double position;
		public double rest;	
		
		public double[] actionTable = new double[5];
		
		public String[][] injuryTable = {{"Head", "OK"},
				{"NECK", "OK"},
				{"CHEST", "OK"},
				{"ABDOMEN", "OK"},
				{"OFF LEG", "OK"},
				{"MAIN LEG", "OK"},
				{"OFF ARM", "OK"},
				{"MAIN ARM", "OK"},
				{"OFF HAND", "OK"},
				{"MAIN HAND", "OK"},
				{"OFF SHOULDER", "OK"},
				{"MAIN SHOULDER", "OK"},
		};		
		
		public String damagedLocation; //used to hold damage received the current round 
		public String damageType;
		
		public String nextAction = "Rest";
		public boolean riposteFlag = false;
		
		public long weaponPowerBonus;
		public long weaponDodgeBonus;
		public long weaponBlockBonus;
		public long weaponStaminaCost;
		public long weaponRiposteBonus;
		
		public String weapon;
		
		public Fighter(GladiatorDataBean gldtr){
			this.gldtr = gldtr;
			getWeaponBonuses("sword");//TODO: Hack to simplify first build
			//all of the below calculations could be cached. However, need to test for whether improves performance or not
			//TODO these calculations will need to be set up as diminishing returns calcs
			
			fighterName = gldtr.getName();
			
			basePower = gldtr.getStrength() + gldtr.getBloodlust() + weaponPowerBonus;
			baseStamina = 2*(gldtr.getWillpower() + gldtr.getConstitution());
			baseRiposteChance = gldtr.getIntelligence() + gldtr.getSpeed() + weaponRiposteBonus;
			baseHitChance = gldtr.getWeaponSkill("Sword") + ((gldtr.getStrength() + gldtr.getSpeed() + gldtr.getAgility())/2);
			baseDodgeChance = gldtr.getAgility() + gldtr.getSpeed() + weaponDodgeBonus;
			baseBlockChance = gldtr.getStrength() + gldtr.getSpeed() + weaponBlockBonus;
			baseInjuryResistance = (gldtr.getStrength() + gldtr.getHeat() + baseStamina);
			restBonus = (int)gldtr.getConstitution()/3;
			
			//set all the variables that will fluctuate during the fight to the starting points
			status = gldtr.getStatus();			
			currentPower = basePower;
			currentStamina = baseStamina;
			currentRiposteChance = baseRiposteChance;
			currentHitChance = baseHitChance;
			currentDodgeChance = baseDodgeChance;
			currentBlockChance = baseBlockChance;
			currentInjuryResistance = baseInjuryResistance;
			nextAction = "Attack";
			
			determination = gldtr.getWillpower() + gldtr.getBloodlust() + gldtr.getHeat() + currentStamina;
			log.info("aggression = " + gldtr.getAggression() + " bloodlust = " + gldtr.getBloodlust());
			attack = 20.0 + (double)((gldtr.getAggression() + gldtr.getBloodlust())/2);
			defend = 20.0 + (double)((gldtr.getAggression() + gldtr.getBloodlust())/2);
			taunt = 15.0 + (double)(gldtr.getHeat()/2);
			position = 15 + (double)(gldtr.getHeat()/2);
			rest = 0;
			generateActiontable();
			
			hitModifier = 0;
			powerModifier = 0;
			defendModifier = 0;
			staminaModifier = 0;
			log.info("Fighter.java completed creating a new fighter");
		}
		
		public void applyRestingOpponentModifier() {
			hitModifier += REST_HIT_MODIFIER;
			
		}

		public void applyRestModifier() {
			staminaModifier += restBonus;			
		}

		public void applyPositionModifier(long modifier) {
			hitModifier += modifier;
			defendModifier =+ modifier;//defend modifier is applied to both dodge and block
			attack += modifier; //also increases chances of attacking next round
			log.info("challenger's maneouvering was successful. Bonus: " + modifier);//TODO: should also affect next round's action table
		}

		public void applyTauntModifier(long modifier) {
			hitModifier -= modifier;//challenger successfully taunts and reduces the attacker's chance to hit
			defendModifier -= modifier;
			attack += modifier;
			defend -= modifier;
			log.info("challenger's taunt successful. Stats modified by: " + modifier);
			
		}

		public String getCurrentStats() {
			return fighterName + " " +
				"Current Power: " + currentPower + " " +
				"Current Stamina: " + currentStamina + " " +
				"Current Riposte Chance: " + currentRiposteChance + " " +
				"Current Hit Chance: " + currentHitChance +" " +
				"Current Dodge Chance: " + currentDodgeChance +"\n " +
				"Current Block Chance: " + currentBlockChance +" " +
				"Current Injury Resistance: " + currentInjuryResistance + " " +
				"Current Determination: " + determination + " " +
				"hitModifier: " + hitModifier + " " +
				"powerModifier: " + powerModifier + " " +
				"defendModifier: " + defendModifier + " " +
				"staminaModifier: " + staminaModifier + "\n " +
				"attack: " + attack + " " +
				"defend: " + defend + " " +
				"taunt: " + taunt + " " +
				"position: " + position + " " +
				"rest: " + rest	+ "\n"		
				;
		}
		
		public void resetForNewRound(){
			//reset temporary round variables
			hitModifier = 0;
			powerModifier = 0;
			defendModifier = 0;
			staminaModifier = 0;
			damagedLocation = "";
			damageType = "";
		}

		public void calculateDamage(Fighter opponent) {
			// choose a location based on bloodlust and chance. There are 12 locations - 8 are non-critical and 4 critical
			// first roll is to see if was aiming for a critical area or not. This is adjusted by blood lust
			// second roll is to determine what area was hit

			Random r = new Random();
			
			int location = (int) (r.nextInt(10) + gldtr.getBloodlust() - 5);
			if (location > CRITICAL_LOCATION_BASE){
				//roll against critical location table
				location = r.nextInt(CRITICAL_BODY_PARTS.length);
				log.info(fighterName + " " + "opponent hit in: " + CRITICAL_BODY_PARTS[location]);
				opponent.applyInjuryToCriticalLocation(location, currentPower);
			} else {
				//roll against non-critical locations
				location = r.nextInt(NON_CRITICAL_BODY_PARTS.length);
				log.info(fighterName + " " + " opponent hit in: " + NON_CRITICAL_BODY_PARTS[location]);
				opponent.applyInjury(location , currentPower);
			}
			
			//determine damage to that location and the result. hands, feet, legs and arms are easier to break than head and chest
			// breaking head, neck, abdomen or chest results in incap and chance of death.			
		}

		private void applyInjury(int location, long power) {
			// determine severity of injury to a non-critical part
			Random r = new Random();
			long damage = (r.nextInt(100) + power) - currentInjuryResistance;
			damagedLocation = NON_CRITICAL_BODY_PARTS[location];
			log.info(fighterName + " received an attack of " + damage + " power against " + currentInjuryResistance + " resistance");
			if (damage > NON_CRITICAL_LOCATION_INJURY_CHANCE){
				//the damaged location is broken. This reduces speed, strength and increases stamina drain
				int i = 0;
				for (i=0; i<injuryTable.length; i++){
					if (NON_CRITICAL_BODY_PARTS[location].equals(injuryTable[i][0])){
						if (injuryTable[i][1].equals("damaged")){
							injuryTable[i][1] = "broken";
							log.info(fighterName + " " + injuryTable[i][0] + " broken");
						} else {
							injuryTable[i][1] = "damaged";
							log.info(fighterName + " " + injuryTable[i][0] + " damaged");
						}
						damageType = injuryTable[i][1];
						i = injuryTable.length;
					}
				}
			} else {
				damageType = "grazed";
			}
		}

		private void applyInjuryToCriticalLocation(int location, long power) {
			// determine severity of injury to a non-critical part
			Random r = new Random();
			long damage = (r.nextInt(100) + power) - currentInjuryResistance;
			damagedLocation = CRITICAL_BODY_PARTS[location];
			log.info(fighterName + " received an attack of " + damage + " power" + " power against " + currentInjuryResistance + " resistance");
			if (damage > currentInjuryResistance*3){
				status = "Dead";
				damageType = "broken";
				log.info(fighterName + " died due to an attack of " + damage + " power");
			} else {
				if (damage > CRITICAL_LOCATION_INJURY_CHANCE){
					//the damaged location is broken. This reduces speed, strength and increases stamina drain
					int i = 0;
					for (i=0; i<injuryTable.length; i++){
						if (CRITICAL_BODY_PARTS[location].equals(injuryTable[i][0])){
							if (injuryTable[i][1].equals("damaged")){
								injuryTable[i][1] = "broken";
								log.info(fighterName + " " + injuryTable[i][0] + " broken");
							} else {
								injuryTable[i][1] = "damaged";
								log.info(fighterName + " " + injuryTable[i][0] + " damaged");
							}
							damageType = injuryTable[i][1];
							
							i = injuryTable.length;
						}
					}
				} else {
					damageType = "grazed";
				}
			}
		}

		public boolean tryDodge(long hitRoll) {
			// dodge chance with modifiers
			Random r = new Random();
			//apply dodge cost
			staminaModifier -= DODGE_STAMINA_MODIFIER;
			if (r.nextInt(100) > (currentDodgeChance - hitRoll))
				return false; //failed dodge
			else
				return true;
		}
		
		public boolean tryBlock(long hitRoll) {
			// dodge chance with modifiers
			Random r = new Random();
			staminaModifier -= BLOCK_STAMINA_MODIFIER;
			
			if (r.nextInt(100) > (currentBlockChance - hitRoll))
				return false; //failed block
			else
				return true;
		}

		public long getAttackRoll() {
			// returns the difference between the hit chance and the roll. If the roll fails (higher than hit chance)
			Random r = new Random(); // the method returns 0 meaning a miss
			int roll = r.nextInt(100);
			//apply stamina cost
			staminaModifier -= weaponStaminaCost;
			if (roll < currentHitChance + hitModifier){
				return (int) (currentHitChance + hitModifier - roll);
			} else {
				return 0;
			}			
		}

		public long defendPositionRoll() {
			// Positioning is based on int and speed
			Random r = new Random();
			return (long)(r.nextInt(20) + (int)(gldtr.getIntelligence() + gldtr.getSpeed()/2));
		}

		public long getPositionRoll() {
			// The attempt to flank or otherwise out position your opponent. 
			Random r = new Random();
			//apply stamina cost of movement
			staminaModifier -= POSITION_STAMINA_MODIFIER;
			return (long)(r.nextInt(20) + (int)(gldtr.getIntelligence() + gldtr.getSpeed()/2));
		}

		public long defendTauntRoll() {
			// // Int/2 minus heat plus 1-100 (subtracting heat makes negative heat reduce the effectiveness of taunts)
			Random r = new Random();
			return (long)(r.nextInt(20) + (int)(gldtr.getIntelligence()/2) - gldtr.getHeat());
		}

		public long getTauntRoll() {
			// Int/2 plus heat plus 1-100
			Random r = new Random();
			return (long)(r.nextInt(20) + (int)(gldtr.getIntelligence()/2) + gldtr.getHeat());
		}

		public void applyNewRoundModifiers(){
			if (!status.equals("Dead") && !status.equals("Incapped")){
				staminaModifier += getInjuryStaminaCost();
				log.info(fighterName + " " + " current stamina = " + currentStamina + " and round modifier is " + staminaModifier);
				currentStamina += staminaModifier;//note that in most cases stamina modifier will be a negative number
				if (baseStamina < currentStamina){
					currentStamina = baseStamina;//prevent the fighter from going above his/her max stamina by resting early in fight
				}
				
				
				long staminaAdjustment = (long)((currentStamina - baseStamina)/5);//gives a negative result, so all adjustments should be addition				
			
				currentPower += powerModifier + staminaAdjustment;
				if (currentPower < 0)
					currentPower = 0;
				log.info(fighterName + " Current Power = " + currentPower + " modifier is " + powerModifier);
				log.info(fighterName + " Current sta adj = " + staminaAdjustment );
	
				currentRiposteChance += staminaAdjustment;
				currentHitChance = currentHitChance + hitModifier + staminaAdjustment;
				currentDodgeChance = currentDodgeChance + defendModifier + staminaAdjustment;
				currentBlockChance = currentBlockChance + defendModifier + staminaAdjustment;
				currentInjuryResistance = currentInjuryResistance + staminaAdjustment;
				rest = baseStamina - currentStamina;
				
				updateStatus();			
				
				if (!status.equals("Dead") && !status.equals("Exhausted")){					
					generateActiontable();
				}
				// determine whether the fighter can continue
				//status = "FIT";
			}
		}
		
		private void updateStatus() {
			// Determine whether the fighter can continue.
			// need to assess the status of the injuries, stamina etc and compare this to the char's willpower stat
			//
			
			if (currentStamina <= (int)baseStamina * .1){			//check to see if the fighter is exhausted
				status = "Exhausted";
				log.info(fighterName + " is exhausted.");
			} else {
				status = "Fit";
			}
			determination += staminaModifier;
			
			long pain = 0;//count up the injuries and get a number for pain
			for (int i = 0; i<injuryTable.length; i++){
				if (injuryTable[i][1] == "damaged"){
					pain += DAMAGED_PAIN_COST;
				} else {
					if (injuryTable[i][1] == "broken"){
						pain =+ BROKEN_PAIN_COST;
					}
				}
			}
			
			if (pain >= determination){
				status = "Concedes";
				log.info(fighterName + " concedes. Pain = " + pain + " determination = " + determination);
			}

			
		}

		private long getInjuryStaminaCost() {
			// Cycle through the injury table and calculate the cost of the injuries
			// The cost is set in static variables for easy adjustment
			long adj = 0;
			for (int i = 0; i<injuryTable.length; i++){
				if (injuryTable[i][1] == "damaged"){
					adj += DAMAGED_STAMINA_COST;
				} else {
					if (injuryTable[i][1] == "broken"){
						adj =+ BROKEN_STAMINA_COST;
					}
				}
			}
			log.info(fighterName +" Total stamina cost of injuries: " + adj);
			return adj;
		}

		public void chooseRiposte(String opponentAction) {
			//TODO this may be tweaked in the future to make the fighter
			// choose something based on chance of success			

			if (opponentAction.equals("Attack")){
				nextAction = "Defend";
			}
			if (opponentAction.equals("Defend")){
				nextAction = "Position";
			}
			if (opponentAction.equals("Taunt")){
				nextAction = "Position";
			}
			if (opponentAction.equals("Position")){
				nextAction = "Position";
			}
			if (opponentAction.equals("Rest")){
				nextAction = "Attack";
			}
			log.info("Fighter.java: " + fighterName + " Riposte successful. Selected: " + nextAction);
			
			riposteFlag = false;			
			
		}
		private void generateActiontable() {
			
			double total = attack + defend + taunt + position + rest;
			
			actionTable[0] = (int)(attack/total * 100);
			actionTable[1] = actionTable[0] + (int)(defend/total * 100);
			actionTable[2] = actionTable[1] + (int)(taunt/total * 100);
			actionTable[3] = actionTable[2] + (int)(position/total * 100);
			actionTable[4] = 100; 
			log.info("Action Table: attack = " + actionTable[0] + " defend = " + actionTable[1] + " taunt = " + actionTable[2] + " position = " + actionTable[3] + " rest = " + actionTable[4]);
		}
		private void getWeaponBonuses(String weapon) {
			if (weapon.equals("sword")){
				weaponPowerBonus = 5;
				weaponDodgeBonus = 0;
				weaponBlockBonus = 10;
				weaponStaminaCost = 6;
				weaponRiposteBonus = 5;
			}				
			
		}
		
		public String chooseAction(){
			if (!status.equals("Exhausted")){
				Random r = new Random();
				int temp = r.nextInt(100);
				boolean match = false;
				int i = 0;
				for (i=0; i<=4; i++){
					if (temp <= (int)actionTable[i] && !match){
						match = true;
						nextAction = ACTIONS[i];
					}
				}
				if (!match){
					log.info("Fighter.class: " + fighterName + " No action selected. Something broke.");
					nextAction = "Error";				
					}
			} else {
				nextAction = "Rest";
			}
			return nextAction;
		}
		
		public long attemptRiposte(){
			//rolling against their initiative attribute to see if they can riposte
			riposteFlag = false;
			Random r = new Random();
			long initiative = r.nextInt(100);
			if (initiative >= currentRiposteChance){
				return -1;
			} else {
				return initiative + (int)(currentRiposteChance/2);//adding the riposte chance modifier makes it more likely that faster fighters win the roll
			}
		}
		
	}
}
