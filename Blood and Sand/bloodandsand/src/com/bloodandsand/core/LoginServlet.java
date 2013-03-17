/**
 * 
 */
package com.bloodandsand.core;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import com.bloodandsand.beans.UserDataBean;
import com.bloodandsand.utilities.*;


import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;


/**
 * @author Andrew Hayward
 * December 2012
 * TODO - clean this up. The interfaces are inconsistent and it should be changed to using beans
 */
@SuppressWarnings("serial")
public class LoginServlet extends BaseServlet {
	/**
	 * 
	 */
//	private static final long serialVersionUID = 4200855935757675947L;
	
	
	
	//class that handles all login requests. All failed authentication checks should be directed here
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
		//check for user cookie and session
		//if yes to both, redirect to user page
		//if no, redirect to login.html
		
		if (checkLogin(req) && req.getSession().getAttribute(userBeanData) != null){
			checkDataFreshness(req.getSession());
			//resp.sendRedirect(loggedInRedirect);
			
			RequestDispatcher rd = req.getRequestDispatcher(loggedInRedirect);
			 rd.forward(req, resp);
		} else {
			resp.sendRedirect(loginPage);
		}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		String username = req.getParameter("username").toLowerCase();
		String pwd 		= req.getParameter("password");
		Boolean login   = false;
		HttpSession sess = req.getSession();
		UserDataBean u = new UserDataBean();
    	try {
			login = u.attemptLogin(username, pwd);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			log.warning("failed algorithm at login");
			e.printStackTrace();
		} catch (InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			log.warning("invalid key at login");
			e.printStackTrace();
		}

		 if (login){
			 log.info("The user " + username + " has logged in.");
			 
			 u.setUserName(username);
			 
			 sess.setAttribute("username", username);
			 
			 boolean check = u.populateUserDataBean(username);
			 if (check){
				 sess.setAttribute(userBeanData, u);
				 sess.setAttribute(userDataRefresh, System.currentTimeMillis());
				 
				 try {
					setSecureCookie(resp, "user", username);
				} catch (NoSuchAlgorithmException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidKeySpecException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		
				 RequestDispatcher rd = req.getRequestDispatcher(loggedInRedirect);
				 rd.forward(req, resp);
			 } else {
				 RequestDispatcher rd = req.getRequestDispatcher(loginPage);
				 req.setAttribute("username", username);
				 req.setAttribute("LoginError", "Login failed. Please enter a valid name and password");
				 rd.forward(req, resp);
			 }			 
			 
		 } else {
			 RequestDispatcher rd = req.getRequestDispatcher(loginPage);
			 req.setAttribute("username", username);
			 req.setAttribute("LoginError", "Login failed. Please enter a valid name and password");
			 rd.forward(req, resp);
		 }
	}

}
