package com.glaurung.batMap.gui;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class MapperPanelCellRenderer extends DefaultListCellRenderer {


    private static final long serialVersionUID = 1L;

    @Override
    public Component getListCellRendererComponent( JList list,
                                                   Object value,
                                                   int index,
                                                   boolean isSelected,
                                                   boolean cellHasFocus ) {
        super.getListCellRendererComponent( list, value, index, isSelected, cellHasFocus );

        setText( value.toString() );

        if (index > - 1) {
            setBackground( RoomColors.getColors()[index] );
        }
        return this;

    }

}
