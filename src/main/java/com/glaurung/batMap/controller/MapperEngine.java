package com.glaurung.batMap.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.SwingUtilities;

import com.glaurung.batMap.gui.*;
import com.glaurung.batMap.gui.corpses.CorpsePanel;
import com.glaurung.batMap.io.AreaDataPersister;
import com.glaurung.batMap.io.GuiDataPersister;
import com.glaurung.batMap.vo.Area;
import com.glaurung.batMap.vo.AreaSaveObject;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;
import com.glaurung.batMap.web.MapState;
import com.mythicscape.batclient.interfaces.BatWindow;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;

/**
 * Holds the map of the current area and all operations on it. The map itself is drawn by the web map,
 * which gets the map contents through getState() and is notified of changes through change listeners.
 * Public methods are synchronized since they are called both from batclient and from the web server threads.
 */
public class MapperEngine implements ComponentListener {

    private static final double ROOM_OVERLAP_DISTANCE = 60;
    private static final String DEFAULT_DELIM = ";";

    SparseMultigraph<Room, Exit> graph;
    Map<Room, Point2D> locations = new HashMap<Room, Point2D>();
    Room currentRoom = null;
    Area area = null;

    String baseDir;
    BatWindow batWindow;
    boolean snapMode = true;
    CorpsePanel corpsePanel;
    MapperPlugin plugin;
    boolean mazemode = false;
    boolean reversableDirsMode = false;
    private final List<Runnable> changeListeners = new CopyOnWriteArrayList<Runnable>();
    private long version = 0;

    public MapperEngine( SparseMultigraph<Room, Exit> graph, MapperPlugin plugin ) {
        this(plugin);
        this.graph = graph;
    }


    public MapperEngine(MapperPlugin plugin) {
        this.plugin = plugin;
        graph = new SparseMultigraph<Room, Exit>();
    }

    /**
     * name the web map uses for this map view
     */
    public String getViewName() {
        return "map";
    }

    public void addChangeListener( Runnable listener ) {
        changeListeners.add( listener );
    }

    /**
     * Tells the web map to redraw
     */
    protected void fireChanged() {
        version++;
        for (Runnable listener : changeListeners) {
            listener.run();
        }
    }

    //areaname;roomUID;exitUsed;indoor boolean;shortDesc;longDesc;exits
    public synchronized void moveToRoom( String areaName, String roomUID, String exitUsed, boolean indoors, String shortDesc, String longDesc, Set<String> exits ) {
        if (this.area == null || ! this.area.getName().equalsIgnoreCase( areaName )) {
            moveToArea( areaName );
        }

        moveToRoom( roomUID, exitUsed, longDesc, shortDesc, indoors, exits );
        setRoomDescsForRoom( currentRoom, longDesc, shortDesc, indoors, exits );
        fireChanged();
    }


    /**
     * This method will create new room if need be, or just draw the exit between prior and new room if it doesn't exist.
     * Looping into same room will have exit drawn, but only if it doesn't exist yet.
     *
     * @param roomUID
     * @param exitUsed
     * @param exits
     * @param indoors
     * @param shortDesc
     * @param longDesc
     * @return true if room created was new, false if it already existed
     */
    public synchronized boolean moveToRoom( String roomUID, String exitUsed, String longDesc, String shortDesc, boolean indoors, Set<String> exits ) {
//		System.out.println(roomUID);
        Room newRoom = getRoomFromGraph( roomUID );
        boolean newRoomAddedToGraph = false;
        if (newRoom == null) {
            newRoom = new Room( roomUID, this.area );
            newRoomAddedToGraph = true;
        }

        Exit exit = new Exit( exitUsed );
        if (currentRoom == null && ! didTeleportIn( exitUsed )) {
            newRoom.setAreaEntrance( true );
        }

        if (currentRoom == null || didTeleportIn( exitUsed )) {
            graph.addVertex( newRoom );// if room existed in this graph, then this just does nothing?
        } else {
            if (GraphUtils.canAddExit( graph.getOutEdges( currentRoom ), exitUsed )) { // parallel exits can exist, but not with same name
                currentRoom.addExit( exit.getExit() );
                graph.addEdge( exit, new Pair<Room>( currentRoom, newRoom ), EdgeType.DIRECTED );

                if(reversableDirsMode && exit.getOpposite() != null){
                    Exit reverseExit = new Exit( exit.getOpposite() );
                    if(GraphUtils.canAddExit( graph.getOutEdges( newRoom ), reverseExit.getExit() )){
                        newRoom.addExit( reverseExit.getExit() );
                        graph.addEdge( reverseExit, new Pair<Room>( newRoom, currentRoom ), EdgeType.DIRECTED );
                    }
                }
            }

        }

        if (newRoomAddedToGraph) {
            Point2D location;
            if (currentRoom != null && locations.get( currentRoom ) != null) {
                location = DrawingUtils.getRelativePosition( locations.get( currentRoom ), exit, this.snapMode );
            } else {
                //either first room in new area, or new room in old area, no connection anywhere, either way lets draw into middle
                location = new Point2D.Double( 0, 0 );
            }
            locations.put( newRoom, getValidLocation( location ) );
        }
        if (currentRoom != null && mazemode) {
            currentRoom.useExit(exitUsed);
        }

        refreshRoomGraphicsAndSetAsCurrent( newRoom, longDesc, shortDesc, indoors, exits );
        fireChanged();
        return newRoomAddedToGraph;
    }


