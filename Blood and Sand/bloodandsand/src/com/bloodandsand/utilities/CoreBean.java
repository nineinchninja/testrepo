package com.bloodandsand.utilities;

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
	
	protected static String rankingsKey = "RANKINGS";
	
	
	protected static int BASE_NUMBER_OF_RECRUITS = 25;//used to limit the query for the market place
	protected static int BASE_NUMBER_OF_TRAINING_GLADIATORS = 1000; //used to create the query for applying training
	protected static int BASE_NUMBER_OF_CHALLENGEABLE_GLADIATORS = 1000;
	protected static int BASE_NUMBER_FOR_ATTRIBUTE_TRAINING_SOFTCAP = 270;
	protected static int MAXIMUM_ATTRIBUTE_SCORE = 50;
	protected static int MAXIMUM_SKILL_SCORE = 50;
	protected static int TRAINING_INCREMENT_AMOUNT = 1; //the amount a skill or attribute increases by on successful training
	
	protected static String[] ATTRIBUTES = {"strength", "agility", "speed", "intelligence", "constitution", "willpower"};
	protected static String[] WEAPONSKILLS = {"sword", "daggers", "greataxe", "greatsword", "maul", "spear", "hand to hand", "quarterstaff", };
	
	protected static String[]  ATTRIBUTE_DESCRIPTION = { "Untrained", "Very Poor", "Poor", "Below Average", "Average",
														"Above Average", "Good", "Very Good", "Excellent", "Outstanding", "Maximum"};
	
	protected static int[]  ATTRIBUTE_RATING = { 5, 9, 14, 19, 24, 
												29, 34, 39, 44, 48, 50};
	
	
	public enum Status {INITIATED, ACCEPTED, DECLINED, EXPIRED, COMPLETED, CANCELED};	
	
	public static int STANDARD_WIN_AMOUNT = 10; //the amount that a gladiator wins for winning a fight, regardless of wager
	public static long BASE_WIN_RATING_BONUS = 10; //the base amount you will gain for a win, regardless of opponent's rating
	
	protected int tournamentFrequency = 24 ;//hours between scheduled tournaments
	protected static int MAX_TOURNAMENTS = 1; //limit for query on existing tournaments

	
	protected String getAttributeRating(long score){
		String description = "";
		for (int i = 0; i<ATTRIBUTE_RATING.length; i++){
			description = ATTRIBUTE_DESCRIPTION[i];
			if (score <= ATTRIBUTE_RATING[i]){				
				i = ATTRIBUTE_RATING.length;
			}
		}
		return description;
	}
	
	public String capitalizeWord(String s){
		
		String capital   = Character.toString(s.charAt(0)).toUpperCase();
		return capital + s.substring(1);
	}
	
	
}
