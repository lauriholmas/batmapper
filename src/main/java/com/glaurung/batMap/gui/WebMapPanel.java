package com.glaurung.batMap.gui;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Batclient tab that points to the web map, since the map itself is drawn in a browser.
 */
public class WebMapPanel extends JPanel implements ActionListener {

    private static final long serialVersionUID = 1L;

    private final Color TEXT_COLOR = Color.LIGHT_GRAY;
    private final Color BG_COLOR = Color.BLACK;
    private final Font font = new Font( "Consolas", Font.PLAIN, 14 );

    private final String url;
    private final JButton openButton = new JButton( "Open map in browser" );

    public WebMapPanel( String url, String error ) {
        this.url = url;
        setBackground( BG_COLOR );
        setLayout( new GridBagLayout() );
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.insets = new Insets( 6, 6, 6, 6 );

        if (url == null) {
            add( makeLabel( "Unable to start the map server: " + error ), c );
            return;
        }

        add( makeLabel( "The map is drawn in your web browser at" ), c );
        JTextField urlField = new JTextField( url );
        urlField.setEditable( false );
        urlField.setFont( font );
        urlField.setForeground( TEXT_COLOR );
        urlField.setBackground( BG_COLOR );
        urlField.setCaretColor( TEXT_COLOR );
        add( urlField, c );
        openButton.addActionListener( this );
        openButton.setToolTipText( "Opens the map in your default web browser" );
        add( openButton, c );
    }

    private JLabel makeLabel( String text ) {
        JLabel label = new JLabel( text );
        label.setFont( font );
        label.setForeground( TEXT_COLOR );
        return label;
    }

    /**
     * @return false if browser couldn't be opened
     */
    public static boolean openInBrowser( String url ) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported( Desktop.Action.BROWSE )) {
                Desktop.getDesktop().browse( new URI( url ) );
                return true;
            }
        } catch (IOException | java.net.URISyntaxException e) {
            System.out.println( e );
        }
        return false;
    }

    @Override
    public void actionPerformed( ActionEvent e ) {
        openInBrowser( url );
    }
}
