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
	
	//corpse = 9
	public boolean barbarianBurn = false;
	public boolean feedCorpseTo = false;
	public boolean beheading = false;
	public boolean desecrateGround=false;
	public boolean burialCere=false;
	public boolean wakeCorpse=false;
	public boolean dig = false;
	public boolean aelenaOrgan=false;
	public boolean aelenaFam = false;
	
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
	private String organ1="";
	private String organ2="";

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

	public void clear() {
		lichdrain = false;
		kharimsoul = false;
		kharimSoulCorpse = false;
		tsaraksoul = false;
		ripSoulToKatana=false;
		arkemile = false;
		gac = false;
		ga = false;
		eatCorpse = false;
		donate = false;
		lootCorpse = false;
		lootGround = false;
		barbarianBurn = false;
		feedCorpseTo = false;
		beheading = false;
		desecrateGround=false;
		burialCere=false;
		wakeCorpse=false;
		dig = false;
		wakeFollow=false;
		wakeAgro=false;
		wakeTalk=false;
		wakeStatic=false;
		lichWake = false;
		vampireWake=false;
		skeletonWake=false;
		zombieWake=false;
		aelenaFam=false;
		aelenaOrgan=false;
		
		delim="";
		mountHandle="";
		lootList = new LinkedList<String>();
		organ1="";
		organ2="";
		
	}

	public String getOrgan1() {
		return organ1;
	}

	public String getOrgan2() {
		return organ2;
	}

	public void setOrgan1(String organ) {
		this.organ1=organ;	
	}

	public void setOrgan2(String organ) {
		this.organ2=organ;

	}
	
}
