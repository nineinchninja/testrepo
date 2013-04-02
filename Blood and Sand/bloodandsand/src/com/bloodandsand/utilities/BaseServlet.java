package com.bloodandsand.utilities;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.*;
import javax.servlet.*;

import com.bloodandsand.beans.GladiatorDataBean;
import com.bloodandsand.beans.UserDataBean;
import com.bloodandsand.beans.TournamentDataBean;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PropertyProjection;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Andrew Hayward
 * December 2012
 * TODO - clean this up. The interfaces are inconsistent
 */
@SuppressWarnings("serial")
public class BaseServlet extends HttpServlet {
	//This is a basic servlet intended to be extended for additional functionality. 
	//It contains the core functions common to handling HTTP requests and responses:
	//		 - writing simple lines and pages
	//		 - checking user authentication
	//		 - conducting memcache and data store actions
	
	
	//
	public Cookie[] cookies;
	
	protected static final boolean TESTTOGGLE = false;//this turns on various testing functions, additional logging etc
	
	protected static int MAX_SESSION_INACTIVE_TIME = 7200;
	
	protected static String loginPage = "/admin/login.jsp";
	protected static String loginRedirect = "/login";
	protected static String loggedInRedirect = "/playerpages/myludus.jsp";
	protected static String gladiatorMarketJsp = "/playerpages/gladiatorMarket.jsp";
	protected static String signUpUrl = "/admin/signup.jsp";
	protected static String gladiatorTrainingJsp = "/playerpages/training.jsp";
	
	protected static String trainingPage = "/gladiatortraining";
	
	protected static String challengeReviewJsp = "/playerpages/challengeReview.jsp";
	protected static String challengeCreateJsp = "/playerpages/challengeCreate.jsp";
	protected static String challengeDetailReviewJsp = "/playerpages/challengeDetailReview.jsp";
	
	protected static String matchResultsJsp = "/playerpages/matchResults.jsp";
	
	protected static String userDataRefresh = "UserRefreshTime";
	
	protected static String userBeanData = "UserData";
	protected static String resultsBeanData = "ResultsBeanData";
	protected static String resultsDetail = "ResultsDetailData";
	
	protected static String rankingsKey = "RANKINGS";
	protected static String resultsKey = "RESULTS";
	protected static String gladsRecruitsKey = "RECRUITSKEYS";
	
	protected static int BASE_NUMBER_OF_TRAINING_GLADIATORS = 1000;
	protected static int BASE_NUMBER_OF_RECRUITS = 20;
	
	protected static long USER_DATA_REFRESH_TIME = 600000;
	
	protected static int MAX_GLADIATORS_ALLOWED = 10;
	
	protected static int MAX_TOURNAMENTS = 5; //limit for query on existing tournaments
	
	Pattern validName = Pattern.compile("[\\W\\s]");

	protected static String challengeEntity = "Challenge";
	protected static String tournamentEntity = "Tournament";
	protected static String gladiatorEntity = "Gladiator";	
	
	
	protected static final Logger log = Logger.getLogger(BaseServlet.class.getName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)  throws IOException, ServletException{


	}
	
