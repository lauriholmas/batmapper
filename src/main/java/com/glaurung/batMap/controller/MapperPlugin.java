package com.glaurung.batMap.controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.glaurung.batMap.gui.corpses.CorpsePanel;
import com.glaurung.batMap.gui.manual.ManualPanel;
import com.glaurung.batMap.gui.search.SearchPanel;
import com.glaurung.batMap.io.AreaDataPersister;
import com.glaurung.batMap.io.GuiDataPersister;
import com.glaurung.batMap.vo.GuiData;
import com.mythicscape.batclient.interfaces.BatClientPlugin;
import com.mythicscape.batclient.interfaces.BatClientPluginTrigger;
import com.mythicscape.batclient.interfaces.BatClientPluginUtil;
import com.mythicscape.batclient.interfaces.BatWindow;
import com.mythicscape.batclient.interfaces.ParsedResult;


public class MapperPlugin extends BatClientPlugin implements BatClientPluginTrigger, ActionListener, BatClientPluginUtil {

    protected static final String MAKERIPACTION = "rip_action set ";
    protected static final String RIPACTION_OFF = "rip_action off";
    protected static final String RIPACTION_ON = "rip_action on";
    protected static final String COMMAND_ADD_LABEL = "add";
    protected static final String COMMAND_REMOVE_LABEL = "del";
    protected static final String COMMAND_RUN_TO_LABEL = "run";
    protected static final String COMMAND_LIST_LABELS = "list";
    protected static final String COMMAND_APPEND_TO_NOTES = "append";
    protected static final String COMMAND_FIND_DESC = "find";
    
    private MapperEngine engine;
    private SearchEngine searchEngine;
    private SearchPanel searchPanel;
    private final String CHANNEL_PREFIX = "BAT_MAPPER";
    //batMap;areaname;roomUID;exitUsed;indoor boolean;shortDesc;longDesc;exits
    private final int PREFIX = 0;
    private final int AREA_NAME = 1;
    private final int ROOM_ID = 2;
    private final int EXIT_USED = 3;
    private final int IS_INDOORS = 4;
    private final int SHORT_DESC = 5;
    private final int LONG_DESC = 6;
    private final int EXITS = 7;

    private final int MESSAGE_LENGTH = 9;
    private final int EXIT_AREA_LENGTH = 2;

    private final String EXIT_AREA_MESSAGE = "REALM_MAP";
    private String BASEDIR = null;


    public void loadPlugin() {
        BASEDIR = this.getBaseDirectory();
        GuiData guiData = GuiDataPersister.load( BASEDIR );

        BatWindow clientWin;
        if (guiData != null) {
            clientWin = this.getClientGUI().createBatWindow( "Mapper", guiData.getX(), guiData.getY(), guiData.getWidth(), guiData.getHeight() );
        } else {
            clientWin = this.getClientGUI().createBatWindow( "Mapper", 300, 300, 820, 550 );
        }

        engine = new MapperEngine(this);
        searchEngine = new SearchEngine(this);
        engine.setBatWindow( clientWin );
        searchEngine.setBatWindow( clientWin );
        clientWin.removeTabAt( 0 );
        clientWin.newTab( "batMap", engine.getPanel() );
        CorpsePanel corpses = new CorpsePanel( BASEDIR, this );
        engine.setCorpsePanel( corpses );
        clientWin.newTab( "Corpses", corpses);
        clientWin.newTab( "manual", new ManualPanel() );
        searchPanel = new SearchPanel(searchEngine);
        clientWin.newTab( "map search", searchPanel);
        clientWin.setVisible( true );
        this.getPluginManager().addProtocolListener( this );
        AreaDataPersister.migrateFilesToNewLocation( BASEDIR );
        engine.setBaseDir( BASEDIR );
        searchEngine.setBaseDir( BASEDIR );
        clientWin.addComponentListener( engine );

    }

    @Override
    public String getName() {
        return "batMap";
    }

    //	ArrayList<BatClientPlugin> plugins=this.getPluginManager().getPlugins();
    @Override
    public ParsedResult trigger( ParsedResult input ) {
        if (input.getStrippedText().startsWith( "SAVED." )) {
            this.engine.save();
        }
        return null;
    }

    @Override
    public void actionPerformed( ActionEvent event ) {
        /**
         *
         received 99 protocol: cMapper;;sunderland;;$apr1$dF!!_X#W$v3dsdL2khaffFpj1BvVrD0;;road;;0;;The long road to Sunderland;;You see a long road stretching northward into the distance. As far as
         you can tell, the way ahead looks clear.
         ;;north,south;;

         event data amount: 9

         */

        //cMapper;areaname;roomUID;exitUsed;indoor boolean;shortDesc;longDesc;exits
        String input = event.getActionCommand();
        String[] values = input.split( ";;", - 1 );
        if (values[PREFIX].equals( CHANNEL_PREFIX ) && values.length == MESSAGE_LENGTH) {
//			System.out.println("\n\n\nvalid input: " +input);
            String areaName = values[AREA_NAME];
            String roomUID = values[ROOM_ID];
            String exitUsed = values[EXIT_USED];
            boolean indoors = convertToBoolean( values[IS_INDOORS] );
            String shortDesc = values[SHORT_DESC];
            String longDesc = values[LONG_DESC].replaceAll( "\n", " " );
            HashSet<String> exits = new HashSet<String>( Arrays.asList( values[EXITS].split( "," ) ) );
            this.engine.moveToRoom( areaName, roomUID, exitUsed, indoors, shortDesc, longDesc, exits );
            this.searchEngine.setMapperArea( areaName );
        } else if (values[PREFIX].equals( CHANNEL_PREFIX ) && values.length != EXIT_AREA_LENGTH) {
//			System.out.println("\n\n\nBROKEN HIDEOUS INPUT: "+input);
        } else if (values[PREFIX].equals( CHANNEL_PREFIX ) && values.length == EXIT_AREA_LENGTH) {
//			System.out.println("\n\n\nexiting area: "+input);
            if (values[AREA_NAME].equals( EXIT_AREA_MESSAGE )) {
                this.engine.moveToArea( null );
                this.searchEngine.setMapperArea( null );
            }
        }

    }

