package com.glaurung.batMap.gui.corpses;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.net.CookiePolicy;
import java.util.LinkedList;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.ListModel;

import com.glaurung.batMap.io.CorpseHandlerDataPersister;

public class CorpsePanel extends JPanel implements ActionListener{

	private CorpseModel model = new CorpseModel();
	private String BASEDIR;

	public CorpsePanel(String BASEDIR) {
		this.BASEDIR=BASEDIR;
		this.model = CorpseHandlerDataPersister.load(BASEDIR);
		if(model == null){
			model = new CorpseModel();
		}else{
			loadFromModel();
		}
		
	}

	private CorpseCheckBox lichdrain = 			new CorpseCheckBox("lich drain soul",false,"lich drain",this);
	private CorpseCheckBox kharimsoul = 		new CorpseCheckBox("kharim drain soul",false,"kharim drain",this);
	private CorpseCheckBox kharimSoulCorpse=	new CorpseCheckBox("kharim dest corpse",false,null,this);
	private CorpseCheckBox tsaraksoul = 		new CorpseCheckBox("tzarak drain soul",false,"tzarak drain soul",this);
	private CorpseCheckBox ripSoulToKatana=		new CorpseCheckBox("shitKatana rip soul",false,"rip soul from corpse",this);
	private CorpseCheckBox arkemile = 			new CorpseCheckBox("necrostaff arkemile",false,"say arkemile",this);
	private CorpseCheckBox gac = 				new CorpseCheckBox("get all from corpse",false,"get all from corpse",this);
	private CorpseCheckBox ga = 				new CorpseCheckBox("get all",false,"get all",this);
	private CorpseCheckBox eatCorpse = 			new CorpseCheckBox("get and eat corpse",false,"get corpse"+getDelim()+"eat corpse",this);
	private CorpseCheckBox donate = 			new CorpseCheckBox("donate noeq and drop rest",false,"get all from corpse"+getDelim()+"donate noeq"+getDelim()+"drop noeq",this);
	private CorpseCheckBox lootCorpse = 		new CorpseCheckBox("get loot from corpse",false,"get "+getLootString()+" from corpse",this);
	private CorpseCheckBox lootGround = 		new CorpseCheckBox("get loot from ground",false,"get "+getLootString(),this);
	private CorpseCheckBox barbarianBurn = 		new CorpseCheckBox("barbarian burn corpse",false,"barbburn",this);
	private CorpseCheckBox feedCorpseTo = 		new CorpseCheckBox("feed corpse to mount",false,"get corpse"+getDelim()+"feed corpse to "+getMountName(),this);
	private CorpseCheckBox beheading = 			new CorpseCheckBox("kharim behead corpse",false,"use beheading of departed",this);
	private CorpseCheckBox desecrateGround=		new CorpseCheckBox("desecrate ground",false,"use desecrate ground",this);
	private CorpseCheckBox burialCere=			new CorpseCheckBox("burial ceremony",false,"use burial ceremony",this);
	private CorpseCheckBox wakeCorpse=			new CorpseCheckBox("",false,"",this);
	private CorpseCheckBox dig = 				new CorpseCheckBox("dig grave",false,"dig grave",this);
	private CorpseCheckBox wakeFollow=			new CorpseCheckBox("follow",false," follow",this);
	private CorpseCheckBox wakeAgro=			new CorpseCheckBox("agro",false," agro",this);
	private CorpseCheckBox wakeTalk=			new CorpseCheckBox("talk",false," talk",this);
	private CorpseCheckBox wakeStatic=			new CorpseCheckBox("static",false," static",this);
	private CorpseCheckBox lichWake = 			new CorpseCheckBox("lich wake corpse",false,"lick wake corpse",this);
	private CorpseCheckBox vampireWake=			new CorpseCheckBox("vampire wake corpse",false,"vampire wake corpse",this);
	private CorpseCheckBox skeletonWake=		new CorpseCheckBox("skeleton wake corpse",false,"skeleton wake corpse",this);
	private CorpseCheckBox zombieWake=			new CorpseCheckBox("zombie wake corpse",false,"zombie wake corpse",this);

	
	
	private static final long serialVersionUID = 1L;
	private ButtonGroup reacts = 	new ButtonGroup();
	private JRadioButton on = 	new JRadioButton("On!"); 
	private JRadioButton off =	new JRadioButton("Off");
	private JTextField delim = 	new JTextField(";;");
	private JTextField mount = new JTextField("mountName");
	
	private JButton clear = 		new JButton("Clear!");
	private JList lootLists = 	new JList();
	
	
	private String getDelim(){
		return model.getDelim();
	}
	

	private String getMountName() {
		return model.getMountHandle();
	}


	private String getLootString() {
		String loots = "";
		for (String lootItem : model.getLootList()){
			loots+=lootItem+getDelim();
		}
		return loots.substring(0, loots.length()-1);
	}

	private void saveToModel(){
		this.model.setMountHandle(mount.getSelectedText());
		this.model.setDelim(delim.getText());
		this.model.setLootList(createStringLootList());
		
		//TODO:also all the fucking checkboxes...
		
	}
	private List<String> createStringLootList() {
		LinkedList<String> list = new LinkedList<String>();
		ListModel listModel = lootLists.getModel();
		for (int i=0;i<listModel.getSize();++i){
			list.add((String) listModel.getElementAt(i));
		}
		return list;
	}


	private void loadFromModel(){
		//TODO: we populate all the crap basedon the model then
	}

	@Override
	public void actionPerformed(ActionEvent event) {
			//TODO: whatever button or thing we click, we update model and update rip_action anyway
		
		
			//TODO: specialcases, so clicking new selection clears some old
		
		
			//1 soul affecting at a time
		
			//1 corpse affecting at a time
		
			//1 waketype at a time ( maybe none)
		
			
		CorpseHandlerDataPersister.save(BASEDIR, model);
	}
	
	
}
