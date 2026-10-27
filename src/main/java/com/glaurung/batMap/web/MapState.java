package com.glaurung.batMap.web;

import java.util.ArrayList;
import java.util.List;

/**
 * Snapshot of everything the web map needs to draw one map view, sent to the browser as json.
 */
public class MapState {

    public String view;
    public long version;
    public String area;
    public boolean snap;
    public boolean maze;
    public boolean revs;
    public boolean canSave = true;
    public List<RoomState> rooms = new ArrayList<RoomState>();
    public List<ExitState> exits = new ArrayList<ExitState>();
    public SelectedRoom selected;
    public List<ColorOption> colors = new ArrayList<ColorOption>();
    public SearchState search;

    public static class RoomState {
        public String id;
        public double x;
        public double y;
        public String shortDesc;
        public String label;
        public String color;
        public boolean indoors;
        public boolean current;
        public boolean picked;
        public boolean explored;
        public boolean lightExits;
        // normalized exit markers drawn in the room: n, ne, e, se, s, sw, w, nw, u, d, special
        public List<String> exits = new ArrayList<String>();
    }

    public static class ExitState {
        public int id;
        public String from;
        public String to;
        public String name;
        public boolean current;
    }

    public static class SelectedRoom {
        public String id;
        public String shortDesc;
        public String longDesc;
        public String exits;
        public String notes;
        public String label;
        public int colorIndex;
    }

    public static class ColorOption {
        public String name;
        public String color;

        public ColorOption( String name, String color ) {
            this.name = name;
            this.color = color;
        }
    }

    public static class SearchState {
        public String text = "";
        public List<SearchResult> results = new ArrayList<SearchResult>();
        public List<String> areas = new ArrayList<String>();
    }

    public static class SearchResult {
        public String area;
        public String roomId;
        public String shortDesc;

        public SearchResult( String area, String roomId, String shortDesc ) {
            this.area = area;
            this.roomId = roomId;
            this.shortDesc = shortDesc;
        }
    }
}
