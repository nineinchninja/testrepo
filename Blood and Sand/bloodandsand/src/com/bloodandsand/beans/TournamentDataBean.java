package com.bloodandsand.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import com.bloodandsand.utilities.CoreBean;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public class TournamentDataBean extends CoreBean implements java.io.Serializable {
	
	protected static final Logger log = Logger.getLogger(TournamentDataBean.class.getName());
	
	private Date createdDate;
	private Date eventDate;
	
	String status;
	
	private List<GladiatorChallengeBean> challenges = new ArrayList<GladiatorChallengeBean>();
	
	private List<MatchResultBean> results = new ArrayList<MatchResultBean>();
	
	private Entity thisEntity = new Entity(tournamentEntity);
	
	public TournamentDataBean (){//use to create a new tournament
		createdDate = new Date();	
		status = "Pending";
		Calendar cal = Calendar.getInstance(); // creates calendar
	    cal.setTime(new Date()); // sets calendar time/date
	    cal.add(Calendar.HOUR_OF_DAY, tournamentFrequency); 
	    eventDate = cal.getTime(); // returns new date object for the time the tournament will happen
	    setUpEntity();		
	}
	
	public TournamentDataBean(Entity t, boolean existing){
		thisEntity = t;
		setUpBean();
		if (existing){
			getAllTournamentChallenges();
			getAllTournamentMatchResults();
		}
	}
	
	private void setUpEntity(){
		thisEntity.setProperty("createdDate", this.createdDate);
		thisEntity.setProperty("eventDate", this.eventDate);
		thisEntity.setProperty("status", this.status);
		
	}
	
	public List<GladiatorChallengeBean> executeTournament(){
		this.setStatus("Complete");
		List<GladiatorChallengeBean> matches = new ArrayList<GladiatorChallengeBean>();
		if (getChallenges() == null || getChallenges().size() < 1){
			getAllTournamentChallenges();		
		}
		
		if (getChallenges() == null){
			return null;
		} else {
			for (GladiatorChallengeBean bean : this.challenges){
				if (bean.getStatusEnum().equals(Status.ACCEPTED)){
					matches.add(bean);
					
				}				
				bean.setStatus(Status.EXPIRED);
				bean.saveChallenge();
			}
		}
		this.saveTournament();
		log.info("Created " + matches.size() + " matches for the tournament");
		return matches;
	}
	
	public void saveTournament(){
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		
		setUpEntity();
		Transaction txn = datastore.beginTransaction();
		try {
			
			datastore.put(thisEntity);	
			
			txn.commit();
		} finally {	
		    if (txn.isActive()) {
		        txn.rollback();
		        log.warning("Save Tournament transaction failed: rolled back");
		    }
		}		
	}		

	private void getAllTournamentMatchResults() {
		
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();	
		
		Query qy = new Query(matchResultEntity, thisEntity.getKey());
		FetchOptions options = FetchOptions.Builder.withChunkSize(200);
        PreparedQuery preparedQuery = datastore.prepare(qy);
        log.info("FOUNDEDEDED " + preparedQuery.countEntities(options) + " match result entities"); 
        for (Entity resul : preparedQuery.asIterable(options)){
        	results.add(new MatchResultBean(resul));
        }			
	}

	private void getAllTournamentChallenges() {

		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();	
		
		Query qy = new Query(challengeEntity, thisEntity.getKey());
		FetchOptions options = FetchOptions.Builder.withChunkSize(200);
        PreparedQuery preparedQuery = datastore.prepare(qy);
        log.info("FOUNDEDEDED " + preparedQuery.countEntities(options) + " entities"); 
        for (Entity chal : preparedQuery.asIterable(options)){
        	challenges.add(new GladiatorChallengeBean(chal, true));
        }		
	}

	private void setUpBean() {
		createdDate = (Date) thisEntity.getProperty("createdDate");
		eventDate = (Date) thisEntity.getProperty("eventDate");		
		
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
		thisEntity.setProperty("createdDate", createdDate);
	}

	public Date getEventDate() {
		return eventDate;
	}

	public void setEventDate(Date eventDate) {
		this.eventDate = eventDate;
		thisEntity.setProperty("eventDate", eventDate);
	}

	public List<GladiatorChallengeBean> getChallenges() {
		return challenges;
	}

	public void setChallenges(List<GladiatorChallengeBean> challenges) {
		this.challenges = challenges;
		//these aren't stored with the entity
	}

	public List<MatchResultBean> getResults() {
		if (results == null || results.size() == 0){
			getAllTournamentMatchResults();
		}
		return results;
	}

	public void setResults(List<MatchResultBean> results) {
		this.results = results;
		//these aren't stored with the entity
	}
	
	public Key getDataStoreKey(){
		return thisEntity.getKey();
	}
	
	public String getKey(){
		return thisEntity.getKey().toString();
	}
	
	public String getStatus(){
		return this.status;
	}
	
	public void setStatus(String status){
		this.status = status;
		thisEntity.setProperty("status", this.status);
	}
	
}