/**
 *  Created by Andy Hayward
 *  Mar 28, 2013
 */
package com.bloodandsand.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bloodandsand.beans.TournamentDataBean;
import com.bloodandsand.utilities.BaseServlet;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * @author dewie
 * 
 * This is run once an hour to ensure that memcache doesn't drop these lists. These lists are used to make quite a few of the tasks run
 * more efficiently
 *
 */
@SuppressWarnings("serial")
public class CacheRefreshServlet extends BaseServlet {
	
	
		
	public void doGet(HttpServletRequest req, HttpServletResponse resp){
		
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		
		if (!syncCache.contains(rankingsKey)){
			log.warning("Rankings not in cache. Inserting now.");
			TournamentDataBean test = new TournamentDataBean();
			test.createRankings();
		} else {
			log.info("rankings in cache");
		}
		
		if (!syncCache.contains(resultsKey)){
			log.warning("Results not in cache. Inserting now.");
			syncCache.put(resultsKey, getMatchResults());
		} else {
			log.info("results in cache");
		} 
		
		if (!syncCache.contains(gladsRecruitsKey)){
			log.warning("Recruits list not in cache. Inserting now. ");
			syncCache.put(gladsRecruitsKey, getGladiatorsOnSale());
		} else {
			log.info("Recruits in cache.");
		}
	}

}
