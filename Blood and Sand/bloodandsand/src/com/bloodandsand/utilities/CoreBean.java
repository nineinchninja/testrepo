package com.bloodandsand.utilities;
import java.util.List;
import java.util.Set;

import com.bloodandsand.beans.GladiatorChallengeBean;

public abstract class CoreBean {
	
	protected static final boolean TESTTOGGLE = false;
	//Datastore key related strings

	protected static String gladiatorEntity = "Gladiator";	

	protected static String accountEntity = "UserAccount";
	
	protected static String ludusEntity = "Ludus";
	
	protected static String challengeEntity = "Challenge";
	
	protected static String matchResultEntity = "MatchResult";
	
	protected static String weaponSkillsEntity = "WeaponSkills";
	
	protected static String tournamentEntity = "Tournament";
	
	
	protected static int BASE_NUMBER_OF_RECRUITS = 25;//used to limit the query for the market place
	protected static int BASE_NUMBER_OF_TRAINING_GLADIATORS = 1000; //used to create the query for applying training
	protected static int BASE_NUMBER_OF_CHALLENGEABLE_GLADIATORS = 1000;
	protected static int BASE_NUMBER_FOR_ATTRIBUTE_TRAINING_SOFTCAP = 270;
	protected static int MAXIMUM_ATTRIBUTE_SCORE = 50;
	protected static int MAXIMUM_SKILL_SCORE = 50;
	protected static int TRAINING_INCREMENT_AMOUNT = 1; //the amount a skill or attribute increases by on successful training
	
	protected static String[] ATTRIBUTES = {"strength", "agility", "speed", "intelligence", "constitution", "willpower"};
	protected static String[] WEAPONSKILLS = {"sword", "daggers", "greataxe", "greatsword", "maul", "spear", "hand to hand", "quarterstaff", };
	
	public enum Status {INITIATED, ACCEPTED, DECLINED, EXPIRED, CANCELED};	
	
	protected int tournamentFrequency = 48 ;//hours between scheduled tournaments
	protected static int MAX_TOURNAMENTS = 5; //limit for query on existing tournaments

}
