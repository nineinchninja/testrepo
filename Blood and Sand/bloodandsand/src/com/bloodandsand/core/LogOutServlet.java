/**
 * 
 */
package com.bloodandsand.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.bloodandsand.utilities.BaseServlet;

/**
 * @author Andrew Hayward
 * December 2012
 *
 */
@SuppressWarnings("serial")
public class LogOutServlet extends BaseServlet {
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
		Boolean deleted = false;

		//check for user cookie and session
		//if yes to both, redirect to user page
		//if no, redirect to login.html

		deleted = deleteCookie(resp, req, "user");
		req.getSession().removeAttribute(userBeanData);
		req.getSession().removeAttribute(rankingsKey);
		
		if (deleted){
			resp.sendRedirect("/login");
		} else {
			write_line(req, resp, "Cookie wasn't deleted");
		}
	}
}
