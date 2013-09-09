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
	private boolean lichdrain = false;
	private boolean kharimsoul = false;
	private boolean kharimSoulCorpse=false;
	private boolean tsaraksoul = false;
	private boolean ripSoulToKatana=false; // rip soul from corpse
	private boolean arkemile = false; // soul AND corpse actually
	
	//loot = 6
	private boolean gac = false;
	private boolean ga = false;
	private boolean eatCorpse = false;
	private boolean donate = false;
	private boolean lootCorpse = false;
	private boolean lootGround = false;
	
	//corpse = 7
	private boolean barbarianBurn = false;
	private boolean feedCorpseTo = false;
	private boolean beheading = false;
	private boolean desecrateGround=false;
	private boolean burialCere=false;
	private boolean wakeCorpse=false;
	private boolean dig = false;
	
	//fucking undead wakes = 8
	private boolean wakeFollow=false;
	private boolean wakeAgro=false;
	private boolean wakeTalk=false;
	private boolean wakeStatic=false;
	private boolean lichWake = false;
	private boolean vampireWake=false;
	private boolean skeletonWake=false;
	private boolean zombieWake=false;
	
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

	public boolean isLichdrain() {
		return lichdrain;
	}

	public void setLichdrain(boolean lichdrain) {
		this.lichdrain = lichdrain;
	}

	public boolean isKharimsoul() {
		return kharimsoul;
	}

	public void setKharimsoul(boolean kharimsoul) {
		this.kharimsoul = kharimsoul;
	}

	public boolean isKharimSoulCorpse() {
		return kharimSoulCorpse;
	}

	public void setKharimSoulCorpse(boolean kharimSoulCorpse) {
		this.kharimSoulCorpse = kharimSoulCorpse;
	}

	public boolean isTsaraksoul() {
		return tsaraksoul;
	}

	public void setTsaraksoul(boolean tsaraksoul) {
		this.tsaraksoul = tsaraksoul;
	}

	public boolean isRipSoulToKatana() {
		return ripSoulToKatana;
	}

	public void setRipSoulToKatana(boolean ripSoulToKatana) {
		this.ripSoulToKatana = ripSoulToKatana;
	}

	public boolean isArkemile() {
		return arkemile;
	}

	public void setArkemile(boolean arkemile) {
		this.arkemile = arkemile;
	}

	public boolean isGac() {
		return gac;
	}

	public void setGac(boolean gac) {
		this.gac = gac;
	}

	public boolean isGa() {
		return ga;
	}

	public void setGa(boolean ga) {
		this.ga = ga;
	}

	public boolean isEatCorpse() {
		return eatCorpse;
	}

	public void setEatCorpse(boolean eatCorpse) {
		this.eatCorpse = eatCorpse;
	}

	public boolean isDonate() {
		return donate;
	}

	public void setDonate(boolean donate) {
		this.donate = donate;
	}

	public boolean isLootCorpse() {
		return lootCorpse;
	}

	public void setLootCorpse(boolean lootCorpse) {
		this.lootCorpse = lootCorpse;
	}

	public boolean isLootGround() {
		return lootGround;
	}

	public void setLootGround(boolean lootGround) {
		this.lootGround = lootGround;
	}

	public boolean isBarbarianBurn() {
		return barbarianBurn;
	}

	public void setBarbarianBurn(boolean barbarianBurn) {
		this.barbarianBurn = barbarianBurn;
	}

	public boolean isFeedCorpseTo() {
		return feedCorpseTo;
	}

	public void setFeedCorpseTo(boolean feedCorpseTo) {
		this.feedCorpseTo = feedCorpseTo;
	}

	public boolean isBeheading() {
		return beheading;
	}

	public void setBeheading(boolean beheading) {
		this.beheading = beheading;
	}

	public boolean isDesecrateGround() {
		return desecrateGround;
	}

	public void setDesecrateGround(boolean desecrateGround) {
		this.desecrateGround = desecrateGround;
	}

	public boolean isBurialCere() {
		return burialCere;
	}

	public void setBurialCere(boolean burialCere) {
		this.burialCere = burialCere;
	}

	public boolean isWakeCorpse() {
		return wakeCorpse;
	}

	public void setWakeCorpse(boolean wakeCorpse) {
		this.wakeCorpse = wakeCorpse;
	}

	public boolean isDig() {
		return dig;
	}

	public void setDig(boolean dig) {
		this.dig = dig;
	}

	public boolean isWakeFollow() {
		return wakeFollow;
	}

	public void setWakeFollow(boolean wakeFollow) {
		this.wakeFollow = wakeFollow;
	}

	public boolean isWakeAgro() {
		return wakeAgro;
	}

	public void setWakeAgro(boolean wakeAgro) {
		this.wakeAgro = wakeAgro;
	}

	public boolean isWakeTalk() {
		return wakeTalk;
	}

	public void setWakeTalk(boolean wakeTalk) {
		this.wakeTalk = wakeTalk;
	}

	public boolean isWakeStatic() {
		return wakeStatic;
	}

	public void setWakeStatic(boolean wakeStatic) {
		this.wakeStatic = wakeStatic;
	}

	public boolean isLichWake() {
		return lichWake;
	}

	public void setLichWake(boolean lichWake) {
		this.lichWake = lichWake;
	}

	public boolean isVampireWake() {
		return vampireWake;
	}

	public void setVampireWake(boolean vampireWake) {
		this.vampireWake = vampireWake;
	}

	public boolean isSkeletonWake() {
		return skeletonWake;
	}

	public void setSkeletonWake(boolean skeletonWake) {
		this.skeletonWake = skeletonWake;
	}

	public boolean isZombieWake() {
		return zombieWake;
	}

	public void setZombieWake(boolean zombieWake) {
		this.zombieWake = zombieWake;
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
