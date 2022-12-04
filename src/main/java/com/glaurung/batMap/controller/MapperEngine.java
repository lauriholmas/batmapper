package com.glaurung.batMap.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.*;

import com.glaurung.batMap.gui.*;
import com.glaurung.batMap.gui.corpses.CorpsePanel;
import com.glaurung.batMap.io.AreaDataPersister;
import com.glaurung.batMap.io.GuiDataPersister;
import com.glaurung.batMap.vo.Area;
import com.glaurung.batMap.vo.AreaSaveObject;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;
import com.mythicscape.batclient.interfaces.BatWindow;

import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;
import edu.uci.ics.jung.visualization.transform.MutableTransformer;

public class MapperEngine implements ItemListener, ComponentListener {


    SparseMultigraph<Room, Exit> graph;
    VisualizationViewer<Room, Exit> vv;
    MapperLayout mapperLayout;
    Room currentRoom = null;
    Area area = null;
    MapperPanel panel;

    PickedState<Room> pickedState;
    String baseDir;
    BatWindow batWindow;
    ScalingGraphMousePlugin scaler;
    MapperPickingGraphMousePlugin mapperPickingGraphMousePlugin;
    boolean snapMode = true;
    CorpsePanel corpsePanel;
    MapperPlugin plugin;
    boolean mazemode = false;
    boolean reversableDirsMode = false;

    public MapperEngine( SparseMultigraph<Room, Exit> graph, MapperPlugin plugin ) {
        this(plugin);
        this.graph = graph;
        this.mapperLayout.setGraph( graph );
    }


    public MapperEngine(MapperPlugin plugin) {
        this.plugin = plugin;
        graph = new SparseMultigraph<Room, Exit>();
        mapperLayout = new MapperLayout( graph );
        mapperLayout.setSize( new Dimension( 500, 500 ) ); //????
        vv = new VisualizationViewer<Room, Exit>( mapperLayout );
        pickedState = vv.getPickedVertexState();
        pickedState.addItemListener( this );
        vv.setPreferredSize( new Dimension( 500, 500 ) ); //????
        RenderContext<Room, Exit> rc = vv.getRenderContext();

        rc.setEdgeLabelTransformer( new ToStringLabeller<Exit>() );
        rc.setEdgeLabelRenderer( new ExitLabelRenderer() );
        rc.setEdgeShapeTransformer( new EdgeShape.QuadCurve<Room, Exit>() );
        rc.setEdgeShapeTransformer( new EdgeShape.Wedge<Room, Exit>( 30 ) );
        rc.setEdgeFillPaintTransformer( new ExitPaintTransformer( vv ) );

        rc.setVertexShapeTransformer( new RoomShape( graph ) );
        rc.setVertexIconTransformer( new RoomIconTransformer() );

        vv.getRenderContext().setLabelOffset( 5 );

        PluggableGraphMouse pgm = new PluggableGraphMouse();
        mapperPickingGraphMousePlugin = new MapperPickingGraphMousePlugin( MouseEvent.BUTTON1_MASK, MouseEvent.BUTTON3_MASK );
        mapperPickingGraphMousePlugin.setEngine( this );
        pgm.add( mapperPickingGraphMousePlugin);
        pgm.add( new TranslatingGraphMousePlugin( MouseEvent.BUTTON1_MASK ) );
        scaler = new ScalingGraphMousePlugin( new CrossoverScalingControl(), 0, 1 / 1.1f, 1.1f );
        pgm.add( scaler );
        vv.setGraphMouse( pgm );
        panel = new MapperPanel( this );
    }


    public void setSize( Dimension dimension ) {
        mapperLayout.setSize( dimension );
        vv.setPreferredSize( dimension );
    }

    //areaname;roomUID;exitUsed;indoor boolean;shortDesc;longDesc;exits
    public void moveToRoom( String areaName, String roomUID, String exitUsed, boolean indoors, String shortDesc, String longDesc, Set<String> exits ) {
        if (this.area == null || ! this.area.getName().equalsIgnoreCase( areaName )) {
            moveToArea( areaName );
        }

        moveToRoom( roomUID, exitUsed, longDesc, shortDesc, indoors, exits );
        setRoomDescsForRoom( currentRoom, longDesc, shortDesc, indoors, exits );

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
    public boolean moveToRoom( String roomUID, String exitUsed, String longDesc, String shortDesc, boolean indoors, Set<String> exits ) {
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

            if (currentRoom != null) {
                Point2D oldroomLocation = mapperLayout.transform( currentRoom );
                Point2D relativeLocation = DrawingUtils.getRelativePosition( oldroomLocation, exit, this.snapMode );
//				relativeLocation = getValidLocation(relativeLocation);
                relativeLocation = mapperLayout.getValidLocation( relativeLocation );
                vv.getGraphLayout().setLocation( newRoom, relativeLocation );
            } else {
                //either first room in new area, or new room in old area, no connection anywhere, either way lets draw into middle
                Point2D possibleLocation = new Point2D.Double( panel.getWidth() / 2, panel.getHeight() / 2 );
//				possibleLocation = getValidLocation(possibleLocation);
                possibleLocation = mapperLayout.getValidLocation( possibleLocation );
                vv.getGraphLayout().setLocation( newRoom, possibleLocation );
            }

        }
        if(mazemode){
            currentRoom.useExit(exitUsed);
        }

        refreshRoomGraphicsAndSetAsCurrent( newRoom, longDesc, shortDesc, indoors, exits );
        repaint();
        moveMapToStayWithCurrentRoom();
        return newRoomAddedToGraph;
    }


