package com.bloodandsand.beans;

import java.io.Serializable;

public class WeaponSkillBean implements Serializable {
	
	public long sword;
	public long spear;
	public long greatAxe;
	public long greatSword;
	public long maul;
	public long quarterstaff;
	public long dualDaggers;
	public long handToHand;
	
	public WeaponSkillBean (){
		sword = 0;
		spear = 0;
		greatAxe = 0;
		greatSword = 0;
		maul = 0;
		quarterstaff = 0; 
		dualDaggers = 0;
		handToHand = 0;		
	}
	
	public long getGreatSword() {
		return greatSword;
	}

	public void setGreatSword(long greatSword) {
		this.greatSword = greatSword;
	}

	public long getMaul() {
		return maul;
	}

	public void setMaul(long maul) {
		this.maul = maul;
	}

	public long getQuarterstaff() {
		return quarterstaff;
	}

	public void setQuarterstaff(long quarterstaff) {
		this.quarterstaff = quarterstaff;
	}

	public long getDualDaggers() {
		return dualDaggers;
	}

	public void setDualDaggers(long dualDaggers) {
		this.dualDaggers = dualDaggers;
	}

	public long getHandToHand() {
		return handToHand;
	}

	public void setHandToHand(long handToHand) {
		this.handToHand = handToHand;
	}

	public long getSword() {
		return sword;
	}

	public long getSpear() {
		return spear;
	}

	public long getGreatAxe() {
		return greatAxe;
	}

	public void setSword(long sword){
		this.sword = sword;
	}
	
	public void setSpear(long spear){
		this.spear = spear;
	}

}
