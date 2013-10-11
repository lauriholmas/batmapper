package com.glaurung.batMap.gui;

import java.awt.FlowLayout;

import javax.swing.JFrame;

import com.glaurung.batMap.controller.SearchEngine;
import com.glaurung.batMap.gui.search.SearchPanel;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;

import edu.uci.ics.jung.graph.SparseMultigraph;

public class SearchTest1 {

    public static void main(String[] args) {

    	JFrame frame = new JFrame("");
    	frame.setLayout(new FlowLayout());
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	
    	SparseMultigraph<Room, Exit> graph = new SparseMultigraph<Room, Exit>();


		SearchEngine engine = new SearchEngine(graph);
    	
    	frame.getContentPane().add(new SearchPanel(engine));
    	frame.setSize(1200, 800);
//    	frame.pack();
    	frame.setVisible(true);

    }
}
