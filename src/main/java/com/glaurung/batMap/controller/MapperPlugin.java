package com.glaurung.batMap.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;

import com.glaurung.batMap.gui.ManualPanel;
import com.glaurung.batMap.gui.corpses.CorpsePanel;
import com.glaurung.batMap.io.AreaDataPersister;
import com.glaurung.batMap.io.CorpseHandlerDataPersister;
import com.glaurung.batMap.io.GuiDataPersister;
import com.glaurung.batMap.vo.GuiData;
import com.mythicscape.batclient.interfaces.BatClientPlugin;
import com.mythicscape.batclient.interfaces.BatClientPluginTrigger;
import com.mythicscape.batclient.interfaces.BatClientPluginUtil;
import com.mythicscape.batclient.interfaces.BatWindow;
import com.mythicscape.batclient.interfaces.ParsedResult;



public class MapperPlugin extends BatClientPlugin implements BatClientPluginTrigger, ActionListener, BatClientPluginUtil{

	protected static final String MAKERIPACTION = "rip_action set ";
	protected static final String RIPACTION_OFF = "rip_action off";
	protected static final String RIPACTION_ON = "rip_action on";
	private MapperEngine engine;
	private final String CHANNEL_PREFIX="BAT_MAPPER";
	//batMap;areaname;roomUID;exitUsed;indoor boolean;shortDesc;longDesc;exits
	private final int PREFIX=0;
	private final int AREA_NAME=1;
	private final int ROOM_ID=2;
	private final int EXIT_USED=3;
	private final int IS_INDOORS=4;
	private final int SHORT_DESC=5;
	private final int LONG_DESC=6;
	private final int EXITS=7;
	
	private final int MESSAGE_LENGTH=9;
	private final int EXIT_AREA_LENGTH=2;
	
	private final String EXIT_AREA_MESSAGE="REALM_MAP";
	private String BASEDIR = null;


	public void loadPlugin() {
		BASEDIR = this.getBaseDirectory();
		GuiData guiData = GuiDataPersister.load(BASEDIR);
		
		BatWindow clientWin;
		if(guiData != null){
			clientWin = this.getClientGUI().createBatWindow("Mapper", guiData.getX(), guiData.getY(),guiData.getWidth(), guiData.getHeight());
		}else{
			clientWin = this.getClientGUI().createBatWindow("Mapper", 300, 300,820, 550);
		}
		
		engine = new MapperEngine();
		engine.setBatWindow(clientWin);
		clientWin.removeTabAt(0);
		clientWin.newTab("batMap", engine.getPanel());
		clientWin.newTab("Corpses", new CorpsePanel(BASEDIR, this));
		clientWin.newTab("manual", new ManualPanel());
		clientWin.setVisible(true);
		this.getPluginManager().addProtocolListener(this);
		AreaDataPersister.migrateFilesToNewLocation(BASEDIR);
		engine.setBaseDir(BASEDIR);
		clientWin.addComponentListener(engine);
		

	}

	@Override
	public String getName() {
		return "batMap";
	}
//	ArrayList<BatClientPlugin> plugins=this.getPluginManager().getPlugins();
	@Override
	public ParsedResult trigger(ParsedResult input) {
		if(input.getStrippedText().startsWith("SAVED.")){
			this.engine.save();
		}
		return null;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		/**
		 * 
received 99 protocol: cMapper;;sunderland;;$apr1$dF!!_X#W$v3dsdL2khaffFpj1BvVrD0;;road;;0;;The long road to Sunderland;;You see a long road stretching northward into the distance. As far as
you can tell, the way ahead looks clear.
;;north,south;;

event data amount: 9

		 */
		
		//batMap;areaname;roomUID;exitUsed;indoor boolean;shortDesc;longDesc;exits
		String input = event.getActionCommand();
		String[] values = input.split(";;",-1);
		if(values[PREFIX].equals(CHANNEL_PREFIX) && values.length == MESSAGE_LENGTH){
//			System.out.println("\n\n\nvalid input\n"+input);
			String areaName = values[AREA_NAME];
			String roomUID = values[ROOM_ID];
			String exitUsed = values[EXIT_USED];
			boolean indoors = convertToBoolean(values[IS_INDOORS]);
			String shortDesc = values[SHORT_DESC];
			String longDesc = values[LONG_DESC].replaceAll("\n", " ");
			HashSet<String> exits = new HashSet<String>(Arrays.asList(values[EXITS].split(",")));
			this.engine.moveToRoom(areaName, roomUID, exitUsed,indoors, shortDesc, longDesc, exits);
		}else if(values[PREFIX].equals(CHANNEL_PREFIX) && values.length !=EXIT_AREA_LENGTH){
//			System.out.println("\n\n\nBROKEN HIDEOUS INPUT\n"+input);
		}else if(values[PREFIX].equals(CHANNEL_PREFIX) && values.length == EXIT_AREA_LENGTH){
			if(values[AREA_NAME].equals(EXIT_AREA_MESSAGE)){
				this.engine.moveToArea(null);
			}
		}
		
	}
	
	private boolean convertToBoolean(String oneOrZero){
		if(new Integer(oneOrZero) == 0)
			return false;
		return true;
		
	}

	@Override
	public void clientExit() {
		this.engine.save();
		
	}

	public void saveRipAction(String ripString) {
		this.getClientGUI().doCommand(MAKERIPACTION+ripString);
	}
	public void toggleRipAction(boolean mode){
		if(mode){
			this.getClientGUI().doCommand(RIPACTION_ON);
		}else{
			this.getClientGUI().doCommand(RIPACTION_OFF);
		}
	}

	public void doCommand(String string) {
		this.getClientGUI().doCommand(string);
		
	}

}
