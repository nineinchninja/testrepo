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

import com.bloodandsand.beans.GladiatorChallengeBean;
import com.bloodandsand.beans.GladiatorDataBean;
import com.bloodandsand.beans.MatchResultBean;
import com.bloodandsand.beans.TournamentDataBean;
import com.bloodandsand.utilities.BaseServlet;


public class GladiatorCombat extends BaseServlet{
	/**
	 * 
	 */
	private static final long serialVersionUID = 362330166532786787L;

	/* this is where the magic happens. This class will take two gladiators and 
	 * run them through a combat sequence and record the outcome. At the end, 
	 * the details of the combat will be available for the owners to read, and
	 * some summary statistics will be available.
	 */	
	
	protected static final Logger log = Logger.getLogger(GladiatorCombat.class.getName());
	
	public static final String[] NON_CRITICAL_BODY_PARTS = {"HAND", "HAND", "ARM", "ARM", 
															"LEG", "LEG", "SHOULDER", "SHOULDER", 
															"ARM", "ARM", 	"LEG", "LEG", 
															"SHOULDER", "SHOULDER",
															"CHEST", "ABDOMEN", "CHEST", "ABDOMEN"}; //values are repeated as a hack for increasing odds of hitting larger areas
	public static final String[] CRITICAL_BODY_PARTS = {"ABDOMEN", "CHEST", "ABDOMEN", "CHEST", "NECK", "HEAD"};	
	
	private static final long DODGE_STAMINA_MODIFIER = 10;
	private static final long BLOCK_STAMINA_MODIFIER = 3;//the attacker's strength is divided by this number
	private static final int BASE_INITIATIVE_ROLL = 20;
		
	private static final long BASE_STAMINA_BONUS = 100; //the amount added to the stamina calculation when the fighter is prepped
	private static final long BASE_HIT_CHANCE = 50;
	private static final long BASE_RIPOSTE_CHANCE = 30; //added when the attacker misses. Not added if the riposter dodged or blocked	
	private static final long BASE_DETERMINATION = 150;	
	private static final long BASE_CHANCE_OF_DEATH = 15; //Con divided by 5 is subtracted from this value to get their ability to resist death from a crit
	private static final long BASE_CHANCE_OF_DEATH_DENOMINATOR = 5;
	private static final long BASE_REST_STAMINA_BONUS = 10;
	private static final long MAXIMUM_CRITICAL_RESIST_CHANCE = 90;	
	
	private static final long MINIMUM_INITIATIVE = 10;
	private static final long MINIMUM_HIT_CHANCE = 25;
	private static final long MINIMUM_CRITICAL_CHANCE = 10;
	private static final long MAXIMUM_CRITICAL_CHANCE = 90;
	private static final long MINIMUM_DODGE_CHANCE = 10;
	private static final long MINIMUM_BLOCK_CHANCE = 10;
	private static final long MINIMUM_RIPOSTE_CHANCE = 10;
	private static final long MINIMUM_DETERMINATION = 10;
	private static final long AGGRESSIVE_TACTICS_BALANCE = 100; //used in dodge and block calculations in order to balange the advantage of bloodlust and aggression
	
	private static final double MAUL_CRITICAL_BONUS = 0.9;
	private static final double GREATAXE_CRITICAL_BONUS = 0.7;
	private static final double GREATSWORD_CRITICAL_BONUS = 0.7;
	private static final double SPEAR_CRITICAL_BONUS = 0.2;
	private static final double SWORD_CRITICAL_BONUS = 0.5;
	private static final double QUARTERSTAFF_CRITICAL_BONUS = 0.2;
	private static final double DAGGERS_CRITICAL_BONUS = 0.3;
	
	private static final long MAUL_STAMINA_COST = 15;
	private static final long GREATAXE_STAMINA_COST = 12;
	private static final long GREATSWORD_STAMINA_COST = 10;
	private static final long SPEAR_STAMINA_COST = 8;
	private static final long SWORD_STAMINA_COST = 7;
	private static final long QUARTERSTAFF_STAMINA_COST = 8;
	private static final long DAGGERS_STAMINA_COST = 5;		
	
	private static final long NORMAL_INJURY_STAMINA_COST = 12;
	private static final long CRITICAL_INJURY_STAMINA_COST = 35;
	
	private static final int MAXIMUM_MATCH_LENGTH = 40;
	
	boolean hit = false;//used to determine whether damage needs to be assessed
	boolean combatComplete;//test boolean used to end the while-loop
	long round;//tracks the number of 6 second turns in the combat
	GladiatorChallengeBean currentMatch;
	String roundDescription;
	String result;
	
