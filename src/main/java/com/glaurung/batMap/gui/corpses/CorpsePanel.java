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

import com.glaurung.batMap.controller.MapperPlugin;
import com.glaurung.batMap.io.CorpseHandlerDataPersister;

public class CorpsePanel extends JPanel implements ActionListener{

	private CorpseModel model = new CorpseModel();
	private String BASEDIR;
	private MapperPlugin plugin;

	public CorpsePanel(String BASEDIR, MapperPlugin plugin) {
		this.BASEDIR=BASEDIR;
		this.model = CorpseHandlerDataPersister.load(BASEDIR);
		if(model == null){
			model = new CorpseModel();
		}else{
			loadFromModel();
		}
		this.plugin = plugin;
		//TODO: next up, the whole damn layout... somehow....
		delim.addActionListener(this);
//		lootLists.addC
		//TODO: add listener for the textfields and lootlists to update the effects on the checkboxes that rely on them
	}

	//TODO: also add aelena familiar drain corpse and aelena fam fetch organ
	
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
			loots+=lootItem+",";
		}
		return loots.substring(0, loots.length());
	}

	private void saveToModel(){
		this.model.setMountHandle(mount.getText());
		this.model.setDelim(delim.getText());
		this.model.setLootList(createStringLootList());
		this.model.lichdrain = lichdrain.isSelected();
		this.model.kharimsoul = kharimsoul.isSelected();
		this.model.kharimSoulCorpse = kharimSoulCorpse.isSelected();
		this.model.tsaraksoul = tsaraksoul.isSelected();
		this.model.ripSoulToKatana=ripSoulToKatana.isSelected();
		this.model.arkemile = arkemile.isSelected();
		this.model.gac = gac.isSelected();
		this.model.ga =ga.isSelected();
		this.model.eatCorpse = eatCorpse.isSelected();
		this.model.donate = donate.isSelected();
		this.model.lootCorpse = lootCorpse.isSelected();
		this.model.lootGround = lootGround.isSelected();
		this.model.barbarianBurn = barbarianBurn.isSelected();
		this.model.feedCorpseTo = feedCorpseTo.isSelected();
		this.model.beheading = beheading.isSelected();
		this.model.desecrateGround=desecrateGround.isSelected();
		this.model.burialCere=burialCere.isSelected();
		this.model.wakeCorpse=wakeCorpse.isSelected();
		this.model.dig = dig.isSelected();
		this.model.wakeFollow=wakeFollow.isSelected();
		this.model.wakeAgro=wakeAgro.isSelected();
		this.model.wakeTalk=wakeTalk.isSelected();
		this.model.wakeStatic=wakeStatic.isSelected();
		this.model.lichWake = lichWake.isSelected();
		this.model.vampireWake=vampireWake.isSelected();
		this.model.skeletonWake=skeletonWake.isSelected();
		this.model.zombieWake=zombieWake.isSelected();
		CorpseHandlerDataPersister.save(BASEDIR, this.model);
		
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
		
		
		lichdrain.setSelected(this.model.lichdrain);
		kharimsoul.setSelected(this.model.kharimsoul);
		kharimSoulCorpse.setSelected(this.model.kharimSoulCorpse);
		tsaraksoul.setSelected(this.model.tsaraksoul);
		ripSoulToKatana.setSelected(this.model.ripSoulToKatana);
		arkemile.setSelected(this.model.arkemile);
		gac.setSelected(this.model.gac);
		ga.setSelected(this.model.ga);
		eatCorpse.setSelected(this.model.eatCorpse);
		donate.setSelected(this.model.donate);
		lootCorpse.setSelected(this.model.lootCorpse);
		lootGround.setSelected(this.model.lootGround);
		barbarianBurn.setSelected(this.model.barbarianBurn);
		feedCorpseTo.setSelected(this.model.feedCorpseTo);
		beheading.setSelected(this.model.beheading);
		desecrateGround.setSelected(this.model.desecrateGround);
		burialCere.setSelected(this.model.burialCere);
		wakeCorpse.setSelected(this.model.wakeCorpse);
		dig.setSelected(this.model.dig);
		wakeFollow.setSelected(this.model.wakeFollow);
		wakeAgro.setSelected(this.model.wakeAgro);
		wakeTalk.setSelected(this.model.wakeTalk);
		wakeStatic.setSelected(this.model.wakeStatic);
		lichWake.setSelected(this.model.lichWake);
		vampireWake.setSelected(this.model.vampireWake);
		skeletonWake.setSelected(this.model.skeletonWake);
		zombieWake.setSelected(this.model.zombieWake);
		
		mount.setText(this.model.getMountHandle());
		delim.setText(this.model.getDelim());
		lootLists.setListData(this.model.getLootList().toArray());
	}

	@Override
	public void actionPerformed(ActionEvent event) {
			Object source = event.getSource();
			if(source == on){
				this.plugin.toggleRipAction(true);
			}else if(source == off){
				this.plugin.toggleRipAction(false);
			}else if (source == kharimSoulCorpse){
				this.plugin.doCommand("kharim set corpseDest foobar");
			}else if(source == lichdrain){
				turnOff(kharimsoul,kharimsoul,ripSoulToKatana,arkemile);
			}else if(source == kharimsoul){
				turnOff(lichdrain,tsaraksoul,ripSoulToKatana,arkemile);
			}else if(source == tsaraksoul){
				turnOff(lichdrain,kharimsoul,ripSoulToKatana,arkemile);
			}else if(source == ripSoulToKatana){
				turnOff(lichdrain,kharimsoul,tsaraksoul,arkemile);
			}else if(source == arkemile){
				turnOff(lichdrain,kharimsoul,tsaraksoul,ripSoulToKatana, eatCorpse, barbarianBurn, feedCorpseTo, 
						beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, zombieWake);
			}else if(source == eatCorpse){
				turnOff(arkemile,eatCorpse, ripSoulToKatana, barbarianBurn, feedCorpseTo,beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, zombieWake);
			}else if(source == barbarianBurn){
				turnOff(arkemile, eatCorpse,ripSoulToKatana, feedCorpseTo,beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, zombieWake);
			}else if(source == feedCorpseTo){
				turnOff(arkemile,eatCorpse, ripSoulToKatana, barbarianBurn,beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, zombieWake);
			}else if(source == beheading){
				turnOff(arkemile,eatCorpse, ripSoulToKatana, barbarianBurn, feedCorpseTo, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, zombieWake);
			}else if(source == desecrateGround){
				turnOff(arkemile, eatCorpse,ripSoulToKatana, barbarianBurn, feedCorpseTo,beheading,burialCere, lichWake, skeletonWake, vampireWake, zombieWake);
			}else if(source == burialCere){
				turnOff(arkemile,eatCorpse, ripSoulToKatana, barbarianBurn, feedCorpseTo,beheading, desecrateGround,  lichWake, skeletonWake, vampireWake, zombieWake);
			}else if(source == lichWake){
				turnOff(arkemile, eatCorpse,ripSoulToKatana, barbarianBurn, feedCorpseTo,beheading, desecrateGround, burialCere, skeletonWake, vampireWake, zombieWake);
			}else if(source == vampireWake){
				turnOff(arkemile, eatCorpse,ripSoulToKatana, barbarianBurn, feedCorpseTo,beheading, desecrateGround, burialCere, lichWake, skeletonWake, zombieWake);
			}else if(source == skeletonWake){
				turnOff(arkemile,eatCorpse, ripSoulToKatana, barbarianBurn, feedCorpseTo,beheading, desecrateGround, burialCere, lichWake,  vampireWake, zombieWake);
			}else if(source == zombieWake){
				turnOff(arkemile,eatCorpse, ripSoulToKatana, barbarianBurn, feedCorpseTo,beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake);
			}else if (source == wakeFollow){
				turnOff( wakeAgro, wakeTalk, wakeStatic);
			}else if (source == wakeAgro){
				turnOff(wakeFollow,  wakeTalk, wakeStatic);
			}else if (source == wakeTalk){
				turnOff(wakeFollow, wakeAgro,  wakeStatic);
			}else if (source == wakeStatic){
				turnOff(wakeFollow, wakeAgro, wakeTalk);
			}

			saveToModel();
			plugin.saveRipAction(makeRipString());
	}

	private void turnOff(CorpseCheckBox ... boxes){
		for(CorpseCheckBox box : boxes){
			box.setSelected(false);
		}
	}

	private String makeRipString() {
		String rip = "";
		
		if(lichdrain.isSelected()){
			rip+=lichdrain.getEffect()+this.model.getDelim();
		}
		if(kharimsoul.isSelected()){
			rip+=kharimsoul.getEffect()+this.model.getDelim();
		}
		if(kharimSoulCorpse.isSelected()){
			rip+=kharimSoulCorpse.getEffect()+this.model.getDelim();
		}
		if(tsaraksoul.isSelected()){
			rip+=tsaraksoul.getEffect()+this.model.getDelim();
		}
		if(ripSoulToKatana.isSelected()){
			rip+=ripSoulToKatana.getEffect()+this.model.getDelim();
		}
		if(arkemile.isSelected()){
			rip+=arkemile.getEffect()+this.model.getDelim();
		}
		if(gac.isSelected()){
			rip+=gac.getEffect()+this.model.getDelim();
		}
		if(ga.isSelected()){
			rip+=ga.getEffect()+this.model.getDelim();
		}
		if(eatCorpse.isSelected()){
			rip+=eatCorpse.getEffect()+this.model.getDelim();
		}
		if(donate.isSelected()){
			rip+=donate.getEffect()+this.model.getDelim();
		}
		if(lootCorpse.isSelected()){
			rip+=lootCorpse.getEffect()+this.model.getDelim();
		}
		if(lootGround.isSelected()){
			rip+=lootGround.getEffect()+this.model.getDelim();
		}
		if(barbarianBurn.isSelected()){
			rip+=barbarianBurn.getEffect()+this.model.getDelim();
		}
		if(feedCorpseTo.isSelected()){
			rip+=feedCorpseTo.getEffect()+this.model.getDelim();
		}
		if(beheading.isSelected()){
			rip+=beheading.getEffect()+this.model.getDelim();
		}
		if(desecrateGround.isSelected()){
			rip+=desecrateGround.getEffect()+this.model.getDelim();
		}
		if(burialCere.isSelected()){
			rip+=burialCere.getEffect()+this.model.getDelim();
		}
		if(wakeCorpse.isSelected()){
			rip+=wakeCorpse.getEffect()+this.model.getDelim();
		}
		if(dig.isSelected()){
			rip+=dig.getEffect()+this.model.getDelim();
		}
		if(wakeFollow.isSelected()){
			rip+=wakeFollow.getEffect()+this.model.getDelim();
		}
		if(wakeAgro.isSelected()){
			rip+=wakeAgro.getEffect()+this.model.getDelim();
		}
		if(wakeTalk.isSelected()){
			rip+=wakeTalk.getEffect()+this.model.getDelim();
		}
		if(wakeStatic.isSelected()){
			rip+=wakeStatic.getEffect()+this.model.getDelim();
		}
		if(lichWake.isSelected()){
			rip+=lichWake.getEffect()+this.model.getDelim();
		}
		if(vampireWake.isSelected()){
			rip+=vampireWake.getEffect()+this.model.getDelim();
		}
		if(skeletonWake.isSelected()){
			rip+=skeletonWake.getEffect()+this.model.getDelim();
		}
		if(zombieWake.isSelected()){
			rip+=zombieWake.getEffect()+this.model.getDelim();
		}
		
		// TODO Auto-generated method stub
		return null;
	}
	
	
}
