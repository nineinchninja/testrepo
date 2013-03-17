/**
 *  Created by Andy Hayward
 *  Feb 25, 2013
 */
package com.bloodandsand.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bloodandsand.beans.GladiatorDataBean;
import com.bloodandsand.beans.UserDataBean;
import com.bloodandsand.utilities.BaseServlet;

/**
 * @author dewie
 *
 */
public class ApplyName extends BaseServlet {

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
		HttpSession sess = req.getSession();
		if (!checkLogin(req) || sess.getAttribute(userBeanData) == null){			
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
				log.info("ApplyName.java No user bean available. Naming request failed");
				resp.sendRedirect(loginRedirect);
			} else {
				String nameError = "";
				boolean changed = false;
				GladiatorDataBean gbean = new GladiatorDataBean();
				List<GladiatorDataBean> gladlist = new ArrayList<GladiatorDataBean>();
				Iterator<GladiatorDataBean> gldtrs = usr.ludus.getGladiators().iterator();
				while (gldtrs.hasNext()){
					gbean = gldtrs.next();
					gladlist.add(gbean);
					String newName = req.getParameter(gbean.getKey());
					log.info("New name should be: " + newName);
					if ( newName != null && newName.length() >= 4 && checkValidCharacters(newName) && newName.length() < 14){						
						log.info("New name passes muster and should be: " + newName);						
						gbean.setName(newName);
						gbean.saveGladiator(gbean.getKey());
						changed = true;
						
					} else {
						nameError = "Invalid name. Names should be between 4 and 14 characters and contain no special characters";
						req.getSession().setAttribute("nameError", nameError);
					}
				}
				if (changed){	
					usr.ludus.setGladiators(gladlist);
					req.getSession().setAttribute(userBeanData, usr);
					//req.getSession().setAttribute(nameError, "");
				}
				resp.sendRedirect(loggedInRedirect);
			}
		}
	}
	
	
}
