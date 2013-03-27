/**
 * 
 */
package com.bloodandsand.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.logging.Logger;


import com.bloodandsand.beans.GladiatorDataBean;
import com.bloodandsand.utilities.BaseServlet;

import com.google.appengine.api.datastore.FetchOptions;


/**
 * @author dewie
 *
 */
@SuppressWarnings("serial")
public class FreshRecruits  extends BaseServlet {
	private static int BASE_NUMBER_OF_RECRUITS = 20; //this is the number of recruits that should be available for purchase at any time.
	protected static final Logger log = Logger.getLogger(FreshRecruits.class.getName());
	private FetchOptions free_recruit_check =
		    FetchOptions.Builder.withLimit(BASE_NUMBER_OF_RECRUITS + 5);
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
		int results = 0;
    	GladiatorDataBean g = new GladiatorDataBean();
		results = g.countAvailableGladiators(free_recruit_check);//check the number of gladiators available
		log.info("current number of recruits= " + results);
		
        if (results <= BASE_NUMBER_OF_RECRUITS){
        	int count = BASE_NUMBER_OF_RECRUITS - results;

        	for (; count > 0; count --){
        		g.createGladiator();
        		g.saveNewGladiator();
        	}       	
        }		
	}

}
