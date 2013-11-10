package com.glaurung.batMap.gui;

import java.awt.FlowLayout;

import javax.swing.JFrame;

import com.glaurung.batMap.gui.manual.ManualPanel;

public class ManualTest1{

    public static void main(String[] args) {

    	JFrame frame = new JFrame("Simple Graph View");
    	frame.setLayout(new FlowLayout());
    	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    	frame.getContentPane().add(new ManualPanel());

    	frame.pack();
    	frame.setVisible(true);

    }


}
