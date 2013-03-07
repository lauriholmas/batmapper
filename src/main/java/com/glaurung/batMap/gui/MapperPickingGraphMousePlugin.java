package com.glaurung.batMap.gui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Collection;

import javax.swing.JComponent;

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
 * on the selected Vertex, then that Vertex will be repositioned to
 * follow the mouse until the button is released.
 * 
 * @author Tom Nelson
 */
public class MapperPickingGraphMousePlugin<Room, Exit> extends AbstractGraphMousePlugin
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
    
    /**
	 * create an instance with default settings
	 */
	public MapperPickingGraphMousePlugin() {
	    this(InputEvent.BUTTON1_MASK, InputEvent.BUTTON1_MASK | InputEvent.SHIFT_MASK);
	}

	/**
	 * create an instance with overides
	 * @param selectionModifiers for primary selection
	 * @param addToSelectionModifiers for additional selection
	 */
    public MapperPickingGraphMousePlugin(int selectionModifiers, int addToSelectionModifiers) {
        super(selectionModifiers);
        this.addToSelectionModifiers = addToSelectionModifiers;
        this.lensPaintable = new LensPaintable();
        this.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
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
    public void setLensColor(Color lensColor) {
        this.lensColor = lensColor;
    }

    /**
     * a Paintable to draw the rectangle used to pick multiple
     * Vertices
     * @author Tom Nelson
     *
     */
    class LensPaintable implements Paintable {

        public void paint(Graphics g) {
            Color oldColor = g.getColor();
            g.setColor(lensColor);
            ((Graphics2D)g).draw(rect);
            g.setColor(oldColor);
        }

        public boolean useTransform() {
            return false;
        }
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
    public void mousePressed(MouseEvent e) {
        down = e.getPoint();

        VisualizationViewer<Room,Exit> vv = (VisualizationViewer)e.getSource();
        GraphElementAccessor<Room,Exit> pickSupport = vv.getPickSupport();
        PickedState<Room> pickedVertexState = vv.getPickedVertexState();
        PickedState<Exit> pickedEdgeState = vv.getPickedEdgeState();
        if(pickSupport != null && pickedVertexState != null) {
            if (e.getClickCount() == 2 && !e.isConsumed() && pickSupport.getVertex(vv.getGraphLayout(), e.getPoint().getX(), e.getPoint().getY()) != null ) {
    		    e.consume();
    		    System.out.println("2clicketyclick, maybe on something");
            }
        	
            Layout<Room,Exit> layout = vv.getGraphLayout();
            if(e.getModifiers() == modifiers) {
                rect.setFrameFromDiagonal(down,down);
                // p is the screen point for the mouse event
                Point2D ip = e.getPoint();

                room = pickSupport.getVertex(layout, ip.getX(), ip.getY());
                if(room != null) {
                    if(pickedVertexState.isPicked(room) == false) {
                    	pickedVertexState.clear();
                    	pickedVertexState.pick(room, true);
                    }
                    // layout.getLocation applies the layout transformer so
                    // q is transformed by the layout transformer only
                    Point2D q = layout.transform(room);
                    // transform the mouse point to graph coordinate system
                    Point2D gp = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.LAYOUT, ip);

                    offsetx = (float) (gp.getX()-q.getX());
                    offsety = (float) (gp.getY()-q.getY());
                } else if((exit = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
                    pickedEdgeState.clear();
                    pickedEdgeState.pick(exit, true);
                } else{
//                    vv.addPostRenderPaintable(lensPaintable);
                	pickedEdgeState.clear();
                    pickedVertexState.clear();
                }
                
            } else if(e.getModifiers() == addToSelectionModifiers) {
            	
                vv.addPostRenderPaintable(lensPaintable);
                rect.setFrameFromDiagonal(down,down);
                Point2D ip = e.getPoint();
                room = pickSupport.getVertex(layout, ip.getX(), ip.getY());
//                if(room != null) {
//                    boolean wasThere = pickedVertexState.pick(room, !pickedVertexState.isPicked(room));
//                    if(wasThere) {
//                    	System.out.println("wasThere!");
//                    	room = null;
//                    } else {
//                    	System.out.println("wasNOOOOOTThere!");
//                        // layout.getLocation applies the layout transformer so
//                        // q is transformed by the layout transformer only
//                        Point2D q = layout.transform(room);
//                        // translate mouse point to graph coord system
//                        Point2D gp = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(Layer.LAYOUT, ip);
//
//                        offsetx = (float) (gp.getX()-q.getX());
//                        offsety = (float) (gp.getY()-q.getY());
//                    }
//                } else if((exit = pickSupport.getEdge(layout, ip.getX(), ip.getY())) != null) {
//                    pickedEdgeState.pick(exit, !pickedEdgeState.isPicked(exit));
//                }
            }
        }
        if(room != null) e.consume();
    }

    /**
	 * If the mouse is dragging a rectangle, pick the
	 * Vertices contained in that rectangle
	 * 
	 * clean up settings from mousePressed
	 */
    @SuppressWarnings("unchecked")
    public void mouseReleased(MouseEvent e) {
        VisualizationViewer<Room,Exit> vv = (VisualizationViewer)e.getSource();
        if(e.getModifiers() == modifiers) {
            if(down != null) {
                Point2D out = e.getPoint();

                if(room == null && heyThatsTooClose(down, out, 5) == false) {
                    pickContainedVertices(vv, down, out, true);
                }
            }
        } else if(e.getModifiers() == this.addToSelectionModifiers) {
            if(down != null) {
                Point2D out = e.getPoint();

                if(room == null && heyThatsTooClose(down,out,5) == false) {
                    pickContainedVertices(vv, down, out, false);
                }
            }
        }
        down = null;
        room = null;
        exit = null;
        rect.setFrame(0,0,0,0);
        vv.removePostRenderPaintable(lensPaintable);
        vv.repaint();
    }
    
    /**
	 * If the mouse is over a picked vertex, drag all picked
	 * vertices with the mouse.
	 * If the mouse is not over a Vertex, draw the rectangle
	 * to select multiple Vertices
	 * 
	 */
    @SuppressWarnings("unchecked")
    public void mouseDragged(MouseEvent e) {
        if(locked == false) {
            VisualizationViewer<Room,Exit> vv = (VisualizationViewer)e.getSource();
            if(room != null) {
                Point p = e.getPoint();
                Point2D graphPoint = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(p);
                Point2D graphDown = vv.getRenderContext().getMultiLayerTransformer().inverseTransform(down);
                Layout<Room,Exit> layout = vv.getGraphLayout();
                double dx = graphPoint.getX()-graphDown.getX();
                double dy = graphPoint.getY()-graphDown.getY();
                PickedState<Room> ps = vv.getPickedVertexState();
                
                for(Room v : ps.getPicked()) {
                    Point2D vp = layout.transform(v);
                    vp.setLocation(vp.getX()+dx, vp.getY()+dy);
                    layout.setLocation(v, vp);
                }
                down = p;

            } else {
                Point2D out = e.getPoint();
                if(e.getModifiers() == this.addToSelectionModifiers ||
                        e.getModifiers() == modifiers) {
                    rect.setFrameFromDiagonal(down,out);
                }
            }
            if(room != null) e.consume();
            vv.repaint();
        }
    }
    
    /**
     * rejects picking if the rectangle is too small, like
     * if the user meant to select one vertex but moved the
     * mouse slightly
     * @param p
     * @param q
     * @param min
     * @return
     */
    private boolean heyThatsTooClose(Point2D p, Point2D q, double min) {
        return Math.abs(p.getX()-q.getX()) < min &&
                Math.abs(p.getY()-q.getY()) < min;
    }
    
    /**
     * pick the vertices inside the rectangle created from points
     * 'down' and 'out'
     *
     */
    protected void pickContainedVertices(VisualizationViewer<Room,Exit> vv, Point2D down, Point2D out, boolean clear) {
        
        Layout<Room,Exit> layout = vv.getGraphLayout();
        PickedState<Room> pickedVertexState = vv.getPickedVertexState();
        
        Rectangle2D pickRectangle = new Rectangle2D.Double();
        pickRectangle.setFrameFromDiagonal(down,out);
         
        if(pickedVertexState != null) {
            if(clear) {
            	pickedVertexState.clear();
            }
            GraphElementAccessor<Room,Exit> pickSupport = vv.getPickSupport();

            Collection<Room> picked = pickSupport.getVertices(layout, pickRectangle);
            for(Room v : picked) {
            	pickedVertexState.pick(v, true);
            }
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
        JComponent c = (JComponent)e.getSource();
        c.setCursor(cursor);
    }

    public void mouseExited(MouseEvent e) {
        JComponent c = (JComponent)e.getSource();
        c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    public void mouseMoved(MouseEvent e) {
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
    public void setLocked(boolean locked) {
        this.locked = locked;
    }
}
