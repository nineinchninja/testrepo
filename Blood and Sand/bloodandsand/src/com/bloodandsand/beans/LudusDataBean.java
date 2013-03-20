
package com.bloodandsand.beans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import com.bloodandsand.utilities.CoreBean;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Transaction;

/**
 * @author Andrew Hayward
 * December 2012
 *
 */
public class LudusDataBean extends CoreBean implements java.io.Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -2520748803858400976L;
	private long availableGold;
	private long wageredGold;
	private long weeklyCosts;
	
	public List<GladiatorDataBean> gladiators = null;
	
	private static final Logger log = Logger.getLogger(LudusDataBean.class.getName());

	
	public LudusDataBean(){
		availableGold = (long) 0;
		wageredGold = (long) 0;
		weeklyCosts = (long) 0;		
	}
	public LudusDataBean (Entity luds){//getting a ludus based on an entity
		availableGold = (Long) luds.getProperty("availableGold");
		wageredGold = (Long) luds.getProperty("wageredGold");
		weeklyCosts = (Long) luds.getProperty("weeklyCosts");
	}	
	
	public void setAvailableGold (Long availableGold){
		this.availableGold = availableGold;		
	}
	
	public void setWageredGold  (Long wageredGold){
		this.wageredGold = wageredGold;		
	}
	
	public void setWeeklyCost(Long weeklyCost){
		this.weeklyCosts = weeklyCost;		
	}
	
	public void setGladiators  (List<GladiatorDataBean> gladiators ){
		this.gladiators = gladiators;		
	}
	
	public long getAvailableGold(){
		return availableGold;
	}
	
	public long getWageredGold(){
		return wageredGold;
	}
	
	public long getWeeklyCost(){
		return weeklyCosts;
	}
	
	public List<GladiatorDataBean> getGladiators(){
		return gladiators;		
	}
	
	public void addNewGladiator(GladiatorDataBean gldtr){
		gladiators.add(gldtr);

	}
	
	public void updateAvailableGold(long gold, String userName){
		this.setAvailableGold(this.availableGold + gold);
		UserDataBean usr = new UserDataBean();
		Entity luds = usr.findUserLudus(userName);
		DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
		Transaction txn = datastore.beginTransaction();
		try {											
			luds.setProperty("availableGold", availableGold);
			datastore.put(luds);
			txn.commit();
		} finally {
		    if (txn.isActive()) {
		        txn.rollback();
		        log.warning("LudusDataBean.java: Gold transaction failed: rolled back");
		    }
		}
	}
	public List<GladiatorDataBean> getMyChallengeableGladiators() {
		if (this.gladiators == null){
			return null;
		} else {
			List<GladiatorDataBean> avail = new ArrayList<GladiatorDataBean>();
			Iterator<GladiatorDataBean> itr = this.gladiators.iterator(); 
			while (itr.hasNext()){
				GladiatorDataBean q = itr.next();
				if (q.isGladiatorAvailableToChallenge()){
					avail.add(q);
					log.info("Added gladiator " + q.getName() + " to my challengers");
				} else {
					log.info("Did NOT add gladiator " + q.getName() + " to my challengers");
				}
				
			}
			return avail;
		}
		
	}
}
