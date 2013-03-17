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
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

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
	
	public LudusDataBean ludus; 
	
	private static final Logger log = Logger.getLogger(BaseServlet.class.getName());

	
	public UserDataBean(){
		
	}
	
	public boolean populateUserDataBean(String UserName){
		Entity userEnt = findUserEntityByName(UserName);
		if (userEnt == null){
			return false;
		} else {
			//set up the user variables relevant to web page rendering
			setUserName(userEnt.getProperty("username").toString());
			setUserLevel(userEnt.getProperty("userLevel").toString());			
			
			//attach the ludus and gladiator variables relevant to web page rendering

			LudusDataBean ludusBean = new LudusDataBean(findUserLudus(UserName));
			
			List<Entity> glads = getUsersGladiators(UserName);
			log.info("total gladiators in stable: " + glads.size());
			List<GladiatorDataBean> gladlist = new ArrayList<GladiatorDataBean>();			
			for  (Entity gladtr : glads){
				
				GladiatorDataBean gladiator = new GladiatorDataBean(gladtr);
				gladiator.getGladiatorsChallenges(); //this call needs to be separate to avoid a nasty loop

				gladlist.add(gladiator);			
			}		
			ludusBean.setGladiators(gladlist);
			setLudus(ludusBean);
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
		Key accountKey = KeyFactory.createKey(accountKindName, accountGroup);	
		Entity usr = new Entity(accountEntity, accountKey);		
		Entity newLudus = new Entity("ludus", usr.getKey());
		Transaction txn = datastore.beginTransaction();
		try {		
			setPasswordHash(passHash);
			
			usr.setProperty("username", name);
			usr.setProperty("passwordHash", passwordHash);
			usr.setProperty("email", emailAdd);
			usr.setProperty("status", stat);
			usr.setProperty("userLevel", userlvl);
			usr.setProperty("created", createdDate);
		    datastore.put(usr);		    
		    
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
	
	public Boolean saveNewUser(){
		//saves the user assuming that all properties have been set
		createdDate = new Date();
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key accountKey = KeyFactory.createKey(accountKindName, accountGroup);	
		Entity usr = new Entity(accountEntity, accountKey);		
		Entity newLudus = new Entity("ludus", usr.getKey());
		if (passwordHash == null || passwordHash == "" ||
				userName == null || userName == "" ||
				emailAddress == null    || emailAddress ==  ""
				){
			log.info("Properties missing. User not saved");
		
			return false;
		}else{			
			Transaction txn = datastore.beginTransaction();
			try {											
				usr.setProperty("username", userName);
				usr.setProperty("passwordHash", passwordHash);
				usr.setProperty("email", emailAddress);
				usr.setProperty("status", status);
				usr.setProperty("userLevel", userLevel);
				usr.setProperty("created", createdDate);
			    datastore.put(usr);
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
			return true;
		}
	}
	
	public Boolean attemptLogin(String userName, String pwd) throws NoSuchAlgorithmException, InvalidKeySpecException {
		//wrapper for the find user and check password functions
		Entity userExists = null;
		Boolean passwordCorrect = false;
		
		userExists = findUserEntityByName(userName);
		if (userExists != null){
			passwordCorrect = checkPassword(pwd, userExists.getProperty("passwordHash").toString());
			if (passwordCorrect){
				return true;
			} else {
				return false;
			}
		} else {
		return false;
		}
	}
	

	public void setPasswordHash(String pwd) throws NoSuchAlgorithmException, InvalidKeySpecException{
		passwordHash = PasswordHash.createHash(pwd);
	}
	
	public String setLastSeen(){
		return lastSeen.toString();
	}
	
	public void setEmailAddress(String emailIn){
		emailAddress = emailIn;
	}
		
	public Boolean setNewEmailAddress(String emailIn){	
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key accountKey = KeyFactory.createKey("account", accountGroup);	
		
		Query q = new Query("account", accountKey);
        q.isKeysOnly();
        q.setFilter(new FilterPredicate ("email", FilterOperator.EQUAL, emailIn));
    	FetchOptions username_search =
    		    FetchOptions.Builder.withLimit(5);
        int results = datastore.prepare(q).countEntities(username_search);
        if (results > 0){
        	return false;
        }
        //if the query returns 0 results, set the user name variable
        emailAddress = emailIn;
		return true;

	}
	public Entity findUserEntityByName(String name){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key accountKey = KeyFactory.createKey("account", accountGroup);	
		Query q = new Query("account", accountKey);

        q.setFilter(new FilterPredicate ("username", FilterOperator.EQUAL, name.toLowerCase()));
        Entity results = datastore.prepare(q).asSingleEntity();
        
        return results;
	}	
	
	private Boolean checkPassword(String pwd, String passHash) throws NoSuchAlgorithmException, InvalidKeySpecException {
		return PasswordHash.validatePassword(pwd, passHash);		
	}
	
	public Entity findUserLudus (String usrName){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Entity usr = findUserEntityByName(usrName);
		Key usrKey = usr.getKey();
		
		Query ludusQuery = new Query("ludus").setAncestor(usrKey);
		Entity ludus = datastore.prepare(ludusQuery).asSingleEntity();
		return ludus;
	}
	
	public Boolean setNewUserName(String usrname){
		//checks to see if the name is available
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Key accountKey = KeyFactory.createKey("account", accountGroup);	
	
		Query q = new Query("account", accountKey);
        q.isKeysOnly();
        q.setFilter(new FilterPredicate ("username", FilterOperator.EQUAL, usrname));
    	FetchOptions username_search =
    		    FetchOptions.Builder.withLimit(5);
        int results = datastore.prepare(q).countEntities(username_search);
        if (results > 0){
        	return false;
        }
        //if the query returns 0 results, set the user name variable
        userName = usrname;
		return true;
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
	}
	
	public void setUserName(String usrname){
		this.userName = usrname;
	}	
	
	public void setLudus(LudusDataBean ludus){
		this.ludus = ludus;
	}
	
	public List<Entity> getUsersGladiators(String usrname){
		List<Entity> gladiators = null;		
		Key gladKey = KeyFactory.createKey(gladiatorKindName, gladiatorGroup);	
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query q = new Query("Gladiator", gladKey);
        Filter u = new FilterPredicate("owner", FilterOperator.EQUAL, usrname);
        q.setFilter(u);
    	FetchOptions user_gladiator_search =
    		    FetchOptions.Builder.withLimit(5);
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
		
		this.userLevel =userLevel; 
	}
	
	public String getUserLevel(){
		return userLevel;
	}
}
