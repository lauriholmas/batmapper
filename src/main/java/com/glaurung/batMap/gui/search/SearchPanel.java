package com.glaurung.batMap.gui.search;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.glaurung.batMap.controller.SearchEngine;
import com.glaurung.batMap.gui.MapperPanel;
import com.glaurung.batMap.io.AreaDataPersister;
import com.glaurung.batMap.vo.Area;
import com.glaurung.batMap.vo.AreaSaveObject;
import com.glaurung.batMap.vo.Room;

public class SearchPanel extends MapperPanel implements ItemListener {

    private static final long serialVersionUID = 1L;

    private final int ELEMENT_HEIGHT = 25;
    private JTextField searchText = new JTextField();
    private SearchEngine engine;
    private DefaultComboBoxModel model = new DefaultComboBoxModel();
    private JComboBox results = new JComboBox( model );
    private JButton save = new JButton( "Save" );

    private DefaultComboBoxModel listAllModel = new DefaultComboBoxModel();
    private JComboBox areaList = new JComboBox( listAllModel );

    public SearchPanel( SearchEngine engine ) {
        super( engine );
        this.engine = engine;
        this.engine.setPanel( this );
        this.searchText.addActionListener( this );
        results.addItemListener( this );
        this.setLayout( null );
        this.BORDERLINE = ELEMENT_HEIGHT + 14;
        searchText.setToolTipText( "Input desc to search here" );
        results.setToolTipText( "Click on a result to see map" );
        save.addActionListener( this );
        areaList.addItemListener( this );
        areaList.setToolTipText( "Select area from list to view map" );
    }


    @Override
    public void componentResized( ComponentEvent e ) {
        super.componentResized( e );
        searchText.setBounds( 20, 7, 120, ELEMENT_HEIGHT );
        results.setBounds( 20 + searchText.getWidth() + 20, 7, 350, ELEMENT_HEIGHT );
        areaList.setBounds( 20 + searchText.getWidth() + 20 + results.getWidth() + 20, 7, 150, ELEMENT_HEIGHT );
        save.setBounds( 20 + searchText.getWidth() + 20 + results.getWidth() + 20 + areaList.getWidth() + 20, 7, 70, ELEMENT_HEIGHT );
        this.add( searchText );
        this.add( results );
        this.add( areaList );
        this.add( save );
        populateAreaList();
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        if (e.getSource() == searchText) {
            searchForRoomsWith( searchText.getText().trim() );
            return;
        } else if (e.getSource() == save) {
            this.engine.save();
        }
        super.actionPerformed( e );
    }


    public void setSearchText( String text ) {
        searchText.setText(text);
    }

    public List<String> searchForRoomsWith( String text ) {
        List<String> foundRooms = new LinkedList<String>();

        model.removeAllElements();
        model.addElement( new SearchResultItem( new Room( "results", "first slot placeholder", new Area( "Search" ) ) ) );
        if (text.equals( "" )) {
            return foundRooms;
        }
        //iterate through all areafiles, iterate through all rooms and look for texts, if matches, add to list
        List<String> areas = AreaDataPersister.listAreaNames( this.engine.getBaseDir() );
        try {
            for (String areaName : areas) {
                AreaSaveObject aso = AreaDataPersister.loadData( this.engine.getBaseDir(), areaName );
                Collection<Room> areaRooms = aso.getGraph().getVertices();
                for (Room room : areaRooms) {
                    if (room.getLongDesc().toLowerCase().contains( text.toLowerCase() ) ||
                            room.getShortDesc().toLowerCase().contains( text.toLowerCase() )) {
                        model.addElement( new SearchResultItem( room ) );
                        
                        String roomString = room.getArea().getName() + ": " + room.getShortDesc();
                        if (!foundRooms.contains(roomString)) {
                            foundRooms.add(roomString);
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return foundRooms;
    }

    private void populateAreaList() {
        listAllModel.removeAllElements();
        listAllModel.addElement( new AreaListItem( new Room( "Area", "first slot placeholder", new Area( "Areas list" ) ) ) );
        List<String> areas = AreaDataPersister.listAreaNames( this.engine.getBaseDir() );
        try {
            for (String areaName : areas) {
                AreaSaveObject aso = AreaDataPersister.loadData( this.engine.getBaseDir(), areaName );
                Collection<Room> areaRooms = aso.getGraph().getVertices();
                if (areaRooms.size() > 0) {
                    listAllModel.addElement( new AreaListItem( areaRooms.iterator().next() ) );
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    public void toggleSaveAbility( boolean canSave ) {
        save.setEnabled( canSave );
    }


    @Override
    public void itemStateChanged( ItemEvent e ) {
        if (e.getSource() == results && e.getStateChange() == ItemEvent.SELECTED) {
            if (this.results.getSelectedIndex() == 0) {
                return;
            }
            SearchResultItem item = (SearchResultItem) this.model.getElementAt( this.results.getSelectedIndex() );
            if (item != null) {
                this.engine.moveToRoom( item.getRoom(), true );
            }
        } else if (e.getSource() == areaList && e.getStateChange() == ItemEvent.SELECTED) {
            if (this.areaList.getSelectedIndex() == 0) {
                return;
            }
            AreaListItem item = (AreaListItem) this.listAllModel.getElementAt( this.areaList.getSelectedIndex() );
            if (item != null) {
                this.engine.moveToRoom( item.getRoom(), false );
            }
        }

    }

}
