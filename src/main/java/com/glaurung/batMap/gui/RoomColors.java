package com.glaurung.batMap.gui;

import java.awt.Color;

public class RoomColors {
    public static final Color PICKED = Color.CYAN;
    public static final Color CURRENT = Color.RED;
    public static final Color EXIT = Color.DARK_GRAY;
    public static final Color LIGHT_EXIT = Color.LIGHT_GRAY;
    public static final Color MAZEMODE_FULLYEXPLORED = new Color(40,40,40);
    public static Color OUTDOOR = new Color(50, 150, 50);
    public static Color INDOOR = new Color(165, 165, 165);
    public static Color NORMAL = null;
    public static Color RED = new Color( 255, 0, 0 );
    public static Color YELLOW = new Color( 255, 255, 0 );
    public static Color BLUE = new Color( 0, 0, 255 );
    public static Color PINK = new Color( 255, 0, 255 );
    public static Color PURPLE = new Color( 160, 32, 240 );
    public static Color ORANGE = new Color( 255, 145, 0 );
    public static Color BROWN = new Color( 139, 69, 19 );
    public static Color TURQUOISE = new Color( 64, 224, 208 );
    public static Color IVORY = new Color( 255, 255, 240 );


    public static String[] getColorNames() {
        String[] colorList = { "normal", "red", "yellow", "blue", "pink", "purple", "orange", "brown", "turquoise", "ivory" };
        return colorList;
    }

    public static Color[] getColors() {
        Color[] colorList = { NORMAL, RED, YELLOW, BLUE, PINK, PURPLE, ORANGE, BROWN, TURQUOISE, IVORY };
        return colorList;
    }


    public static int getIndex( Color color ) {
        if (color == null) {
            return 0;
        } else if (color.equals( RED )) {
            return 1;
        } else if (color.equals( YELLOW )) {
            return 2;
        } else if (color.equals( BLUE )) {
            return 3;
        } else if (color.equals( PINK )) {
            return 4;
        } else if (color.equals( PURPLE )) {
            return 5;
        } else if (color.equals( ORANGE )) {
            return 6;
        } else if (color.equals( BROWN )) {
            return 7;
        } else if (color.equals( TURQUOISE )) {
            return 8;
        } else if (color.equals( IVORY )) {
            return 9;
        } else {
            return 0; // Unsupported color, default to 0 (instead of previously value 1000 which caused out of bounds exception in rare occasions). 
        }

    }
}
