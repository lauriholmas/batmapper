package com.glaurung.batMap.vo;

import java.awt.Color;
import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Room implements Serializable {


    private static final long serialVersionUID = 9036581185666106041L;
    private String id;
    private String shortDesc;
    private String longDesc;
    private boolean areaEntrance = false;
    private boolean current = false;
    private boolean drawn = false;
    private Area area;
    private boolean picked = false;
    Set<String> exits = new HashSet<String>();
    private boolean indoors;
    private String notes;
    private Color color = null;


    public Room( String shortDesc, String id ) {
        this.shortDesc = shortDesc;
        this.id = id;
    }

    public Room( String shortDesc, String id, Area area ) {
        this.shortDesc = shortDesc;
        this.id = id;
        this.area = area;
    }

    public Room( String id, Area area ) {
        this.id = id;
        this.area = area;
    }

    public String getId() {
        return id;
    }

    public void setId( String id ) {
        this.id = id;
    }

    public String getShortDesc() {
        return shortDesc;
    }

    public void setShortDesc( String shortDesc ) {
        this.shortDesc = shortDesc;
    }

    public String getLongDesc() {
        return longDesc;
    }

    public void setLongDesc( String longDesc ) {
        this.longDesc = longDesc;
    }


    public boolean isAreaEntrance() {
        return areaEntrance;
    }


    public void setAreaEntrance( boolean areaEntrance ) {
        this.areaEntrance = areaEntrance;
    }


    public boolean isCurrent() {
        return current;
    }


    public void setCurrent( boolean current ) {
        this.current = current;
    }


    public boolean isDrawn() {
        return drawn;
    }


    public void setDrawn( boolean drawn ) {
        this.drawn = drawn;
    }


    public Area getArea() {
        return area;
    }

    public void setArea( Area area ) {
        this.area = area;
    }

    public boolean isPicked() {
        return picked;
    }

    public void setPicked( boolean picked ) {
        this.picked = picked;
    }

    public Set<String> getExits() {
        return exits;
    }

    public void setExits( Set<String> exits ) {
        this.exits = exits;
    }

    public boolean isIndoors() {
        return indoors;
    }

    public void setIndoors( boolean indoors ) {
        this.indoors = indoors;
    }

    public String toString() {
        return this.shortDesc;
    }

    public boolean equals( Object o ) {
        if (o instanceof Room) {
            if (this.id.equals( ( (Room) o ).getId() ))
                return true;
        }
        return false;

    }

    public void setDescs( String shortDesc, String longDesc ) {
        this.shortDesc = shortDesc;
        this.longDesc = longDesc;
    }

    public void addExits( Collection<String> outExits ) {
        this.exits.addAll( outExits );
    }

    public void addExit( String exit ) {
        this.exits.add( exit );
    }

    public String getNotes() {
        return notes;
    }

    public Color getColor() {
        return color;
    }

    public void setColor( Color color ) {
        this.color = color;
    }

    public void setNotes( String notes ) {
        this.notes = notes;
    }


}
