package com.bloodandsand.utilities;
import java.util.List;
import java.util.Set;

import com.bloodandsand.beans.GladiatorChallengeBean;

public abstract class CoreBean {
	//Datastore key related strings
	protected static String gladiatorGroup = "gladiators";	
	protected static String gladiatorKindName = "Gladiators";
	protected static String gladiatorEntity = "Gladiator";
	
	protected static String accountGroup = "UserAccounts";
	protected static String accountKindName = "account";
	protected static String accountEntity = "account";
	
	protected static String challengeGroup = "challenges";
	protected static String challengeKindName = "Challenges";
	protected static String challengeEntity = "challenge";
	
	protected static String weaponSkillsKind = "WeaponSkills";
	
	
	protected static int BASE_NUMBER_OF_RECRUITS = 25;//used to limit the query for the market place
	protected static int BASE_NUMBER_OF_TRAINING_GLADIATORS = 1000; //used to create the query for applying training
	protected static int BASE_NUMBER_OF_CHALLENGEABLE_GLADIATORS = 1000;
	protected static int BASE_NUMBER_FOR_TRAINING_SOFTCAP = 350;
	protected static int MAXIMUM_ATTRIBUTE_SCORE = 50;
	protected static int MAXIMUM_SKILL_SCORE = 20;
	protected static int TRAINING_INCREMENT_AMOUNT = 1; //the amount a skill or attribute increases by on successful training
	
	protected static String[] ATTRIBUTES = {"strength", "agility", "speed", "intelligence", "constitution", "willpower"};
	protected static String[] WEAPONSKILLS = {"sword", "daggers", "greataxe", "greatsword", "maul", "spear", "hand to hand", "quarterstaff", };
	
	public enum Status {INITIATED, ACCEPTED, DECLINED, EXPIRED, CANCELED};	

}
