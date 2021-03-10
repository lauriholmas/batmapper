package com.glaurung.batMap.vo;

import java.io.Serializable;

public class Exit implements Serializable {


    private static final long serialVersionUID = 3983564665752135097L;
    private String exit;
    private String compassDir;
    private boolean currentExit;
    public final String TELEPORT = "teleport";


    public Exit( String exit ) {
        this.exit = exit;
        this.compassDir = this.checkWhatExitIs( exit );
    }

    private String checkWhatExitIs( String exit ) {
        if (exit.equalsIgnoreCase( "n" ) || exit.equalsIgnoreCase( "north" ))
            return "n";
        if (exit.equalsIgnoreCase( "e" ) || exit.equalsIgnoreCase( "east" ))
            return "e";
        if (exit.equalsIgnoreCase( "s" ) || exit.equalsIgnoreCase( "south" ))
            return "s";
        if (exit.equalsIgnoreCase( "w" ) || exit.equalsIgnoreCase( "west" ))
            return "w";
        if (exit.equalsIgnoreCase( "ne" ) || exit.equalsIgnoreCase( "northeast" ))
            return "ne";
        if (exit.equalsIgnoreCase( "nw" ) || exit.equalsIgnoreCase( "northwest" ))
            return "nw";
        if (exit.equalsIgnoreCase( "se" ) || exit.equalsIgnoreCase( "southeast" ))
            return "se";
        if (exit.equalsIgnoreCase( "sw" ) || exit.equalsIgnoreCase( "southwest" ))
            return "sw";
        if (exit.equalsIgnoreCase( "d" ) || exit.equalsIgnoreCase( "down" ))
            return "d";
        if (exit.equalsIgnoreCase( "u" ) || exit.equalsIgnoreCase( "up" ))
            return "u";
        return null;
    }

    public String getExit() {
        return exit;
    }

    public void setExit( String exit ) {
        this.exit = exit;
    }

    public String toString() {
        return this.exit;
    }

    public String getCompassDir() {
        return compassDir;
    }

    public boolean isCurrentExit() {
        return currentExit;
    }

    public void setCurrentExit( boolean currentExit ) {
        this.currentExit = currentExit;
    }

    public boolean equals( Object o ) {
        if (o instanceof Exit) {
            if (this.exit.equals( ( (Exit) o ).getExit() ))
                return true;
        }
        return false;

    }

}
