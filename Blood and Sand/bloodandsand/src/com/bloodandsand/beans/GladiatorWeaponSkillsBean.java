/**
 *  Created by Andy Hayward
 *  Feb 20, 2013
 */
package com.bloodandsand.beans;
import java.util.logging.Logger;

import com.bloodandsand.utilities.CoreBean;

import com.google.appengine.api.datastore.EmbeddedEntity;

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
	
	private EmbeddedEntity thisEntity = new EmbeddedEntity();	
	
	
	public GladiatorWeaponSkillsBean(){ //standard constructor used for creating new gladiators
		sword = 0;
		daggers  = 0;
		greatsword  = 0;
		greataxe  = 0;
		spear  = 0;
		quarterstaff  = 0;
		maul  = 0;	
		setUpEntity();
	}
		
	public GladiatorWeaponSkillsBean( EmbeddedEntity embeddedEntity){
		thisEntity = embeddedEntity;
		this.sword = (Long)(embeddedEntity.getProperty("sword"));
		this.daggers = (Long)(embeddedEntity.getProperty("daggers"));
		this.greatsword = (Long)(embeddedEntity.getProperty("greatsword"));
		this.greataxe = (Long)(embeddedEntity.getProperty("greataxe"));
		this.spear = (Long)(embeddedEntity.getProperty("spear"));
		this.quarterstaff = (Long)(embeddedEntity.getProperty("quarterstaff"));
		this.maul = (Long)(embeddedEntity.getProperty("maul"));
		this.handToHand = (Long)embeddedEntity.getProperty("handToHand");
	}	
	
	private void setUpEntity(){
		thisEntity.setProperty("sword", sword);
		thisEntity.setProperty("daggers", daggers);
		thisEntity.setProperty("greatsword", greatsword);
		thisEntity.setProperty("greataxe", greataxe);
		thisEntity.setProperty("spear", spear);
		thisEntity.setProperty("quarterstaff", quarterstaff);					
		thisEntity.setProperty("maul", maul); 
		thisEntity.setProperty("handToHand", handToHand);
	}
	
	public EmbeddedEntity getEntity(){
		return thisEntity;
	}

	public long getSword() {
		return sword;
	}
	
	public String getSwordString(){
		return getAttributeRating(sword);
	}

	public void setSword(long sword) {
		this.sword = sword;
		thisEntity.setProperty("sword", sword);		
	}
	
	public void trainSword(){
		this.sword += TRAINING_INCREMENT_AMOUNT;
		thisEntity.setProperty("sword", sword);	
	}

	public long getDaggers() {
		return daggers;
	}
	
	public String getDaggersString(){
		return getAttributeRating(daggers);
	}

	public void setDaggers(long daggers) {
		this.daggers = daggers;
		thisEntity.setProperty("daggers", daggers);	
	}
	
	public void trainDaggers(){
		this.daggers += TRAINING_INCREMENT_AMOUNT;
		thisEntity.setProperty("daggers", daggers);
	}

	public long getGreatsword() {
		return greatsword;
	}
	
	public String getGreatswordString(){
		return getAttributeRating(greatsword);
	}

	public void setGreatsword(long greatsword) {
		this.greatsword = greatsword;
		thisEntity.setProperty("greatsword", greatsword);
	}
	
	public void trainGreatsword(){
		this.greatsword += TRAINING_INCREMENT_AMOUNT;
		thisEntity.setProperty("greatsword", greatsword);
	}

	public long getGreataxe() {
		return greataxe;
	}
	
	public String getGreataxeString(){
		return getAttributeRating(greataxe);
	}

	public void setGreataxe(long greataxe) {
		this.greataxe = greataxe;
		thisEntity.setProperty("greataxe", greataxe);
	}
	
	public void trainGreataxe(){
		this.greataxe += TRAINING_INCREMENT_AMOUNT;
		thisEntity.setProperty("greataxe", greataxe);
	}

	public long getSpear() {
		return spear;
	}
	
	public String getSpearString(){
		return getAttributeRating(spear);
	}

	public void setSpear(long spear) {
		this.spear = spear;
		thisEntity.setProperty("spear", spear);
	}
	
	public void trainSpear(){
		this.spear += TRAINING_INCREMENT_AMOUNT;
		thisEntity.setProperty("spear", spear);
	}

	public long getQuarterstaff() {
		return quarterstaff;
	}
	
	public String getQuarterstaffString(){
		return getAttributeRating(quarterstaff);
	}

	public void setQuarterstaff(long quarterstaff) {
		this.quarterstaff = quarterstaff;
		thisEntity.setProperty("quarterstaff", quarterstaff);
	}
	
	public void trainQuarterstaff(){
		this.quarterstaff += TRAINING_INCREMENT_AMOUNT;
		thisEntity.setProperty("quarterstaff", quarterstaff);
	}

	public long getMaul() {
		return maul;
	}
	
	public String getMaulString(){
		return getAttributeRating(maul);
	}

	public void setMaul(long maul) {
		this.maul = maul;
		thisEntity.setProperty("maul", maul);
	}
	
	public void trainMaul(){
		this.maul += TRAINING_INCREMENT_AMOUNT;
		thisEntity.setProperty("maul", maul);
	}

	public long getHandToHand() {
		return handToHand;
	}
	
	public String getHandtoHandString(){
		return getAttributeRating(handToHand);
	}

	public void setHandToHand(long handToHand) {
		this.handToHand = handToHand;
		thisEntity.setProperty("handToHand", handToHand);
	}
	
	public void trainHandToHand(){
		this.handToHand += TRAINING_INCREMENT_AMOUNT;
		thisEntity.setProperty("handToHand", handToHand);
	}

}
