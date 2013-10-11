package com.glaurung.batMap.gui.search;

import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import com.glaurung.batMap.controller.SearchEngine;
import com.glaurung.batMap.gui.MapperPanel;
import com.glaurung.batMap.io.AreaDataPersister;
import com.glaurung.batMap.vo.AreaSaveObject;
import com.glaurung.batMap.vo.Room;

public class SearchPanel extends MapperPanel {

	private static final long serialVersionUID = 1L;

	private final int ELEMENT_HEIGHT=25;
	private JTextField searchText = new JTextField();
	private SearchEngine engine;
	private DefaultComboBoxModel model = new DefaultComboBoxModel();
	private JComboBox results = new JComboBox(model);
	
	public SearchPanel(SearchEngine engine) {
		super(engine);
		this.engine = engine;
		this.searchText.addActionListener(this);
		results.addActionListener(this);
		this.setLayout(null);
		this.BORDERLINE=ELEMENT_HEIGHT+14;
		searchText.setToolTipText("Input desc to search here");
		results.setToolTipText("Click on a result to see map");
		
	}

	
	
	@Override
	public void componentResized(ComponentEvent e) {
		super.componentResized(e);
		searchText.setBounds(20, 7, 120, ELEMENT_HEIGHT);
		results.setBounds(20+searchText.getWidth()+20, 7, 500, ELEMENT_HEIGHT);
		this.add(searchText);
		this.add(results);

	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(e.getSource()==searchText){
			searchForRoomsWith(searchText.getText().trim());
			return;
		}else if(e.getSource() == results){
			SearchResultItem item = (SearchResultItem) this.model.getElementAt(this.results.getSelectedIndex());

			this.engine.moveToRoom(item.getRoom());
			return;
		}
		super.actionPerformed(e);
	}



	private void searchForRoomsWith(String text) {
		model.removeAllElements();
		if(text.equals("")){
			return;
		}
		//iterate through all areafiles, iterate through all rooms and look for texts, if matches, add to list
		List<String> areas = AreaDataPersister.listAreaNames(this.engine.getBaseDir());
		try {
			for(String areaName : areas){
				AreaSaveObject aso = AreaDataPersister.loadData(this.engine.getBaseDir(), areaName);
				Collection<Room> areaRooms = aso.getGraph().getVertices();
				for(Room room: areaRooms){
					if(room.getLongDesc().contains(text)){
						model.addElement(new SearchResultItem(room));
					}
				}
			}
		}catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}

	}

}
