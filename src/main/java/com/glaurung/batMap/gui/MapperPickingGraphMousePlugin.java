package com.glaurung.batMap.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.swing.*;

import com.glaurung.batMap.controller.MapperEngine;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;
import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
import edu.uci.ics.jung.visualization.control.AbstractGraphMousePlugin;
import edu.uci.ics.jung.visualization.picking.PickedState;

/**
 * PickingGraphMousePlugin supports the picking of graph elements
 * with the mouse. MouseButtonOne picks a single vertex
 * or edge, and MouseButtonTwo adds to the set of selected Vertices
 * or EdgeType. If a Vertex is selected and the mouse is dragged while
 * on the seected Vertex, then that Vertex will be repositioned to
 * follow the mouse until the button is released.
 *
 * @author Tom Nelson
 */
public class MapperPickingGraphMousePlugin extends AbstractGraphMousePlugin
        implements MouseListener, MouseMotionListener {

    /**
     * the picked Vertex, if any
     */
    protected Room room;

    /**
     * the picked Edge, if any
     */
    protected Exit exit;

    /**
     * the x distance from the picked vertex center to the mouse point
     */
    protected double offsetx;

    /**
     * the y distance from the picked vertex center to the mouse point
     */
    protected double offsety;

    /**
     * controls whether the Vertices may be moved with the mouse
     */
    protected boolean locked;

    /**
     * additional modifiers for the action of adding to an existing
     * selection
     */
    protected int addToSelectionModifiers;

    /**
     * used to draw a rectangle to contain picked vertices
     */
    protected Rectangle2D rect = new Rectangle2D.Float();

    /**
     * the Paintable for the lens picking rectangle
     */
    protected Paintable lensPaintable;

    /**
     * color for the picking rectangle
     */
    protected Color lensColor = Color.cyan;

    private boolean snapmode = true;

    private boolean shiftDown = false;
    MapperEngine engine;

    /**
     * create an instance with default settings
     */
    public MapperPickingGraphMousePlugin() {
        this( InputEvent.BUTTON1_MASK, InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK );
    }

    /**
     * create an instance with overides
     *
     * @param selectionModifiers      for primary selection
     * @param addToSelectionModifiers for additional selection
     */
    public MapperPickingGraphMousePlugin(int selectionModifiers, int addToSelectionModifiers) {
        super( selectionModifiers );
        this.addToSelectionModifiers = addToSelectionModifiers;
        this.lensPaintable = new LensPaintable();
        this.cursor = Cursor.getPredefinedCursor( Cursor.HAND_CURSOR );
    }

    /**
     * @return Returns the lensColor.
     */
    public Color getLensColor() {
        return lensColor;
    }

    /**
     * @param lensColor The lensColor to set.
     */
    public void setLensColor( Color lensColor ) {
        this.lensColor = lensColor;
    }


    /**
     * a Paintable to draw the rectangle used to pick multiple
     * Vertices
     *
     * @author Tom Nelson
     */
    class LensPaintable implements Paintable {

        public void paint( Graphics g ) {
            Color oldColor = g.getColor();
            g.setColor( lensColor );
            ( (Graphics2D) g ).draw( rect );
            g.setColor( oldColor );
        }

        public boolean useTransform() {
            return false;
        }
    }


    public void setSnapmode(boolean willSnapRoomsIntoNiftyLocations){
        this.snapmode = willSnapRoomsIntoNiftyLocations;
    }

    /**
     * For primary modifiers (default, MouseButton1):
     * pick a single Vertex or Edge that
     * is under the mouse pointer. If no Vertex or edge is under
     * the pointer, unselect all picked Vertices and edges, and
     * set up to draw a rectangle for multiple selection
     * of contained Vertices.
     * For additional selection (default Shift+MouseButton1):
     * Add to the selection, a single Vertex or Edge that is
     * under the mouse pointer. If a previously picked Vertex
     * or Edge is under the pointer, it is un-picked.
     * If no vertex or Edge is under the pointer, set up
     * to draw a multiple selection rectangle (as above)
     * but do not unpick previously picked elements.
     *
     * @param e the event
     */
    @SuppressWarnings("unchecked")
    public void mousePressed( MouseEvent e ) {
        down = e.getPoint();

        VisualizationViewer<Room, Exit> vv = (VisualizationViewer) e.getSource();
        GraphElementAccessor<Room, Exit> pickSupport = vv.getPickSupport();
        PickedState<Room> pickedVertexState = vv.getPickedVertexState();
        PickedState<Exit> pickedEdgeState = vv.getPickedEdgeState();
        if (pickSupport != null && pickedVertexState != null) {
//            if (e.getClickCount() == 2 && ! e.isConsumed() && pickSupport.getVertex( vv.getGraphLayout(), e.getPoint().getX(), e.getPoint().getY() ) != null) {
//                e.consume();
//                System.out.println( "2clicketyclick, maybe on something" );
//            }

            Layout<Room, Exit> layout = vv.getGraphLayout();
            if (e.getModifiers() == modifiers) {
                rect.setFrameFromDiagonal( down, down );
                // p is the screen point for the mouse event
                Point2D ip = e.getPoint();

                room = pickSupport.getVertex( layout, ip.getX(), ip.getY() );
                if (room != null) {
                    if (pickedVertexState.isPicked( room ) == false) {
                        pickedVertexState.clear();
                        pickedVertexState.pick( room, true );
                    }
                    // layout.getLocation applies the layout transformer so
                    // q is transformed by the layout transformer only
                    Point2D q = layout.transform( room );
                    // transform the mouse point to graph coordinate system
                    Point2D gp = vv.getRenderContext().getMultiLayerTransformer().inverseTransform( Layer.LAYOUT, ip );

                    offsetx = (float) ( gp.getX() - q.getX() );
                    offsety = (float) ( gp.getY() - q.getY() );
                } else if (( exit = pickSupport.getEdge( layout, ip.getX(), ip.getY() ) ) != null) {
                    pickedEdgeState.clear();
                    pickedEdgeState.pick( exit, true );
                } else {
//                    vv.addPostRenderPaintable(lensPaintable);
                    pickedEdgeState.clear();
                    pickedVertexState.clear();
                }

            } else if (e.getModifiers() == addToSelectionModifiers) {

                vv.addPostRenderPaintable( lensPaintable );
                rect.setFrameFromDiagonal( down, down );
                Point2D ip = e.getPoint();
                room = pickSupport.getVertex( layout, ip.getX(), ip.getY() );

            }
        }
        if (room != null) e.consume();
    }

    /**
     * If the mouse is dragging a rectangle, pick the
     * Vertices contained in that rectangle
     * <p>
     * clean up settings from mousePressed
     */
    @SuppressWarnings("unchecked")
    public void mouseReleased( MouseEvent e ) {
        VisualizationViewer<Room, Exit> vv = (VisualizationViewer) e.getSource();
        if (e.getModifiers() == modifiers) {
            if (down != null) {
                Point2D out = e.getPoint();

                if (room == null && heyThatsTooClose( down, out, 5 ) == false) {
                    pickContainedVertices( vv, down, out, true );
                }
            }
        } else if (e.getModifiers() == this.addToSelectionModifiers) {
            if (down != null) {
                Point2D out = e.getPoint();

                if (room == null && heyThatsTooClose( down, out, 5 ) == false) {
                    pickContainedVertices( vv, down, out, false );
                }
            }
        }
        down = null;
        room = null;
        exit = null;
        rect.setFrame( 0, 0, 0, 0 );
        vv.removePostRenderPaintable( lensPaintable );
        vv.repaint();
    }

    /**
     * If the mouse is over a picked vertex, drag all picked
     * vertices with the mouse.
     * If the mouse is not over a Vertex, draw the rectangle
     * to select multiple Vertices
     */
    @SuppressWarnings("unchecked")
    public void mouseDragged( MouseEvent e ) {
        if (locked == false) {
            VisualizationViewer<Room, Exit> vv = (VisualizationViewer) e.getSource();
            if (room != null) {
                Point p = e.getPoint();
                Point2D graphPoint = vv.getRenderContext().getMultiLayerTransformer().inverseTransform( p );
                Point2D graphDown = vv.getRenderContext().getMultiLayerTransformer().inverseTransform( down );
                Layout<Room, Exit> layout = vv.getGraphLayout();

                double dx,dy;
                if(this.snapmode){
                     dx = calcAlignedDelta(graphPoint.getX() - graphDown.getX());
                     dy = calcAlignedDelta(graphPoint.getY() - graphDown.getY());
                }else{
                     dx = graphPoint.getX() - graphDown.getX();
                     dy = graphPoint.getY() - graphDown.getY();
                }

                if ( !snapmode || dx != 0 || dy != 0) {
                    PickedState<Room> ps = vv.getPickedVertexState();

                    for (Room v : ps.getPicked()) {
                        Point2D vp = layout.transform(v);
                        vp.setLocation(vp.getX() + dx, vp.getY() + dy);
                        layout.setLocation(v, vp);
                    }
                    down = p;
                }
            } else {
                Point2D out = e.getPoint();
                if (e.getModifiers() == this.addToSelectionModifiers ||
                        e.getModifiers() == modifiers) {
                    rect.setFrameFromDiagonal( down, out );
                }
            }
            if (room != null) e.consume();
            vv.repaint();
        }
    }

    /**
     * rejects picking if the rectangle is too small, like
     * if the user meant to select one vertex but moved the
     * mouse slightly
     *
     * @param p
     * @param q
     * @param min
     * @return
     */
    private boolean heyThatsTooClose( Point2D p, Point2D q, double min ) {
        return Math.abs( p.getX() - q.getX() ) < min &&
                Math.abs( p.getY() - q.getY() ) < min;
    }

    /**
     * pick the vertices inside the rectangle created from points
     * 'down' and 'out'
     */
    protected void pickContainedVertices( VisualizationViewer<Room, Exit> vv, Point2D down, Point2D out, boolean clear ) {

        Layout<Room, Exit> layout = vv.getGraphLayout();
        PickedState<Room> pickedVertexState = vv.getPickedVertexState();

        Rectangle2D pickRectangle = new Rectangle2D.Double();
        pickRectangle.setFrameFromDiagonal( down, out );

        if (pickedVertexState != null) {
            if (clear) {
                pickedVertexState.clear();
            }
            GraphElementAccessor<Room, Exit> pickSupport = vv.getPickSupport();

            Collection<Room> picked = pickSupport.getVertices( layout, pickRectangle );
            for (Room pickedRoom : picked) {
                pickedRoom.setPicked(true);
                pickedVertexState.pick( pickedRoom, true );
            }
        }
    }

    /**
     *
     *
     * Click on room to select
     * shift-click on room to add/remove to/from selected group
     * right-click on selected room to ask for deletion
     *
     * click on exit to select
     * right-click on exit to ask for exit change/deletion
     *
     *
     * @param e
     */
    public void mouseClicked( MouseEvent e ) {

        VisualizationViewer<Room, Exit> vv = (VisualizationViewer) e.getSource();
        PickedState<Room> pickedState = vv.getPickedVertexState();
        Room clickedRoom = vv.getPickSupport().getVertex(vv.getGraphLayout(), e.getX(), e.getY());

        if( clickedRoom != null && e.isControlDown()){
           String dirs = this.engine.checkDirsFromCurrentToomTo(clickedRoom);
            Object[] options = {"Ok",
                    "Send to mud",
                    "Send to party"};
            int selection = JOptionPane.showOptionDialog(vv,
                    dirs,
                    "Dirs to "+clickedRoom.getShortDesc(),
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[0]);
            if( selection == 1){
                this.engine.sendToMud( dirs );
            }else if(selection == 2){
                this.engine.sendToParty( dirs);
            }
            return;
        }

        Exit clickedExit = vv.getPickSupport().getEdge(vv.getGraphLayout(),e.getX(), e.getY());

        if (e.getClickCount() == 2 && ! e.isConsumed() && clickedExit != null && clickedRoom == null) {
            //double click on exit, invoke edit option
            String exitDir = (String) JOptionPane.showInputDialog(
                    vv,
                    "What do you want to change the exit to?",
                    "Exit edit",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    null,
                    clickedExit.getExit() );

            if (exitDir != null && ! exitDir.equals( "" )) {
                clickedExit.setExit(exitDir);
            }
            vv.getPickedEdgeState().clear();
            return;
        }
        if(e.getModifiers() == MouseEvent.BUTTON3_MASK){
            //right click is for deletion
            if(clickedRoom != null && pickedState.isPicked(clickedRoom)){
                //right click on room ask to delete selected
                String roomdesc = clickedRoom.getShortDesc();
                if(pickedState.getPicked().size() > 1){
                    roomdesc = ""+pickedState.getPicked().size()+" rooms";
                }
                int response =  JOptionPane.showConfirmDialog(vv,"Do you wish to delete?\n"+roomdesc,"Room delete", JOptionPane.YES_NO_OPTION);
                if( response == JOptionPane.YES_OPTION){
                    for(Room deletedRoom: pickedState.getPicked()){
                        vv.getGraphLayout().getGraph().removeVertex(deletedRoom);
                    }
                    pickedState.clear();
                }
            }else if(clickedExit != null && clickedRoom == null){
                //right click on exit, ask to delete
                vv.getPickedEdgeState().clear();
                pickedState.clear();
                vv.getPickedEdgeState().pick(clickedExit, true);
                int retVal = JOptionPane.showConfirmDialog(vv, "Do you wish to delete this exit?\n"+clickedExit.getExit(), "Exit delete", JOptionPane.YES_NO_OPTION);
                if(retVal == JOptionPane.YES_OPTION){
                    vv.getGraphLayout().getGraph().removeEdge(clickedExit);
                    vv.getPickedEdgeState().clear();
                }
            }
            return;
        }

        if(e.getModifiersEx() == MouseEvent.SHIFT_DOWN_MASK){
            if(clickedRoom != null){
                if(pickedState.isPicked(clickedRoom)){
                    clickedRoom.setPicked(false);
                    pickedState.pick(clickedRoom, false);
                }else{
                    clickedRoom.setPicked(true);
                    pickedState.pick(clickedRoom, true);
                }
            }
        }else{
            for(Room room: pickedState.getPicked()){
                room.setPicked(false);
            }
            pickedState.clear();
            if(clickedRoom != null){
                clickedRoom.setPicked(true);
                pickedState.pick(clickedRoom, true);
            }
        }
    }

    public void mouseEntered( MouseEvent e ) {
        JComponent c = (JComponent) e.getSource();
        c.setCursor( cursor );
    }

    public void mouseExited( MouseEvent e ) {
        JComponent c = (JComponent) e.getSource();
        c.setCursor( Cursor.getPredefinedCursor( Cursor.DEFAULT_CURSOR ) );
    }

    public void mouseMoved( MouseEvent e ) {
    }

    /**
     * @return Returns the locked.
     */
    public boolean isLocked() {
        return locked;
    }

    /**
     * @param locked The locked to set.
     */
    public void setLocked( boolean locked ) {
        this.locked = locked;
    }

    /**
     * Align delta value to be always times of `2 * ROOM_SIZE`
     */
    public double calcAlignedDelta(double delta) {
        return Math.round(delta / (2 * DrawingUtils.ROOM_SIZE)) * 2 * DrawingUtils.ROOM_SIZE;
    }

    public void setEngine( MapperEngine engine ){
        this.engine = engine;

    }
}
