package com.bloodandsand.beans;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.bloodandsand.utilities.BaseServlet;
import com.bloodandsand.utilities.CoreBean;
import com.bloodandsand.utilities.PasswordHash;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class UserDataBean extends CoreBean implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8037775347860095706L;
	private String userName;
	private String cookieVal;
	private Date createdDate;
	private String passwordHash;
	private String emailAddress;
	private Date lastSeen;
	private String status;
	private String userLevel; 
	
	private Key key;
	
	private Entity thisEntity = new Entity(accountEntity);
	
	public LudusDataBean ludus; 
	
	private static final Logger log = Logger.getLogger(BaseServlet.class.getName());

	
	public UserDataBean(){
		
		setUpEntity();
		
	}
	
	public UserDataBean (Entity e){
		thisEntity = e;
		//ugly hack but too tired to do more
		setUpBean();
	}
	
	private void setUpBean(){
		//set up the user variables relevant to web page rendering
		userName = (String)thisEntity.getProperty("userName");
		userLevel = (String) thisEntity.getProperty("userLevel");	
		emailAddress = (String) thisEntity.getProperty("email");
		//lastSeen = (Date)thisEntity.getProperty("lastSeen");
		createdDate = (Date)thisEntity.getProperty("createdDate");
		key = thisEntity.getKey();
		passwordHash = (String) thisEntity.getProperty("passwordHash");
		key = thisEntity.getKey();
		//attach the ludus and gladiator variables relevant to web page rendering

		LudusDataBean ludusBean = new LudusDataBean(findUserLudus(thisEntity));
		
		List<Entity> glads = getUsersGladiators(userName);
		log.info("total gladiators in stable: " + glads.size());
		List<GladiatorDataBean> gladlist = new ArrayList<GladiatorDataBean>();			
		for  (Entity gladtr : glads){
			
			GladiatorDataBean gladiator = new GladiatorDataBean(gladtr);
			gladiator.getGladiatorsChallenges(); //this call needs to be separate to avoid a nasty loop

			gladlist.add(gladiator);			
		}		
		ludusBean.setGladiators(gladlist);
		setLudus(ludusBean);		
	}
	
	private void setUpEntity(){
				
		thisEntity.setProperty("userName", userName);
		thisEntity.setProperty("passwordHash", passwordHash);
		thisEntity.setProperty("email", emailAddress);
		thisEntity.setProperty("status", status);
		thisEntity.setProperty("userLevel", userLevel);
		thisEntity.setProperty("created", createdDate);	
		thisEntity.setProperty("lastSeen", lastSeen);		
	}	
		
	public boolean populateUserDataBean(String UserName){//used when logging in. It's ugly but works for now
		thisEntity = findUserEntityByName(UserName);
		if (thisEntity == null){
			return false;
		} else {			
			setUpBean();
			setLastSeen(new Date());
			return true;
		}
	}
		
	public void setCookieVal (String cookieVal){
		this.cookieVal = cookieVal;
	}
	
	public String getCookieVal(){
		return cookieVal;
	}
	
	public Boolean saveNewUser(String name, String passHash, String emailAdd, String stat, String userlvl) throws NoSuchAlgorithmException, InvalidKeySpecException {
		createdDate = new Date();
		//saves a new user accepting all required properties in the method call
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
				
		Entity newLudus = new Entity(ludusEntity, thisEntity.getKey());
		Transaction txn = datastore.beginTransaction();
		try {		
			setPasswordHash(passHash);
			setUserName(name);
			setEmailAddress(emailAdd);
			setStatus(stat);
			setUserLevel(userlvl);
			setCreatedDate(createdDate);			
			
		    datastore.put(thisEntity);		    
		    
		    newLudus.setProperty("availableGold", 100);
		    newLudus.setProperty("wageredGold", 0);
		    newLudus.setProperty("weeklyCosts", 0);
		    datastore.put(newLudus);
		    txn.commit();
		} finally {
		    if (txn.isActive()) {
		        txn.rollback();
		        log.warning("Save New User transaction failed: rolled back");
		        return false;		        
		    }
		}
		return true;
	}
	
	private void setCreatedDate(Date createdDate2) {
		this.createdDate = createdDate2;
		thisEntity.setProperty("created", createdDate);		
	}

	public Boolean saveNewUser(){
		//saves the user assuming that all properties have been set
		createdDate = new Date();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();	
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		
		//if (thisEntity == null){
		setUpEntity();
		//}
		
		Entity newLudus = new Entity(ludusEntity, thisEntity.getKey());
		if (passwordHash == null || passwordHash == "" ||
				userName == null || userName == "" ||
				emailAddress == null    || emailAddress ==  ""){
			
			log.info("Properties missing. User not saved");		
			return false;
		}else{
			
			Transaction txn = datastore.beginTransaction();
			try {											

			    datastore.put(thisEntity);
				//create a new ludus for the new user	
			    newLudus.setProperty("availableGold", 100);
			    newLudus.setProperty("wageredGold", 0);
			    newLudus.setProperty("weeklyCosts", 0);
			    datastore.put(newLudus);	
			    txn.commit();
			} finally {
			    if (txn.isActive()) {
			        txn.rollback();
			        log.warning("Save New User transaction failed: rolled back");
			    }
			}	
			key = thisEntity.getKey();
			//put the name and key in memcache
			syncCache.put(userKey + userName, thisEntity.getKey());
			return true;
		}
	}
	
	public void saveUser(){
		//saves the user assuming the entity exists
		//note only saves the user details, not the ludus
		if (thisEntity != null){
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Transaction txn = datastore.beginTransaction();
			try {
				datastore.put(thisEntity);
			} finally {
				if (txn.isActive()) {
			        txn.rollback();
			        log.warning("Save New User transaction failed: rolled back");
				}
			}
		}
	}
	
	public Boolean attemptLogin(String userName, String pwd) throws NoSuchAlgorithmException, InvalidKeySpecException, EntityNotFoundException {
		//wrapper for the find user and check password functions
		//Entity userExists = null;
		Boolean passwordCorrect = false;
		
		thisEntity = findUserEntityByName(userName);
		if (thisEntity != null){
			log.info("found user");
			passwordCorrect = checkPassword(pwd, thisEntity.getProperty("passwordHash").toString());
			if (passwordCorrect){
				return true;
			} else {
				log.info("password wrong");
				return false;
			}
		} else {
		log.info("did not find user entity");
		return false;
		}
	}
	
	public String getDataStoreKey(){
		return KeyFactory.keyToString(thisEntity.getKey());
	}
	
	public void setDataStoreKey(Key keyIn){
		this.key = keyIn;
	}	
	
	public void setPasswordHash(String pwd) {
		try {
			passwordHash = PasswordHash.createHash(pwd);
			thisEntity.setProperty("passwordHash", pwd);
		} catch (NoSuchAlgorithmException e) {
			log.warning("New user password not saved: No such algorithm Exception.");
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			log.warning("New user password not saved: Invalid Key Spec Exception.");
			e.printStackTrace();
		}				
	}
	
	public void setLastSeen(Date lstSeen){
		this.lastSeen = lstSeen;
		thisEntity.setProperty("lastSeen", lstSeen);
	}
	
	public void setEmailAddress(String emailIn){
		emailAddress = emailIn;
		thisEntity.setProperty("email", emailIn);		
	}
		
	public Boolean setNewEmailAddress(String emailIn){	
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		Query q = new Query(accountEntity);
        q.isKeysOnly();
        q.setFilter(new FilterPredicate ("email", FilterOperator.EQUAL, emailIn));
    	FetchOptions username_search =
    		    FetchOptions.Builder.withLimit(5);
    	
        int results = datastore.prepare(q).countEntities(username_search);
        if (results > 0){
        	return false;
        } else {
        //if the query returns 0 results, set the user name variable
	        setEmailAddress(emailIn);	        			
			return true;
        }		

	}
	public Entity findUserEntityByName(String name){//a general function for finding users. Probably should be moved to baseServlet
		//first check memcache to get key
		
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query(accountEntity);
		Entity results = null;
		if (key == null){//if this is a simple refresh, the key should be available
			
			key = (Key) syncCache.get(userKey + name);//if not, try memcache first
			if (key == null || !syncCache.contains(userKey + name)){//didn't find in memcache, so doing the more expensive way
				
				log.warning("User not in cache. Using database query. ");			

		        q.setFilter(new FilterPredicate ("userName", FilterOperator.EQUAL, name.toLowerCase()));
		        results = datastore.prepare(q).asSingleEntity();
			        if (results!= null){
			        	syncCache.put(userKey + name, results.getKey());
			        	log.info("Added user to cache.");
			        } else {
			        	log.info("Can't find user. ");
			        }
				} else {
					log.info("HEREEERERE" + key.toString());
					//found the user in the cache, using key for more efficient query
					try {
						results = datastore.get(key);
						log.info("Found user in cache. ");
					} catch (EntityNotFoundException e) {
						log.warning("Found user key in cache, didn't find user in db.");
						e.printStackTrace();
					}
				}
		}
        return results;
	}	
	
	private Boolean checkPassword(String pwd, String passHash) throws NoSuchAlgorithmException, InvalidKeySpecException {
		return PasswordHash.validatePassword(pwd, passHash);		
	}
	
	public Entity findUserLudus (Entity thisEntity2){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	
		Key usrKey = thisEntity2.getKey();
		
		Query ludusQuery = new Query(ludusEntity).setAncestor(usrKey);
		Entity ludus = datastore.prepare(ludusQuery).asSingleEntity();
		return ludus;
	}
	
	public Boolean setNewUserName(String usrname){
		//checks to see if the name is available
		//first check memcache
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		
		if (syncCache.contains(userKey + usrname)){
			return false;
		} else {//now try the database			
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
			Query q = new Query(accountEntity);
	        q.isKeysOnly();
	        q.setFilter(new FilterPredicate ("userName", FilterOperator.EQUAL, usrname));
	    	FetchOptions username_search =
	    		    FetchOptions.Builder.withLimit(5);
	    	
	        int results = datastore.prepare(q).countEntities(username_search);
	        if (results > 0){
	        	return false;
	        }
	        //if the query returns 0 results, set the user name variable
	        setUserName(usrname);        
			return true;
		}
	}
	
	public String getCreatedDate(){
		return createdDate.toString();
	}
	
	public String getLastSeen(){
		return lastSeen.toString();
	}
		
	public String getEmail(){
		return emailAddress.toString();
	}
	public String getStatus(){
		return status;
	}
	
	public void setStatus(String stts){
		status = stts;
		thisEntity.setProperty("status", stts);		
	}
	
	public void setUserName(String usrname){
		this.userName = usrname;
		thisEntity.setProperty("userName", usrname);		
		
	}	
	
	public void setLudus(LudusDataBean ludus){
		this.ludus = ludus;
	}
	
	public List<Entity> getUsersGladiators(String usrname){
		List<Entity> gladiators = null;		
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query(gladiatorEntity);
        Filter u = new FilterPredicate("ownerKey", FilterOperator.EQUAL, key);
        q.setFilter(u);
    	FetchOptions user_gladiator_search =
    		    FetchOptions.Builder.withLimit(BASE_NUMBER_OF_CHALLENGEABLE_GLADIATORS);
        gladiators = datastore.prepare(q).asList(user_gladiator_search);
		
		return gladiators;
	}
	
	public LudusDataBean getLudus(){
		return ludus;
	}

	public String getUserName(){
		return userName;
	}
	
	public void setUserLevel(String userLevel){
		
		this.userLevel = userLevel; 
		thisEntity.setProperty("userLevel", userLevel);
	}
	
	public String getUserLevel(){
		return userLevel;
	}
}
