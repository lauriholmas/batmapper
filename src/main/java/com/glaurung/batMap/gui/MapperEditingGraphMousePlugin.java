package com.glaurung.batMap.gui;

import java.awt.event.MouseEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import javax.swing.JOptionPane;

import com.glaurung.batMap.controller.MapperEngine;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;

import edu.uci.ics.jung.algorithms.layout.GraphElementAccessor;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.UndirectedGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.EditingGraphMousePlugin;

/**
 * This class used for drawing new exits between rooms
 * @author lauri
 *
 */
public class MapperEditingGraphMousePlugin extends EditingGraphMousePlugin<Room, Exit> {
//	private SparseMultigraph<Room, Exit> graph;
	private MapperEngine engine;

	public MapperEditingGraphMousePlugin(MapperEngine mapperEngine) {
		super(null, null);
		this.engine=mapperEngine;
	}
	
	
	public void mousePressed(MouseEvent e){
		

		if (e.getClickCount() == 2 && !e.isConsumed()) {
		    e.consume();
		    final VisualizationViewer<Room,Exit> vv =(VisualizationViewer<Room,Exit>)e.getSource();
		    final Point2D clickPoint = e.getPoint();
		    GraphElementAccessor<Room,Exit> pickSupport = vv.getPickSupport();
		    Exit exit = pickSupport.getEdge(vv.getModel().getGraphLayout(), clickPoint.getX(), clickPoint.getY());
		    
		    if(exit == null){
//		    	System.out.println(pickSupport.getVertex(vv.getModel().getGraphLayout(), clickPoint.getX(), clickPoint.getY()));
		    	this.engine.toggleDescs();
		    }else{
			    String exitDir = (String)JOptionPane.showInputDialog(
	                    null,
	                    "What do you want to change the exit to?",
	                    "Customized Dialog",
	                    JOptionPane.PLAIN_MESSAGE,
	                    null,
	                    null,
	                    exit.getExit());
			    if(exitDir != null && !exitDir.equals("")){
			    	Room room = this.engine.getGraph().getEndpoints(exit).getFirst();
			    	if(room.getExits().contains(exit.getExit())){
			    		room.getExits().remove(exit.getExit());
			    	}
			    	exit.setExit(exitDir);
			    	room.addExit(exitDir);
			    	vv.getRenderer().renderVertex(vv.getRenderContext(), vv.getGraphLayout(), room);
			    	vv.repaint();
			    }else{
			    	Room room = this.engine.getGraph().getEndpoints(exit).getFirst();
			    	if(room.getExits().contains(exit.getExit())){
			    		room.getExits().remove(exit.getExit());
			    	}
			    	this.engine.getGraph().removeEdge(exit);
			    	vv.getRenderer().renderVertex(vv.getRenderContext(), vv.getGraphLayout(), room);
			    	vv.repaint();
			    }
		    }

		}else if(checkModifiers(e)) {
            @SuppressWarnings("unchecked")
			final VisualizationViewer<Room,Exit> vv =
                (VisualizationViewer<Room,Exit>)e.getSource();
            final Point2D p = e.getPoint();
            GraphElementAccessor<Room,Exit> pickSupport = vv.getPickSupport();
            if(pickSupport != null) {
            	// set default edge type
            	edgeIsDirected = EdgeType.DIRECTED;

            	
                final Room room = pickSupport.getVertex(vv.getModel().getGraphLayout(), p.getX(), p.getY());
                if(room != null) { // get ready to make an edge
                    startVertex = room;
                    down = e.getPoint();
                    transformEdgeShape(down, down);
                    vv.addPostRenderPaintable(edgePaintable);
                    if((e.getModifiers() & MouseEvent.SHIFT_MASK) != 0
                    		&& vv.getModel().getGraphLayout().getGraph() instanceof UndirectedGraph == false) {
                        edgeIsDirected = EdgeType.DIRECTED;
                    }
                    if(edgeIsDirected == EdgeType.DIRECTED) {
                        transformArrowShape(down, e.getPoint());
                        vv.addPostRenderPaintable(arrowPaintable);
                    }
                }
            }
            vv.repaint();
        }
	}
	
	
	public void mouseReleased(MouseEvent e) {
        if(checkModifiers(e)) {
            @SuppressWarnings("unchecked")
			final VisualizationViewer<Room,Exit> vv =
                (VisualizationViewer<Room,Exit>)e.getSource();
            final Point2D p = e.getPoint();
            Layout<Room,Exit> layout = vv.getModel().getGraphLayout();
            GraphElementAccessor<Room,Exit> pickSupport = vv.getPickSupport();
            if(pickSupport != null) {
                final Room room = pickSupport.getVertex(layout, p.getX(), p.getY());
                if(room != null && startVertex != null) {
//                    Graph<Room,Exit> graph = vv.getGraphLayout().getGraph();
                    String proposition = getExitDir(layout.transform(startVertex), layout.transform(room));

                    String exitDir = (String)JOptionPane.showInputDialog(
                            null,
                            "What direction for the exit?",
                            "Customized Dialog",
                            JOptionPane.PLAIN_MESSAGE,
                            null,
                            null,
                            proposition);
                    if(exitDir != null){
                    	if(GraphUtils.canAddExit(this.engine.getGraph().getOutEdges(startVertex), exitDir)){
                    		startVertex.addExit(exitDir);
//                    		Exit exit = new Exit(exitDir);
                    		this.engine.getGraph().addEdge(new Exit(exitDir),startVertex, room, edgeIsDirected);
                    		vv.getRenderer().renderVertex(vv.getRenderContext(), vv.getGraphLayout(), startVertex);
                    	}
                    	
                    	
                    }

                    vv.repaint();
                }
            }
            startVertex = null;
            down = null;
            edgeIsDirected = EdgeType.UNDIRECTED;
            vv.removePostRenderPaintable(edgePaintable);
            vv.removePostRenderPaintable(arrowPaintable);
        }
    }

