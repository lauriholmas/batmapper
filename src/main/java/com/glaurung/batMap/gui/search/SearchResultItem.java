package com.glaurung.batMap.gui.search;

import com.glaurung.batMap.vo.Room;

public class SearchResultItem {

	public SearchResultItem(Room room){
		this.room = room;
	}
	
	private Room room;

	public String toString(){
		return this.room.getArea()+" - "+this.room.getShortDesc();
	}
	
	public Room getRoom(){
		return this.room;
	}
}
