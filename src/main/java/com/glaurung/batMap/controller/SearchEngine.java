package com.glaurung.batMap.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.glaurung.batMap.io.AreaDataPersister;
import com.glaurung.batMap.vo.AreaSaveObject;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;
import com.glaurung.batMap.web.MapState;

import edu.uci.ics.jung.graph.SparseMultigraph;

/**
 * Engine for the map search view, which shows saved maps of any area.
 */
public class SearchEngine extends MapperEngine {

    private String areaName = "";
    private String mapperArea;
    private boolean savingAlreadyDisabled = false;
    private boolean canSave = true;
    private String searchText = "";
    private List<MapState.SearchResult> results = new ArrayList<MapState.SearchResult>();

    public SearchEngine( SparseMultigraph<Room, Exit> graph, MapperPlugin plugin ) {
        this(plugin);
        this.graph = graph;
    }

    public SearchEngine(MapperPlugin plugin) {
        super(plugin);
    }

    @Override
    public String getViewName() {
        return "search";
    }

    /**
     * Shows the map of an area, with the given room highlighted
     *
     * @param areaName area to show
     * @param roomId   room to highlight, or null to not highlight any
     */
    public synchronized void showRoom( String areaName, String roomId ) {
        moveToArea( areaName );

        currentRoom = null;
        for (Room aRoom : graph.getVertices()) {

            if (roomId != null && aRoom.getId().equals( roomId )) {
                currentRoom = aRoom;
                aRoom.setPicked( true );
                aRoom.setCurrent( true );
            } else {
                aRoom.setPicked( false );
                aRoom.setCurrent( false );
            }
        }
        fireChanged();
    }

    @Override
    public synchronized void save() {
        if (canSave) {
            saveCurrentArea();
        }
    }

    @Override
    protected synchronized void moveToArea( String areaName ) {
        AreaSaveObject areaSaveObject = null;
        try {
            areaSaveObject = AreaDataPersister.loadData( baseDir, areaName );
        } catch (ClassNotFoundException e) {
            return;
        } catch (IOException e) {
            return;
        }
        if (areaSaveObject.getGraph().getVertexCount() == 0) {
            return;
        }
        displayLoadedData( areaSaveObject );
        this.area = this.graph.getVertices().iterator().next().getArea();
        this.areaName = this.area.getName();
        this.currentRoom = null;
        if (! this.areaName.equalsIgnoreCase( this.mapperArea )) {
            savingAlreadyDisabled = false;
        }
        checkIfCanSaveMap();
        fireChanged();
    }

    public void checkIfCanSaveMap() {
        if (this.areaName.equalsIgnoreCase( this.mapperArea ) || savingAlreadyDisabled) {
            canSave = false;
            savingAlreadyDisabled = true;
        } else {
            canSave = true;
        }

    }

    public synchronized void setMapperArea( String areaName ) {
        boolean couldSave = canSave;
        this.mapperArea = areaName;
        checkIfCanSaveMap();
        if (couldSave != canSave) {
            fireChanged();
        }
    }

    /**
     * Searches all saved areas for rooms with the text in their short or long desc.
     * Results are shown in the search view.
     *
     * @return list of "area: short desc" lines of found rooms
     */
    public synchronized List<String> searchForRoomsWith( String text ) {
        List<String> foundRooms = new LinkedList<String>();
        searchText = text;
        results = new ArrayList<MapState.SearchResult>();
        if (text.equals( "" )) {
            fireChanged();
            return foundRooms;
        }
        //iterate through all areafiles, iterate through all rooms and look for texts, if matches, add to list
        for (String areaFileName : AreaDataPersister.listAreaNames( baseDir )) {
            AreaSaveObject aso;
            try {
                aso = AreaDataPersister.loadData( baseDir, areaFileName );
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                continue;
            }
            Collection<Room> areaRooms = aso.getGraph().getVertices();
            for (Room room : areaRooms) {
                if (room.getShortDesc() == null || room.getLongDesc() == null ) {
                    // in case of bad room data, just skip it
                    continue;
                }
                if (room.getLongDesc().toLowerCase().contains( text.toLowerCase() ) ||
                        room.getShortDesc().toLowerCase().contains( text.toLowerCase() )) {
                    results.add( new MapState.SearchResult( room.getArea().getName(), room.getId(), room.getShortDesc() ) );

                    String roomString = room.getArea().getName() + ": " + room.getShortDesc();
                    if (!foundRooms.contains(roomString)) {
                        foundRooms.add(roomString);
                    }
                }
            }
        }
        fireChanged();
        return foundRooms;
    }

    @Override
    public synchronized MapState getState() {
        MapState state = super.getState();
        state.canSave = canSave;
        state.search = new MapState.SearchState();
        state.search.text = searchText;
        state.search.results = results;
        state.search.areas = AreaDataPersister.listAreaNames( baseDir );
        return state;
    }

}
