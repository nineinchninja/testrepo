/**
 *  Created by Andy Hayward
 *  Feb 20, 2013
 */
package com.bloodandsand.beans;
import java.util.logging.Logger;

import com.bloodandsand.utilities.CoreBean;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
/**
 * @author dewie
 * Feb 20 2013
 * intended to be able to get around the problem of storing weapon sets. 
 * 
 * This bean simply holds the gladiator's weapon skills and has get and set methods
 * Each gladiator should have 1 and only 1 of these beans
 */
public class GladiatorWeaponSkillsBean extends CoreBean implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4314632199832289974L;
	protected static final Logger log = Logger.getLogger(GladiatorWeaponSkillsBean.class.getName());
	private long sword;
	private long daggers;
	private long greatsword;
	private long greataxe;
	private long spear;
	private long quarterstaff;
	private long maul;	
	private long handToHand;
	
	
	
	public GladiatorWeaponSkillsBean(){ //standard constructor used for creating new gladiators
		sword = 0;
		daggers  = 0;
		greatsword  = 0;
		greataxe  = 0;
		spear  = 0;
		quarterstaff  = 0;
		maul  = 0;		
	}
	
	public GladiatorWeaponSkillsBean( Entity skills){
		this.setSword((Long)(skills.getProperty("sword")));
		this.setDaggers((Long)(skills.getProperty("daggers")));
		this.setGreatsword((Long)(skills.getProperty("greatsword")));
		this.setGreataxe((Long)(skills.getProperty("greataxe")));
		this.setSpear((Long)(skills.getProperty("spear")));
		this.setQuarterstaff((Long)(skills.getProperty("quarterstaff")));
		this.setMaul((Long)(skills.getProperty("maul")));
		this.setHandToHand((Long)skills.getProperty("handToHand"));
	}
	
	

	public Entity createWeaponSkillsEntity(Key gladKey){
		Entity weaponSkills = new Entity(weaponSkillsKind, gladKey); //link the weapon skills entity to its owner gladiator
		weaponSkills.setProperty("sword", sword);
		weaponSkills.setProperty("daggers", daggers);
		weaponSkills.setProperty("greatsword", greatsword);
		weaponSkills.setProperty("greataxe", greataxe);
		weaponSkills.setProperty("spear", spear);
		weaponSkills.setProperty("quarterstaff", quarterstaff);					
		weaponSkills.setProperty("maul", maul); 
		weaponSkills.setProperty("handToHand", handToHand);
		
		return weaponSkills;
	}
	
	public long getSword() {
		return sword;
	}

	public void setSword(long sword) {
		this.sword = sword;
	}
	
	public void trainSword(){
		this.sword += TRAINING_INCREMENT_AMOUNT;
	}

	public long getDaggers() {
		return daggers;
	}

	public void setDaggers(long daggers) {
		this.daggers = daggers;
	}
	
	public void trainDaggers(){
		this.daggers += TRAINING_INCREMENT_AMOUNT;
	}

	public long getGreatsword() {
		return greatsword;
	}

	public void setGreatsword(long greatsword) {
		this.greatsword = greatsword;
	}
	
	public void trainGreatsword(){
		this.greatsword += TRAINING_INCREMENT_AMOUNT;
	}

	public long getGreataxe() {
		return greataxe;
	}

	public void setGreataxe(long greataxe) {
		this.greataxe = greataxe;
	}
	
	public void trainGreataxe(){
		this.greataxe += TRAINING_INCREMENT_AMOUNT;
	}

	public long getSpear() {
		return spear;
	}

	public void setSpear(long spear) {
		this.spear = spear;
	}
	
	public void trainSpear(){
		this.spear += TRAINING_INCREMENT_AMOUNT;
	}

	public long getQuarterstaff() {
		return quarterstaff;
	}

	public void setQuarterstaff(long quarterstaff) {
		this.quarterstaff = quarterstaff;
	}
	
	public void trainQuarterstaff(){
		this.quarterstaff += TRAINING_INCREMENT_AMOUNT;
	}

	public long getMaul() {
		return maul;
	}

	public void setMaul(long maul) {
		this.maul = maul;
	}
	
	public void trainMaul(){
		this.maul += TRAINING_INCREMENT_AMOUNT;
	}

	public long getHandToHand() {
		return handToHand;
	}

	public void setHandToHand(long handToHand) {
		this.handToHand = handToHand;
	}
	
	public void trainHandToHand(){
		this.handToHand += TRAINING_INCREMENT_AMOUNT;
	}



}
