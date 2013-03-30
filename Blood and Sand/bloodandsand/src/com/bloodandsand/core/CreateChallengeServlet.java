package com.bloodandsand.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bloodandsand.beans.GladiatorChallengeBean;
import com.bloodandsand.beans.GladiatorDataBean;
import com.bloodandsand.beans.UserDataBean;
import com.bloodandsand.utilities.BaseServlet;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;

public class CreateChallengeServlet extends BaseServlet {
	
	protected static final Logger log = Logger.getLogger(CreateChallengeServlet.class.getName());
	private boolean logEnabled = false;
	/**
	 * 
	 */
	private static final long serialVersionUID = -3104867880036392496L;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
		HttpSession sess = req.getSession();
		if (!checkLogin(req) || req.getSession().getAttribute(userBeanData) == null){			
			resp.sendRedirect(loginPage);
		} else {
			//ensure bean is fresh
			refreshUserBean(req);	
			// get all gladiators that the player owns that can be added
			UserDataBean usr = (UserDataBean) sess.getAttribute(userBeanData);
			if (logEnabled){log.info("This person has requested their fighters to create challenges: " + usr.getUserName());}
			List<GladiatorDataBean> myChallengers = usr.ludus.getMyChallengeableGladiators();
			
			
			if (myChallengers != null){
				sess.setAttribute("MyChallengers", myChallengers);
			}
			//get the rankings
			 MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
			 syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
			 
			 List<GladiatorDataBean> rankings = new ArrayList<GladiatorDataBean>();
			 rankings = (ArrayList<GladiatorDataBean>) syncCache.get(rankingsKey);
			 
			 if (rankings == null){
				 log.warning("No rankings found");
			 }
			 sess.setAttribute(rankingsKey, rankings);
			
			// get all gladiators that can be challenged 
			GladiatorDataBean temp = new GladiatorDataBean();
			List<GladiatorDataBean> challengeableGladiators = new ArrayList<GladiatorDataBean>();
			challengeableGladiators = temp.getAllChallengeableGladiators(usr.getUserName());
			
			sess.setAttribute("Opponents", challengeableGladiators);			

			RequestDispatcher rd = req.getRequestDispatcher(challengeCreateJsp);
			rd.forward(req, resp);
		}	
		
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
		HttpSession sess = req.getSession();
		if (!checkLogin(req) || req.getSession().getAttribute(userBeanData) == null){			
			resp.sendRedirect(loginPage);
		} else {
			// get all the various and sundry required
			UserDataBean usr = new UserDataBean();
			List<GladiatorDataBean> opponents = new ArrayList<GladiatorDataBean>();
			String opponent = "";
			String challenger = "";
			long wager = 0;
			GladiatorDataBean challengerBean = null;
			GladiatorDataBean opponentBean = null;
			usr = (UserDataBean) sess.getAttribute(userBeanData);
			opponents = (List<GladiatorDataBean>) sess.getAttribute("Opponents");			
			opponent = req.getParameter("opponent");
			challenger = req.getParameter("challenger");
			wager = getLongFromString(req.getParameter("wagerAmount"));
			if (wager <= 0){
				wager = 0;
			}
			if (logEnabled){log.info("wager amount received: " + wager);}
			if (opponent == null || challenger == null ||
					opponents == null || usr == null){
				write_line(req, resp, "Your request could not be processed at this time. Please try again later.");
				log.warning("Something was null when trying to create a new challenge");
			} else {
				//need to match the challenger selected with the gladiators in the ludus, and ensure it is still available
				for (GladiatorDataBean gladitr : usr.ludus.gladiators){
					if (gladitr.getName().equalsIgnoreCase((challenger))){
						challengerBean = gladitr;
					}
				}
				//need to find the opponent in the list of opponents
				for (GladiatorDataBean gladitr : opponents){
					if (gladitr.getName().equalsIgnoreCase((opponent))){
						opponentBean = gladitr;
					}
				}
				log.info("Created challenge: Challenger - " + challengerBean.getName() + 
						" opponent - " + opponentBean.getName());
				//test to ensure it worked right
				if (challengerBean == null || opponentBean == null){
					write_line(req, resp, "Your request could not be processed at this time. Please try again later.");
					log.warning("Challenger or opponent bean was null when trying to create a new challenge");
				} else {
					//need to create the challenge
					
					if (wager > usr.ludus.getAvailableGold()){						
							write_line(req, resp, "You do not have enough gold to wager that amount.");
						} else {
							GladiatorChallengeBean chall = new GladiatorChallengeBean(challengerBean, opponentBean, wager);
							usr.ludus.setWager(wager);
							usr.ludus.saveLudus();
							
							chall.saveNewChallenge();
							if (logEnabled){log.info("Challenge saved");}
							//need to update the user data bean and db with new stuff
							usr.populateUserDataBean(usr.getUserName());
							sess.setAttribute(userBeanData, usr);
							RequestDispatcher rd = req.getRequestDispatcher(challengeReviewJsp);
							rd.forward(req, resp);
						}						
					}					
			
			}			
		}
	}

}