    private void transformEdgeShape(Point2D down, Point2D out) {
        float x1 = (float) down.getX();
        float y1 = (float) down.getY();
        float x2 = (float) out.getX();
        float y2 = (float) out.getY();

        AffineTransform xform = AffineTransform.getTranslateInstance(x1, y1);
        
        float dx = x2-x1;
        float dy = y2-y1;
        float thetaRadians = (float) Math.atan2(dy, dx);
        xform.rotate(thetaRadians);
        float dist = (float) Math.sqrt(dx*dx + dy*dy);
        xform.scale(dist / rawEdge.getBounds().getWidth(), 1.0);
        edgeShape = xform.createTransformedShape(rawEdge);
    }
    
    private void transformArrowShape(Point2D down, Point2D out) {
        float x1 = (float) down.getX();
        float y1 = (float) down.getY();
        float x2 = (float) out.getX();
        float y2 = (float) out.getY();

        AffineTransform xform = AffineTransform.getTranslateInstance(x2, y2);
        
        float dx = x2-x1;
        float dy = y2-y1;
        float thetaRadians = (float) Math.atan2(dy, dx);
        xform.rotate(thetaRadians);
        arrowShape = xform.createTransformedShape(rawArrowShape);
    }
    
    private String getExitDir(Point2D from, Point2D to){
    	StringBuilder exit = new StringBuilder();
    	if(from.getY() > to.getY()){
    		exit.append("n");
    	}else if(from.getY() < to.getY()){
    		exit.append("s");
    	}
    	if(from.getX() > to.getX()){
    		exit.append("w");
    	}else if(from.getX() < to.getX()){
    		exit.append("e");
    	}
    	
    	return exit.toString();
    }


//	public void setGraph(SparseMultigraph<Room, Exit> graph) {
//		this.graph = graph;
//		
//	}
	
	
}
