package com.glaurung.batMap.controller;

import java.awt.event.ItemListener;
import java.io.IOException;
import java.sql.Savepoint;

import com.glaurung.batMap.gui.search.SearchPanel;
import com.glaurung.batMap.io.AreaDataPersister;
import com.glaurung.batMap.vo.AreaSaveObject;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;

import edu.uci.ics.jung.graph.SparseMultigraph;

public class SearchEngine extends MapperEngine implements ItemListener {

    private String areaName = "";
    private String mapperArea;
    private boolean savingAlreadyDisabled = false;

    public SearchEngine( SparseMultigraph<Room, Exit> graph, MapperPlugin plugin ) {
        this(plugin);
        this.graph = graph;
        this.mapperLayout.setGraph( graph );
    }

    public SearchEngine(MapperPlugin plugin) {
        super(plugin);
        panel = new SearchPanel( this );
    }

    public void moveToRoom( Room room, boolean highlight ) {
        moveToArea( room.getArea().getName() );

        for (Room aRoom : graph.getVertices()) {

            if (aRoom.equals( room ) && highlight) {
                currentRoom = aRoom;
                aRoom.setPicked( true );
                aRoom.setCurrent( true );
            } else {
                aRoom.setPicked( false );
                aRoom.setCurrent( false );
            }
        }

        singleRoomPicked(room);
//        pickedRoom = room;
//        this.panel.setTextForDescs( pickedRoom.getShortDesc(), pickedRoom.getLongDesc(), makeExitsStringFromPickedRoom(), pickedRoom );
        repaint();
        moveMapToStayWithCurrentRoom();
    }

    public void save() {
        try {
            if (this.area != null) {
                AreaDataPersister.save( baseDir, graph, mapperLayout );
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void setPanel( SearchPanel panel ) {
        this.panel = panel;
    }

    public void moveToArea( String areaName ) {
        AreaSaveObject areaSaveObject = null;
        try {
            areaSaveObject = AreaDataPersister.loadData( baseDir, areaName );
        } catch (ClassNotFoundException e) {
            return;
        } catch (IOException e) {
            return;
        }
        this.areaName = areaName;
        this.graph = areaSaveObject.getGraph();
        this.area = this.graph.getVertices().iterator().next().getArea();
        mapperLayout.displayLoadedData( areaSaveObject );
        if (! this.areaName.equalsIgnoreCase( this.mapperArea )) {
            savingAlreadyDisabled = false;
        }
        checkIfCanSaveMap();
    }


    public void setBaseDir( String baseDir ) {
        this.baseDir = baseDir;

    }

    public String getBaseDir() {
        return this.baseDir;
    }

    public void checkIfCanSaveMap() {
        if (this.areaName.equalsIgnoreCase( this.mapperArea ) || savingAlreadyDisabled) {
            SearchPanel tempPanel = (SearchPanel) this.panel;
            tempPanel.toggleSaveAbility( false );
            savingAlreadyDisabled = true;
        } else {
            SearchPanel tempPanel = (SearchPanel) this.panel;
            tempPanel.toggleSaveAbility( true );
        }

    }

    public void setMapperArea( String areaName ) {
        this.mapperArea = areaName;
        checkIfCanSaveMap();
    }

}
