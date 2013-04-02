/**
 * 
 */
package com.bloodandsand.beans;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import com.bloodandsand.utilities.CoreBean;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EmbeddedEntity;
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
import com.google.appengine.api.datastore.TransactionOptions;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

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
	
	private boolean logEnabled = false;
	private static final long serialVersionUID = -1717471076334573064L;
	private long strength;
	private long agility;
	private long speed;
	private long intelligence;
	private long constitution;
	private long willpower;
	
	private long bloodlust; 
	private long aggression; 
	
	private String personality;

	
	public String name;
	private String gender;
	
	private List<GladiatorChallengeBean> challenges;
	public long wins;
	public long losses;
	public long ties; 
	public long matches; //total matches 
	private long rating = 0;
	
	private String currentTrainingFocus;
	private Date lastTrainingChangeDate;
	private String trainingHistory;
	
	public long price;	
	private String status;	
		
	public String owner; 
	private Key ownerKey;
	
	private Entity thisEntity = new Entity(gladiatorEntity);
	private GladiatorWeaponSkillsBean weaponSkills = new GladiatorWeaponSkillsBean();
	
	public Key key;
	
	protected static final Logger log = Logger.getLogger(GladiatorDataBean.class.getName());
	
	
	public GladiatorDataBean(){
		
	}
	
	public GladiatorDataBean(Entity g) {
		
		thisEntity = g;
		setUpBean();			
	}
	
	private void setUpBean(){
		this.key = (thisEntity.getKey());
		this.strength = ((Long)(thisEntity.getProperty("strength")));
		this.agility = ((Long)(thisEntity.getProperty("agility")));
		this.speed = ((Long)(thisEntity.getProperty("speed")));
		this.intelligence = ((Long)(thisEntity.getProperty("intelligence")));
		this.constitution = ((Long)(thisEntity.getProperty("constitution")));
		this.bloodlust = ((Long)(thisEntity.getProperty("bloodlust")));
		this.aggression = ((Long)(thisEntity.getProperty("aggression")));

		this.willpower = ((Long)(thisEntity.getProperty("willpower")));
		this.wins = ((Long)(thisEntity.getProperty("wins")));
		this.losses = ((Long)(thisEntity.getProperty("losses")));
		this.ties = ((Long)(thisEntity.getProperty("ties")));
		this.matches = ((Long)thisEntity.getProperty("matches"));

		this.price = ((Long)(thisEntity.getProperty("price")));
		
		this.status = ((String)(thisEntity.getProperty("status")));		
		
		this.currentTrainingFocus = ((String)(thisEntity.getProperty("currentTrainingFocus")));
		this.lastTrainingChangeDate = ((Date)(thisEntity.getProperty("lastTrainingChangeDate")));
		this.trainingHistory = ((String)(thisEntity.getProperty("trainingHistory")));
		this.owner = ((String)thisEntity.getProperty("owner"));
		
		this.gender = ((String)thisEntity.getProperty("gender"));
		this.name = ((String)thisEntity.getProperty("name"));		
		this.weaponSkills = new GladiatorWeaponSkillsBean ((EmbeddedEntity) thisEntity.getProperty("weaponSkills"));
		
		if (thisEntity.hasProperty("ownerKey") && thisEntity.getProperty("ownerKey") != null){		
			this.ownerKey = (Key)thisEntity.getProperty("ownerKey"); 
		} else {
			if (this.owner != null){
				thisEntity.setProperty("ownerKey", findOwnerKey((String)thisEntity.getProperty("owner")));	
				this.ownerKey = (Key)thisEntity.getProperty("ownerKey");
				saveGladiator();
			}
		}
		if (thisEntity.hasProperty("rating") && thisEntity.getProperty("rating") != null){		
			this.rating = (Long) thisEntity.getProperty("rating"); 
		} else {
			thisEntity.setProperty("rating", 0);	
			saveGladiator();
		}
		
		if (thisEntity.hasProperty("personality") && thisEntity.getProperty("personality") != null){		
			this.personality = (String) thisEntity.getProperty("personality"); 
		} else {
			thisEntity.setProperty("personality", getPersonality());	
			saveGladiator();
		}
	}			
	
	public Key getOwnerKey(){
		if (ownerKey == null ){
			ownerKey = findOwnerKey(owner);
		}
		
		return ownerKey;
	}
	
	private Key findOwnerKey(String property) {
		//temporary solution to avoid having to reset the gladiators in the test env
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query(accountEntity);

        q.setFilter(new FilterPredicate ("userName", FilterOperator.EQUAL, property));
        Entity owner = datastore.prepare(q).asSingleEntity(); 
        if (owner != null){
        	
        	return owner.getKey();
        } else {
        	
        	log.warning("Owner key not found");
        	return null;
        }
	}

	public void createGladiator(){
		thisEntity = new Entity(gladiatorEntity);
		Boolean sanityCheck = false;
		Random r = new Random();
		while (!sanityCheck){//included in a while loop to avoid gladiators with too extreme stats	
			setStrength(r.nextInt(19) + 10);
			setAgility(r.nextInt(19) + 10);
			setSpeed(r.nextInt(19) + 10);
			setIntelligence(r.nextInt(19) + 10);
			setConstitution(r.nextInt(19) + 10);
			setWillpower (r.nextInt(19) + 10);
			long i = strength + agility + speed + intelligence + constitution + willpower;
			if (i >= 110 && i <= 150){
				sanityCheck = true;
			}
		}		
		setBloodlust(r.nextInt(15) ); 
		setAggression(r.nextInt(15)); 
		
		setPersonality();
	

		weaponSkills = new GladiatorWeaponSkillsBean();//creates a new bean with all weapon skills at 0
		
		setCurrentTrainingFocus("None"); //training record information
		setTrainingHistory("");
		setLastTrainingChangeDate(new Date());
		setWins(0);
		setLosses(0);
		setTies(0);
		setMatches (0);
		setName(null);
		setGender(setGender());
		
		setStatus("FIT");
		setOwner(null, null);
		setRating(0);
		setPrice();
		setUpGladiatorEntity();
	}
	
	public void saveNewGladiator(){	

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		if (weaponSkills == null){
			log.warning("No weaponskill entity found");
		}
		setUpGladiatorEntity();		
		
		Transaction txn = datastore.beginTransaction();
		
		try {	
			datastore.put(thisEntity);				
			txn.commit();
		} finally {	
		    if (txn.isActive()) {
		        txn.rollback();
		        log.warning("Save Gladiator transaction failed: rolled back");
		    }
		}	
	}
	
	
	public void saveNewGladiatorsToCache(List<Entity> newGlads) {//this is for saving a new gladiator	
		
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		
		syncCache.put(gladsRecruitsKey, newGlads);	
		
	}
	
	private void setUpGladiatorEntity(){
		//one stop shop for saving gladiator entities, ensures consistency
		
		thisEntity.setProperty("strength", strength);
		thisEntity.setProperty("agility", agility);
		thisEntity.setProperty("speed", speed);
		thisEntity.setProperty("intelligence", intelligence);
		thisEntity.setProperty("constitution", constitution);
		thisEntity.setProperty("willpower", willpower);			
		
		thisEntity.setProperty("bloodlust", bloodlust); 
		thisEntity.setProperty("aggression", aggression); 
		
		if (this.personality != null){
			thisEntity.setProperty("personality", personality);
		} else {
			getPersonality();
		}
		
		thisEntity.setProperty("wins", wins);
		thisEntity.setProperty("losses", losses);
		thisEntity.setProperty("ties", ties);
		thisEntity.setProperty("matches", matches);
		
		thisEntity.setProperty("price", price);
		
		thisEntity.setProperty("status", status);
			
		thisEntity.setProperty("currentTrainingFocus", currentTrainingFocus) ;//training record information
		thisEntity.setProperty("trainingHistory", trainingHistory);
		thisEntity.setProperty("lastTrainingChangeDate", lastTrainingChangeDate);			
		
		thisEntity.setProperty("owner", owner); 
		thisEntity.setProperty("ownerKey", ownerKey);
		if (name != null){
			thisEntity.setProperty("name", name.toLowerCase());
		} else {
			thisEntity.setProperty("name", name);
		}
		
		thisEntity.setProperty("rating", this.rating);
		
		thisEntity.setProperty("gender", gender);		
		
		thisEntity.setProperty("weaponSkills", weaponSkills.getEntity());
		
	}
	
	public void saveGladiator() {//updating an existing gladiator after making some changes

		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		if (weaponSkills == null){
			log.warning("No weaponskill entity found");
		}
		setUpGladiatorEntity();
		
		syncCache.put(key, thisEntity);
		
		
		Transaction txn = datastore.beginTransaction();
		
		try {	
			datastore.put(thisEntity);				
			txn.commit();
		} finally {	
		    if (txn.isActive()) {
		        txn.rollback();
		        log.warning("Save Gladiator transaction failed: rolled back");
		    }
		}	
	}

	public Entity findGladiatorEntityByKey(Key gladKey){

		Entity result = null;
		result = getGladiatorFromCache(gladKey);
		if (result == null){
			//gladiator not found in cache
			log.warning("Gladiator not foundin cache. Doing it the hard way." );
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Query q = new Query(gladiatorEntity);

	        //q.setFilter(new FilterPredicate (Entity.KEY_RESERVED_PROPERTY, FilterOperator.EQUAL, gladKey));
	        try {
				result = datastore.get(gladKey);
				MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
				syncCache.put(result.getKey(), result);
			} catch (EntityNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}       	        
		}
		
		return result;		
		
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
			Filter challngr = new FilterPredicate("challengerKey", FilterOperator.EQUAL, thisEntity.getKey());
			Filter incumb = new FilterPredicate("incumbantKey", FilterOperator.EQUAL, thisEntity.getKey());
			Filter currentChallenges = CompositeFilterOperator.or(incumb, challngr); //inactive);
			q.setFilter(currentChallenges);
			FetchOptions max_number_challenges =
				    FetchOptions.Builder.withLimit(10);
			results = datastore.prepare(q).asList(max_number_challenges);
			if (logEnabled){log.info("Found " + results.size() + " challenges for " + name);}
			if (results.size() > 0){
				List<GladiatorChallengeBean> challs = new ArrayList<GladiatorChallengeBean>();
				for (Entity ent : results){
					GladiatorChallengeBean nxt = new GladiatorChallengeBean(ent, false);
					if (nxt.getStatusEnum() == Status.ACCEPTED || nxt.getStatusEnum() == Status.INITIATED || nxt.getStatusEnum() == Status.DECLINED && nxt != null){
						//add the gladiators to the challenge without creating a loop					
						if (this.getKey().equals(nxt.getChallengerKey())){
							nxt.setChallenger(this);
							nxt.findIncumbant();
						} else {
							if (this.getKey().equals(nxt.getIncumbantKey())){
								nxt.setIncumbant(this);
								nxt.findChallenger();
							} else {
								log.warning("No match on keys");
							}							
						}
						challs.add(nxt);
						if (logEnabled){log.info("added challenge to " + this.name);}
					}											
				}
				if (challs.size() > 0){
					this.challenges = challs;
				}
				
				if (logEnabled){log.info("Added a total of " + challs.size() + " to " + name);}
			}						
		}		
	}
	
	public boolean isGladiatorAvailableToChallenge(){
		if (!this.status.equals("FIT")){
			if (logEnabled){log.info("Gladiator is not fit");}
			return false;
		}
		
		if (challenges == null) {
			this.getGladiatorsChallenges();
		}

		
		if (this.name == null || this.name.equalsIgnoreCase("none")){
			if (logEnabled){log.info("Gladiator has no name");}
			return false;
		}
		boolean avail = true;
		if (challenges != null){
			Iterator<GladiatorChallengeBean> iter = challenges.iterator();
			while (iter.hasNext()){
				if (iter.next().getStatusEnum() == Status.ACCEPTED){
					avail = false;
					if (logEnabled){log.info("Gladiator has accepted a challenge");}
				}
			}
		}				
		return avail;
	}
	
	public List<GladiatorDataBean> getAllChallengeableGladiators(String userName){//used in creating challenges
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		List<GladiatorDataBean> gladiators = new ArrayList<GladiatorDataBean>(); 
		
		Query q = new Query(gladiatorEntity);
		
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
        		if (logEnabled){log.info("Gladiator: " + temp.getName() + " has been added, owned by " + temp.getOwner());}
        		gladiators.add(temp);
        	}        	
        }
        if (gladiators != null){
        	if (logEnabled){log.info("total returned challengeable gladiators: " + gladiators.size());}
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
		setCurrentTrainingFocus(training);
		setLastTrainingChangeDate(new Date());
		
		thisEntity.setProperty("trainingHistory", trainingHistory);		
	}	

	public void setKey(String key) {
		this.key = KeyFactory.stringToKey(key);	
	}
	
	public String getKey(){
		if (this.key != null){
			return KeyFactory.keyToString(thisEntity.getKey());
		} else {
			return null;
		}
	}
	
	public void setDataStoreKey(Key key2) {
		this.key = key2;
	}
	
	public Key getDataStoreKey(){
		return thisEntity.getKey();
	}

	public void setStrength (long strength) {		
		this.strength = strength;		
		thisEntity.setProperty("strength", strength);
	}
	
	public long getStrength () {		
		return strength;		
	}
	
	public void setAgility (long agility) {
		this.agility = agility;
		thisEntity.setProperty("agility", agility);
	}
	
	public long getAgility () {		
		return agility;		
	}
	
	public void setSpeed (long speed) {
		this.speed = speed;
		thisEntity.setProperty("speed", speed);
	}
	
	public long getSpeed () {		
		return speed;		
	}
	
	public void setIntelligence (long intelligence) {
		this.intelligence = intelligence;
		thisEntity.setProperty("intelligence", intelligence);
	}
	
	public long getIntelligence () {		
		return intelligence;		
	}
	
	public void setConstitution (long constitution ) {
		this.constitution = constitution;
		thisEntity.setProperty("constitution", constitution);
	}
	
	public long getConstitution () {		
		return constitution;		
	}
	
	public void setWillpower (long willpower) {
		this.willpower = willpower;		
		thisEntity.setProperty("willpower", willpower);
	}
	
	public long getWillpower () {		
		return willpower;		
	}
	
	public void setBloodlust (long bloodlust) {
		this.bloodlust = bloodlust;
		thisEntity.setProperty("bloodlust", bloodlust);
	}
	
	public long getBloodlust () {		
		return bloodlust;		
	}
	
	public void setAggression (long aggression) {
		this.aggression = aggression;
		thisEntity.setProperty("agression", aggression);
	}
	
	public long getAggression () {		
		return aggression;		
	}
	
	public String getPersonality(){
		if (this.personality == null){
			setPersonality();
		} 
		
		return this.personality;
			
	}
	
	public void setPersonality(){
		Random r = new Random();
		this.personality = PERSONALITIES[r.nextInt(PERSONALITIES.length)];
		
	}
	
	public void setPersonality(String persnlty){
		this.personality = persnlty;
		
	}
	
	
	public void setWins (long wins) {
		this.wins = wins;
		thisEntity.setProperty("wins", wins);
	}
	
	public long getWins () {		
		return wins;		
	}
	
	public void setLosses (long losses) {
		this.losses = losses;
		thisEntity.setProperty("losses", losses);
	}
	
	public long getLosses () {		
		return losses;		
	}	
	
	public void setTies(long ties) {
		this.ties = ties;		
		thisEntity.setProperty("ties", ties);
	}
	
	public long getTies() {
		return ties;		
	}

	public void setMatches(long property) {
		matches = property;	
		thisEntity.setProperty("matches", matches);
	}
	
	public long getMatches(){
		return matches;
	}	

	public void setPrice (long price) {
		this.price = price;
		thisEntity.setProperty("price", price);
	}
	
	public void setPrice(){
		Random r = new Random();
		setPrice((int)((strength + agility + speed + intelligence + constitution + willpower + r.nextInt(35)) / 10));		
	}
	
	public long getPrice () {		
		return price;		
	}	
	
	public void setStatus (String status) {
		this.status = status;
		thisEntity.setProperty("status", status);
	}
	
	public String getStatus () {		
		return status;		
	}		
	
	public void setOwner (String owner, Key ownerKey) {
		this.owner = owner;
		thisEntity.setProperty("owner", owner);
		this.ownerKey = ownerKey;
		thisEntity.setProperty("ownerKey", ownerKey);
	}
	
	public String getOwner () {		
		return owner;		
	}			
	
	public GladiatorWeaponSkillsBean getWeaponSkills(){
		return this.weaponSkills;
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
		thisEntity.setProperty("gender", gender);
		
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
	
	private Entity getGladiatorFromCache(){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		Entity results = (Entity)syncCache.get(key);
		if (!syncCache.contains(key) || results == null){
			return null;
		} else {
			return results;
		}
	}
	private Entity getGladiatorFromCache(Key keyIn){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		Entity results = (Entity)syncCache.get(keyIn);
		if (!syncCache.contains(keyIn) || results == null){
			return null;
		} else {
			return results;
		}
	}
	
	private void removePurchasedGladiatorFromMarket(){
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		
		List<Entity> recruits = (List<Entity>) syncCache.get(gladsRecruitsKey);
		if (recruits.remove(thisEntity)){
			if(logEnabled){log.info("Found and deleted gladiator from recruits.");}
		}
		
		syncCache.put(gladsRecruitsKey, recruits);
	}	
	
	public void attemptTraining() {
		if (logEnabled){log.info(name + " owned by " + owner + " attempted to train " + currentTrainingFocus);}
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
		if (trainingClass.equals("Weaponskill")){							//check against the hard cap for the stat or skill
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
		//if training an attribute and the attribute cap is close, the chances shrink to none.
		if (canTrain && trainingClass.equals("Attribute")){														//check against the soft cap for all skills
			successChance = (BASE_NUMBER_FOR_ATTRIBUTE_TRAINING_SOFTCAP - (strength + agility + speed + intelligence + constitution + willpower ));
			if (successChance <=0 || r.nextInt(BASE_NUMBER_FOR_ATTRIBUTE_TRAINING_SOFTCAP) > successChance){
				canTrain = false;
			} else {
				canTrain = true;
			}
		}				
		if (canTrain && trainingClass.equals("Weaponskill")){
			successChance = 3 * (MAXIMUM_SKILL_SCORE - (getWeaponSkill("currentTrainingFocus")));
			if (successChance <=0 || r.nextInt(MAXIMUM_SKILL_SCORE) > successChance){
				canTrain = false;
			} else {
				canTrain = true;
			}
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
		saveGladiator();
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
		thisEntity.setProperty("currentTrainingFocus", currentTrainingFocus);
	}

	public Date getLastTrainingChangeDate() {
		return lastTrainingChangeDate;
	}

	public void setLastTrainingChangeDate(Date lastTrainingChangeDate) {
		this.lastTrainingChangeDate = lastTrainingChangeDate;
		thisEntity.setProperty("lastTrainingChangeDate", lastTrainingChangeDate);
	}

	public String getTrainingHistory() {
		return trainingHistory;
	}

	public void setTrainingHistory(String trainingHistory) {
		this.trainingHistory = trainingHistory;
		thisEntity.setProperty("trainingHistory", trainingHistory);
	}

	public void setName(String name) {
		
		this.name = name;
		thisEntity.setProperty("name", name);
	}	
	
	public String getName(){
		if (name == null){
			return "No name selected";
		}
		return name;
	}
	
	public Entity getEntity(){
		return this.thisEntity;
	}
	
	public String getCapitalizedName(){
		if (name!= null){
			return capitalizeWord(name);
		} else {
			return null;
		}
		
	}
	

	public void setNewOwner(String owner, String userKey) {//When a gladiator is purchased, pass the owner name and the
		
		//first remove it from the available listings
		removePurchasedGladiatorFromMarket();
		this.owner = owner.toLowerCase();	
		this.ownerKey = KeyFactory.stringToKey(userKey);// key a a string to this function.
		
		thisEntity.setProperty("owner", owner);
		thisEntity.setProperty("ownerKey", ownerKey);
		
		saveGladiator();
		

	}
	public GladiatorDataBean getDummyGladiator (){
		GladiatorDataBean g =  new GladiatorDataBean();
		g.createGladiator();
		return g;
	}

	public void addWin(long opponentRating) {
		// add 1 to the wins and total matches

		wins += 1;
		matches += 1;
		thisEntity.setProperty("wins", wins);
		thisEntity.setProperty("matches", matches);
		//adjust this gladiator's rating based on the opponent
		rating += BASE_WIN_RATING_BONUS + 0.5*opponentRating;//currently the winner always receives the same calculation
															//regardless of whether he or she was higher ranked before the fight
		thisEntity.setProperty("rating", rating);
	}
	
	public void addLoss(long opponentRating) {
		// add 1 to the wins and total matches
		losses += 1;
		matches += 1;
		thisEntity.setProperty("losses", losses);
		thisEntity.setProperty("matches", matches);
		//adjust this gladiator's rating based on the opponent
		long adjust = 0;

		if (rating >= opponentRating){//if the opponent was lower ranked, ranking takes a bigger hit
			 adjust = (long) (0.5*(rating - opponentRating));
			if (adjust < 10){
				adjust = 10;
			}
			
		} else {//if the opponent was higher ranked
			adjust = (long) (0.5*(opponentRating - rating));
			if (adjust < 10){
				adjust = 10;
			}
		}
		rating -= adjust;
		if (rating < 0) {rating = 0;}
		thisEntity.setProperty("rating", rating);
	}

	public void addTie(long opponentRating) {
		matches+= 1;
		ties +=1;	
		thisEntity.setProperty("ties", ties);
		thisEntity.setProperty("matches", matches);	
		//adjust the rating
		long adjust = 0;
		if (rating < opponentRating){//in a tie, the lower ranked fighter moves half the distance to the higher ranked fighter
			adjust = (long)(0.5*(opponentRating - rating));
		} else {//in a tie, the higher ranked fighter has no change to rating
			adjust = 0;
		}
		rating += adjust;
		thisEntity.setProperty("rating", rating);
		
	}

	public String getBestWeaponSkill() {
		String skill = "sword";
		if (weaponSkills == null){
			log.info("Gladiator has no weaponskill entity");
			return skill;
		}
		long highest = weaponSkills.getSword();
		if (weaponSkills.getGreataxe() > highest){
			highest = weaponSkills.getGreataxe();
			skill = "greataxe";
		}
		if (weaponSkills.getGreatsword() > highest){
			highest = weaponSkills.getGreatsword();
			skill = "greatsword";
		}
		if (weaponSkills.getQuarterstaff() > highest){
			highest = weaponSkills.getQuarterstaff();
			skill = "quarterstaff";
		}
		if (weaponSkills.getSpear() > highest){
			highest = weaponSkills.getSpear();
			skill = "spear";
		}
		if (weaponSkills.getDaggers() > highest){
			highest = weaponSkills.getDaggers();
			skill = "daggers";
		}
		if (weaponSkills.getMaul() > highest){
			highest = weaponSkills.getMaul();
			skill = "maul";
		}
		return skill;
		
	}	
	
	public long getRating(){
		return this.rating;
	}
	
	public void setRating(long rating){
		this.rating = rating;
		thisEntity.setProperty("rating",this.rating);
	}
	
	public String getStrengthString(){
		 return getAttributeRating(strength);
	}
	public String getAgilityString(){
		 return getAttributeRating(agility);
	}
	public String getIntelligenceString(){
		 return getAttributeRating(intelligence);
	}
	public String getConstitutionString(){
		 return getAttributeRating(constitution);
	}
	public String getWillpowerString(){
		 return getAttributeRating(willpower);
	}
	public String getSpeedString(){
		 return getAttributeRating(speed);
	}
	
}