	List<GladiatorChallengeBean> matches = new ArrayList<GladiatorChallengeBean>();
	Fighter challenger;
	Fighter incumbant;
	
	private MatchResultBean results_bean;
	private TournamentDataBean tournament;
		
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException{
		long startTime = + System.currentTimeMillis();
		log.info("Start of getting matchups: " + startTime);
		
		getGladiatorMatches();//gets all of the matches that are set to 'accepted' changes all other matches to expired
		log.info("Total accepted challenges found: " + matches.size());
		if (!matches.isEmpty() && matches != null){
			long startMatchesTime = System.currentTimeMillis();
			log.info("Total Time for getting matchups: " + (startTime - startMatchesTime));
			
			Iterator<GladiatorChallengeBean> it = matches.iterator();
			log.info("GladiatorCombat Class: Starting matches");
			while (it.hasNext()){
				currentMatch = it.next();
				//creating the fighters obj by sending the gladiator bean populates all the initial variables
				challenger = new Fighter(currentMatch.getChallenger());//this also calculates all initial variables and sets up the action table
				incumbant = new Fighter(currentMatch.getIncumbant());
				results_bean = new MatchResultBean(currentMatch.getChallenger(), currentMatch.getIncumbant(), challenger.weapon, incumbant.weapon);
				combatComplete = false;
				round = 0;	
				results_bean.describeMatchStart();
				
				if (TESTTOGGLE){
					write_line(req, resp, challenger.getStatistics() + "\n");
					write_line(req, resp, incumbant.getStatistics() + "\n");
				}
				while (!combatComplete){
					
					round +=1;
					
					results_bean.initializeNewRound(round); //sets up the variables. 					
					

					if (incumbant.rollInitiative() > challenger.rollInitiative()){ //winner either attacks or rests. loser defends, attacks or rests
						determineActions(incumbant, challenger); //first param = winner, second = loser
					} else {
						determineActions(challenger, incumbant);
					}
					
					if (incumbant.hitOpponent){//determines what effect, if any, the hit has
						determineOutcome(incumbant, challenger);
					}
					if (challenger.hitOpponent){
						determineOutcome(challenger, incumbant);
					}
					if (TESTTOGGLE) {
						log.info("Round " + round + " current stam modifiers = " + challenger.roundStaminaModifier + " and " + incumbant.roundStaminaModifier);
					}

					setRoundEndStats();	//updates all the relevant stats on the fighters for the next round		

					combatComplete = isCombatComplete();//check to see if the fighers can go on			
					
				}
				//when combat is complete, declare winner, loser, store resultbean, update ludus and gladiator stats
				closeMatch(currentMatch);
	
				if (TESTTOGGLE){
					write_line(req, resp, results_bean.getFightDescription());
					write_line(req, resp, challenger.fighterName + "'s stam is: " + challenger.currentStamina + " adj= " + challenger.staminaAdjustment);
					write_line(req, resp, incumbant.fighterName + "'s stam is: " + incumbant.currentStamina + " adj= " +incumbant.staminaAdjustment + "\n\n");
				}
			}
			long endTime = + System.currentTimeMillis();
			log.info("End of matchups: " + endTime);
			log.info("Total elapsed time: " + (endTime - startTime));
			log.info("Total time for matches: " + (endTime - startMatchesTime));
			
			TournamentDataBean nextTourney = new TournamentDataBean();
			nextTourney.saveTournament();
			
		} else {
			log.info("GladiatorCombat.java - no tournament to run");
		}				
	}	
	
