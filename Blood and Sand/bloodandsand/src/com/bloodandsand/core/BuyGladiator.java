/**
 * 
 */
package com.bloodandsand.core;

import java.io.IOException;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.bloodandsand.beans.GladiatorDataBean;
import com.bloodandsand.beans.LudusDataBean;
import com.bloodandsand.beans.UserDataBean;
import com.bloodandsand.utilities.*;


/**
 * @author dewie
 *
 */
@SuppressWarnings("serial")
public class BuyGladiator extends BaseServlet{

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
		      throws IOException, ServletException {
		if (!checkLogin(req) || req.getSession().getAttribute(userBeanData) == null){			
			resp.sendRedirect(loginRedirect);
		} else {
			write_line(req, resp, "Page not available in this manner.");
		}		
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws IOException {
		if (!checkLogin(req)){	//redirect if not logged in		
			resp.sendRedirect(loginRedirect);
		} else {
			String gladKey = req.getParameter("gladKey");
			log.info(gladKey);
			LudusDataBean recruits = (LudusDataBean)req.getSession().getAttribute("Recruits");
			if (gladKey == null || recruits == null){//if data didn't get passed correctly, 
				log.info("BuyGladiator.class: key value or bean not available");				
				write_line(req, resp, "Your request could not be processed at this time. Please try again later.");
			} else {
					Iterator<GladiatorDataBean> gldtrs = recruits.getGladiators().iterator();
					GladiatorDataBean selected = new GladiatorDataBean();
					Boolean test = false;
					while (gldtrs.hasNext() && !test){
						selected = gldtrs.next();
						if (selected.getKey().equals(gladKey)){
							test = true;
							log.info("BuyGladiator.java: Key found in list of available gladiators:" + selected.getKey());
						}
					}
				if (!test){
						log.info("BuyGladiator.java: key value or bean not available");
						resp.sendRedirect(loginRedirect);
						write_line(req, resp, "Your request could not be processed at this time. Please try again later.");
				} else {
						UserDataBean usr = (UserDataBean)req.getSession().getAttribute(userBeanData);
						if (usr == null){
							log.info("BuyGladiator.java No user bean available. Purchase was not processed");
							resp.sendRedirect(loginRedirect);
						} else {
								if (usr.ludus.gladiators.size() >= MAX_GLADIATORS_ALLOWED){
									log.info("BuyGladiator.java: User attempted to hold more than maximum number of gladiators");
									write_line(req, resp, "You have the maximum number of gladiators already.");
								} else {
									selected.setNewOwner((String)req.getSession().getAttribute("username"), (String)usr.getDataStoreKey());//with the added key string it is saved to the datastore
									usr.ludus.addNewGladiator(selected);//update the client so as to show the new gladiator when returning to home page
									usr.ludus.updateAvailableGold(-(selected.getPrice()));
									usr.ludus.saveLudus();
									req.getSession().setAttribute(userBeanData, usr);
									req.getSession().setAttribute(userDataRefresh, System.currentTimeMillis());
									resp.sendRedirect(loginRedirect);
								}
						}
						
					}				
				}
			}			
		}

}
