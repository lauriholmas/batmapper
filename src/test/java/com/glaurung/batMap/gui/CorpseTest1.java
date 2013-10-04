package com.glaurung.batMap.gui;

import java.awt.FlowLayout;

import javax.swing.JFrame;

import com.glaurung.batMap.gui.corpses.CorpsePanel;

public class CorpseTest1{

    public static void main(String[] args) {

    	JFrame frame = new JFrame("");
    	frame.setLayout(new FlowLayout());
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.getContentPane().add(new CorpsePanel("", new MockPlugin()));
    	frame.setSize(1200, 800);
//    	frame.pack();
    	frame.setVisible(true);

    }


}