	private void determineActions(Fighter wonInitiative, Fighter lostInitiative) {
		// first determine if the wonInitiative wants to rest. If no, then wonInitiative attacks and lostInitiative defends with chance to riposte
		// if wonInitiative rests, lostInitiative chooses whether to attack or rest
		if (!wonInitiative.needsRest()){
	//**************attacker attacks, defender defends ******************
			//process attack, defend, riposte, defend
			
			if (wonInitiative.tryAttack()){
				results_bean.recordHit(wonInitiative.fighterName);//successful attack roll
				
				if (!lostInitiative.tryDodge()){//defender try dodge
					
					if (!lostInitiative.tryBlock(wonInitiative.baseStrength)){//defender failed dodge, trying block
						
						wonInitiative.hitOpponent();//defender failed block attacker gets a hit
						
					} else {//successful blocked attack
						results_bean.recordBlock(lostInitiative.fighterName);//defender blocked successfully
						
						tryRiposte(lostInitiative, wonInitiative, false);//lost initiative can try to riposte. False indicates that the attack hit
					}					
				} else {
					results_bean.recordDodge(lostInitiative.fighterName);//defender dodged successfully. No hit
					
					tryRiposte(lostInitiative, wonInitiative, false);//lost initiative can try to riposte. False indicates that the attack hit
				}
			} else {//attacker failed attack roll
				results_bean.recordMiss(wonInitiative.fighterName);	
				
				tryRiposte(lostInitiative, wonInitiative, true);//defender can try to riposte. True indicates a complete miss
			}
	//**************attacker rests, defender can try to attack *********************
		} else {
			results_bean.recordRest(wonInitiative.fighterName);
			wonInitiative.rest();
			//the intitiative winner (attacker) elects to rest. The defender can do the same or attack
			if (!lostInitiative.needsRest()){
				
				if (lostInitiative.tryAttack()){
					results_bean.recordHit(lostInitiative.fighterName);//successful attack roll for defender
				
					if (!wonInitiative.tryDodge()){//attacker try dodge
						
						if (!wonInitiative.tryBlock(lostInitiative.baseStrength)){//attacker failed dodge, trying block
							
							lostInitiative.hitOpponent();//attacker failed block attacker gets a hit
							
						} else {//successful blocked attack
							results_bean.recordBlock(wonInitiative.fighterName);//attacker blocked successfully
							
						}					
					} else {
						results_bean.recordDodge(wonInitiative.fighterName);//attacker dodged successfully. No hit
						
					}
				} else {//attacker failed attack roll
					results_bean.recordMiss(lostInitiative.fighterName);
					
				}
			} else {
				//both fighters rested
				lostInitiative.rest();
				results_bean.recordRest(lostInitiative.fighterName);
			}
		} 		
	}

	private void tryRiposte(Fighter riposter, Fighter attacker, boolean didAttackHit) {
		//Quick and/or smart fighters can try to capitalize on their opponent's mistakes
		// the boolean 'miss' indicates whether the opponent missed (true) or if the riposter
		// dodged or blocked. 
		if (riposter.tryRiposte(didAttackHit)){//determine whether riposter finds an opening to try to riposte
			results_bean.recordRiposte(riposter.fighterName);
			if (riposter.tryAttack()){//riposter hits
				results_bean.recordHit(riposter.fighterName);
				if (!attacker.avoidCounter()){//the attacker gets one try, either block or dodge, to avoid the counterattack
					riposter.hitOpponent();
				} else {//attacker narrowly avoids the attack
					results_bean.recordAvoidRiposte(attacker.fighterName);
				}
			} else {//riposter misses
				results_bean.recordMiss(riposter.fighterName);
			}			
		}		
	}

	private void determineOutcome(Fighter hitter, Fighter receiver ) {
		// The fighter that has the flag hitOpponent set to true has successfully hit the opponent
		// This method will see what effect the hit has (critical hit, location, effects on stats)
		//
		// 1) Determine if the attacker criticaled
		// 2) Determine whether the receiver resists the critical
		// 3) Determine where the attack hit
		// 4) Determine the effects of the hit
		
		if (hitter.madeCriticalHit()){				//critical hit! Receiver attempts to resist
			results_bean.recordCriticalHit(hitter.fighterName);
			if (!receiver.resistCritical()){
				results_bean.recordReceivedCritical(receiver.fighterName);
				receiver.applyCriticalWound();
			} else {								//receiver resisted the critical
				results_bean.recordResistedCritical(receiver.fighterName);
				receiver.applyNormalWound();
			}			
		} else {									//not a critical attack
			results_bean.recordNormalWound(receiver.fighterName);
			results_bean.recordNormalHit(hitter.fighterName);
			receiver.applyNormalWound();
		}
	}

