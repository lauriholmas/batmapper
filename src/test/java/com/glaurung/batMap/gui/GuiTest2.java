package com.glaurung.batMap.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.glaurung.batMap.controller.MapperEngine;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;

import edu.uci.ics.jung.graph.SparseMultigraph;

public class GuiTest2 implements ActionListener{

	static JButton button;
	static JButton sbutton;
	static JButton lbutton;
	static JTextField field;
	
    public static void main(String[] args) {
    	SparseMultigraph<Room, Exit> graph = new SparseMultigraph<Room, Exit>();


		MapperEngine engine = new MapperEngine(graph);

    	JFrame frame = new JFrame("Simple Graph View");
    	frame.setLayout(new FlowLayout());
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.getContentPane().add(engine.getPanel());

    	frame.pack();
    	frame.setVisible(true);
    	
    	HashSet<String> exits = new HashSet<String>();
    	exits.add("ne"); exits.add("n");
    	engine.moveToRoom("testArea", "1", "enter path", false, "areaFirstRoom", "jshgfskgyfhsgfjhtsgdfhgsdfsygfsgdyfuysgdfjkhsgdfskuygfsuygfusygfuysd", exits);
    	exits.clear(); exits.add("ne"); exits.add("n");exits.add("w"); exits.add("sw");
    	engine.moveToRoom("testArea", "2", "n", false, "secondRoom", "jshgfskgyfhsgfjhtsgdfhgsdfsygfsgdyfuysgdfjkhsgdfskuygfsuygfusygfuysd", exits);
    	exits.clear(); exits.add("ne"); exits.add("n");exits.add("w"); exits.add("sw");
    	engine.moveToRoom("testArea", "3", "e", true, "thirdRoom", "jshgfskgyfhsgfjhtsgdfhgsdfsygfsgdyfuysgdfjkhsgdfskuygfsuygfusygfuysd", exits);
    	exits.clear(); exits.add("ne"); exits.add("n");exits.add("w"); exits.add("sw");
    	engine.moveToRoom("testArea", "4", "s", true, "fourthRoom", "jshgfskgyfhsgfjhtsgdfhgsdfsygfsgdyfuysgdfjkhsgdfskuygfsuygfusygfuysd", exits);
    	exits.clear(); exits.add("w"); exits.add("w");exits.add("e"); exits.add("e");
    	engine.moveToRoom("testArea", "5", "w", true, "fifthRoom", "jshgfskgyfhsgfjhtsgdfhgsdfsygfsgdyfuysgdfjkhsgdfskuygfsuygfusygfuysd", exits);
    	exits.clear(); exits.add("ne"); exits.add("nw");exits.add("sw"); exits.add("se");
    	engine.moveToRoom("testArea", "6", "n", false, "sixthRoom", "jshgfskgyfhsgfjhtsgdfhgsdfsygfsgdyfuysgdfjkhsgdfskuygfsuygfusygfuysd", exits);
    	exits.clear(); exits.add("ne"); exits.add("nw");exits.add("sw"); exits.add("se");
    	engine.moveToRoom("testArea", "7", "s", true, "seventhRoom", "jshgfskgyfhsgfjhtsgdfhgsdfsygfsgdyfuysgdfjkhsgdfskuygfsuygfusygfuysd", exits);
//    	exits.clear(); exits.add("ne"); exits.add("nw");exits.add("sw"); exits.add("se");
//    	engine.moveToRoom("testArea", "1", "jump to start", false, "areaFirstRoom", "jshgfskgyfhsgfjhtsgdfhgsdfsygfsgdyfuysgdfjkhsgdfskuygfsuygfusygfuysd", exits);

    }

    
	@Override
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		if(source.equals(sbutton)){
			
		}
		
	}
}
