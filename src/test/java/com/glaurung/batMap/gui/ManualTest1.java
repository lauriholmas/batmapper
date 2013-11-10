package com.glaurung.batMap.gui;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import com.glaurung.batMap.controller.MapperEngine;
import com.glaurung.batMap.gui.manual.ManualPanel;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;

import edu.uci.ics.jung.graph.SparseMultigraph;

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
