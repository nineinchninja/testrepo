/**
 * 
 */
package com.bloodandsand.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.logging.Logger;


import com.bloodandsand.beans.GladiatorDataBean;
import com.bloodandsand.utilities.BaseServlet;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;


/**
 * @author dewie
 *
 */
@SuppressWarnings("serial")
public class FreshRecruits  extends BaseServlet {
	protected static final Logger log = Logger.getLogger(FreshRecruits.class.getName());
	private boolean logEnabled = false;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
		List<Entity> results = null;
		results = getGladiatorsOnSale();//check the number of gladiators available
		if (logEnabled){log.info("current number of recruits= " + results);}
		
        if (results.size() <= BASE_NUMBER_OF_RECRUITS){
        	int count = BASE_NUMBER_OF_RECRUITS - results.size();
        	GladiatorDataBean g = new GladiatorDataBean();
        	for (; count > 0; count --){
        		g.createGladiator();
        		g.saveNewGladiator();
        		results.add(g.getEntity());        		
        	}        	
        	g.saveNewGladiatorsToCache(results);
        }		
	}

}
