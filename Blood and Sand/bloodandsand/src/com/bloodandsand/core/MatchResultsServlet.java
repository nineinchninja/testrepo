package com.bloodandsand.core;

import java.io.IOException;
import java.util.List;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bloodandsand.beans.TournamentDataBean;
import com.bloodandsand.utilities.BaseServlet;

public class MatchResultsServlet extends BaseServlet {
	
	
	
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
			throws IOException{
		
	}

}
