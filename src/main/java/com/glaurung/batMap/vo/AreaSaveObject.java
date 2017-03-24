package com.glaurung.batMap.vo;

import java.awt.geom.Point2D;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import edu.uci.ics.jung.graph.SparseMultigraph;

public class AreaSaveObject implements Serializable {


    private static final long serialVersionUID = - 787030872360880875L;

    private SparseMultigraph<Room, Exit> graph;
    private Map<Room, Point2D> locations;
    private String fileName;

    public AreaSaveObject() {
        this.graph = new SparseMultigraph<Room, Exit>();
        this.locations = new HashMap<Room, Point2D>();
    }

    public SparseMultigraph<Room, Exit> getGraph() {
        return graph;
    }

    public void setGraph( SparseMultigraph<Room, Exit> graph ) {
        this.graph = graph;
    }

    public Map<Room, Point2D> getLocations() {
        return locations;
    }

    public void setLocations( Map<Room, Point2D> locations ) {
        this.locations = locations;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName( String fileName ) {
        this.fileName = fileName;
    }


}
