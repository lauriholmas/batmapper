package com.glaurung.batMap.gui.manual;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.apache.commons.io.IOUtils;

public class ManualPanel extends JPanel implements ComponentListener{

	private static final long serialVersionUID = 8922764153155463898L;
	private JTextArea manualTextArea = new JTextArea();
	private final String MANUAL_FILE = "/manual.txt";
	private final Color TEXT_COLOR = Color.LIGHT_GRAY;
	private final Color BG_COLOR = Color.BLACK;
	private final int BORDERLINE=7;
	private JScrollPane scrollPane;
	
	
	public ManualPanel(){
		this.addComponentListener(this);
		this.setLayout(null);
		manualTextArea.setWrapStyleWord(true);

		manualTextArea.setText(readManual());
		manualTextArea.setEditable(false);
		scrollPane = new JScrollPane(manualTextArea);
		scrollPane.setPreferredSize(new Dimension(800, 534));
		scrollPane.setBounds(BORDERLINE, BORDERLINE, 800, 534);
		this.setPreferredSize(new Dimension(800, 534));
		this.setBackground(BG_COLOR);
		manualTextArea.setForeground(TEXT_COLOR);
		manualTextArea.setBackground(BG_COLOR);
		manualTextArea.setLineWrap(true);
		this.add(scrollPane);
	}
	
	private String readManual(){
		
		try{
			InputStream stream = getClass().getResourceAsStream(MANUAL_FILE);
			return IOUtils.toString(stream);
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void componentHidden(ComponentEvent e) {

	}

	@Override
	public void componentMoved(ComponentEvent e) {
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		this.scrollPane.setBounds(BORDERLINE, BORDERLINE, this.getWidth()-BORDERLINE*2, this.getHeight()-BORDERLINE*2);
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
	
	}
	
	
}
