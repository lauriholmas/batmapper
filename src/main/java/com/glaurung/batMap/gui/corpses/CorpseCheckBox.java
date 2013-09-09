package com.glaurung.batMap.gui.corpses;

import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

public class CorpseCheckBox extends JCheckBox{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String effect;
	
	
	public CorpseCheckBox(String label, boolean defaultMode, String effect, ActionListener listener){
		super(label, defaultMode);
		this.setEffect(effect);
		this.addActionListener(listener);
	}


	public String getEffect() {
		return effect;
	}


	public void setEffect(String effect) {
		this.effect = effect;
	}
	
}
