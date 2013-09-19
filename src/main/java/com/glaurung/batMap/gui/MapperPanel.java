package com.glaurung.batMap.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.Point2D;

import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.glaurung.batMap.controller.MapperEngine;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;

import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * This class contains the gui placement etc for the damn mapper into 
 * @author lauri
 *
 */
public class MapperPanel extends JPanel implements ComponentListener, DocumentListener, ActionListener{


	private static final long serialVersionUID = 3042867076711631076L;
	
	private VisualizationViewer<Room, Exit> vv;

	public final Dimension MIN_PANEL_SIZE=new Dimension(820, 550);
	private final int DESC_WIDTH = 280;
	private final int SHORT_DESC_HEIGHT=40;
	private final int LONG_DESC_HEIGHT=270;
	private final int EXITS_HEIGHT=40;
	private final int NOTES_HEIGHT=140;
	private final int BORDERLINE=7;
	private final int BUTTON_HEIGHT=25;
	private final int BUTTON_WIDTH=100;
	
	private final Color BORDER_COLOR = Color.WHITE;
	private final Color TEXT_COLOR = Color.LIGHT_GRAY;
	private final Color BG_COLOR = Color.BLACK;
	
	JTextArea roomShortDesc = new JTextArea();
	JTextArea roomLongDesc = new JTextArea();
	JTextArea roomExits = new JTextArea();
	JTextArea roomNotes = new JTextArea();
	JScrollPane scrollableNotes = new JScrollPane(roomNotes, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
	JPanel descPanel = new JPanel();
	private Room room = new Room("", "");
	private Font font = new Font("Consolas",Font.PLAIN,14);
	
	private boolean visibleDescs=true;
	
	private JComboBox roomColor;
	MapperEngine engine;
	

	
	public MapperPanel(MapperEngine engine){
		super();
		roomShortDesc.setWrapStyleWord(true);
		roomLongDesc.setWrapStyleWord(true);
		roomExits.setWrapStyleWord(true);
		roomNotes.setWrapStyleWord(true);
		this.setPreferredSize(new Dimension(800, 534));
		this.setMinimumSize(MIN_PANEL_SIZE);
		this.engine=engine;
		this.vv = engine.getVV();
		this.addComponentListener(this);
		this.setBackground(BG_COLOR);
		this.setLayout(null);

		vv.setBorder(new LineBorder(BORDER_COLOR));
		vv.setBackground(BG_COLOR);
		vv.setBounds(7, 7, 500, 500);
		
		descPanel.setBounds(514, 7, DESC_WIDTH, SHORT_DESC_HEIGHT+LONG_DESC_HEIGHT+EXITS_HEIGHT+NOTES_HEIGHT+4*BORDERLINE);
		descPanel.setAlignmentX(Component.RIGHT_ALIGNMENT);
		descPanel.setLayout(null);
		descPanel.setBackground(BG_COLOR);
		
		roomShortDesc.setBounds(0, 0, DESC_WIDTH, SHORT_DESC_HEIGHT);
		roomShortDesc.setColumns(25);
		roomShortDesc.setBorder(new LineBorder(BORDER_COLOR));
		roomShortDesc.setAlignmentY(Component.TOP_ALIGNMENT);
		roomShortDesc.setEditable(false);
		roomShortDesc.setLineWrap(true);
		roomShortDesc.setForeground(TEXT_COLOR);
//		roomShortDesc.setText("This is an example shortish room descThis is an example shortish room descThis is an example shortish room descThis is an example shortish room desc");
		roomShortDesc.setBackground(BG_COLOR);
		roomShortDesc.setFont(font);
		roomShortDesc.setToolTipText("This is room short description");
		
		descPanel.add(roomShortDesc);
		
		
		roomLongDesc.setBounds(0, SHORT_DESC_HEIGHT+BORDERLINE, DESC_WIDTH, LONG_DESC_HEIGHT);
		roomLongDesc.setEditable(false);
		roomLongDesc.setColumns(25);
		roomLongDesc.setBorder(new LineBorder(BORDER_COLOR));
		roomLongDesc.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		roomLongDesc.setLineWrap(true);
//		roomLongDesc.setText("Two well-traveled roads cross paths here. One road leads north to a great black castle on a hill. To the west, the road leads to a mountain ridge. Taking the east path will lead you through a forest, and the south path will lead to more familiar areas.\r\n");
		roomLongDesc.setBackground(BG_COLOR);
		roomLongDesc.setForeground(TEXT_COLOR);
		roomLongDesc.setFont(font);
		descPanel.add(roomLongDesc);
		roomLongDesc.setToolTipText("This is room long description");
		
		roomExits.setBounds(0, SHORT_DESC_HEIGHT+BORDERLINE+LONG_DESC_HEIGHT+BORDERLINE, DESC_WIDTH, EXITS_HEIGHT);
		roomExits.setEditable(false);
		roomExits.setColumns(25);
		roomExits.setBorder(new LineBorder(BORDER_COLOR));
		roomExits.setLineWrap(true);
//		roomExits.setText("n,s,e,ne");
		roomExits.setBackground(BG_COLOR);
		roomExits.setForeground(TEXT_COLOR);
		roomExits.setFont(font);
		descPanel.add(roomExits);
		roomExits.setToolTipText("This lists room exits");
			
		roomColor = new JComboBox(RoomColors.getColorNames());
		descPanel.add(roomColor);
		roomColor.setFont(font);
		roomColor.addActionListener(this);
		roomColor.setEditable(false);
		roomColor.setBounds(BUTTON_WIDTH+BORDERLINE, SHORT_DESC_HEIGHT+BORDERLINE+LONG_DESC_HEIGHT+BORDERLINE+EXITS_HEIGHT+BORDERLINE, BUTTON_WIDTH, BUTTON_HEIGHT);
		roomColor.setToolTipText("Use this to change room color to highlight it");
		roomColor.setRenderer(new MapperPanelCellRenderer());
		
		roomNotes.setBounds(0, BORDERLINE+SHORT_DESC_HEIGHT+BORDERLINE+LONG_DESC_HEIGHT+BORDERLINE+EXITS_HEIGHT+BORDERLINE+BUTTON_HEIGHT+BORDERLINE, DESC_WIDTH, NOTES_HEIGHT);
		roomNotes.setEditable(true);
		roomNotes.setColumns(25);
		roomNotes.setBorder(new LineBorder(BORDER_COLOR));
		roomNotes.setLineWrap(true);
		roomNotes.setBackground(BG_COLOR);
		roomNotes.setForeground(TEXT_COLOR);
		roomNotes.getDocument().addDocumentListener(this);
		roomNotes.setFont(font);
		
//		descPanel.add(roomNotes);
		roomNotes.setToolTipText("Feel free to write your own notes here.");
		descPanel.add(scrollableNotes);
		
		
		this.setLayout(null);
		this.setBorder(new LineBorder(BORDER_COLOR));
		this.add(vv);
		this.add(descPanel);
		this.setVisible(true);
	}
	
	@Override
	public void componentHidden(ComponentEvent e) {

	}

	@Override
	public void componentMoved(ComponentEvent e) {
		
	}

	@Override
	public void componentResized(ComponentEvent e) {

		if(visibleDescs){
			vv.setBounds(7, 7, this.getWidth()-(DESC_WIDTH+32), this.getHeight()-14);
//			roomNotes.setBounds(0, SHORT_DESC_HEIGHT+BORDERLINE+LONG_DESC_HEIGHT+BORDERLINE+EXITS_HEIGHT+BORDERLINE+BUTTON_HEIGHT+BORDERLINE, DESC_WIDTH, descPanel.getHeight()-(4*BORDERLINE+LONG_DESC_HEIGHT+EXITS_HEIGHT+SHORT_DESC_HEIGHT+BUTTON_HEIGHT));
			scrollableNotes.setBounds(0, SHORT_DESC_HEIGHT+BORDERLINE+LONG_DESC_HEIGHT+BORDERLINE+EXITS_HEIGHT+BORDERLINE+BUTTON_HEIGHT+BORDERLINE, DESC_WIDTH, descPanel.getHeight()-(4*BORDERLINE+LONG_DESC_HEIGHT+EXITS_HEIGHT+SHORT_DESC_HEIGHT+BUTTON_HEIGHT));
			descPanel.setBounds(this.getWidth()-(20+DESC_WIDTH), 7, DESC_WIDTH, this.getHeight()-14);
		}else{
			vv.setBounds(7, 7, this.getWidth()-(2*BORDERLINE), this.getHeight()-(2*BORDERLINE));
			descPanel.setBounds(0, 0, 0, 0);
		}

	}

	@Override
	public void componentShown(ComponentEvent e) {
		
	}

	public void setTextForDescs(String shortDesc, String longDesc, String exits,Room room){
		this.roomShortDesc.setText(shortDesc);
		this.roomLongDesc.setText(longDesc);
		this.roomExits.setText(exits);
		this.room = room;
		if(this.room != null){
			this.roomNotes.setText(room.getNotes());
			
			if(room.getColor()!= null){
				this.roomColor.setSelectedIndex(RoomColors.getIndex(room.getColor()));
			}else{
				this.roomColor.setSelectedIndex(0);
			}
		}else{
			this.roomNotes.setText("");
		}
		
	}
	
	public Point2D getMapperCentralPoint(){
		return new Point2D.Double(this.vv.getWidth()/2.0, this.vv.getHeight()/2.0);
		
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
		if(this.room != null){
			this.room.setNotes(this.roomNotes.getText());
		}
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		if(this.room != null){
			this.room.setNotes(this.roomNotes.getText());
		}

	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		if(this.room != null){
			this.room.setNotes(this.roomNotes.getText());
		}

	}

	public void toggleDescs() {
		visibleDescs = !visibleDescs;
		this.componentResized(null);
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(roomColor)){
			this.engine.changeRoomColor(RoomColors.getColors()[roomColor.getSelectedIndex()]);
		}
		
	}


}
