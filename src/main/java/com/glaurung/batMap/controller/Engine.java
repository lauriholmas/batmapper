package com.glaurung.batMap.controller;

import java.awt.Color;

import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;
import com.mythicscape.batclient.interfaces.BatWindow;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;

public abstract class Engine {

	public abstract void toggleDescs();
	
	public abstract SparseMultigraph<Room, Exit> getGraph();
	
	public abstract void setBatWindow(BatWindow window);

	public abstract VisualizationViewer<Room, Exit> getVV();

	public abstract void changeRoomColor(Color color);
	
}