    private boolean didTeleportIn( String exitUsed ) {
        return exitUsed.equalsIgnoreCase( new Exit( "" ).TELEPORT );
    }

    /**
     * Moves the location away from other rooms until it doesn't overlap any of them
     */
    protected Point2D getValidLocation( Point2D checkThisLocation ) {
        while (getRoomNear( checkThisLocation ) != null) {
            checkThisLocation.setLocation( checkThisLocation.getX() - 20, checkThisLocation.getY() - 20 );
        }
        return checkThisLocation;
    }

    private Room getRoomNear( Point2D point ) {
        for (Room room : graph.getVertices()) {
            Point2D location = locations.get( room );
            if (location != null && location.distance( point ) <= ROOM_OVERLAP_DISTANCE) {
                return room;
            }
        }
        return null;
    }


    /**
     * This method will set short and long descs for this room, aka room where player currently is.
     * Should be called right after addRoomAndMoveToit if it returned true
     *
     * @param longDesc
     * @param shortDesc
     * @param indoors
     */
    public void setRoomDescsForRoom( Room room, String longDesc, String shortDesc, boolean indoors, Set<String> exits ) {

        room.setLongDesc( longDesc );
        room.setShortDesc( shortDesc );
        room.setIndoors( indoors );
        room.addExits( exits );
    }

    protected void refreshRoomGraphicsAndSetAsCurrent( Room newRoom, String longDesc, String shortDesc, boolean indoors, Set<String> exits ) {
        if (currentRoom != null) {
            currentRoom.setCurrent( false );
            // selection follows the player if the room player was in was selected
            if (currentRoom.isPicked()) {
                currentRoom.setPicked( false );
                newRoom.setPicked( true );
            }
        }
        newRoom.setCurrent( true );
        currentRoom = newRoom;
    }

    /**
     * This will save mapdata for current area, try to load data for new area. If no data found, empty data created.
     * moveToRoom method should be called after this one to know where player is
     *
     * @param areaName name for area, or pass null if leaving area into outerworld or such.
     */
    protected synchronized void moveToArea( String areaName ) {

        if (areaName == null) {
            clearExtraCurrentAndChosenValuesFromRooms();
            saveCurrentArea();
            this.area = null;
            currentRoom = null;
            this.graph = new SparseMultigraph<Room, Exit>();
            this.locations = new HashMap<Room, Point2D>();
        } else {
            saveCurrentArea();
            AreaSaveObject areaSaveObject = null;
            try {
                areaSaveObject = AreaDataPersister.loadData( baseDir, areaName );

            } catch (ClassNotFoundException e) {
//				log.error(e.getMessage()+" "+e.getStackTrace());
            } catch (IOException e) {
//				log.error("Unable to find file "+e.getMessage());
            }
            if (areaSaveObject == null) {//area doesn't exist so we create new saveobject which has empty graphs and maps
                areaSaveObject = new AreaSaveObject();
            }
            displayLoadedData( areaSaveObject );
            if (graph.getVertexCount() > 0) {
                this.area = graph.getVertices().iterator().next().getArea();
            } else {
                this.area = new Area( areaName );
            }
            this.currentRoom = null;
        }

        fireChanged();
    }

    protected void displayLoadedData( AreaSaveObject saveObject ) {
        this.graph = saveObject.getGraph();
        this.locations = new HashMap<Room, Point2D>();
        for (Room room : graph.getVertices()) {
            Point2D location = saveObject.getLocations().get( room );
            if (location != null) {
                locations.put( room, location );
            }
            room.setDrawn( true );
        }
        for (Room room : graph.getVertices()) {
            if (! locations.containsKey( room )) {
                locations.put( room, getValidLocation( new Point2D.Double( 0, 0 ) ) );
            }
        }
    }


