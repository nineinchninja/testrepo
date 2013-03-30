/**
 *  Created by Andy Hayward
 *  Mar 28, 2013
 */
package com.bloodandsand.core;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bloodandsand.beans.TournamentDataBean;
import com.bloodandsand.utilities.BaseServlet;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

/**
 * @author dewie
 *
 */
public class SetRankings extends BaseServlet {
	
	
		
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException{
		
		
		MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
		
		if (!syncCache.contains(rankingsKey)){
			log.warning("Rankings not in cache. Inserting now.");
			TournamentDataBean test = new TournamentDataBean();
			test.createRankings();
		} else {
			log.info("rankings in cache");
		}
    	
	}

}
