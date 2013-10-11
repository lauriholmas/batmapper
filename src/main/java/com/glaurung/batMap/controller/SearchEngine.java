package com.glaurung.batMap.controller;

import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;

import edu.uci.ics.jung.graph.SparseMultigraph;

public class SearchEngine extends MapperEngine{

	public SearchEngine(){
		super();
	}
	
	public SearchEngine(SparseMultigraph<Room, Exit> graph){
		super(graph);
	}
	
	public void moveToRoom(Room room){
		//TODO: implement it! probably mainly need to load area and set room as current or so
		System.out.println("movinr to room not implemented yet");
		
	}
	
}