    private void clearExtraCurrentAndChosenValuesFromRooms() {
        for (Room room : this.graph.getVertices()) {
            room.setCurrent( false );
            room.setPicked( false );
        }

    }



    protected void saveCurrentArea() {
        try {
            if (this.area != null) {
                AreaDataPersister.save( baseDir, graph, locations );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public synchronized void save() {
        saveCurrentArea();
    }

    public synchronized void clearCurrentArea() {
        if (area == null) {
            return;
        }
        String areaName = area.getName();
        displayLoadedData( new AreaSaveObject() );
        this.area = new Area( areaName );
        this.currentRoom = null;
        fireChanged();
    }

    protected Room getRoomFromGraph( String uid ) {
        for (Room room : this.graph.getVertices()) {
            if (room.getId().equals( uid )) {
                return room;
            }
        }

        return null;
    }

    private Exit getExitFromGraph( int uid ) {
        for (Exit exit : this.graph.getEdges()) {
            if (exit.getUid() == uid) {
                return exit;
            }
        }
        return null;
    }

    private List<Room> getPickedRooms() {
        List<Room> picked = new ArrayList<Room>();
        for (Room room : graph.getVertices()) {
            if (room.isPicked()) {
                picked.add( room );
            }
        }
        return picked;
    }

    private void clearPicked() {
        for (Room room : graph.getVertices()) {
            room.setPicked( false );
        }
    }

    /**
     * Select a room, or with toggle add/remove it from the selected rooms. Null room id clears selection.
     */
    public synchronized void pickRoom( String roomId, boolean toggle ) {
        Room room = roomId == null ? null : getRoomFromGraph( roomId );
        if (toggle) {
            if (room != null) {
                room.setPicked( ! room.isPicked() );
            }
        } else {
            clearPicked();
            if (room != null) {
                room.setPicked( true );
            }
        }
        fireChanged();
    }

    /**
     * Select rooms from a dragged selection rectangle
     */
    public synchronized void pickRooms( Collection<String> roomIds, boolean clearOthers ) {
        if (clearOthers) {
            clearPicked();
        }
        for (String roomId : roomIds) {
            Room room = getRoomFromGraph( roomId );
            if (room != null) {
                room.setPicked( true );
            }
        }
        fireChanged();
    }

    public synchronized void moveRooms( Map<String, Point2D> newLocations ) {
        for (Map.Entry<String, Point2D> entry : newLocations.entrySet()) {
            Room room = getRoomFromGraph( entry.getKey() );
            if (room != null) {
                locations.put( room, entry.getValue() );
            }
        }
        fireChanged();
    }

    public synchronized void changeRoomColor( Color color ) {
        for(Room room: getPickedRooms()){
            room.setColor(color);
        }
        fireChanged();

    }

    public synchronized void deletePickedRooms() {
        for (Room room : getPickedRooms()) {
            graph.removeVertex( room );
            locations.remove( room );
            if (room == currentRoom) {
                currentRoom = null;
            }
        }
        fireChanged();
    }

    public synchronized void deleteExit( int exitUid ) {
        Exit exit = getExitFromGraph( exitUid );
        if (exit != null) {
            graph.removeEdge( exit );
        }
        fireChanged();
    }

    public synchronized void renameExit( int exitUid, String newName ) {
        Exit exit = getExitFromGraph( exitUid );
        if (exit != null && newName != null && ! newName.equals( "" )) {
            exit.setExit( newName );
        }
        fireChanged();
    }

    public synchronized void setNotes( String roomId, String notes ) {
        Room room = getRoomFromGraph( roomId );
        if (room != null) {
            room.setNotes( notes );
        }
        fireChanged();
    }

    /**
     * Appends a line to notes of the selected room, or the current room if there isn't exactly one selected.
     *
     * @return false if there was no room to add notes to
     */
    public synchronized boolean appendToNotes( String text ) {
        List<Room> picked = getPickedRooms();
        Room room = picked.size() == 1 ? picked.get( 0 ) : currentRoom;
        if (room == null) {
            return false;
        }
        String notes = room.getNotes() == null ? "" : room.getNotes();
        room.setNotes( notes + text + "\n" );
        fireChanged();
        return true;
    }

    /**
     * @return number that grows every time the map changes
     */
    public synchronized long getVersion() {
        return version;
    }

    /**
     * Snapshot of the map for drawing it
     */
    public synchronized MapState getState() {
        MapState state = new MapState();
        state.view = getViewName();
        state.version = version;
        state.area = area == null ? null : area.getName();
        state.snap = snapMode;
        state.maze = mazemode;
        state.revs = reversableDirsMode;

        for (Room room : graph.getVertices()) {
            MapState.RoomState roomState = new MapState.RoomState();
            Point2D location = locations.get( room );
            roomState.id = room.getId();
            roomState.x = location == null ? 0 : location.getX();
            roomState.y = location == null ? 0 : location.getY();
            roomState.shortDesc = room.getShortDesc();
            roomState.label = room.getLabel();
            roomState.indoors = room.isIndoors();
            roomState.current = room.isCurrent();
            roomState.picked = room.isPicked();
            roomState.explored = room.allExitsHaveBeenUSed();
            if (roomState.explored) {
                roomState.color = RoomColors.toHex( RoomColors.MAZEMODE_FULLYEXPLORED );
                roomState.lightExits = true;
            } else {
                roomState.color = RoomColors.toHex( room.getColor() );
                roomState.lightExits = RoomColors.needsLightExits( room.getColor() );
            }
            roomState.exits = DrawingUtils.getExitMarkers( room.getExits() );
            state.rooms.add( roomState );
        }

        for (Exit exit : graph.getEdges()) {
            Pair<Room> ends = graph.getEndpoints( exit );
            MapState.ExitState exitState = new MapState.ExitState();
            exitState.id = exit.getUid();
            exitState.from = ends.getFirst().getId();
            exitState.to = ends.getSecond().getId();
            exitState.name = exit.getExit();
            exitState.current = exit.isCurrentExit();
            state.exits.add( exitState );
        }

        List<Room> picked = getPickedRooms();
        if (picked.size() == 1) {
            state.selected = makeSelectedRoom( picked.get( 0 ) );
        }

        String[] colorNames = RoomColors.getColorNames();
        Color[] colors = RoomColors.getColors();
        for (int i = 0; i < colorNames.length; i++) {
            state.colors.add( new MapState.ColorOption( colorNames[i], RoomColors.toHex( colors[i] ) ) );
        }
        return state;
    }

    private MapState.SelectedRoom makeSelectedRoom( Room room ) {
        MapState.SelectedRoom selected = new MapState.SelectedRoom();
        selected.id = room.getId();
        selected.shortDesc = room.getShortDesc();
        selected.longDesc = room.getLongDesc();
        selected.notes = room.getNotes();
        selected.label = room.getLabel();
        selected.colorIndex = RoomColors.getIndex( room.getColor() );
        selected.exits = makeExitsStringFromPickedRoom( room );
        return selected;
    }

    protected String makeExitsStringFromPickedRoom(Room room) {
        Set<String> exits = new LinkedHashSet<String>( room.getExits() );
        Collection<Exit> outExits = graph.getOutEdges(room);
        if (outExits != null) {
            for (Exit exit : outExits) {
                exits.add( exit.getExit() );
            }
        }
        StringBuilder exitString = new StringBuilder();
        Iterator<String> roomExitIterator = exits.iterator();
        while (roomExitIterator.hasNext()) {
            exitString.append(roomExitIterator.next());
            if (roomExitIterator.hasNext()) {
                exitString.append(", ");
            }
        }
        return exitString.toString();
    }


    public SparseMultigraph<Room, Exit> getGraph() {
        return graph;
    }


    public void setBaseDir( String baseDir ) {
        this.baseDir = baseDir;
    }

    public String getBaseDir() {
        return this.baseDir;
    }


    public void setBatWindow( BatWindow clientWin ) {
        this.batWindow = clientWin;

    }

    public void saveGuiData( Point location, Dimension size ) {
        GuiDataPersister.save( this.baseDir, location, size );
    }


    @Override
    public void componentHidden( ComponentEvent e ) {

    }


    @Override
    public void componentMoved( ComponentEvent e ) {
        if (this.batWindow != null) {
            GuiDataPersister.save( this.baseDir, this.batWindow.getLocation(), this.batWindow.getSize() );
        }
    }


    @Override
    public void componentResized( ComponentEvent e ) {
        if (this.batWindow != null) {
            GuiDataPersister.save( this.baseDir, this.batWindow.getLocation(), this.batWindow.getSize() );
        }
    }


    @Override
    public void componentShown( ComponentEvent e ) {

    }

    public synchronized void setRoomSnapping(boolean roomsWillSnapIntoPlaces){
        this.snapMode = roomsWillSnapIntoPlaces;
        fireChanged();
    }

    /**
     * Directions from current room to the given room
     *
     * @return the directions, or null if there is no known path
     */
    public synchronized String getDirsTo( String roomId, boolean shortDirs ) {
        Room targetroom = getRoomFromGraph( roomId );
        if (targetroom == null || currentRoom == null || ! graph.containsVertex( currentRoom )) {
            return null;
        }
        return checkDirsFromCurrentRoomTo( targetroom, shortDirs );
    }

    public String checkDirsFromCurrentRoomTo(Room targetroom, boolean shortDirs){
        DijkstraShortestPath<Room, Exit> algorithm = new DijkstraShortestPath<Room, Exit>( this.getGraph() );
        StringBuilder returnvalue= new StringBuilder();
        String delim = getDelim();
        List<Exit> path = algorithm.getPath( currentRoom, targetroom );
        if (path.isEmpty()) {
            return null;
        }
        if(shortDirs){
            //plan is to transform "north, north, north, south, east, tunnel" into
            // 3 n;s;e;tunnel
            int count_repeated_dirs=1;
            String previous = null;
            Iterator<Exit> pathIterator= path.iterator();

            while( pathIterator.hasNext() ){
                Exit exit = pathIterator.next();
                String shortExit = Exit.checkWhatExitIs( exit.getExit() );
                if (shortExit == null) {
                    shortExit = exit.getExit();
                }
                if(previous == null){
                    previous = shortExit;
                }else{
                    if(previous.equalsIgnoreCase( shortExit )){
                        count_repeated_dirs++;
                    }else{
                        if(count_repeated_dirs == 1){
                            returnvalue.append( previous ).append( delim );
                        }else{
                            returnvalue.append( count_repeated_dirs).append( " " ).append( previous ).append( delim );
                        }
                        previous = shortExit;
                        count_repeated_dirs=1;
                    }
                }
            }

            if(count_repeated_dirs == 1){
                returnvalue.append( previous ).append( delim );
            }else{
                returnvalue.append( count_repeated_dirs).append( " " ).append( previous ).append( delim );
            }
        }else{
            for(Exit exit: path){
                returnvalue.append( exit.getExit() ).append( delim );
            }
        }
        return returnvalue.toString();
    }

    private String getDelim() {
        return this.corpsePanel != null ? this.corpsePanel.getDelim() : DEFAULT_DELIM;
    }


    public void setCorpsePanel( CorpsePanel corpsePanel){
        this.corpsePanel = corpsePanel;
    }

    public void sendToMud( final String command ) {
        if (this.plugin == null) {
            System.out.println( "would send to mud: " + command );
            return;
        }
        // handed over to the gui thread so that batclient is never called while holding the map lock
        SwingUtilities.invokeLater( new Runnable() {
            public void run() {
                plugin.doCommand( command );
            }
        } );
    }

    public void sendToParty(String message){
        sendToMud( "party say "+message.replace( getDelim(), "," ) );
    }

    public synchronized boolean removeLabelFromCurrent(){
        if (this.currentRoom == null) {
            return false;
        }
        this.currentRoom.setLabel(null);
        fireChanged();
        return true;
    }
    public synchronized boolean setLabelToCurrentRoom(String label){
        if (this.currentRoom == null) {
            return false;
        }
        this.currentRoom.setLabel(label);
        fireChanged();
        return true;
    }

    public synchronized boolean roomLabelExists(String label){
        for( Room room: this.graph.getVertices()){
            if(room.getLabel() != null && room.getLabel().equalsIgnoreCase(label)){
                return true;
            }
        }
        return false;
    }

    public synchronized void runtoLabel(String label){
        Room targetroom = null;
        for( Room room: this.graph.getVertices()){
            if(room.getLabel() != null && room.getLabel().equalsIgnoreCase(label)){
                targetroom = room;
            }
        }
        String dirs = getDirsTo( targetroom.getId(), false );
        if (dirs != null) {
            sendToMud(dirs);
        }
    }
    public synchronized List<String> getLabels(){
        List<String> labels = new LinkedList<>();
        for( Room room: this.graph.getVertices()){
            if(room.getLabel() != null){
                labels.add(String.format("%-10s %s", room.getLabel(), room.getShortDesc()));
            }
        }
        return labels;
    }

    public synchronized void setMazeMode(boolean enabled){
        this.mazemode = enabled;
        for(Room room: this.graph.getVertices()){
                room.resetExitUsage();
        }
        fireChanged();
    }

    public synchronized void setReversableDirsMode(boolean enabled){
        this.reversableDirsMode = enabled;
        fireChanged();
    }

}