	public void write_line(HttpServletRequest req, HttpServletResponse resp, String content_string)	 {
		//Basic write /print line function, used primarily for error message pages
		resp.setContentType("text/plain");
		try {
			resp.getWriter().println(content_string);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public List<TournamentDataBean> getMatchResults(){
		List<TournamentDataBean> out = new ArrayList<TournamentDataBean>();
		List<Entity> tournaments = null;
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Query trn = new Query(tournamentEntity);
		Filter pnding = new FilterPredicate("status", FilterOperator.EQUAL, "Complete");
		trn.setFilter(pnding);
		trn.addSort("eventDate", SortDirection.DESCENDING);
		
		tournaments = datastore.prepare(trn).asList(FetchOptions.Builder.withDefaults().limit(MAX_TOURNAMENTS));
		if (tournaments == null || tournaments.size() == 0){//for those odd situations where there are no tournaments
			return null;
		} else {
			for (Entity tourney : tournaments){
				TournamentDataBean tournament = new TournamentDataBean(tourney, false);
				tournament.getAllResults();
				out.add(tournament);
			}
		}		
		return out;	
	}
	
	public void updateResultsCache(){
		//called after a tournament to update the results in memcache
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();	

		log.warning("Updating tournament results in memcache.");
		syncCache.put(resultsKey, getMatchResults());
   	
	}	
	
	
public List<GladiatorDataBean> getGladiatorsForTraining(){
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		
		int inCache = 0;
		
		List<Entity> results = null;
		List<GladiatorDataBean> gladiatorsForTraining = new ArrayList<GladiatorDataBean>();
		
		Query q = new Query(gladiatorEntity).setKeysOnly();
		q.setFilter(new FilterPredicate ("ownerKey", FilterOperator.NOT_EQUAL, null));
		q.setFilter(new FilterPredicate ("status", FilterOperator.NOT_EQUAL, "DEAD"));
		FetchOptions gladiator_training_check = FetchOptions.Builder.withChunkSize(BASE_NUMBER_OF_TRAINING_GLADIATORS);
		results = datastore.prepare(q).asList(gladiator_training_check);
		
		for (Entity ent : results){
			Entity temp = (Entity) syncCache.get(ent.getKey());
			if (temp == null || !syncCache.contains(ent.getKey())){//not in cache
				try {
					temp = datastore.get(ent.getKey());
				} catch (EntityNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				inCache += 1;
			}
			
			if (temp != null){
				GladiatorDataBean x = new GladiatorDataBean(temp);
				if (!x.getCurrentTrainingFocus().equalsIgnoreCase("none")){
					gladiatorsForTraining.add(x);
				}				
			}
		}//end for
		log.info("Found " + inCache + " gladiators in cache.");
		if (gladiatorsForTraining == null || gladiatorsForTraining.isEmpty()){
			return null;
		} else {
			return gladiatorsForTraining;
		}
	}
	
	public List<Entity> getGladiatorsOnSale(){//used in the gladiator market
		
		//first attempt to get from memcache
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();

		List<Entity> results = null;
		
		results = (List<Entity>) syncCache.get(gladsRecruitsKey);
		
		if (results == null || !syncCache.contains(gladsRecruitsKey)){//not in cache. need to go to the DS
			log.info("Recruits not in cache. getting from DS");
			DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			Query q = new Query(gladiatorEntity);
			
		    Filter unowned = new FilterPredicate("owner", FilterOperator.EQUAL, null);
		    Filter alive = new FilterPredicate("status", FilterOperator.NOT_EQUAL, "DEAD");
		    q.setFilter(CompositeFilterOperator.and(unowned, alive));
		    
		    FetchOptions gladiator_market_check = FetchOptions.Builder.withLimit(BASE_NUMBER_OF_TRAINING_GLADIATORS);
		    results = datastore.prepare(q).asList(gladiator_market_check);
		    if (TESTTOGGLE){log.info("total returned available recruits: " + results.size());}
		}

	    return results;
	}

	protected boolean uniqueGladiatorName(String gladName){//checks to ensure the name is available for the gladiator
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
			
		Query q = new Query(gladiatorEntity);
        q.isKeysOnly();
        q.setFilter(new FilterPredicate ("name", FilterOperator.EQUAL, gladName.toLowerCase()));
    	FetchOptions username_search =
    		    FetchOptions.Builder.withLimit(5);
        int results = datastore.prepare(q).countEntities(username_search);
        if (results > 0){
        	return false;
        } else {
        	return true;
        }
	}
	
	public String getNextTournamentDate(){
		Query tourneys = new Query(tournamentEntity);
		tourneys.addProjection(new PropertyProjection("eventDate", Date.class));
		Filter pnding = new FilterPredicate("status", FilterOperator.EQUAL, "Pending");
		tourneys.setFilter(pnding);
		tourneys.addSort("eventDate", SortDirection.DESCENDING);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		List<Entity> trns = datastore.prepare(tourneys)
		                                  .asList(FetchOptions.Builder.withLimit(MAX_TOURNAMENTS));
		if (trns.size() > 0){
			Date event = (Date)trns.get(0).getProperty("eventDate");
			
			DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM);
			
			return df.format(event);
		} else {
			return "No tournament scheduled";
		}	
	}

	//Session methods - creating and setting variables
	//    default session timeout period is 1 hour

	//Cookie functions
	// get a cookie value from the cookies array passed, set secure cookies using a basic hashing mechanism
	// check a value against the hashing to ensure it is secure
    
	public Boolean checkLogin(HttpServletRequest req){

		if (req.getCookies() == null){
			return false;
		} else {		
			cookies = req.getCookies();
			if (cookies.length == 0 || cookies == null){
				return false;
			} else {
				String userValue = "";
				userValue = getCookieValue(cookies, "user", null);
				if (userValue == null || userValue==""){
					return false;
				} else {
					log.info("BaseServlet: login check:" + userValue);
					if (!checkSecureCookie(userValue)){
						return false;
					} else {
						return true;
					}
				}
			}
		}
	}
	
	public void refreshUserBean(HttpServletRequest req) {//if it has been longer than the prescribed time limit, attempt to refresh the data on the userbean
		if ((System.currentTimeMillis() - (Long)req.getSession().getAttribute(userDataRefresh)) > USER_DATA_REFRESH_TIME ){
			UserDataBean refreshed = (UserDataBean) req.getSession().getAttribute(userBeanData);
			refreshed.populateUserDataBean(refreshed.getUserName());
			req.getSession().setAttribute(userBeanData, refreshed);
			req.getSession().setAttribute(userDataRefresh, System.currentTimeMillis());
			log.info("User Data Refreshed");
		} else {
			log.info("User Data refresh not required.");
		}
	}
	
	public String getCookieValue(Cookie[] cookies, String cookieName, String defaultValue) {
		for(int i=0; i<cookies.length; i++) {
			Cookie cookie = cookies[i];
			if (cookieName.equals(cookie.getName()))
			return(cookie.getValue());
			}
		return defaultValue;
	}

	public void setSecureCookie (HttpServletResponse resp, String cookieName, String value) throws NoSuchAlgorithmException, InvalidKeySpecException, UnsupportedEncodingException{
		//INCOMPLETE: sets a secure cookie using a hash of the string provided
		String secureValue = "";
		secureValue = value + "|" + getSecureValue(value);
		
		Cookie cook = new Cookie(cookieName, secureValue);
		resp.addCookie(cook);
	}
	
	public Boolean checkSecureCookie(String cookieValue){
		String secureValue = "";
		String baseValue = "";
		if (cookieValue.indexOf("|") < 0){
			return false;
		} else {
			secureValue = cookieValue.substring(cookieValue.indexOf("|") + 1);
			baseValue = cookieValue.substring(0, cookieValue.indexOf("|"));
			if (secureValue.equals(getSecureValue(baseValue))){
				return true;
			} else {
				return false;
			}
		}
	}
	
	public String readSecureCookie(String cookieName){
		//this takes a value and checks it. It doesn't grab cookies.
		return cookieName;
	}
	
	public Boolean deleteCookie (HttpServletResponse resp, HttpServletRequest req, String cookieName){
		//INCOMPLETE: deletes a cookie
		Boolean found = false;
		cookies = req.getCookies();
		for(int i=0; i<cookies.length; i++) {
			Cookie cookie = cookies[i];
			if (cookieName.equals(cookie.getName())){
			cookie.setMaxAge(0);
			resp.addCookie(cookies[i]);
			found = true;
			}
		}
		return found;		
	}
	//used in creating secure cookies
	private String getSecureValue(String value) {

		//this isn't a perfect implementation, but it should be sufficient for a cookie
		StringBuffer sb = new StringBuffer();
		 try {
		       java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
		       byte[] array = md.digest(value.getBytes("UTF-8"));
		       
		       for (int i = 0; i < array.length; ++i) {
		         sb.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1,3));
		       }
		       
		    } catch (java.security.NoSuchAlgorithmException e) {
		    	e.printStackTrace();
		    } catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		 return sb.toString();
	}
	
	public String capitalizeWord(String s){
		
		String capital   = Character.toString(s.charAt(0)).toUpperCase();
		return capital + s.substring(1);
	}
	
	public boolean checkValidCharacters(String entry){
		Matcher m = validName.matcher(entry);
		if (m.find()){			
			log.info("invalid user name");
			return false;
		} else {
			return true;
		}		
	}	
	
	public long getLongFromString(String s){
		//attempt to get a long value from a string. Return null if failed
		
		try {
	         long l = Long.parseLong(s);
	         log.info("long l = " + l);
	         return l;
	      } catch (NumberFormatException nfe) {
	         log.info("NumberFormatException: " + nfe.getMessage());
	         return 0;
	      }
	}
}
