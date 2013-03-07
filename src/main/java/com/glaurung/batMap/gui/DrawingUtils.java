package com.glaurung.batMap.gui;

import java.awt.geom.Point2D;

import com.glaurung.batMap.vo.Exit;

/**
 * This clas is used to get relative positions of rooms, give it a location and exit, 
 * and it will give new location in distance to direction of exit
 * @author lauri
 *
 */
public class DrawingUtils {

	public static final int ROOM_SIZE = 90;
	
	
	public static Point2D getRelativePosition(Point2D location, Exit exit) {
		double x = getXByExit(exit, location.getX());
		double y = getYByExit(exit, location.getY());
		return new Point2D.Double(x,y);
	}
	//y going up to down
	private static double getYByExit(Exit exit, double y) {
		if(exit.getCompassDir() == null)
			return y - 3*DrawingUtils.ROOM_SIZE;
		if(exit.getCompassDir().startsWith("n"))
			return y - 2*DrawingUtils.ROOM_SIZE;
		
		if(exit.getCompassDir().startsWith("s"))
			return y + 2*DrawingUtils.ROOM_SIZE;
		
		return y;
	}

	
	//x going left to right
	private static double getXByExit(Exit exit, double x) {
		if(exit.getCompassDir() == null)
			return x + 3*DrawingUtils.ROOM_SIZE;
		
		if(exit.getCompassDir().endsWith("e"))
			return x + 2*DrawingUtils.ROOM_SIZE;
		
		if(exit.getCompassDir().endsWith("w"))
			return x - 2*DrawingUtils.ROOM_SIZE;
		
		return x;
	}

	
	
}
