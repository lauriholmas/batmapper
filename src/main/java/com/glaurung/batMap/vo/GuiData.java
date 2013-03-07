package com.glaurung.batMap.vo;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;

public class GuiData implements Serializable{


	private static final long serialVersionUID = 1L;

	public GuiData(Point location, Dimension size){
		this.location = location;
		this.size = size;
	}
	
	
	private Point location;
	private Dimension size;
	
	public int getX(){
		return this.location.x;
	}
	
	public int getY(){
		return this.location.y;
	}
	
	public int getHeight(){
		return this.size.height;
	}
	
	public int getWidth(){
		return this.size.width;
	}

}
