package com.glaurung.batMap.gui;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Collection;

import com.glaurung.batMap.vo.AreaSaveObject;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;

import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.picking.RadiusPickSupport;


/**
 * This class used for drawing a map from a graph, with preloaded locations or otherwise
 *
 * @author lauri
 */
public class MapperLayout extends AbstractLayout<Room, Exit> {

    boolean snapMode = false;

    public MapperLayout( Graph<Room, Exit> graph ) {
        super( graph );
    }

    public void setSnapMode(boolean snapmode){
        this.snapMode = snapmode;
    }

    @Override
    public void initialize() {
        Dimension d = getSize();

        if (d != null) {

            double y = d.getHeight() / 2;
            double x = d.getWidth() / 2;

            Room startRoom = getStartRoomAndResetDraweds();
            if (startRoom == null) {
                return;
            }
//			System.out.println("iter drawing: "+x+" "+y);
            Point2D center = new Point2D.Double( x, y );
            iterativeDrawing( startRoom, center );
        }
    }

    private Room getStartRoomAndResetDraweds() {
        Room returnable = null;
        for (Room room : this.graph.getVertices()) {
            room.setDrawn( false );
            if (room.isAreaEntrance())
                returnable = room;
        }
        return returnable;
    }

    private void iterativeDrawing( Room room, Point2D location ) {
        if (room.isDrawn())
            return;
        placeRoomAndSetItDrawn( room, location );


        Collection<Exit> exits = this.graph.getOutEdges( room );
        for (Exit exit : exits) {
            Room other = getOtherEndOfExit( exit, room );
            Point2D positionForOtherRoom = DrawingUtils.getRelativePosition( location, exit, snapMode);
            positionForOtherRoom = getValidLocation( positionForOtherRoom );
            iterativeDrawing( other, positionForOtherRoom );
        }


    }


    private void placeRoomAndSetItDrawn( Room room, Point2D location ) {
//		System.out.println(room+" - "+location);
        Point2D coord = transform( room );
        coord.setLocation( location );
        room.setDrawn( true );
    }


    private Room getOtherEndOfExit( Exit exit, Room room ) {
        Pair<Room> ends = this.graph.getEndpoints( exit );
        if (ends.getFirst() == room) {
            return ends.getSecond();
        } else {
            return ends.getFirst();
        }
    }

    @Override
    public void reset() {
        initialize();
    }

    public void reDrawFromRoom( Room room, Point2D point ) {
        getStartRoomAndResetDraweds();

        iterativeDrawing( room, point );

    }


    public void displayLoadedData( AreaSaveObject saveObject ) {
        this.graph = saveObject.getGraph();
        for (Room room : graph.getVertices()) {

            Point2D coord = transform( room );
            coord.setLocation( saveObject.getLocations().get( room ) );

            placeRoomAndSetItDrawn( room, saveObject.getLocations().get( room ) );
        }
    }

    public Point2D getValidLocation( Point2D checkThisLocation ) {
        GraphElementAccessor<Room, Exit> pickSupport = new RadiusPickSupport<Room, Exit>( 60 );
        Room room = pickSupport.getVertex( this, checkThisLocation.getX(), checkThisLocation.getY() );
        if (room != null) {
            checkThisLocation.setLocation( checkThisLocation.getX() - 20, checkThisLocation.getY() - 20 );
            return getValidLocation( checkThisLocation );
        }
        return checkThisLocation;
    }


}
