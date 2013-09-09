package com.glaurung.batMap.gui.corpses;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class CorpseModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	//souls = 5
	public boolean lichdrain = false;
	public boolean kharimsoul = false;
	public boolean kharimSoulCorpse=false;
	public boolean tsaraksoul = false;
	public boolean ripSoulToKatana=false; // rip soul from corpse
	public boolean arkemile = false; // soul AND corpse actually
	
	//loot = 6
	public boolean gac = false;
	public boolean ga = false;
	public boolean eatCorpse = false;
	public boolean donate = false;
	public boolean lootCorpse = false;
	public boolean lootGround = false;
	
	//corpse = 7
	public boolean barbarianBurn = false;
	public boolean feedCorpseTo = false;
	public boolean beheading = false;
	public boolean desecrateGround=false;
	public boolean burialCere=false;
	public boolean wakeCorpse=false;
	public boolean dig = false;
	
	//fucking undead wakes = 8
	public boolean wakeFollow=false;
	public boolean wakeAgro=false;
	public boolean wakeTalk=false;
	public boolean wakeStatic=false;
	public boolean lichWake = false;
	public boolean vampireWake=false;
	public boolean skeletonWake=false;
	public boolean zombieWake=false;
	
	private String delim=";";
	private String mountHandle="snowman";
	private List<String> lootList = new LinkedList<String>();

	public void updateModel(boolean[] modes, String delim, String mount, List<String> lootList){
		if (modes.length == 27){
			lichdrain = modes[0];
			kharimsoul = modes[1];
			kharimSoulCorpse = modes[2];
			tsaraksoul = modes[3];
			ripSoulToKatana=modes[4];
			arkemile = modes[5];
			gac = modes[6];
			ga = modes[7];
			eatCorpse = modes[8];
			donate = modes[9];
			lootCorpse = modes[10];
			lootGround = modes[11];
			barbarianBurn = modes[12];
			feedCorpseTo = modes[13];
			beheading = modes[14];
			desecrateGround=modes[15];
			burialCere=modes[16];
			wakeCorpse=modes[17];
			dig = modes[18];
			wakeFollow=modes[19];
			wakeAgro=modes[20];
			wakeTalk=modes[21];
			wakeStatic=modes[22];
			lichWake = modes[23];
			vampireWake=modes[24];
			skeletonWake=modes[25];
			zombieWake=modes[26];
		}
		this.delim=delim;
		this.mountHandle=mount;
		this.setLootList(lootList);
	}

	public String getDelim() {
		return delim;
	}

	public void setDelim(String delim) {
		this.delim = delim;
	}

	public String getMountHandle() {
		return mountHandle;
	}

	public void setMountHandle(String mountHandle) {
		this.mountHandle = mountHandle;
	}

	public List<String> getLootList() {
		return lootList;
	}

	public void setLootList(List<String> lootList) {
		this.lootList = lootList;
	}
	
}
