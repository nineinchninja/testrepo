/**
 * 
 */
package com.bloodandsand.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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

/**
 * @author Andrew Hayward
 * Created December 2012
 *
 */
public class GladiatorDataBean extends CoreBean implements java.io.Serializable  {

	/**
	 * This class is used to manage all activities related to gladiators. It is a wrapper for the 
	 * datastore operations, and contains key methods for creating new gladiators for the market, 
	 * updating gladiator skills and attributes etc
	 * 
	 */
	private static final long serialVersionUID = -1717471076334573064L;
	private long strength;
	private long agility;
	private long speed;
	private long intelligence;
	private long constitution;
	private long willpower;
	
	private long bloodlust; 
	private long aggression; 
	private long heat; 							
	private long consistency;
	
	public String name;
	private String gender;
	
	private List<GladiatorChallengeBean> challenges;
	public long wins;
	public long losses;
	public long ties; 
	public long matches; //total matches 
	public long popularity;
	
	private String currentTrainingFocus;
	private Date lastTrainingChangeDate;
	private String trainingHistory;
	
	public long price;	
	private String status;	
	private GladiatorWeaponSkillsBean weaponSkills;	
	public String owner; 
	
	public Key key;
	
	protected static final Logger log = Logger.getLogger(GladiatorDataBean.class.getName());
	
	
	public GladiatorDataBean(){
		
	}
	
	public GladiatorDataBean(Entity g) {
		this.setDataStoreKey(g.getKey());
		this.setStrength((Long)(g.getProperty("strength")));
		this.setAgility((Long)(g.getProperty("agility")));
		this.setSpeed((Long)(g.getProperty("speed")));
		this.setIntelligence((Long)(g.getProperty("intelligence")));
		this.setConstitution((Long)(g.getProperty("constitution")));
		this.setBloodlust((Long)(g.getProperty("bloodlust")));
		this.setAggression((Long)(g.getProperty("aggression")));
		this.setHeat((Long)(g.getProperty("chattiness")));
		this.setConsistency((Long)(g.getProperty("consistency")));
		this.setWillpower((Long)(g.getProperty("willpower")));
		this.setWins((Long)(g.getProperty("wins")));
		this.setLosses((Long)(g.getProperty("losses")));
		this.setTies((Long)(g.getProperty("ties")));
		this.setMatches((Long)g.getProperty("matches"));
		this.setPopularity((Long)(g.getProperty("popularity")));
		this.setPrice((Long)(g.getProperty("price")));
		
		this.setStatus((String)(g.getProperty("status")));
		
		
		this.weaponSkills = findGladiatorWeaponSkills(g);
		
		this.setCurrentTrainingFocus((String)(g.getProperty("currentTrainingFocus")));
		this.setLastTrainingChangeDate((Date)(g.getProperty("lastTrainingChangeDate")));
		this.setTrainingHistory((String)(g.getProperty("trainingHistory")));
		
		this.setOwner((String)g.getProperty("owner")); 
		this.setGender((String)g.getProperty("gender"));
		this.setName((String)g.getProperty("name"));		
	}
	
	private GladiatorWeaponSkillsBean findGladiatorWeaponSkills(Entity g) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();		
		Key gladKey = g.getKey();
		
		Query weaponSkillQuery = new Query(weaponSkillsKind).setAncestor(gladKey);
		
