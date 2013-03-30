/**
 *  Created by Andy Hayward
 *  Feb 21, 2013
 */
package com.bloodandsand.core;

import com.bloodandsand.utilities.BaseServlet;
import java.io.IOException;
import java.util.ArrayList;

import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.bloodandsand.beans.GladiatorDataBean;
import com.bloodandsand.beans.UserDataBean;

/**
 * @author dewie
 *
 */
@SuppressWarnings("serial")
public class GladiatorTraining  extends BaseServlet{
	
	private boolean logEnabled = false;
	
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
		//HttpSession sess = req.getSession();
		if (!checkLogin(req) || req.getSession().getAttribute(userBeanData) == null){			
			resp.sendRedirect(loginPage);
		} else {
			refreshUserBean(req);
			RequestDispatcher rd = req.getRequestDispatcher(gladiatorTrainingJsp);
			rd.forward(req, resp);
		}		
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {		
		/*
		 * Checks the value of the input fields
		 * if not = No Change, updates the gladiatorbean and the database with new training
		 * redirects back with message about being updated
		 */
		if (!checkLogin(req)){	//redirect if not logged in		
			resp.sendRedirect(loginRedirect);
		} else {
			UserDataBean usr = (UserDataBean)req.getSession().getAttribute(userBeanData);
			if (usr == null){
				log.warning("GladiatorTraining.java No user bean available. Training request failed");
				resp.sendRedirect(loginRedirect);
			} else {
				boolean changed = false;
				GladiatorDataBean gbean = new GladiatorDataBean();
				List<GladiatorDataBean> gladlist = new ArrayList<GladiatorDataBean>();
				Iterator<GladiatorDataBean> gldtrs = usr.ludus.getGladiators().iterator();
				while (gldtrs.hasNext()){
					gbean = gldtrs.next();
					gladlist.add(gbean);
					String newTraining = req.getParameter(gbean.getKey());
					if (logEnabled){log.info("New training value should be: " + newTraining);}
					
					if ( newTraining != null && !newTraining.equals("No change")){						
						if (logEnabled){log.info("New training value should be: " + newTraining);}						
						gbean.setNewTraining(newTraining);
						gbean.saveGladiator();
						changed = true;
					}				
				}
				if (changed){	
					usr.ludus.setGladiators(gladlist);
					req.getSession().setAttribute(userBeanData, usr);					
				}
				resp.sendRedirect(trainingPage);
			}
		}
	}

}
