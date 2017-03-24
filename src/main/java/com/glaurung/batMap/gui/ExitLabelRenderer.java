package com.glaurung.batMap.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;

import javax.swing.JComponent;

import com.glaurung.batMap.vo.Exit;

import edu.uci.ics.jung.visualization.renderers.DefaultEdgeLabelRenderer;


/**
 * currently used for coloring edgelabels based on selected or not
 *
 * @author lauri
 */
public class ExitLabelRenderer extends DefaultEdgeLabelRenderer {


    private static final long serialVersionUID = - 7389656600818368218L;

    protected Color pickedEdgeLabelColor = Color.blue;

    protected Color unPickedEdgeLabelColor = Color.red;

    public ExitLabelRenderer( Color pickedEdgeLabelColor, boolean rotateEdgeLabels ) {
        super( pickedEdgeLabelColor, rotateEdgeLabels );
    }


    public ExitLabelRenderer() {
        super( Color.black, false );
    }


    public <E> Component getEdgeLabelRendererComponent( JComponent vv, Object value, Font font, boolean isSelected, E edge ) {

        super.setForeground( vv.getForeground() );
        if (edge instanceof Exit) {
            Exit exit = (Exit) edge;
            if (exit.isCurrentExit()) {
                setForeground( pickedEdgeLabelColor );
            } else {
                setForeground( unPickedEdgeLabelColor );
            }
        }

        super.setBackground( vv.getBackground() );

        if (font != null) {
            setFont( font );
        } else {
            setFont( vv.getFont() );
        }
        setIcon( null );
        setBorder( noFocusBorder );
        setValue( value );
        return this;
    }

}