		GladiatorWeaponSkillsBean x = new GladiatorWeaponSkillsBean( datastore.prepare(weaponSkillQuery).asSingleEntity());	
		return x;
	}
	
	private Entity findGladiatorWeaponSkills(Key keyIn) {
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();		
		Query weaponSkillQuery = new Query(weaponSkillsKind).setAncestor(keyIn);
		
		return datastore.prepare(weaponSkillQuery).asSingleEntity();	
	}

	public void setDataStoreKey(Key key2) {
		// TODO Auto-generated method stub
		this.key = key2;
	}
	
	public GladiatorWeaponSkillsBean getWeaponSkills(){
		return this.weaponSkills;
	}

	public void createGladiator(){
		Boolean sanityCheck = false;
		Random r = new Random();
		while (!sanityCheck){//included in a while loop to avoid gladiators with too extreme stats	
			strength = r.nextInt(19) + 10;
			agility = r.nextInt(19) + 10;
			speed = r.nextInt(19) + 10;
			intelligence = r.nextInt(19) + 10;
			constitution = r.nextInt(19) + 10;
			willpower = r.nextInt(19) + 10;
			long i = strength + agility + speed + intelligence + constitution + willpower;
			if (i >= 70 && i <= 100){
				sanityCheck = true;
			}
		}		
		bloodlust = r.nextInt(10) - 6; 
		aggression = r.nextInt(10) - 6; 
		heat = r.nextInt(10) - 6; 						
		consistency = r.nextInt(10) - 6;	

		weaponSkills = new GladiatorWeaponSkillsBean();//creates a new bean with all weapon skills at 0
		
		currentTrainingFocus = "None"; //training record information
		trainingHistory = "";
		lastTrainingChangeDate = new Date();
		wins = 0;
		losses = 0;
		ties = 0;
		matches = 0;
		popularity = 50;
		name = null;
		gender = setGender();
		
		status = "FIT";
		owner = null;
		setPrice();
	}
	
	public void saveGladiator() {//this is for saving a new gladiator
		Key gladiatorKey = KeyFactory.createKey("Gladiators", gladiatorGroup);
		Entity newGladiator = new Entity("Gladiator", gladiatorKey);
		Entity newWeaponSkills = weaponSkills.createWeaponSkillsEntity(newGladiator.getKey());
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		setUpGladiatorEntity(newGladiator, newWeaponSkills);
		Transaction txn = datastore.beginTransaction();
		try {
			
			datastore.put(newGladiator);
			
			datastore.put(newWeaponSkills);
			txn.commit();
		} finally {	
		    if (txn.isActive()) {
		        txn.rollback();
		        log.warning("Save New Gladiator transaction failed: rolled back");
		    }
		}	
	}
	
	private void setUpGladiatorEntity(Entity newGladiator, Entity newWeaponSkills){
		newGladiator.setProperty("strength", strength);
		newGladiator.setProperty("agility", agility);
		newGladiator.setProperty("speed", speed);
		newGladiator.setProperty("intelligence", intelligence);
		newGladiator.setProperty("constitution", constitution);
		newGladiator.setProperty("willpower", willpower);			
		
		newGladiator.setProperty("bloodlust", bloodlust); 
		newGladiator.setProperty("aggression", aggression); 
		newGladiator.setProperty("chattiness", heat); 							
		newGladiator.setProperty("consistency", consistency);
		
		newGladiator.setProperty("wins", wins);
		newGladiator.setProperty("losses", losses);
		newGladiator.setProperty("ties", ties);
		newGladiator.setProperty("matches", matches);
		newGladiator.setProperty("popularity", popularity);
		
		newGladiator.setProperty("price", price);
		
		newGladiator.setProperty("status", status);
			
		newGladiator.setProperty("currentTrainingFocus", currentTrainingFocus) ;//training record information
		newGladiator.setProperty("trainingHistory", trainingHistory);
		newGladiator.setProperty("lastTrainingChangeDate", lastTrainingChangeDate);			
		
		newGladiator.setProperty("owner", owner); 
		
		newGladiator.setProperty("name", name);
		newGladiator.setProperty("gender", gender);	
		
		newWeaponSkills.setProperty("sword", weaponSkills.getSword());
		newWeaponSkills.setProperty("daggers", weaponSkills.getDaggers());
		newWeaponSkills.setProperty("greatsword", weaponSkills.getGreatsword());
		newWeaponSkills.setProperty("greataxe", weaponSkills.getGreataxe());
		newWeaponSkills.setProperty("spear", weaponSkills.getSpear());
		newWeaponSkills.setProperty("quarterstaff", weaponSkills.getQuarterstaff());					
		newWeaponSkills.setProperty("maul", weaponSkills.getMaul()); 
		newWeaponSkills.setProperty("handToHand", weaponSkills.getHandToHand());
	}
	
	public void saveGladiator(String keyIn) {//updating an existing gladiator after making some changes

		Key gladiatorKey = KeyFactory.stringToKey(keyIn); //which looks the gladiator up in the ds and updates it		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity newGladiator;
		Entity newWeaponSkills;
		try {
			newWeaponSkills  = findGladiatorWeaponSkills(gladiatorKey);
			newGladiator = datastore.get(gladiatorKey);
		} catch (EntityNotFoundException e) {
			log.info("GladiatorDataBean.java: Didn't find the gladiator when using key search");
			e.printStackTrace();
			return;
		}
		
		if (newWeaponSkills == null){
			log.info("No weaponskill entity found");
		}
		setUpGladiatorEntity(newGladiator, newWeaponSkills);
		Transaction txn = datastore.beginTransaction();
		
		try {	
			datastore.put(newGladiator);	
			datastore.put(newWeaponSkills);
			txn.commit();
		} finally {	
		    if (txn.isActive()) {
		        txn.rollback();
		        log.warning("Save Gladiator transaction failed: rolled back");
		    }
		}	
	}

	public Entity findGladiatorEntityByKey(Key challengerKey){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query(gladiatorEntity);

        q.setFilter(new FilterPredicate (Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, challengerKey));
        Entity results = datastore.prepare(q).asSingleEntity();        
        return results;
	}	
	
	public int countAvailableGladiators(FetchOptions free_recruit_check){//used in the FreshRecruits cron job
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query(gladiatorEntity);
        q.isKeysOnly();
        Filter unowned = new FilterPredicate("owner", FilterOperator.EQUAL, null);
        Filter alive = new FilterPredicate("status", FilterOperator.EQUAL, "FIT");
        Filter currentRecruits = CompositeFilterOperator.and(unowned, alive);
        q.setFilter(currentRecruits);
        int results = datastore.prepare(q).countEntities(free_recruit_check);
        log.info("total returned: " + results);
        return results;
	}
	
	public void getGladiatorsChallenges(){
		//first ensure the key is available to search by
		if (this.key == null){
			log.warning("Gladiator Key not found: Cannot search for gladiator's challenges");
		} else {
			List<Entity> results = null;		
			//queries are limited in their filters, so I filtered based on the status manually
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Query q = new Query(challengeEntity);
			Filter challngr = new FilterPredicate("challengerKey", FilterOperator.EQUAL, this.getKey());
			Filter incumb = new FilterPredicate("incumbantKey", FilterOperator.EQUAL, this.getKey());
			Filter currentChallenges = CompositeFilterOperator.or(incumb, challngr); //inactive);
			q.setFilter(currentChallenges);
			FetchOptions max_number_challenges =
				    FetchOptions.Builder.withLimit(10);
			results = datastore.prepare(q).asList(max_number_challenges);
			log.info("Found " + results.size() + " challenges for " + name);
			if (results.size() > 0){
				List<GladiatorChallengeBean> challs = new ArrayList<GladiatorChallengeBean>();
				for (Entity ent : results){
					GladiatorChallengeBean nxt = new GladiatorChallengeBean(ent, false);
					//add the gladiators to the challenge without creating a loop
					if (this.getKey().equals(nxt.getChallengerKey())){
						nxt.setChallenger(this);
						nxt.findIncumbant();
					} else {
						if (this.getKey().equals(nxt.getIncumbantKey())){
							nxt.setIncumbant(this);
							nxt.findChallenger();
						} else {
							log.info("No match on keys");
						}
					}
					if (nxt.getStatusEnum() != Status.EXPIRED && nxt.getStatusEnum() != Status.CANCELED && nxt != null){
						challs.add(nxt);
						log.info("added challenge to " + this.name);
					}				
				}
				this.challenges = challs;
			}
						
		}
		
	}
	
	public boolean isGladiatorAvailableToChallenge(){
		if (challenges == null) {
			this.getGladiatorsChallenges();
		}
		if (!this.status.equals("FIT")){
			log.info("Gladiator is not fit");
			return false;
		}
		
		if (this.name.equalsIgnoreCase("none")){
			log.info("Gladiator has no name");
			return false;
		}
		boolean avail = true;
		if (challenges != null){
			Iterator<GladiatorChallengeBean> iter = challenges.iterator();
			while (iter.hasNext()){
				if (iter.next().getStatusEnum() == Status.ACCEPTED){
					avail = false;
					log.info("Gladiator has accepted a challenge");
				}
			}
		}
				
		return avail;
	}
	
	public List<GladiatorDataBean> getAllChallengeableGladiators(String userName){//used in creating challenges
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		List<GladiatorDataBean> gladiators = new ArrayList<GladiatorDataBean>(); 
		
		Query q = new Query("Gladiator");
		
        Filter owned = new FilterPredicate("owner", FilterOperator.NOT_EQUAL, null);
        Filter alive = new FilterPredicate("status", FilterOperator.EQUAL, "FIT");
        Filter available = CompositeFilterOperator.and(owned, alive);
        q.setFilter(available);
        FetchOptions max_available_gladiators = FetchOptions.Builder.withLimit(BASE_NUMBER_OF_CHALLENGEABLE_GLADIATORS);
        List<Entity> results = datastore.prepare(q).asList(max_available_gladiators);
        log.info("total returned challengeable gladiators: " + results.size());
        Iterator<Entity> it = results.iterator();
        while (it.hasNext()){
        	GladiatorDataBean temp = new GladiatorDataBean(it.next());
        	if (temp.isGladiatorAvailableToChallenge() && !temp.getOwner().equals(userName)){
        		log.info("Gladiator: " + temp.getName() + " has been added, owned by " + temp.getOwner());
        		gladiators.add(temp);
        	}        	
        }
        if (gladiators != null){
        	log.info("total returned challengeable gladiators: " + gladiators.size());
        }
        
        return gladiators;
	}
	
	
	public List<GladiatorDataBean> getGladiatorsOnSale(){//used in the gladiator market
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		List<GladiatorDataBean> gladiators = new ArrayList<GladiatorDataBean>(); 
		Query q = new Query("Gladiator");
		
        Filter unowned = new FilterPredicate("owner", FilterOperator.EQUAL, null);
        Filter alive = new FilterPredicate("status", FilterOperator.EQUAL, "FIT");
        Filter currentRecruits = CompositeFilterOperator.and(unowned, alive);
        q.setFilter(currentRecruits);
        FetchOptions gladiator_market_check = FetchOptions.Builder.withLimit(BASE_NUMBER_OF_RECRUITS);
        List<Entity> results = datastore.prepare(q).asList(gladiator_market_check);
        log.info("total returned available recruits: " + results.size());
        Iterator<Entity> it = results.iterator();
        while (it.hasNext()){
        	GladiatorDataBean temp = new GladiatorDataBean(it.next());
        	gladiators.add(temp);
        }
        return gladiators;
	}
	
	public void setChallenges(List<GladiatorChallengeBean> challenges){
		this.challenges = challenges;
	}
	
	public List<GladiatorChallengeBean> getChallenges(){
		if (challenges != null){
			return challenges;
		} else {
			return null;
		}
	}
	
	
	public String setGender(){//this with no arguments is used to set the gender for a new gladiator, not for
		Random r = new Random();// sex change operations
		int g = r.nextInt(10);
		if (g > 3){
			return "M";
		} else {
			return "F";
		}
	}
	
	public void setNewTraining(String training){
		this.trainingHistory.concat("/n " + this.currentTrainingFocus);
		this.currentTrainingFocus = training;
		this.lastTrainingChangeDate = new Date();
	}	

	public void setKey(String key) {
		this.key = KeyFactory.stringToKey(key);	
	}
	
	public String getKey(){
		if (this.key != null){
			return KeyFactory.keyToString(this.key);
		} else {
			return null;
		}
	}
	
	public Key getDataStoreKey(){
		return this.key;
	}

	public void setStrength (long strength) {		
		this.strength = strength;		
	}
	
	public long getStrength () {		
		return strength;		
	}
	
	public void setAgility (long agility) {
		this.agility = agility;
	}
	
	public long getAgility () {		
		return agility;		
	}
	
	public void setSpeed (long speed) {
		this.speed = speed;
	}
	
	public long getSpeed () {		
		return speed;		
	}
	
	public void setIntelligence (long intelligence) {
		this.intelligence = intelligence;
	}
	
	public long getIntelligence () {		
		return intelligence;		
	}
	
	public void setConstitution (long constitution ) {
		this.constitution = constitution;
	}
	
	public long getConstitution () {		
		return constitution;		
	}
	
	public void setWillpower (long willpower) {
		this.willpower = willpower;		
	}
	
	public long getWillpower () {		
		return willpower;		
	}
	
	public void setBloodlust (long bloodlust) {
		this.bloodlust = bloodlust;
	}
	
	public long getBloodlust () {		
		return bloodlust;		
	}
	
	public void setAggression (long aggression) {
		this.aggression = aggression;
	}
	
	public long getAggression () {		
		return aggression;		
	}
	
	public void setHeat (long chattiness) {
		this.heat = chattiness;
	}
	
	public long getHeat () {		
		return heat;		
	}	
	
	public void setConsistency (long consistency) {
		this.consistency = consistency;
	}
	
	public long getConsistency () {		
		return consistency;		
	}		
	
	public void setWins (long wins) {
		this.wins = wins;
	}
	
	public long getWins () {		
		return wins;		
	}
	
	public void setLosses (long losses) {
		this.losses = losses;
	}
	
	public long getLosses () {		
		return losses;		
	}	
	
	public void setTies(long ties) {
		this.ties = ties;		
	}
	
	public long getTies() {
		return ties;		
	}

	public void setMatches(long property) {
		matches = property;		
	}
	
	public long getMatches(){
		return matches;
	}
	
	public void setPopularity (long popularity) {
		this.popularity = popularity;
	}
	
	public long getPopularity () {		
		return popularity;		
	}	
	
	public void setPrice (long price) {
		this.price = price;
	}
	
	public void setPrice(){
		Random r = new Random();
		price = (int)((strength + agility + speed + intelligence + constitution + willpower + r.nextInt(25)) / 10);
	}
	
	public long getPrice () {		
		return price;		
	}	
	
	public void setStatus (String status) {
		this.status = status;
	}
	
	public String getStatus () {		
		return status;		
	}		
	
	public void setOwner (String owner) {
		this.owner = owner;
	}
	
	public String getOwner () {		
		return owner;		
	}			
	
	public void setWeaponsSkill (String name, long value) {
		
		if (name.equals("sword"))
			this.weaponSkills.setSword(value);
		if (name.equals("daggers"))
			this.weaponSkills.setDaggers(value);
		if (name.equals("greatsword"))
			this.weaponSkills.setGreatsword(value);
		if (name.equals("greataxe"))
			this.weaponSkills.setGreataxe(value);
		if (name.equals("spear"))
			this.weaponSkills.setSpear(value);
		if (name.equals("quarterstaff"))
			this.weaponSkills.setQuarterstaff(value);
		if (name.equals("maul"))
			this.weaponSkills.setMaul(value);
	}	
	
	public long getWeaponSkill(String skill){
		long skillVal = 0;
		if (weaponSkills == null){
			return 0;
		} else {
			if (skill.equals("sword"))
				skillVal = this.weaponSkills.getSword();
			if (skill.equals("daggers"))
				skillVal = this.weaponSkills.getDaggers();
			if (skill.equals("greatsword"))
				skillVal = this.weaponSkills.getGreatsword();
			if (skill.equals("greataxe"))
				skillVal = this.weaponSkills.getGreataxe();
			if (skill.equals("spear"))
				skillVal = this.weaponSkills.getSpear();
			if (skill.equals("quarterstaff"))
				skillVal = this.weaponSkills.getQuarterstaff();
			if (skill.equals("maul"))
				skillVal = this.weaponSkills.getMaul();
		}
		return skillVal;
	}

	public void setGender(String gender) {
		this.gender = gender;
		
	}			
	public String getGender() {
		return gender;
		
	}
	
	public String getPossessive(){
		if (gender.equals("M")){
			return "his";
		} else {
			return "her";
		}
	}
	
	public List<GladiatorDataBean> getGladiatorsForTraining(){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		List<GladiatorDataBean> gladiators = new ArrayList<GladiatorDataBean>(); 
		Query q = new Query("Gladiator");
		
        //Filter owned = new FilterPredicate("owner", FilterOperator.NOT_EQUAL, null);//only train those who are owned by a player
        Filter alive = new FilterPredicate("status", FilterOperator.EQUAL, "FIT"); //those injured or dead cannot train
        Filter assignedTraining = new FilterPredicate("currentTrainingFocus", FilterOperator.NOT_EQUAL, "None");
        Filter currentRecruits = CompositeFilterOperator.and( alive, assignedTraining);
        q.setFilter(currentRecruits);
        FetchOptions gladiator_training_check = FetchOptions.Builder.withLimit(BASE_NUMBER_OF_TRAINING_GLADIATORS);
        List<Entity> results = datastore.prepare(q).asList(gladiator_training_check);
        log.info("total returned available gladiators for training: " + results.size());
        Iterator<Entity> it = results.iterator();
        while (it.hasNext()){
        	GladiatorDataBean temp = new GladiatorDataBean(it.next());
        	gladiators.add(temp);
        }
        
        return gladiators;
	}
	
	public void attemptTraining() {
		log.info(name + " owned by " + owner + " attempted to train " + currentTrainingFocus);
		//first check to see if the stat being trained is at the max cap
		//then attempt against the soft cap for all stats. If successful
		//then attempt against the cap for that stat. If successful, increment
		//the skill/attribute
		boolean canTrain = true;
		long successChance = 0;
		Random r = new Random();
		
		String trainingClass = "Weaponskill";
		for (int t = 0; t<ATTRIBUTES.length; t++){
			if (ATTRIBUTES[t].equals(currentTrainingFocus)){
				trainingClass = "Attribute";
				t = ATTRIBUTES.length;
			}
		}
		if (trainingClass.equals("Weaponskill")){
			long currentSkillValue = getWeaponSkill("currentTrainingFocus");
			if (currentSkillValue >= MAXIMUM_SKILL_SCORE){
				canTrain = false;
			}
		}
		if (trainingClass.equals("Attribute")){
			long currentAttValue = getTrainingAttribute();
			if (currentAttValue >= MAXIMUM_ATTRIBUTE_SCORE){
				canTrain = false;
			}			
		}	
		
		successChance = (BASE_NUMBER_FOR_TRAINING_SOFTCAP - (strength + agility + speed + intelligence + constitution + willpower + 
				weaponSkills.getDaggers() + weaponSkills.getGreataxe() + weaponSkills.getGreatsword() +
				weaponSkills.getHandToHand() + weaponSkills.getMaul() + weaponSkills.getQuarterstaff() + 
				weaponSkills.getSpear() + weaponSkills.getSword()));
		if (successChance <=0 || r.nextInt(BASE_NUMBER_FOR_TRAINING_SOFTCAP) > successChance){
			canTrain = false;
		} else {
			canTrain = true;
		}
		
		if (canTrain){
			this.applyTraining();
			log.info(name + " owned by " + owner + " successfully trained " + currentTrainingFocus);
		} else {
			log.info(name + " owned by " + owner + " failed to train " + currentTrainingFocus);
		}
	}
	
	private void applyTraining(){
		if (currentTrainingFocus.equalsIgnoreCase("Strength")){
			strength += TRAINING_INCREMENT_AMOUNT;
		}
		if (currentTrainingFocus.equalsIgnoreCase("Agility")){
			agility += TRAINING_INCREMENT_AMOUNT;
		}
		if (currentTrainingFocus.equalsIgnoreCase("Intelligence")){
			intelligence += TRAINING_INCREMENT_AMOUNT;
		}
		if (currentTrainingFocus.equalsIgnoreCase("Constitution")){
			constitution += TRAINING_INCREMENT_AMOUNT;
		}
		if (currentTrainingFocus.equalsIgnoreCase("Speed")){
			speed += TRAINING_INCREMENT_AMOUNT;
		}
		if (currentTrainingFocus.equalsIgnoreCase("Willpower")){
			willpower+= TRAINING_INCREMENT_AMOUNT;
		}
		
		if (currentTrainingFocus.equalsIgnoreCase("Sword")){
			weaponSkills.trainSword();
		}
		if (currentTrainingFocus.equalsIgnoreCase("Daggers")){
			weaponSkills.trainDaggers();
		}
		if (currentTrainingFocus.equalsIgnoreCase("Greataxe")){
			weaponSkills.trainGreataxe();
		}
		if (currentTrainingFocus.equalsIgnoreCase("Greatsword")){
			weaponSkills.trainGreatsword();
		}
		if (currentTrainingFocus.equalsIgnoreCase("Spear")){
			weaponSkills.trainSpear();
		}
		if (currentTrainingFocus.equalsIgnoreCase("Quarterstaff")){
			weaponSkills.trainQuarterstaff();
		}
		if (currentTrainingFocus.equalsIgnoreCase("Maul")){
			weaponSkills.trainMaul();
		}
		if (currentTrainingFocus.equalsIgnoreCase("HandToHand")){
			weaponSkills.trainHandToHand();
		}
		saveGladiator(getKey());
	}

	private long getTrainingAttribute() {
		long attValue = 0;
		if (currentTrainingFocus.equalsIgnoreCase("Strength")){
			attValue = strength;
		}
		if (currentTrainingFocus.equalsIgnoreCase("Agility")){
			attValue = agility;
		}
		if (currentTrainingFocus.equalsIgnoreCase("Intelligence")){
			attValue = intelligence;
		}
		if (currentTrainingFocus.equalsIgnoreCase("Constitution")){
			attValue = constitution;
		}
		if (currentTrainingFocus.equalsIgnoreCase("Speed")){
			attValue = speed;
		}
		if (currentTrainingFocus.equalsIgnoreCase("Willpower")){
			attValue = willpower;
		}
		return attValue;
	}

	public String getCurrentTrainingFocus() {
		return currentTrainingFocus;
	}

	public void setCurrentTrainingFocus(String currentTrainingFocus) {
		this.currentTrainingFocus = currentTrainingFocus;
	}

	public Date getLastTrainingChangeDate() {
		return lastTrainingChangeDate;
	}

	public void setLastTrainingChangeDate(Date lastTrainingChangeDate) {
		this.lastTrainingChangeDate = lastTrainingChangeDate;
	}

	public String getTrainingHistory() {
		return trainingHistory;
	}

	public void setTrainingHistory(String trainingHistory) {
		this.trainingHistory = trainingHistory;
	}

	public void setName(String name) {
		if (name == "null"){
			this.name = "No name selected";
		}
		this.name = name;
	}	
	
	public String getName(){
		if (name == null){
			return "No name selected";
		}
		return name;
	}

	public void setNewOwner(String owner, String keyIn) {//When a gladiator is purchased, pass the owner name and the
		this.owner = owner;								// key a a string to this function.
		Key gladiatorKey = KeyFactory.stringToKey(keyIn); //which looks the gladiator up in the ds and updates it
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity newGladiator;
		try {
			newGladiator = datastore.get(gladiatorKey);
		} catch (EntityNotFoundException e) {
			// TODO Auto-generated catch block
			log.info("GladiatorDataBean.java: Didn't find the gladiator when using key search");
			e.printStackTrace();
			return;
		}		
		Transaction txn = datastore.beginTransaction();
		try {			
			newGladiator.setProperty("owner", owner);		
			//TODO This needs to be made more secure so that someone creating an account with teh same name as one deleted doesn't 
			//wind up with the previous account's gladiators
			datastore.put(newGladiator);
			txn.commit();
		} finally {	
		    if (txn.isActive()) {
		        txn.rollback();
		        log.info("GladiatorDataBean: Transaction failed");
		    } 
		}	
	}
	public GladiatorDataBean getDummyGladiator (){
		GladiatorDataBean g =  new GladiatorDataBean();
		g.createGladiator();
		return g;
	}

	public void addWin() {
		// add 1 to the wins and total matches
		wins += 1;
		matches += 1;
	}

	public void addTie() {
		matches+= 1;
		ties +=1;		
	}	
}
