/**
 *  Created by Andy Hayward
 *  Feb 23, 2013
 */
package com.bloodandsand.core;

import java.io.IOException;

import java.util.Iterator;
import java.util.List;

import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bloodandsand.beans.GladiatorDataBean;
import com.bloodandsand.utilities.BaseServlet;


/**
 * @author dewie
 * a cron job to run through all gladiators and apply any training bonuses
 * 
 * Runs a query to get all gladiators who are fit, have training assigned, and belong to a stable
 * Then takes all stats and adds to a total. This number is used against a soft cap to 
 * establish the chance of improvement. If the random is successful, 1 point is added to the 
 * skill/attribute being trained
 */
public class ReviewTraining extends BaseServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8866858882131693139L;
	protected static final Logger log = Logger.getLogger(FreshRecruits.class.getName());
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
		
		List<GladiatorDataBean> gladsForTraining = getGladiatorsForTraining();
		if (gladsForTraining != null){
			for (GladiatorDataBean glad : gladsForTraining){
				glad.attemptTraining();
			}
		}	
	}

	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		//not needed at this time
	}


}
