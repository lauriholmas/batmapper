package com.glaurung.batMap.gui.corpses;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

public class CorpseCheckBox extends JCheckBox {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String effect;


    public CorpseCheckBox( String label, boolean defaultMode, String effect, ActionListener listener, Font font ) {
        super( label, defaultMode );
        this.setEffect( effect );
        this.addActionListener( listener );
        this.setBackground( Color.BLACK );
        this.setForeground( Color.LIGHT_GRAY );
        this.setFont( font );
    }


    public String getEffect() {
        return effect;
    }


    public void setEffect( String effect ) {
        this.effect = effect;
    }

}
