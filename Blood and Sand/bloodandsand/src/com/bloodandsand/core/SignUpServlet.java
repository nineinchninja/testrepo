/**
 * Created by Andrew Hayward
 * Dec 2012
 */
package com.bloodandsand.core;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.regex.*;

import com.bloodandsand.beans.UserDataBean;
import com.bloodandsand.utilities.*;

@SuppressWarnings("serial")
public class SignUpServlet extends BaseServlet{
	String userName;
	String emailAddress;
	String password;
	String errorMessageName;
	String errorMessagePassword;
	String errorMessageEmail;
	String signUpUrl = "/admin/signup.jsp";
	String loginUrl = "/login";	
	
	Pattern validPassword = Pattern.compile("[\\W\\s^\\\\<>\\[\\]|{}]");
	
	private boolean logEnabled = false;
	
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException {
		resp.sendRedirect("/admin/signup.jsp");
		if (logEnabled){log.info("Redirect to signup.jsp");}
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException, ServletException {
		//reset all variables
		userName = req.getParameter("username").toLowerCase();
		emailAddress = req.getParameter("email");
		password = req.getParameter("password");
		errorMessageName = null;
		errorMessagePassword = null;
		errorMessageEmail = null;
		
		//run entry validation checks
		Boolean nameCheck = false;
		Boolean passwordCheck = false;
		Boolean emailCheck = false;
		Boolean nameAvailable = false;
		Boolean emailAvailable = false;		
		
		nameCheck = checkUserName(userName);
		passwordCheck = checkPassword(password, req.getParameter("verify"));
		emailCheck = checkEmail(emailAddress);
		//check for any messages. If there are any, redirect to signup page
		if (!nameCheck || !passwordCheck ||	!emailCheck ){
			
			if (!nameCheck){
				userName = null;
				if (logEnabled){log.info("Name Check Failed");}
			}
			if (!passwordCheck){
				password = null;
				if (logEnabled){log.info("Password Validity Check Failed");}
			}
			
			if (!emailCheck){
				if (logEnabled){log.info("Email Validity Check Failed");}				
			}					
		}
			
		UserDataBean usr = new UserDataBean();
		//Check if name is available. If so, store it in the object
		if (nameCheck){
			nameAvailable = usr.setNewUserName(userName);
			if (!nameAvailable){
				userName = null;
				errorMessageName="That name is already in use. Please choose another.";				
				if (logEnabled){log.info("Name Availabilty Check Failed");}
			}
		}
		//Check if email is available. If so, store it in the class
		if (emailCheck){
			emailAvailable = usr.setNewEmailAddress(emailAddress);
			if (!emailAvailable){
				errorMessageEmail = "That email address is already in use. Please choose another.";
				if (logEnabled){log.info("Email Availabilty Check Failed");}
			}
		}
		
		if (nameCheck && emailCheck && passwordCheck && nameAvailable && emailAvailable){	
			if (logEnabled){log.info("Verifications and validations checked. User to be saved.");}
			try {
				usr.setPasswordHash(password);
			} catch (NoSuchAlgorithmException e) {
				// TODO Auto-generated catch block
				log.severe("hash function failed");
				e.printStackTrace();
			} catch (InvalidKeySpecException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				log.severe("hash function failed");
			}

			usr.setStatus("Active");
			usr.setUserLevel("Normal");
			usr.saveNewUser(); //saves the user data and creates a new ludus
			req.getSession().setAttribute("username", userName);
			resp.sendRedirect(loginUrl);

		} else {//something went wrong. redirect with error messages

			req.setAttribute("username", userName);
			req.setAttribute("emailAddress", emailAddress);
			req.setAttribute("errorMessageName", errorMessageName);
			req.setAttribute("errorMessagePassword", errorMessagePassword);
			req.setAttribute("errorMessageEmail", errorMessageEmail);
			RequestDispatcher rd = req.getRequestDispatcher(signUpUrl);
			rd.forward(req, resp);
		}
	}
	
	private Boolean checkUserName(String userName){
		if (userName == null || userName == "" || userName.length() < 4 || userName.length() > 13){
			errorMessageName = "Please enter a user name between 4 and 13 characters.";
			if (logEnabled){log.info("no user name");}
			return false;
		}
		
		if (!checkValidCharacters(userName)){
			errorMessageName = "User names may only contain letters, numbers and _ and -";
			return false;
		}		
						
		return true;
	}
	
	private Boolean checkPassword(String passwrd, String verification){
		if (passwrd == null || passwrd == "" || passwrd.length() < 6 || passwrd.length() > 15){
			errorMessagePassword = "Please enter a valid password between 6 and 12 characters.";
			return false;
		}
		if (!passwrd.equals(verification)){
			errorMessagePassword = "The password and the verification didn't match.";
			return false;
		}
		
		Matcher m = validPassword.matcher(password);
		if (m.find()){
			errorMessagePassword = "The password contained invalid characters";
			return false;
		}
		return true;
	}
	
	private Boolean checkEmail(String email){
		if (email == null || email == "") {
			errorMessageEmail = "Please enter an email address.";
			return false;
		}
		
		if (email.contains(" ") || !emailAddress.matches(".+@.+\\.[a-z]+")){
			errorMessageEmail = "Please enter an email address.";
			return false;
		}		
		return true;
	}

}
