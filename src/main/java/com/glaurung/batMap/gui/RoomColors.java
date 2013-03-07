package com.glaurung.batMap.gui;

import java.awt.Color;

public class RoomColors {

	public static Color NORMAL = null;
	public static Color RED = new Color(255,0,0);
	public static Color YELLOW = new Color(255,255,0);
	public static Color BLUE = new Color(0,0,255);
	public static Color PINK = new Color(255,0,255);

	
	public static String[] getColorNames(){
		String[] colorList = {"normal","red","yellow","blue", "pink"};
		return colorList;
	}
	
	public static Color[] getColors(){
		Color[] colorList = {NORMAL,RED,YELLOW,BLUE,PINK};
		return colorList;
	}
	
	
	public static int getIndex(Color color){
		if(color == null){
			return 0;
		}else if(color == RED){
			return 1;
		}else if(color == YELLOW){
			return 2;
		}else if(color == BLUE){
			return 3;
		}else if(color == PINK){
			return 4;
		}else{
			return 1000;
		}
		
	}
}
