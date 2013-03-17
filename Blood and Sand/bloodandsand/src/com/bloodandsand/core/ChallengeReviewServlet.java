/**
 *  Created by Andy Hayward
 *  Mar 13, 2013
 */
package com.bloodandsand.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bloodandsand.beans.GladiatorChallengeBean;
import com.bloodandsand.beans.GladiatorDataBean;
import com.bloodandsand.beans.UserDataBean;
import com.bloodandsand.utilities.BaseServlet;

/**
 * @author dewie
 * This simple servlet gets the user's current challenges and 
 * prepares them for the JSP to display
 *
 */
public class ChallengeReviewServlet extends BaseServlet {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5552646934022591261L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
		//check for login status and bean
		HttpSession sess = req.getSession();
		if (!checkLogin(req) || req.getSession().getAttribute(userBeanData) == null){			
			resp.sendRedirect(loginPage);
		} else {
			//ensure bean is fresh
			refreshUserBean(req);			

			RequestDispatcher rd = req.getRequestDispatcher(challengeReviewJsp);
			rd.forward(req, resp);
		}		
		 
	}
	
	public void doPost (HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
		HttpSession sess = req.getSession();
		if (!checkLogin(req) || sess.getAttribute(userBeanData) == null){			
			resp.sendRedirect(loginPage);
		} else {
			//ensure bean is fresh
			refreshUserBean(req);
			UserDataBean usr = new UserDataBean();
			usr = (UserDataBean) sess.getAttribute(userBeanData);
			String acceptedChallenge = "";
			boolean accpted = false;
			//get the challenge accepted
			acceptedChallenge = req.getParameter("accepted");

			if (usr == null || acceptedChallenge == null){
				write_line(req, resp, "Your request could not be processed at this time. Please try again later.");
				log.info("Something was null when trying to accept the challenge");
			} else {
				// Get the information from the user bean. If not on the user bean, go to the db and retrieve it
				//
				for (GladiatorDataBean gladtr: usr.ludus.gladiators){
					List<GladiatorChallengeBean> challs = new ArrayList<GladiatorChallengeBean>();
					challs = gladtr.getChallenges();
					if (challs == null){
						log.info("gladiator challenges not available");
					} else {
						for (GladiatorChallengeBean challnge: challs ){
							if (challnge.getGladiatorChallengeKey().equals(acceptedChallenge)){
								accpted = challnge.acceptChallenge();
							}
						}
					}					
				}
				if (!accpted){
					write_line(req, resp, "That gladiator has already accepted another challenge");
					log.info("Challenge not accepted due to gladiator not being available");
				} else {
					usr.populateUserDataBean(usr.getUserName());
					sess.setAttribute(userBeanData, usr);
					RequestDispatcher rd = req.getRequestDispatcher(challengeReviewJsp);
					rd.forward(req, resp);
				}
				
			}		
		}	
		//
	}
	
}