	private void setRoundEndStats() {
		// applies all stamina modifiers and other results of damage
		incumbant.applyRoundEffects();
		challenger.applyRoundEffects();	
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
	
	private void closeMatch(GladiatorChallengeBean match) {
		//update the total matches for each gladiator:
		
		// declare the winner
		if (challenger.status.equals("Dead") || challenger.status.equals("Concedes") ){
			//incumbant wins
			incumbant.gldtr.addWin(); //adds a match to the total and increments total matches
			match.applyIncumbantWin(); //awards the wager, if any, plus standard win amount
			log.info(incumbant.fighterName + " declared winner!");
			challenger.gldtr.addLoss();
			results_bean.setWinner(incumbant.fighterName);			
			if (challenger.status.equals("Dead")){
				challenger.gldtr.setStatus("DEAD");
			}
		} else {
			if ( incumbant.status.equals("Dead") || incumbant.status.equals("Concedes") ){
				//challenger wins
				challenger.gldtr.addWin(); //adds a match to the total and increments total matches
				match.applyChallengerWin();//awards the challenger with the standard amount for winning plus any wager
				incumbant.gldtr.addLoss();
				log.info(challenger.fighterName + " declared winner!");
				results_bean.setWinner(challenger.fighterName);				
				if (incumbant.status.equals("Dead")){
					incumbant.gldtr.setStatus("DEAD");
				}
			} else {
				//tie
				challenger.gldtr.addTie();				
				incumbant.gldtr.addTie();
				match.applyTie();//restores the wagered gold to available gold for each ludus
				results_bean.setWinner("Tie");
				log.info("Match declared a draw");
			}
		}
		//store all data
		if (!TESTTOGGLE){
			match.saveChallenge();
			challenger.gldtr.saveGladiator();
			incumbant.gldtr.saveGladiator();
			results_bean.saveNewResults(tournament);
			results_bean.writeSummaryStats();
		}

	}
	
	private void getGladiatorMatches(){
		//clears the list to be sure to start fresh, then queries the db for accepted matches.
		//
		matches.clear();
		//first get all the matches that are ready to go
		// get the next tournament and all related matches
		
		GladiatorChallengeBean temp = new GladiatorChallengeBean();
		
		tournament = temp.getTournament();
		//if (TESTTOGGLE){
			log.info("GladiatorCombat set new tournament date");
			tournament.setEventDate(new Date());
			tournament.saveTournament();
		//}
		if (tournament.checkTournamentDate()){
			log.info("tournament scheduled for today");
			for (GladiatorChallengeBean match: tournament.executeTournament()){
				matches.add(match);
			} 
			log.info("GladiatorCombat - total matches for tournament= " + matches.size());
		} else {
			//log.info("tournament date not equal to today, no tournament run");
		}
	}
		
	
//******************************************************************************************************************
	private class Fighter{
		
		private GladiatorDataBean gldtr;
		//core stats
		public String fighterName = "";
		private long baseStrength;
		private long baseAgility;
		private long baseSpeed;
		private long baseConstitution;
		private long baseWillpower;
		private long baseIntelligence;
		private long baseAggression; //higher = more attacking, lower = more defending
		private long baseBloodlust; //higher = more likely to kill, less likely to quit, lower = more finesse and more likely to quit
		private long baseWeaponSkill;
		
		//calculated stats
		private long baseInitiative;		
		private long baseStamina;
		private double baseRiposteChance;
		private double baseHitChance;
		private double baseDodgeChance;
		private double baseBlockChance;

		private double baseQuitChance;
		private double baseCriticalChance;
		private double baseResistCritChance;
		private double baseResistDeathChance;
		private long baseRestBonus;		
		
		public String status;//Fit, Injured, Concedes, Dead
		
		private double staminaAdjustment;
		
		private long currentInitiative;		
		private long currentStamina;
		private long weaponStaminaCost;
		private double currentRiposteChance;
		private double currentHitChance;
		private double currentDodgeChance;
		private double currentBlockChance;
		private double currentRestChance50;
		private double currentRestChance25;
		private double currentCriticalChance;
		private double currentResistCritChance;
		
		private long roundStaminaModifier = 0; //used to store up the events that effect the current stamina throughout the round
		private boolean hitOpponent = false;
		
		public String weapon = "sword";
		
		private Random rng = new Random();
		
		public Fighter(GladiatorDataBean gldtr){
			this.gldtr = gldtr;
			weapon = gldtr.getBestWeaponSkill().toLowerCase();		//TODO: Hack to simplify first runs. Will change later to select the weapon they have the 

			baseWeaponSkill = gldtr.getWeaponSkill(weapon);			// most skill in, and default to sword
			
			fighterName = gldtr.getName();

			baseStrength = gldtr.getStrength();
			baseAgility = gldtr.getAgility();
			baseSpeed = gldtr.getSpeed();
			baseConstitution = gldtr.getConstitution();
			baseWillpower = gldtr.getWillpower();
			baseIntelligence = gldtr.getIntelligence();
			baseAggression = gldtr.getAggression() + getOwnerAggression(); 
			
			if (baseAggression <= 0){baseAggression = 1;}
			if (baseAggression > 50){baseAggression = 50;}
			
			log.info(fighterName + " aggression: " + baseAggression);
			baseBloodlust = gldtr.getBloodlust() + getOwnerBloodlust();
			
			if (baseBloodlust > 50){baseBloodlust = 50;}
			if (baseBloodlust <= 0){baseBloodlust = 1;}
			log.info(fighterName + " bloodlust" + baseBloodlust);			
			
			baseStamina = BASE_STAMINA_BONUS + baseWillpower + baseConstitution;
			staminaAdjustment = 1.0;
			baseInitiative = baseAggression + baseSpeed/2 + baseBloodlust/2;	
			baseHitChance = getHitChance();
			baseDodgeChance = getDodgeChance();
			baseBlockChance = getBlockChance();
			baseRiposteChance = getRiposteChance();	
			baseCriticalChance = getCriticalChance();
			
			getWeaponBonuses();			
			
			baseQuitChance = 100 * ((baseWillpower + baseAggression + baseBloodlust) / BASE_DETERMINATION);
			if (baseQuitChance <= MINIMUM_DETERMINATION){
				baseQuitChance = MINIMUM_DETERMINATION;
			}
			
			baseResistCritChance = baseConstitution * 2;
			if (baseResistCritChance > MAXIMUM_CRITICAL_RESIST_CHANCE){
				baseResistCritChance = MAXIMUM_CRITICAL_RESIST_CHANCE;
			}
			baseResistDeathChance = BASE_CHANCE_OF_DEATH - baseConstitution/BASE_CHANCE_OF_DEATH_DENOMINATOR;
			baseRestBonus = getRestBonus();	
			
			status = "Fit";	
			
			//now set all the variables that will manage the ongoing stats. So much easier with Python :(			
			
			currentInitiative = baseInitiative;		
			currentStamina = baseStamina;
			
			currentRestChance50 = getRestChance50();//these need current stamina to be calculated
			currentRestChance25 = getRestChance25();
			currentRiposteChance = baseRiposteChance;
			currentHitChance = baseHitChance;
			currentDodgeChance = baseDodgeChance;
			currentBlockChance = baseBlockChance;
			currentCriticalChance = baseCriticalChance;	
			currentResistCritChance = baseResistCritChance;
		}

	
		public String getStatistics() {
			// Part of the testing harness. Only runs when TESTTOGGLE is on in BaseServlet
			String stats = "";
			stats = fighterName + ": " + "str: " + baseStrength + ", " +
								"agility: " + baseAgility + ", " +
								"speed: " + baseSpeed + ", " +
								"Con: " + baseConstitution + ", " +
								"Willpower: " + baseWillpower + ", " +
								"Int: " + baseIntelligence + "\n" +
								"Aggr: " + baseAggression + ", " +
								"BL: " + baseBloodlust + "\n" +
								"Initiative: " + baseInitiative + ", " +
								"Dodge: " + baseDodgeChance + ", " +
								"Block: " + baseBlockChance + ", " +
								"Riposte: " + baseRiposteChance + ", " +
								"Crit: " + baseCriticalChance + ", " +
								"CritResist: " + baseResistCritChance + "\n" +
								"Rest50 Chance: " + currentRestChance50 + ", " +
								"Rest25 Chance: " + currentRestChance25 + ", " +
								"Stam: " + baseStamina + ", " ;
					
			return stats;
		}


		//**********round action stat modifiers***************
		
		public void applyNormalWound() {
			// 1) Determine location
			// 2) Apply effects to base stats
			// 3) Apply effects to stam
			int location = 0;
			location = rng.nextInt(NON_CRITICAL_BODY_PARTS.length);	
			results_bean.recordNonCriticalInjury(this.fighterName, NON_CRITICAL_BODY_PARTS[location]);
			
			roundStaminaModifier -= NORMAL_INJURY_STAMINA_COST;
			
			if (NON_CRITICAL_BODY_PARTS[location].equals("HAND")){
				baseHitChance -= 10;
			}
			if (NON_CRITICAL_BODY_PARTS[location].equals("ARM")){
				baseHitChance -= 5;
				baseCriticalChance -= 5;
			}
			if (NON_CRITICAL_BODY_PARTS[location].equals("LEG")){
				baseDodgeChance -= 10;				
			}
			if (NON_CRITICAL_BODY_PARTS[location].equals("SHOULDER")){
				baseCriticalChance -=5;
				
			}
			if (NON_CRITICAL_BODY_PARTS[location].equals("CHEST")){
				roundStaminaModifier -= 10;
				baseResistCritChance -= 5;
				
			}
			if (NON_CRITICAL_BODY_PARTS[location].equals("ABDOMEN")){
				roundStaminaModifier -= 5;
				baseDodgeChance -= 5;				
			}					
		}

		public void applyCriticalWound() {
			//After determining that the fighter survives the hit
			// 1) Determine location
			// 2) Apply effects to base stats
			// 3) Apply effects to stam
			//
			int location = 0;
			location = rng.nextInt(CRITICAL_BODY_PARTS.length);
			results_bean.recordCriticalInjury(this.fighterName, CRITICAL_BODY_PARTS[location]);
			if (rng.nextInt(100) >= baseResistDeathChance){
				//injured. Might continue depending on a determination check at end of the round
				status = "Injured";				
				roundStaminaModifier -= CRITICAL_INJURY_STAMINA_COST;
				baseResistCritChance -= 10;//after being critted once, chances resisting decrease				
				
				if (CRITICAL_BODY_PARTS[location].equals("CHEST")){
					roundStaminaModifier -= 10;					
					
				}
				if (CRITICAL_BODY_PARTS[location].equals("ABDOMEN")){
					roundStaminaModifier -= 10;
					baseDodgeChance -= 15;				
				}
				if (CRITICAL_BODY_PARTS[location].equals("NECK")){
					baseDodgeChance -= 15;
					baseBlockChance -= 15;
					
				}
				if (CRITICAL_BODY_PARTS[location].equals("HEAD")){
					baseRiposteChance -= 15;
					baseDodgeChance -= 15;
					baseBlockChance -= 15;					
				}
			} else {
				status = "Dead";
				gldtr.setStatus("Dead");
				results_bean.recordDeath(this.fighterName);
			}			
		}

		private void applyBlock(long opponentStr){
			roundStaminaModifier -= opponentStr / BLOCK_STAMINA_MODIFIER;
		}
		
		private void applyDodge(){
			roundStaminaModifier -= DODGE_STAMINA_MODIFIER;
		}
		
		private void applyAttack(){
			roundStaminaModifier -= weaponStaminaCost;
		}
		
		public void hitOpponent() {
			hitOpponent = true;
			
		}
		
		public void applyRoundEffects(){
			//these two numbers affect all the other numbers so must be set first
			applyStaminaModifiers();
			if (currentStamina <=0){
				currentStamina = 1;
			}
			getStaminaAdjustment();		
			if (status.equals("Injured") || status.equals("Exhausted")){
				determineIfCanContinue();
			}			
			
			if (!status.equals("Dead") && !status.equals("Concedes")){
				
				currentInitiative = (long) (baseInitiative * staminaAdjustment);	
				if (currentInitiative < MINIMUM_INITIATIVE ){
					currentInitiative = MINIMUM_INITIATIVE;
				}
				
				currentRestChance50 = getRestChance50();//these need current stamina to be calculated
				currentRestChance25 = getRestChance25();
				currentRiposteChance = baseRiposteChance * staminaAdjustment;
				currentHitChance = baseHitChance * staminaAdjustment;
				currentDodgeChance = baseDodgeChance * staminaAdjustment;
				currentBlockChance = baseBlockChance * staminaAdjustment;
				currentCriticalChance = baseCriticalChance * staminaAdjustment;	
				currentResistCritChance = baseResistCritChance * staminaAdjustment;
				
				if (staminaAdjustment < 0.1){
					results_bean.recordExhausted(this.fighterName);
				} else {
					if (staminaAdjustment < 0.25){
						results_bean.recordTired(this.fighterName);
					} else {
						if (staminaAdjustment < 0.5){
							results_bean.recordTiring(this.fighterName);
						}
					}
				}
				//reset round specific variables
				roundStaminaModifier = 0;
				hitOpponent = false;
			}// TODO: dead or concedes stuff here

		}
		
		//****************Combat rolls********************		


		private void determineIfCanContinue() {
			if (rng.nextInt(100) > baseQuitChance){
				status = "Concedes";
				results_bean.recordConcede(this.fighterName);
			}
		
		}
		
		public boolean tryRiposte(boolean didAttackHit) {
			// This is just to see if the fighter can find an opening to counterattack
			double chance = currentRiposteChance;
			if (didAttackHit){
				chance += BASE_RIPOSTE_CHANCE;
			}
			if (rng.nextInt(100) < chance){
				return true;
			} else {
				return false;
			}
		}
		
		public boolean avoidCounter() {
			// The fighter gets one chance to avoid a counterattack, whichever is LOWER, dodge or block
			double chance = currentBlockChance;
			if (currentDodgeChance > currentBlockChance){
				chance = currentDodgeChance;
			}
			if ((rng.nextDouble()*100) > chance){
				return false;
			} else {
				return true;
			}
		}

		public boolean tryBlock(long opponentStr) {
			// Fighter attempts to block. The stamina cost of a successful block is dependent on the attacker's strength
			double roll = rng.nextDouble() * 100;
			if (roll > currentBlockChance){
				return false;
			} else {
				applyBlock(opponentStr);
				return true;
			}
		}

		public boolean tryDodge() {
			// fighter attempts to dodge
			double roll = rng.nextDouble() * 100;
			if (roll > currentDodgeChance){
				return false;
			} else {
				applyDodge();
				return true;
			}
		}

		public boolean tryAttack() {
			double roll = rng.nextDouble() * 100;
			applyAttack();
			if (roll > currentHitChance){
				return false;
			} else {
				return true;
			}
		}
		
		public boolean needsRest() {
			// check staminaAdjustment. If below .5, see if the fighter rests using the currentRestChance50
			if (staminaAdjustment > 0.5){//never rest if above 50% stamina
				return false;
			}
			if (staminaAdjustment > 0.25){//if between 25 and 50%, use the currentRestChance50. 
				int chance = rng.nextInt(100);
				if (chance > currentRestChance50){ //higher than restChance = doesn't rest
					return false;
				} else {
					return true;
				}
			}
			if (staminaAdjustment > 0.1){//if between 10 and 50%, use the currentRestChance25. 
				int chance = rng.nextInt(100);
				if (chance > currentRestChance25){ //higher than restChance = doesn't rest
					return false;
				} else {
					return true;
				}
			}
			status = "Exhausted";
			return true;//if below 10% stamina, always rest
		}

		public long rollInitiative() {
			// simple random generated plus the current initiative stat
			return rng.nextInt(BASE_INITIATIVE_ROLL) + currentInitiative;
		}
		
		public boolean madeCriticalHit() {
			// Determines whether the attacker's hit was a critical hit
			if ((rng.nextDouble() * 100) < currentCriticalChance){
				return true;
			} else {
				return false;
			}
		}
		
		public boolean resistCritical() {
			// the receiver of a critical hit attempts to shrug it off
			// the ability is based entirely on constitution and current stamina.

			if ((rng.nextDouble()*100 > currentResistCritChance)){
				return false;
			} else {
				return false;
			}			
		}


	////**********Setters for various stats*********************************************
		
		
		private void applyStaminaModifiers() {
			// 
			currentStamina += roundStaminaModifier;
			
		}
		private void getStaminaAdjustment(){
			//double  = currentStamina;
			if (baseStamina <=0 ){
				baseStamina = 1;
			}
			if (currentStamina <= 0){
				currentStamina = 0;
			}
			staminaAdjustment = currentStamina / (double) baseStamina;
		}
		
		public void rest() {
			roundStaminaModifier += baseRestBonus;
			
		}		
		
		private void getWeaponBonuses(){
			if (weapon.equals("maul")){
				weaponStaminaCost = MAUL_STAMINA_COST;
				baseCriticalChance += MAUL_CRITICAL_BONUS * baseWeaponSkill;
				baseStrength += 10;
			}
			if (weapon.equals("greataxe")){
				weaponStaminaCost = GREATAXE_STAMINA_COST;
				baseCriticalChance += GREATAXE_CRITICAL_BONUS * baseWeaponSkill;
				baseStrength += 5;				
			}
			if (weapon.equals("greatsword")){
				weaponStaminaCost = GREATSWORD_STAMINA_COST;
				baseCriticalChance += GREATSWORD_CRITICAL_BONUS * baseWeaponSkill;		
			}
			if (weapon.equals("spear")){
				weaponStaminaCost = SPEAR_STAMINA_COST;
				baseCriticalChance += SPEAR_CRITICAL_BONUS * baseWeaponSkill;
				baseSpeed += 5;	
			}
			if (weapon.equals("sword")){
				weaponStaminaCost = SWORD_STAMINA_COST;
				baseCriticalChance += SWORD_CRITICAL_BONUS * baseWeaponSkill;
			}
			if (weapon.equals("quarterstaff")){
				weaponStaminaCost = QUARTERSTAFF_STAMINA_COST;
				baseCriticalChance += QUARTERSTAFF_CRITICAL_BONUS * baseWeaponSkill;
				baseIntelligence += 5;	
			}
			if (weapon.equals("daggers")){
				weaponStaminaCost = DAGGERS_STAMINA_COST;
				baseCriticalChance += DAGGERS_CRITICAL_BONUS * baseWeaponSkill;
				baseSpeed += 10;	
			}
		}
		
		private long getRestBonus() {
			// 
			return baseConstitution + BASE_REST_STAMINA_BONUS;
		}

		private double getRestChance25() {
			// will the fighter rest when below 25% stamina. Maximum amount here is 
			if ((currentStamina + (baseWillpower/2)) >= 100.0){
				return 0.0;
			} else {
				return 100.0 - (currentStamina + (baseWillpower/2));
			}				
		}

		private double getRestChance50() {
			// This number is used when the fighter's stamina is below 50%
			if ((currentStamina + baseWillpower >= 100)){
				return 0.0;
			} else {
				return 100.0 - (currentStamina + baseWillpower);
			}			
		}

		private double getCriticalChance() {
			// Strength is a large determining factor in critical chance. 
			double chance = baseStrength;
			if (baseAgility >= baseStrength){
				chance = baseAgility/4;
			}
			if (baseIntelligence >= baseAgility){
				chance = baseIntelligence;
			}
			
			chance += ((baseStrength/2) + (baseBloodlust/2)) * staminaAdjustment;
			if (chance <= MINIMUM_CRITICAL_CHANCE){
				return MINIMUM_CRITICAL_CHANCE;
			}
			if (chance >= MAXIMUM_CRITICAL_CHANCE){
				return MAXIMUM_CRITICAL_CHANCE;
			}
			return chance;
		}

		private double getRiposteChance() {
			// This is only to see if the fighter finds an opening for a riposte. If successful, 
			// a hit roll is still required
			// NOTE: the base riposte chance is only added when the attacker misses, not when the 
			// riposter dodges or blocks
			double chance = 0.0;
			if (baseIntelligence > baseSpeed){
				chance = (baseWeaponSkill + baseIntelligence) * staminaAdjustment;
			} else {
				chance = (baseWeaponSkill + baseSpeed) * staminaAdjustment;
			}
			if (chance > MINIMUM_RIPOSTE_CHANCE){
				return chance;
			} else {
				return MINIMUM_RIPOSTE_CHANCE;
			}
		}
		
		private double getBlockChance() {
			// Block chance can be based on strength or speed.
			double chance = 0.0;
			if (baseStrength > baseSpeed){
				chance = ((AGGRESSIVE_TACTICS_BALANCE / (baseAggression + baseBloodlust)) + 
						(baseStrength /2 )) * staminaAdjustment;
			} else {
				chance = ((AGGRESSIVE_TACTICS_BALANCE / (baseAggression + baseBloodlust)) + 
						(baseSpeed /2 )) * staminaAdjustment;
			}			
			
			if (chance > MINIMUM_BLOCK_CHANCE){
				return chance;
			} else {
				return MINIMUM_BLOCK_CHANCE;
			}
		}

		private double getDodgeChance() {
			// dodge is affected by aggression, in order to balance defensive tactics			
			double chance = ((AGGRESSIVE_TACTICS_BALANCE / (baseAggression + baseBloodlust)) + 
					(baseAgility + baseSpeed)/4 ) * staminaAdjustment;
			
			if (chance > MINIMUM_DODGE_CHANCE){
				return chance;
			} else {
				return MINIMUM_DODGE_CHANCE;
			}
		}

		private double getHitChance() {
			// hit chance depends on either speed or strength, whichever is greater
			double chance = 0.0;
			if (baseSpeed >= baseStrength){
				chance = (BASE_HIT_CHANCE + baseWeaponSkill + (baseSpeed / 2)) * staminaAdjustment;
				if (chance > MINIMUM_HIT_CHANCE){
					return chance;
				} else {
					return MINIMUM_HIT_CHANCE;
				}				 
			} else {
				chance = (BASE_HIT_CHANCE + baseWeaponSkill + (baseStrength / 2)) * staminaAdjustment;
				if (chance > MINIMUM_HIT_CHANCE){
					return chance;
				} else {
					return MINIMUM_HIT_CHANCE;
				}
			}			
		}

		private long getOwnerBloodlust() {
			// TODO When available through strategy panel
			return rng.nextInt(50);
		}

		private long getOwnerAggression() {
			// TODO When available through strategy panel
			return rng.nextInt(50);
		}		
	}
}
