/**
 * 
 */
package com.bloodandsand.core;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bloodandsand.beans.GladiatorDataBean;
import com.bloodandsand.beans.LudusDataBean;
import com.bloodandsand.utilities.BaseServlet;

/**
 * @author Andrew Hayward
 * December 2012
 *
 * A simple function that finds all gladiators that are not dead and do not belong to a player
 * then forwards the data to the market jsp
 */

@SuppressWarnings("serial")
public class GladiatorMarket extends BaseServlet{
	
	private boolean logEnabled = false;
		
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
		if (!checkLogin(req) || req.getSession().getAttribute(userBeanData) == null){			
			resp.sendRedirect(loginPage);
		} else {
			if (logEnabled){log.info("starting search for recruits");}
			GladiatorDataBean temp = new GladiatorDataBean();
			LudusDataBean availableRecruits = new LudusDataBean();//this is a bit of a hack, but it works for now. Using a ludus object to hold all of the
			availableRecruits.setGladiators(temp.getGladiatorsOnSale());// gladiators in the market
			HttpSession sess = req.getSession();			
			sess.setAttribute("Recruits", availableRecruits);
			RequestDispatcher rd = req.getRequestDispatcher(gladiatorMarketJsp);
			rd.forward(req, resp);
		}		
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {		
		//Not required at this time
	}

}