    public void repaint() {
        vv.repaint();
    }


    private boolean didTeleportIn( String exitUsed ) {
        return exitUsed.equalsIgnoreCase( new Exit( "" ).TELEPORT );
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
//System.out.println("newroom: "+newRoom+"\n\tcurrentroom: "+currentRoom+"\n\tpickedRoom: "+pickedRoom);
        if (currentRoom != null) {
            currentRoom.setCurrent( false );
            if (currentRoom.isPicked()) {
                setRoomDescsForRoom( newRoom, longDesc, shortDesc, indoors, exits );
                singleRoomPicked( newRoom );

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
    protected void moveToArea( String areaName ) {

        if (areaName == null) {
            clearExtraCurrentAndChosenValuesFromRooms();
            saveCurrentArea();
            this.area = null;
            currentRoom = null;

            pickedState.clear();
            this.graph = new SparseMultigraph<Room, Exit>();
            mapperLayout.setGraph( graph );
            Room nullRoom = null;
            this.panel.setTextForDescs( "", "", "", nullRoom );
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
            this.graph = areaSaveObject.getGraph();
            mapperLayout.setGraph( graph );
//			mousePlugin.setGraph(graph);
            mapperLayout.displayLoadedData( areaSaveObject );
            if (graph.getVertexCount() > 0) {
                this.area = graph.getVertices().iterator().next().getArea();
            } else {
                this.area = new Area( areaName );
            }
            this.currentRoom = null;


            /**
             *
             * 	PluggableGraphMouse pgm = new PluggableGraphMouse();
             pgm.add(new PickingGraphMousePlugin<Room,Exit>());
             pgm.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON3_MASK));
             pgm.add(new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, 1 / 1.1f, 1.1f));
             pgm.add(new MapperEditingGraphMousePlugin(graph));
             vv.setGraphMouse(pgm);
             */
        }

        repaint();
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
                AreaDataPersister.save( baseDir, graph, mapperLayout );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void save() {
        saveCurrentArea();
    }

    public void clearCurrentArea() {
	String areaName = area.getName();
	AreaSaveObject areaSaveObject = new AreaSaveObject();
	this.graph = areaSaveObject.getGraph();
	mapperLayout.setGraph( graph );
	mapperLayout.displayLoadedData( areaSaveObject );
	this.area = new Area( areaName );
	this.currentRoom = null;
	repaint();
    }

    private Room getRoomFromGraph( String uid ) {
        for (Room room : this.graph.getVertices()) {
            if (room.getId().equals( uid )) {
                return room;
            }
        }

        return null;
    }


    @Override
    public void itemStateChanged( ItemEvent e ) {
        Object subject = e.getItem();
        if (subject instanceof Room) {
            Room tempRoom = (Room) subject;
            if(e.getStateChange() == ItemEvent.SELECTED){
                pickedState.pick(tempRoom, true);
                tempRoom.setPicked(true);

            }else if(e.getStateChange() == ItemEvent.DESELECTED){
                pickedState.pick(tempRoom, false);
                tempRoom.setPicked(false);
            }
            repaint();
        }
        if(pickedState.getPicked().size() == 1){
            singleRoomPicked(pickedState.getPicked().iterator().next());
        }else{
            this.panel.setTextForDescs( "", "", "", null );
        }

    }

    public void changeRoomColor( Color color ) {
        for(Room room: pickedState.getPicked()){
            room.setColor(color);
        }
        repaint();

    }


    protected void singleRoomPicked( Room room ) {
        this.panel.setTextForDescs( room.getShortDesc(), room.getLongDesc(), makeExitsStringFromPickedRoom(room), room );


    }

    protected String makeExitsStringFromPickedRoom(Room room) {

            Collection<Exit> outExits = graph.getOutEdges(room);
            StringBuilder exitString = new StringBuilder();
            if (outExits != null) {
                Iterator<Exit> exitIterator = outExits.iterator();

                while (exitIterator.hasNext()) {
                    Exit exit = exitIterator.next();
                    room.getExits().add(exit.getExit());
                }
            }

            Iterator<String> roomExitIterator = room.getExits().iterator();
            while (roomExitIterator.hasNext()) {
                exitString.append(roomExitIterator.next());
                if (roomExitIterator.hasNext()) {
                    exitString.append(", ");
                }
            }


            return exitString.toString();

    }



    public VisualizationViewer<Room, Exit> getVV() {
        return this.vv;
    }

    public void setMapperSize( Dimension size ) {
        this.mapperLayout.setSize( size );
        this.vv.setSize( size );
        repaint();
    }


    public MapperPanel getPanel() {
        return this.panel;
    }

    /**
     * This method refocuses current room into middle of map,
     * if current room is over away from center by 50% of distance to windowedge
     */
    protected void moveMapToStayWithCurrentRoom() {


        Point2D currentRoomPoint = this.mapperLayout.transform( currentRoom );

        Point2D mapViewCenterPoint = this.panel.getMapperCentralPoint();
        Point2D viewPoint = vv.getRenderContext().getMultiLayerTransformer().transform( currentRoomPoint );
        if (needToRelocate( viewPoint, mapViewCenterPoint )) {
            MutableTransformer modelTransformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer( Layer.LAYOUT );
            viewPoint = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(viewPoint);
            mapViewCenterPoint = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(mapViewCenterPoint);
            float dx = (float) ( mapViewCenterPoint.getX() - viewPoint.getX() );
            float dy = (float) ( mapViewCenterPoint.getY() - viewPoint.getY() );
            modelTransformer.translate( dx, dy );
            repaint();
        }
    }

    private boolean needToRelocate( Point2D currentRoomPoint, Point2D mapViewCenterPoint ) {
        if (currentRoomPoint.getX() < 0.6 * mapViewCenterPoint.getX() || currentRoomPoint.getX() > 1.3 * mapViewCenterPoint.getX() ){
            return true;
        }

        if (currentRoomPoint.getY() < 0.6 * mapViewCenterPoint.getY() || currentRoomPoint.getY() > 1.3 * mapViewCenterPoint.getY()) {
            return true;
        }

        return false;
    }


//    public void redraw() {
//        this.mapperLayout.reDrawFromRoom( pickedRoom, this.mapperLayout.transform( pickedRoom ) );
//        repaint();
//
//    }


    public SparseMultigraph<Room, Exit> getGraph() {
        return graph;
    }


    public void toggleDescs() {
        this.panel.toggleDescs();

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


    public ScalingGraphMousePlugin getScaler() {
        return scaler;
    }

    public void setRoomSnapping(boolean roomsWillSnapIntoPlaces){
        this.snapMode = roomsWillSnapIntoPlaces;
        mapperPickingGraphMousePlugin.setSnapmode(roomsWillSnapIntoPlaces);
        this.mapperLayout.setSnapMode(roomsWillSnapIntoPlaces);
    }

    public void zoomIn(){
        this.scaler.getScaler().scale(this.vv, 1.1f, this.vv.getCenter());
    }

    public void zoomOut(){
        this.scaler.getScaler().scale(this.vv, 1/1.1f, this.vv.getCenter());
    }

    public String checkDirsFromCurrentRoomTo(Room targetroom, boolean shortDirs){
        DijkstraShortestPath<Room, Exit> algorithm = new DijkstraShortestPath<Room, Exit>( this.getGraph() );
        StringBuilder returnvalue= new StringBuilder();
        String delim = this.corpsePanel.getDelim();
        List<Exit> path = algorithm.getPath( currentRoom, targetroom );
        if(shortDirs){
            //plan is to transform "north, north, north, south, east, tunnel" into
            // 3 n;s;e;tunnel
            int count_repeated_dirs=1;
            String previous = null;
            Iterator<Exit> pathIterator= path.iterator();

            while( pathIterator.hasNext() ){
                String shortExit = Exit.checkWhatExitIs(  pathIterator.next().getExit());
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


    public void setCorpsePanel( CorpsePanel corpsePanel){
        this.corpsePanel = corpsePanel;
    }

    public void sendToMud(String command){
        this.plugin.doCommand( command );
    }

    public void sendToParty(String message){
        this.plugin.doCommand( "party say "+message.replace( this.corpsePanel.getDelim(), "," ) );
    }

    public void removeLabelFromCurrent(){
        this.currentRoom.setLabel(null);
    }
    public void setLabelToCurrentRoom(String label){
        this.currentRoom.setLabel(label);
    }

    public boolean roomLabelExists(String label){
        for( Room room: this.graph.getVertices()){
            if(room.getLabel() != null && room.getLabel().equalsIgnoreCase(label)){
                return true;
            }
        }
        return false;
    }

    public void runtoLabel(String label){
        Room targetroom = null;
        for( Room room: this.graph.getVertices()){
            if(room.getLabel() != null && room.getLabel().equalsIgnoreCase(label)){
                targetroom = room;
            }
        }
        String dirs = checkDirsFromCurrentRoomTo(targetroom, false);
        sendToMud(dirs);
    }
    public List<String> getLabels(){
        List<String> labels = new LinkedList<>();
        for( Room room: this.graph.getVertices()){
            if(room.getLabel() != null){
                labels.add(String.format("%-10s %s", room.getLabel(), room.getShortDesc()));
            }
        }
        return labels;
    }

    public void setMazeMode(boolean enabled){
        this.mazemode = enabled;
        for(Room room: this.graph.getVertices()){
                room.resetExitUsage();
        }
            repaint();
    }

    public void setReversableDirsMode(boolean enabled){
        this.reversableDirsMode = enabled;
    }

}