    private boolean convertToBoolean( String oneOrZero ) {
        if (new Integer( oneOrZero ) == 0)
            return false;
        return true;

    }

    @Override
    public void clientExit() {
        this.engine.save();

    }

    public void saveRipAction( String ripString ) {
        this.getClientGUI().doCommand( MAKERIPACTION + ripString );
    }

    public void toggleRipAction( boolean mode ) {
        if (mode) {
            this.getClientGUI().doCommand( RIPACTION_ON );
        } else {
            this.getClientGUI().doCommand( RIPACTION_OFF );
        }
    }

    public void doCommand( String string ) {
        this.getClientGUI().doCommand( string );

    }

    @Override
    public void process(Object input){
        if ( input == null){
            printConsoleMessage("Mapper has following commands:");
            printConsoleMessage(String.format("\t%s <label> - to add label to current room",COMMAND_ADD_LABEL));
            printConsoleMessage(String.format("\t%s <label> - to run to room with that label ( need to set delim in corpsepanel)", COMMAND_RUN_TO_LABEL));
            printConsoleMessage(String.format("\t%s         - to remove label from current room",COMMAND_REMOVE_LABEL));
            printConsoleMessage(String.format("\t%s        - to list labels and rooms",COMMAND_LIST_LABELS));
            printConsoleMessage(String.format("\t%s        - to append a line to roomnotes",COMMAND_APPEND_TO_NOTES));
            printConsoleMessage(String.format("\t%s <desc> - to find rooms by long desc",COMMAND_FIND_DESC));
        }
        if(input instanceof String){
            String[] params = ((String)input).split(" ");
            if(params.length == 1){
                String command = params[0];
                if(command.equalsIgnoreCase(COMMAND_REMOVE_LABEL)){
                    this.engine.removeLabelFromCurrent();
                }else if(command.equalsIgnoreCase(COMMAND_LIST_LABELS)){
                    for(String entry : this.engine.getLabels()){
                        printConsoleMessage(entry);
                    }
                }else{
                    printConsoleError(String.format("unknown command: [%s]", command));
                }
            }else if(params.length== 2){
                String command = params[0];
                String label = params[1];
                if(command.equalsIgnoreCase(COMMAND_ADD_LABEL)){
                    if( ! this.engine.roomLabelExists(label)){
                        this.engine.setLabelToCurrentRoom(label);
                        printConsoleMessage(String.format("added label [%s] to this room", label));
                    }else{
                        printConsoleError(String.format("label [%s] already exists, must be unique per area", label));
                    }
                }else if(command.equalsIgnoreCase(COMMAND_RUN_TO_LABEL)){
                    printConsoleMessage(String.format("running to room [%s]", label));
                    if(this.engine.roomLabelExists(label)){
                        this.engine.runtoLabel(label);
                    }else{
                        printConsoleError(String.format("label [%s] not found", label));
                    }
                }else if(command.equalsIgnoreCase( COMMAND_APPEND_TO_NOTES )){
                    printConsoleMessage(String.format("Appending to notes [%s]", label));
                    this.engine.getPanel().appentToNotes( label );
                }else{
                    printConsoleError(String.format("unknown command: [%s]", command));
                }

            }else if(params.length > 2){
                String command = params[0];
                if(command.equalsIgnoreCase( COMMAND_APPEND_TO_NOTES )){
                    String notes = ( (String) input ).substring( COMMAND_APPEND_TO_NOTES.length() );
                    this.engine.getPanel().appentToNotes( notes );
                }else if(command.equalsIgnoreCase( COMMAND_FIND_DESC )){
                    String findSring = ( (String) input ).substring( COMMAND_FIND_DESC.length() ).trim();
                    searchPanel.setSearchText(findSring);
                    List<String> rooms = searchPanel.searchForRoomsWith(findSring);
                    for(String room:  rooms) {
                        printConsoleMessage(room);
                    }
                }
                else{
                    printConsoleError(String.format("unknown command: [%s] or too many params, slow down!", command));
                }

            }
            else{
                printConsoleError("only 1 or 2 or KAZILLION params accepted");
            }
        }
    }

    private void printConsoleError(String msg){
        this.getClientGUI().printText("general","[Mapper error] "+msg+"\n", "F7856D");
    }
    private void printConsoleMessage(String msg){
        this.getClientGUI().printText("general","[Mapper] "+msg+"\n", "6AFA63");
    }
}


