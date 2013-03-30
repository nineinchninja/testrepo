package com.bloodandsand.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;


import com.bloodandsand.beans.MatchResultBean;
import com.bloodandsand.beans.TournamentDataBean;
import com.bloodandsand.utilities.BaseServlet;

public class MatchResultsServlet extends BaseServlet {
	
	/**
	 * This servlet serves up a list of the most recent tournaments and their match results.
	 * 
	 */
	private static final long serialVersionUID = -1691635922241201395L;
	
	private boolean logEnabled = false;

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException{
		HttpSession sess = req.getSession();
		if (!checkLogin(req) || req.getSession().getAttribute(userBeanData) == null){			
			resp.sendRedirect(loginPage);
		} else {
			refreshUserBean(req);
			List<TournamentDataBean> tournaments = getMatchResults();
			sess.setAttribute(resultsBeanData, tournaments);
			RequestDispatcher rd = req.getRequestDispatcher(matchResultsJsp);
			rd.forward(req, resp);
		}				
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException{
		if (logEnabled){log.info("Viewing Match Detail Initiated.");}
		HttpSession sess = req.getSession();
		if (!checkLogin(req) || sess.getAttribute(userBeanData) == null){			
			resp.sendRedirect(loginPage);
		} else {
			//ensure bean is fresh
			refreshUserBean(req);
			List<TournamentDataBean> tournaments = (List<TournamentDataBean>) sess.getAttribute(resultsBeanData);
			String resultToReview = "";
			//get the result to be reviewed
			resultToReview = (String)req.getParameter("resultKey");
			boolean found = false;

			if (tournaments == null || resultToReview == null){
				write_line(req, resp, "Your request could not be processed at this time. Please try again later.");
				log.warning("Something was null when trying to review a match detail");
			} else {
				// 
				//
				for (TournamentDataBean tourney: tournaments){
					List<MatchResultBean> reslts = new ArrayList<MatchResultBean>();
					reslts = tourney.getResults();
					if (logEnabled){log.info("Number of results in tourney: " + reslts.size());}
					if (reslts == null || reslts.size() == 0){
						if (logEnabled){log.info("No matches available on tourney");}
					} else {
						for (MatchResultBean result: reslts ){
							
							if (result.getResultKey().equals(resultToReview)){
								sess.setAttribute(resultsDetail, result);								
								found = true;
							}
						}
					}					
				}
				if (!found){
					write_line(req, resp, "That match result is not available");
					if (logEnabled){log.info("Match detail selected not available");}
				} else {					
					RequestDispatcher rd = req.getRequestDispatcher(challengeDetailReviewJsp);
					rd.forward(req, resp);
				}				
			}		
		}	
		//
	}
}
