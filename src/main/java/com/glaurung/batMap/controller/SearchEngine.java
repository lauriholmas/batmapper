package com.glaurung.batMap.controller;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import com.glaurung.batMap.gui.ExitLabelRenderer;
import com.glaurung.batMap.gui.ExitPaintTransformer;
import com.glaurung.batMap.gui.MapperEditingGraphMousePlugin;
import com.glaurung.batMap.gui.MapperLayout;
import com.glaurung.batMap.gui.MapperPanel;
import com.glaurung.batMap.gui.MapperPickingGraphMousePlugin;
import com.glaurung.batMap.gui.RoomIconTransformer;
import com.glaurung.batMap.gui.RoomShape;
import com.glaurung.batMap.gui.search.SearchPanel;
import com.glaurung.batMap.io.AreaDataPersister;
import com.glaurung.batMap.vo.Area;
import com.glaurung.batMap.vo.AreaSaveObject;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;
import com.mythicscape.batclient.interfaces.BatWindow;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.RenderContext;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.PluggableGraphMouse;
import edu.uci.ics.jung.visualization.control.ScalingGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.TranslatingGraphMousePlugin;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.picking.PickedState;

public class SearchEngine extends Engine implements ItemListener{

	SparseMultigraph<Room, Exit> graph;
	VisualizationViewer<Room, Exit> vv;
	MapperLayout mapperLayout;
//	Room currentRoom = null;
	Room pickedRoom = null;
	Area area = null;
	MapperPanel panel;
	MapperEditingGraphMousePlugin mousePlugin;
	PickedState<Room> pickedState;
	String baseDir;
	BatWindow batWindow;
	ScalingGraphMousePlugin scaler;
	RoomIconTransformer roomDrawingThing;
	
	public SearchEngine(SparseMultigraph<Room, Exit> graph){
		this();
		this.graph = graph;
		this.mapperLayout.setGraph(graph);
	}
	
	public SearchEngine(){
		graph = new SparseMultigraph<Room, Exit>();
		mapperLayout = new MapperLayout(graph);
		mapperLayout.setSize(new Dimension(500,500)); //????
		vv = new VisualizationViewer<Room, Exit>(mapperLayout);
		pickedState = vv.getPickedVertexState();
		pickedState.addItemListener(this);
		vv.setPreferredSize(new Dimension(500,500)); //????
		RenderContext<Room, Exit> rc =   vv.getRenderContext();
		
		rc.setEdgeLabelTransformer(new ToStringLabeller<Exit>());
		rc.setEdgeLabelRenderer(new ExitLabelRenderer());
		rc.setEdgeShapeTransformer(new EdgeShape.QuadCurve<Room,Exit>());
		rc.setEdgeShapeTransformer(new EdgeShape.Wedge<Room,Exit>(30));
		rc.setEdgeFillPaintTransformer( new ExitPaintTransformer(vv) );
    	
    	rc.setVertexShapeTransformer(new RoomShape(graph));
    	roomDrawingThing = new RoomIconTransformer();
    	rc.setVertexIconTransformer(roomDrawingThing);
    	
    	vv.getRenderContext().setLabelOffset(5);
    	
    	PluggableGraphMouse pgm = new PluggableGraphMouse();
        pgm.add(new MapperPickingGraphMousePlugin<Room, Exit>(MouseEvent.BUTTON1_MASK,MouseEvent.BUTTON3_MASK));
        pgm.add(new TranslatingGraphMousePlugin(MouseEvent.BUTTON1_MASK));
        scaler = new ScalingGraphMousePlugin(new CrossoverScalingControl(), 0, 1 / 1.1f, 1.1f);
        pgm.add(scaler);
        mousePlugin = new MapperEditingGraphMousePlugin(this);
        pgm.add(mousePlugin);
        vv.setGraphMouse(pgm);
        panel = new SearchPanel(this);
	}
	
	public void moveToRoom(Room room){
		if(area == null || !area.getName().equals( room.getArea().getName())){
			moveToArea(room.getArea().getName());			
		}
		for(Room aRoom : graph.getVertices()){

			if(aRoom.equals(room)){
				aRoom.setPicked(true);
				aRoom.setCurrent(true);
			}else{
				aRoom.setPicked(false);
				aRoom.setCurrent(false);
			}
			
			
		}
		room.setPicked(true);
		room.setCurrent(true);
		pickedRoom = room;
//		currentRoom = room;
		this.panel.setTextForDescs(pickedRoom.getShortDesc(), pickedRoom.getLongDesc(), makeExitsStringFromPickedRoom(), pickedRoom);
		repaint();
	
	}
	
	private String makeExitsStringFromPickedRoom() {
		Collection<Exit> outExits = graph.getOutEdges(pickedRoom);
		StringBuilder exitString = new StringBuilder();
		if(outExits != null){
			Iterator<Exit> exitIterator = outExits.iterator();
		
			while(exitIterator.hasNext()){
				Exit exit = exitIterator.next();
				pickedRoom.getExits().add(exit.getExit());
			}
		}
		
		Iterator<String> roomExitIterator = pickedRoom.getExits().iterator();
		while(roomExitIterator.hasNext()){
			exitString.append(roomExitIterator.next());
			if(roomExitIterator.hasNext()){
				exitString.append(", ");
			}
		}
		

		return exitString.toString();
	}

	private void repaint() {
		vv.repaint();
	}

	protected void saveCurrentArea(){
		
	}

	public void setPanel(SearchPanel panel) {
		this.panel = panel;
	}

	protected void moveToArea(String areaName){
		AreaSaveObject areaSaveObject= null;
		try {
			areaSaveObject = AreaDataPersister.loadData(baseDir,areaName);	
		} catch (ClassNotFoundException e) {
			return;
		} catch (IOException e) {
			return;
		}
		
		this.graph = areaSaveObject.getGraph();
//		mapperLayout.setGraph(graph);
		mapperLayout.displayLoadedData(areaSaveObject);
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
		Object subject = e.getItem();
		if(subject instanceof Room){
			Room tempRoom = (Room)subject;
			if(pickedState.isPicked(tempRoom)){
				thisRoomIsPicked(tempRoom);
				repaint();		
			}
		}
	}

	private void thisRoomIsPicked(Room tempRoom) {
		drawRoomAsPicked(pickedRoom, false);	
		drawRoomAsPicked(tempRoom,true);
		pickedRoom = tempRoom;
		this.panel.setTextForDescs(pickedRoom.getShortDesc(), pickedRoom.getLongDesc(), makeExitsStringFromPickedRoom(), pickedRoom);
	}

	private void drawRoomAsPicked(Room room, boolean isPicked){
		if(room != null){
			room.setPicked(isPicked);
		}

	}
	
	@Override
	public void toggleDescs() {
		this.panel.toggleDescs();
		
	}

	@Override
	public SparseMultigraph<Room, Exit> getGraph() {
		return graph;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
		
	}

	@Override
	public void setBatWindow(BatWindow window) {
		this.batWindow = window;	
	}

	public String getBaseDir(){
		return this.baseDir;
	}

	@Override
	public VisualizationViewer<Room, Exit> getVV() {
		return this.vv;
	}

	@Override
	public void changeRoomColor(Color color) {
		if(this.pickedRoom!= null){
			this.pickedRoom.setColor(color);
			repaint();
		}
	}
	
}
