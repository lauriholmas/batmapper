package com.glaurung.batMap.gui.corpses;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.glaurung.batMap.controller.MapperPlugin;
import com.glaurung.batMap.io.CorpseHandlerDataPersister;

public class CorpsePanel extends JPanel implements ActionListener, ComponentListener, KeyListener, DocumentListener {

    private CorpseModel model = new CorpseModel();
    private String BASEDIR;
    private MapperPlugin plugin;
    private final Color TEXT_COLOR = Color.LIGHT_GRAY;
    private final Color BG_COLOR = Color.BLACK;
    private final int BORDERLINE = 7;
    private final int TOP_BORDER = 20;
    private Font font = new Font( "Consolas", Font.PLAIN, 14 );
    private final int CB_WIDTH = 200;
    private final int BUTTON_WIDTH = 70;
    private final int LABEL_WIDTH = 100;
    private final int CB_HEIGHT = 25;
    private final String[] organs = { "antenna", "arm", "beak", "bladder", "brain", "ear", "eye", "foot", "gill", "heart", "horn", "kidney", "leg", "liver", "lung", "nose", "paw", "snout", "spleen", "stomach", "tail", "tendril", "wing" };


    public CorpsePanel( String BASEDIR, MapperPlugin plugin ) {
        this.BASEDIR = BASEDIR;
        this.model = CorpseHandlerDataPersister.load( BASEDIR );
        if (model == null) {
            model = new CorpseModel();
        } else {
            loadFromModel();
        }
        this.plugin = plugin;
        this.setPreferredSize( new Dimension( 1200, 800 ) );
        this.redoLayout();

        this.delim.addActionListener( this );

        this.mount.addActionListener( this );
        this.setBackground( BG_COLOR );
        on.addActionListener( this );
        off.addActionListener( this );
        clear.addActionListener( this );
        lootLists.setSelectionMode( ListSelectionModel.SINGLE_INTERVAL_SELECTION );
        add.addActionListener( this );
        del.addActionListener( this );
        lootItem.addActionListener( this );
        lootLists.addKeyListener( this );
        organ1.addActionListener( this );
        organ2.addActionListener( this );
        doIt.addActionListener( this );
        delim.addActionListener( this );
        delim.getDocument().addDocumentListener( this );
        mount.getDocument().addDocumentListener( this );
        lootLists.setToolTipText( "These items will be picked up by loot commands" );
        lootCorpse.setToolTipText( "Control looted items in the list" );
        lootGround.setToolTipText( "Control looted items in the list" );
    }

    DefaultListModel listModel = new DefaultListModel();
    private CorpseCheckBox lichdrain = new CorpseCheckBox( "lich drain soul", false, "lich drain", this, font );
    private CorpseCheckBox kharimsoul = new CorpseCheckBox( "kharim drain soul", false, "kharim drain", this, font );
    private CorpseCheckBox kharimSoulCorpse = new CorpseCheckBox( "kharim dest corpse", false, null, this, font );
    private CorpseCheckBox tsaraksoul = new CorpseCheckBox( "tzarakk chaosfeed", false, "tzarakk chaosfeed corpse", this, font );
    private CorpseCheckBox ripSoulToKatana = new CorpseCheckBox( "shitKatana rip soul", false, "rip soul from corpse", this, font );
    private CorpseCheckBox arkemile = new CorpseCheckBox( "necrostaff arkemile", false, "say arkemile", this, font );
    private CorpseCheckBox gac = new CorpseCheckBox( "get all from corpse", false, "get all from corpse", this, font );
    private CorpseCheckBox ga = new CorpseCheckBox( "get all from ground", false, "get all", this, font );
    private CorpseCheckBox eatCorpse = new CorpseCheckBox( "get and eat corpse", false, "get corpse" + getDelim() + "eat corpse", this, font );
    private CorpseCheckBox donate = new CorpseCheckBox( "donate noeq and drop rest", false, "get all from corpse" + getDelim() + "donate noeq" + getDelim() + "drop noeq", this, font );
    private CorpseCheckBox lootCorpse = new CorpseCheckBox( "get loot from corpse", false, "get " + getLootString() + " from corpse", this, font );
    private CorpseCheckBox lootGround = new CorpseCheckBox( "get loot from ground", false, "get " + getLootString(), this, font );
    private CorpseCheckBox barbarianBurn = new CorpseCheckBox( "barbarian burn corpse", false, "barbburn", this, font );
    private CorpseCheckBox feedCorpseTo = new CorpseCheckBox( "feed corpse to mount", false, "get corpse" + getDelim() + "feed corpse to " + getMountName(), this, font );
    private CorpseCheckBox beheading = new CorpseCheckBox( "kharim behead corpse", false, "use beheading of departed at corpse", this, font );
    private CorpseCheckBox desecrateGround = new CorpseCheckBox( "desecrate ground", false, "use desecrate ground", this, font );
    private CorpseCheckBox burialCere = new CorpseCheckBox( "burial ceremony", false, "use burial ceremony", this, font );
    private CorpseCheckBox dig = new CorpseCheckBox( "dig grave", false, "dig grave", this, font );
    private CorpseCheckBox wakeFollow = new CorpseCheckBox( "follow", false, " follow", this, font );
    private CorpseCheckBox wakeAgro = new CorpseCheckBox( "agro", false, " agro", this, font );
    private CorpseCheckBox wakeTalk = new CorpseCheckBox( "talk", false, " talk", this, font );
    private CorpseCheckBox wakeStatic = new CorpseCheckBox( "static", false, "", this, font );
    private CorpseCheckBox lichWake = new CorpseCheckBox( "lich wake corpse", false, "lich wake corpse", this, font );
    private CorpseCheckBox vampireWake = new CorpseCheckBox( "vampire wake corpse", false, "vampire wake corpse", this, font );
    private CorpseCheckBox skeletonWake = new CorpseCheckBox( "skeleton wake corpse", false, "skeleton wake corpse", this, font );
    private CorpseCheckBox zombieWake = new CorpseCheckBox( "zombie wake corpse", false, "zombie wake corpse", this, font );
    private CorpseCheckBox aelenaOrgan = new CorpseCheckBox( "aelena extract organ", false, "familiar harvest antenna antenna", this, font );
    private CorpseCheckBox aelenaFam = new CorpseCheckBox( "aelena fam consume corpse", false, "familiar consume corpse", this, font );
    private CorpseCheckBox dissect = new CorpseCheckBox( "dissection", false, "use dissection at corpse try ", this, font );
    private CorpseCheckBox tin = new CorpseCheckBox( "tin corpse", false, "tin corpse", this, font );


    private static final long serialVersionUID = 1L;
    private JCheckBox on = new JCheckBox( "On!" );
    private JCheckBox off = new JCheckBox( "Off" );
    private JTextField delim = new JTextField( ";" );
    private JTextField mount = new JTextField( "" );
    private JButton clear = new JButton( "Clear!" );
    private JButton doIt = new JButton( "Do it!" );
    private JList lootLists = new JList( listModel );
    private JScrollPane listPane = new JScrollPane( lootLists );


    private Border whiteline = BorderFactory.createLineBorder( Color.white );
    private JPanel soulPanel = new JPanel();
    private JPanel listPanel = new JPanel();
    private JPanel controlPanel = new JPanel();
    private JPanel wakePanel = new JPanel();
    private JPanel lootPanel = new JPanel();
    private JPanel corpsePanel = new JPanel();
    private JLabel delimLabel = new JLabel( "delimeter:" );
    private JLabel mountLabel = new JLabel( "mount name:" );
    private JComboBox organ1 = new JComboBox( organs );
    private JComboBox organ2 = new JComboBox( organs );
    private JLabel organ1Label = new JLabel( "first organ:" );
    private JLabel organ2Label = new JLabel( "second organ:" );

    private JButton add = new JButton( "add" );
    private JButton del = new JButton( "del" );
    private JTextField lootItem = new JTextField();

    private String getDelim() {
        return model.getDelim();
    }


    private String getMountName() {
        return model.getMountHandle();
    }


    private String getLootString() {
        String loots = "grep -q -v \"There is no\" get ";
        if (listModel.size() < 1) {
            return "";
        }
        for (Object lootItem : listModel.toArray()) {
            loots += (String) lootItem + ",";
        }
        return loots.substring( 0, loots.length() - 1 );


        /**
         * (00:03) Torc tells you ''loottinoids' is a command-alias to 'grep -q -v "There is no" ga mithril,all batium,all anipium,all platinum,all gold,all gem,all box,all
         chest,all safe,all scroll,all emerald disc,all rune,all compass,all shard,all key from all corpse;grep -q -v "There is no" ga mithril,all batium,all anipium,all
         platinum,all gold,all chest,all box,all safe,all gem,all scroll,all emerald disc,all rune,all compass,all shard,all key'.'

         */
    }

    private void saveToModel() {
        this.model.setMountHandle( mount.getText() );
        this.model.setDelim( delim.getText() );
        this.model.setLootList( createStringLootList() );
        this.model.setOrgan1( (String) organ1.getSelectedItem() );
        this.model.setOrgan2( (String) organ2.getSelectedItem() );
        this.model.lichdrain = lichdrain.isSelected();
        this.model.kharimsoul = kharimsoul.isSelected();
        this.model.kharimSoulCorpse = kharimSoulCorpse.isSelected();
        this.model.tsaraksoul = tsaraksoul.isSelected();
        this.model.ripSoulToKatana = ripSoulToKatana.isSelected();
        this.model.arkemile = arkemile.isSelected();
        this.model.gac = gac.isSelected();
        this.model.ga = ga.isSelected();
        this.model.eatCorpse = eatCorpse.isSelected();
        this.model.donate = donate.isSelected();
        this.model.lootCorpse = lootCorpse.isSelected();
        this.model.lootGround = lootGround.isSelected();
        this.model.barbarianBurn = barbarianBurn.isSelected();
        this.model.feedCorpseTo = feedCorpseTo.isSelected();
        this.model.beheading = beheading.isSelected();
        this.model.desecrateGround = desecrateGround.isSelected();
        this.model.burialCere = burialCere.isSelected();
        this.model.dig = dig.isSelected();
        this.model.wakeFollow = wakeFollow.isSelected();
        this.model.wakeAgro = wakeAgro.isSelected();
        this.model.wakeTalk = wakeTalk.isSelected();
        this.model.wakeStatic = wakeStatic.isSelected();
        this.model.lichWake = lichWake.isSelected();
        this.model.vampireWake = vampireWake.isSelected();
        this.model.skeletonWake = skeletonWake.isSelected();
        this.model.zombieWake = zombieWake.isSelected();
        this.model.aelenaFam = aelenaFam.isSelected();
        this.model.aelenaOrgan = aelenaOrgan.isSelected();
        this.model.dissect = dissect.isSelected();
        this.model.tin = tin.isSelected();
        CorpseHandlerDataPersister.save( BASEDIR, this.model );

    }

    private List<String> createStringLootList() {
        LinkedList<String> list = new LinkedList<String>();
        for (int i = 0; i < listModel.getSize(); ++ i) {
            list.add( (String) listModel.getElementAt( i ) );
        }
        return list;
    }

    private void loadFromModel() {


        lichdrain.setSelected( this.model.lichdrain );
        kharimsoul.setSelected( this.model.kharimsoul );
        kharimSoulCorpse.setSelected( this.model.kharimSoulCorpse );
        tsaraksoul.setSelected( this.model.tsaraksoul );
        ripSoulToKatana.setSelected( this.model.ripSoulToKatana );
        arkemile.setSelected( this.model.arkemile );
        gac.setSelected( this.model.gac );
        ga.setSelected( this.model.ga );
        eatCorpse.setSelected( this.model.eatCorpse );
        donate.setSelected( this.model.donate );
        lootCorpse.setSelected( this.model.lootCorpse );
        lootGround.setSelected( this.model.lootGround );
        barbarianBurn.setSelected( this.model.barbarianBurn );
        feedCorpseTo.setSelected( this.model.feedCorpseTo );
        beheading.setSelected( this.model.beheading );
        desecrateGround.setSelected( this.model.desecrateGround );
        burialCere.setSelected( this.model.burialCere );
        dig.setSelected( this.model.dig );
        wakeFollow.setSelected( this.model.wakeFollow );
        wakeAgro.setSelected( this.model.wakeAgro );
        wakeTalk.setSelected( this.model.wakeTalk );
        wakeStatic.setSelected( this.model.wakeStatic );
        lichWake.setSelected( this.model.lichWake );
        vampireWake.setSelected( this.model.vampireWake );
        skeletonWake.setSelected( this.model.skeletonWake );
        zombieWake.setSelected( this.model.zombieWake );
        aelenaFam.setSelected( this.model.aelenaFam );
        aelenaOrgan.setSelected( this.model.aelenaOrgan );
        dissect.setSelected( this.model.dissect );
        tin.setSelected( this.model.dissect );

        mount.setText( this.model.getMountHandle() );
        delim.setText( this.model.getDelim() );
        organ1.setSelectedItem( this.model.getOrgan1() );
        organ2.setSelectedItem( this.model.getOrgan2() );
        listModel.clear();
        for (String item : this.model.getLootList()) {
            listModel.addElement( item );
        }
        updateLoots();
        updateDelimAndMountAffected();
        updateOrganAffected();
//		plugin.saveRipAction(makeRipString());

    }

    @Override
    public void actionPerformed( ActionEvent event ) {

        Object source = event.getSource();
        if (source == kharimSoulCorpse) {
            this.plugin.doCommand( "kharim set corpseDest foobar" );
        } else if (source == lichdrain) {
            turnOff( kharimsoul, kharimsoul, ripSoulToKatana, arkemile );
        } else if (source == kharimsoul) {
            turnOff( lichdrain, tsaraksoul, ripSoulToKatana, arkemile );
        } else if (source == tsaraksoul) {
            turnOff( lichdrain, kharimsoul, ripSoulToKatana, arkemile );
        } else if (source == ripSoulToKatana) {
            turnOff( lichdrain, kharimsoul, tsaraksoul, arkemile );
        } else if (source == arkemile) {
            turnOff( tin, lichdrain, kharimsoul, tsaraksoul, ripSoulToKatana, eatCorpse, barbarianBurn, feedCorpseTo,
                    beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, zombieWake, aelenaFam, aelenaOrgan, dig, dissect );
        } else if (source == eatCorpse) {
            turnOff( tin, arkemile, dig, dissect, ripSoulToKatana, barbarianBurn, feedCorpseTo, beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, zombieWake, aelenaFam, aelenaOrgan );
        } else if (source == barbarianBurn) {
            turnOff( tin, arkemile, eatCorpse, dig, dissect, ripSoulToKatana, feedCorpseTo, beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, zombieWake, aelenaFam, aelenaOrgan );
        } else if (source == feedCorpseTo) {
            turnOff( tin, arkemile, eatCorpse, dig, dissect, ripSoulToKatana, barbarianBurn, beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, zombieWake, aelenaFam, aelenaOrgan );
        } else if (source == beheading) {
            turnOff( tin, arkemile, eatCorpse, dig, dissect, ripSoulToKatana, barbarianBurn, feedCorpseTo, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, zombieWake, aelenaFam, aelenaOrgan );
        } else if (source == desecrateGround) {
            turnOff( tin, arkemile, eatCorpse, dig, dissect, ripSoulToKatana, barbarianBurn, feedCorpseTo, beheading, burialCere, lichWake, skeletonWake, vampireWake, zombieWake, aelenaFam, aelenaOrgan );
        } else if (source == burialCere) {
            turnOff( tin, arkemile, eatCorpse, dig, dissect, ripSoulToKatana, barbarianBurn, feedCorpseTo, beheading, desecrateGround, lichWake, skeletonWake, vampireWake, zombieWake, aelenaFam, aelenaOrgan );
        } else if (source == lichWake) {
            turnOff( tin, arkemile, eatCorpse, dig, dissect, ripSoulToKatana, barbarianBurn, feedCorpseTo, beheading, desecrateGround, burialCere, skeletonWake, vampireWake, zombieWake, aelenaFam, aelenaOrgan );
        } else if (source == vampireWake) {
            turnOff( tin, arkemile, eatCorpse, dig, dissect, ripSoulToKatana, barbarianBurn, feedCorpseTo, beheading, desecrateGround, burialCere, lichWake, skeletonWake, zombieWake, aelenaFam, aelenaOrgan );
        } else if (source == skeletonWake) {
            turnOff( tin, arkemile, eatCorpse, dig, dissect, ripSoulToKatana, barbarianBurn, feedCorpseTo, beheading, desecrateGround, burialCere, lichWake, vampireWake, zombieWake, aelenaFam, aelenaOrgan );
        } else if (source == zombieWake) {
            turnOff( tin, arkemile, eatCorpse, dig, dissect, ripSoulToKatana, barbarianBurn, feedCorpseTo, beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, aelenaFam, aelenaOrgan );
        } else if (source == aelenaFam) {
            turnOff( tin, arkemile, eatCorpse, dig, dissect, ripSoulToKatana, barbarianBurn, feedCorpseTo, beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, aelenaOrgan );
        } else if (source == aelenaOrgan) {
            turnOff( tin, arkemile, eatCorpse, dig, dissect, ripSoulToKatana, barbarianBurn, feedCorpseTo, beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, aelenaFam );
        } else if (source == dig) {
            turnOff( tin, arkemile, eatCorpse, dissect, aelenaOrgan, ripSoulToKatana, barbarianBurn, feedCorpseTo, beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, aelenaFam );
        } else if (source == dissect) {
            turnOff( tin, arkemile, eatCorpse, dig, aelenaOrgan, ripSoulToKatana, barbarianBurn, feedCorpseTo, beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, aelenaFam );
        } else if (source == tin) {
            turnOff( dissect, arkemile, eatCorpse, dig, aelenaOrgan, ripSoulToKatana, barbarianBurn, feedCorpseTo, beheading, desecrateGround, burialCere, lichWake, skeletonWake, vampireWake, aelenaFam );
        } else if (source == wakeFollow) {
            turnOff( wakeAgro, wakeTalk, wakeStatic );
        } else if (source == wakeAgro) {
            turnOff( wakeFollow, wakeTalk, wakeStatic );
        } else if (source == wakeTalk) {
            turnOff( wakeFollow, wakeAgro, wakeStatic );
        } else if (source == wakeStatic) {
            turnOff( wakeFollow, wakeAgro, wakeTalk );
        } else if (source == clear) {

            int confirmation = JOptionPane.showConfirmDialog( null, "Sure you want to clear everything?" );
            if (confirmation == 0) {
                this.model.clear();
                organ1.setSelectedIndex( 0 );
                organ2.setSelectedIndex( 0 );
                this.loadFromModel();
            }

        } else if (source == add && ! lootItem.getText().trim().equals( "" )) {
            listModel.addElement( lootItem.getText() );
            lootItem.setText( "" );
            updateLoots();
        } else if (source == lootItem && ! lootItem.getText().trim().equals( "" )) {
            listModel.addElement( lootItem.getText() );
            lootItem.setText( "" );
            updateLoots();
        } else if (source == del && lootLists.getSelectedIndex() > - 1) {
            listModel.remove( lootLists.getSelectedIndex() );
            updateLoots();
        } else if (source == organ1) {
            updateOrganAffected();
        } else if (source == organ2) {
            updateOrganAffected();
        }

        if (source == doIt) {
            plugin.doCommand( makeRipString() );
        } else if (source == on) {
            on.setSelected( true );
            off.setSelected( false );
            this.plugin.toggleRipAction( true );
        } else if (source == off) {
            on.setSelected( false );
            off.setSelected( true );
            this.plugin.toggleRipAction( false );
        } else {
            persistAndUpdate();
        }

    }


    private void updateOrganAffected() {
        dissect.setEffect( "use dissection at corpse try " + organ1.getSelectedItem() + " " + organ2.getSelectedItem() );
        aelenaOrgan.setEffect( "familiar harvest " + organ1.getSelectedItem() + " " + organ2.getSelectedItem() );
    }


    private void updateLoots() {
        if (getLootString().equals( "" )) {
            lootCorpse.setEffect( "" );
            lootGround.setEffect( "" );
        } else {
            lootCorpse.setEffect( getLootString() + " from corpse" );
            lootGround.setEffect( getLootString() );
        }

    }


    private void turnOff( CorpseCheckBox... boxes ) {
        for (CorpseCheckBox box : boxes) {
            box.setSelected( false );
        }
    }

    private String makeRipString() {
        String rip = "";
        //first souls
        if (lichdrain.isSelected()) {
            rip += lichdrain.getEffect() + this.model.getDelim();
        }
        if (kharimsoul.isSelected()) {
            rip += kharimsoul.getEffect() + this.model.getDelim();
        }
        if (kharimSoulCorpse.isSelected()) {
            rip += kharimSoulCorpse.getEffect() + this.model.getDelim();
        }
        if (ripSoulToKatana.isSelected()) {
            rip += ripSoulToKatana.getEffect() + this.model.getDelim();
        }

        if (gac.isSelected()) {
            rip += gac.getEffect() + this.model.getDelim();
        }
        if (lootCorpse.isSelected()) {
            rip += lootCorpse.getEffect() + this.model.getDelim();
        }
        if (tsaraksoul.isSelected()) {
            rip += tsaraksoul.getEffect() + this.model.getDelim();
        }
        if (arkemile.isSelected()) {
            rip += arkemile.getEffect() + this.model.getDelim();
        }
        if (eatCorpse.isSelected()) {
            rip += eatCorpse.getEffect() + this.model.getDelim();
        }
        if (donate.isSelected()) {
            rip += donate.getEffect() + this.model.getDelim();
        }
        if (barbarianBurn.isSelected()) {
            rip += barbarianBurn.getEffect() + this.model.getDelim();
        }
        if (feedCorpseTo.isSelected()) {
            rip += feedCorpseTo.getEffect() + this.model.getDelim();
        }
        if (beheading.isSelected()) {
            rip += beheading.getEffect() + this.model.getDelim();
        }
        if (desecrateGround.isSelected()) {
            rip += desecrateGround.getEffect() + this.model.getDelim();
        }
        if (burialCere.isSelected()) {
            rip += burialCere.getEffect() + this.model.getDelim();
        }
        if (dig.isSelected()) {
            rip += dig.getEffect() + this.model.getDelim();
        }
        if (lichWake.isSelected()) {
            rip += lichWake.getEffect() + getWakeMode() + this.model.getDelim();
        }
        if (vampireWake.isSelected()) {
            rip += vampireWake.getEffect() + getWakeMode() + this.model.getDelim();
        }
        if (skeletonWake.isSelected()) {
            rip += skeletonWake.getEffect() + getWakeMode() + this.model.getDelim();
        }
        if (zombieWake.isSelected()) {
            rip += zombieWake.getEffect() + getWakeMode() + this.model.getDelim();
        }
        if (aelenaFam.isSelected()) {
            rip += aelenaFam.getEffect() + this.model.getDelim();
        }
        if (aelenaOrgan.isSelected()) {
            rip += aelenaOrgan.getEffect() + this.model.getDelim();
        }
        if (dissect.isSelected()) {
            rip += dissect.getEffect() + this.model.getDelim();
        }
        if (tin.isSelected()) {
            rip += tin.getEffect() + this.model.getDelim();
        }
        if (ga.isSelected()) {
            rip += ga.getEffect() + this.model.getDelim();
        }
        if (lootGround.isSelected()) {
            rip += lootGround.getEffect() + this.model.getDelim();
        }


        if (rip.length() > this.model.getDelim().length()) {
            rip = rip.substring( 0, rip.length() - this.model.getDelim().length() );
        }

        return rip;
    }


    private String getWakeMode() {
        if (wakeFollow.isSelected()) {
            return wakeFollow.getEffect();
        }
        if (wakeAgro.isSelected()) {
            return wakeAgro.getEffect();
        }
        if (wakeTalk.isSelected()) {
            return wakeTalk.getEffect();
        }
        //else no effect:
        return wakeStatic.getEffect();

    }

    @Override
    public void componentHidden( ComponentEvent arg0 ) {
    }


    @Override
    public void componentMoved( ComponentEvent arg0 ) {
    }


    @Override
    public void componentResized( ComponentEvent arg0 ) {
        redoLayout();
    }


    @Override
    public void componentShown( ComponentEvent arg0 ) {
    }

    private void redoLayout() {
        this.setLayout( null );


        soulPanel.setBorder( BorderFactory.createTitledBorder( whiteline, "Souls", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, TEXT_COLOR ) );
        soulPanel.setBackground( Color.BLACK );
        listPanel.setBorder( BorderFactory.createTitledBorder( whiteline, "Items to loot", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, TEXT_COLOR ) );
        listPanel.setBackground( Color.BLACK );
        controlPanel.setBorder( BorderFactory.createTitledBorder( whiteline, "Controls", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, TEXT_COLOR ) );
        controlPanel.setBackground( Color.BLACK );
        wakePanel.setBorder( BorderFactory.createTitledBorder( whiteline, "Wake up corpse!", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, TEXT_COLOR ) );
        wakePanel.setBackground( Color.BLACK );
        lootPanel.setBorder( BorderFactory.createTitledBorder( whiteline, "Looting", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, TEXT_COLOR ) );
        lootPanel.setBackground( Color.BLACK );
        corpsePanel.setBorder( BorderFactory.createTitledBorder( whiteline, "Carcasses", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, font, TEXT_COLOR ) );
        corpsePanel.setBackground( Color.BLACK );

        soulPanel.setBounds( BORDERLINE * 2, BORDERLINE * 2, ( CB_WIDTH * 2 ) + ( 2 * TOP_BORDER ), ( CB_HEIGHT * 4 ) );
        soulPanel.setLayout( null );
        kharimsoul.setBounds( BORDERLINE, TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        kharimSoulCorpse.setBounds( CB_WIDTH + BORDERLINE, TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        lichdrain.setBounds( BORDERLINE, CB_HEIGHT + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        tsaraksoul.setBounds( CB_WIDTH + BORDERLINE, CB_HEIGHT + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        ripSoulToKatana.setBounds( BORDERLINE, ( CB_HEIGHT * 2 ) + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        arkemile.setBounds( CB_WIDTH + BORDERLINE, ( CB_HEIGHT * 2 ) + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        soulPanel.add( kharimsoul );
        soulPanel.add( kharimSoulCorpse );
        soulPanel.add( lichdrain );
        soulPanel.add( tsaraksoul );
        soulPanel.add( ripSoulToKatana );
        soulPanel.add( arkemile );
        this.add( soulPanel );

        corpsePanel.setBounds( BORDERLINE * 2, soulPanel.getHeight() + ( BORDERLINE * 4 ), ( CB_WIDTH * 2 ) + ( 2 * TOP_BORDER ), ( CB_HEIGHT * 7 ) );
        corpsePanel.setLayout( null );
        barbarianBurn.setBounds( BORDERLINE, TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        feedCorpseTo.setBounds( CB_WIDTH + BORDERLINE, TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        beheading.setBounds( BORDERLINE, CB_HEIGHT + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        desecrateGround.setBounds( CB_WIDTH + BORDERLINE, CB_HEIGHT + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        burialCere.setBounds( BORDERLINE, ( CB_HEIGHT * 2 ) + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        dig.setBounds( CB_WIDTH + BORDERLINE, ( CB_HEIGHT * 2 ) + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        aelenaOrgan.setBounds( BORDERLINE, ( CB_HEIGHT * 3 ) + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        aelenaFam.setBounds( CB_WIDTH + BORDERLINE, ( CB_HEIGHT * 3 ) + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        eatCorpse.setBounds( BORDERLINE, ( CB_HEIGHT * 4 ) + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        dissect.setBounds( CB_WIDTH + BORDERLINE, ( CB_HEIGHT * 4 ) + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        tin.setBounds( BORDERLINE, ( CB_HEIGHT * 5 ) + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        corpsePanel.add( tin );
        corpsePanel.add( barbarianBurn );
        corpsePanel.add( feedCorpseTo );
        corpsePanel.add( beheading );
        corpsePanel.add( desecrateGround );
        corpsePanel.add( burialCere );
        corpsePanel.add( dig );
        corpsePanel.add( aelenaOrgan );
        corpsePanel.add( aelenaFam );
        corpsePanel.add( dissect );
        corpsePanel.add( eatCorpse );

        this.add( corpsePanel );

        wakePanel.setBounds( BORDERLINE * 2, corpsePanel.getHeight() + soulPanel.getHeight() + ( BORDERLINE * 6 ), ( CB_WIDTH * 2 ) + ( 2 * TOP_BORDER ), ( CB_HEIGHT * 5 ) );
        wakePanel.setLayout( null );
        lichWake.setBounds( BORDERLINE, TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        skeletonWake.setBounds( BORDERLINE, CB_HEIGHT + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        zombieWake.setBounds( BORDERLINE, ( CB_HEIGHT * 2 ) + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        vampireWake.setBounds( BORDERLINE, ( CB_HEIGHT * 3 ) + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        wakeFollow.setBounds( CB_WIDTH + BORDERLINE, TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        wakeAgro.setBounds( CB_WIDTH + BORDERLINE, CB_HEIGHT + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        wakeTalk.setBounds( CB_WIDTH + BORDERLINE, ( CB_HEIGHT * 2 ) + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        wakeStatic.setBounds( CB_WIDTH + BORDERLINE, ( CB_HEIGHT * 3 ) + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        wakePanel.add( wakeFollow );
        wakePanel.add( wakeAgro );
        wakePanel.add( wakeTalk );
        wakePanel.add( wakeStatic );
        wakePanel.add( lichWake );
        wakePanel.add( vampireWake );
        wakePanel.add( skeletonWake );
        wakePanel.add( zombieWake );
        this.add( wakePanel );

        lootPanel.setBounds( BORDERLINE * 2, wakePanel.getHeight() + corpsePanel.getHeight() + soulPanel.getHeight() + ( BORDERLINE * 8 ), ( CB_WIDTH * 2 ) + ( 2 * TOP_BORDER ), ( CB_HEIGHT * 4 ) );
        lootPanel.setLayout( null );
        gac.setBounds( BORDERLINE, TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        ga.setBounds( BORDERLINE, CB_HEIGHT + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        donate.setBounds( BORDERLINE, ( CB_HEIGHT * 2 ) + TOP_BORDER, CB_WIDTH * 2, CB_HEIGHT );
        lootCorpse.setBounds( CB_WIDTH + BORDERLINE, TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        lootGround.setBounds( CB_WIDTH + BORDERLINE, CB_HEIGHT + TOP_BORDER, CB_WIDTH, CB_HEIGHT );
        lootPanel.add( gac );
        lootPanel.add( ga );
        lootPanel.add( donate );
        lootPanel.add( lootCorpse );
        lootPanel.add( lootGround );

        this.add( lootPanel );

        controlPanel.setBounds( ( BORDERLINE * 4 ) + lootPanel.getWidth(), BORDERLINE * 2, ( LABEL_WIDTH * 2 ) + ( 2 * TOP_BORDER ), ( CB_HEIGHT * 9 ) );
        controlPanel.setLayout( null );


        on.setBounds( BORDERLINE, TOP_BORDER, BUTTON_WIDTH, CB_HEIGHT );
        on.setBackground( BG_COLOR );
        on.setForeground( TEXT_COLOR );
        off.setBounds( BORDERLINE, CB_HEIGHT + TOP_BORDER, BUTTON_WIDTH, CB_HEIGHT );
        off.setBackground( BG_COLOR );
        off.setForeground( TEXT_COLOR );
        delimLabel.setBounds( BORDERLINE, ( CB_HEIGHT * 3 ) + TOP_BORDER, LABEL_WIDTH, CB_HEIGHT );
        delimLabel.setForeground( TEXT_COLOR );
        delim.setBounds( LABEL_WIDTH + BORDERLINE, ( CB_HEIGHT * 3 ) + TOP_BORDER, LABEL_WIDTH, CB_HEIGHT );
        mountLabel.setBounds( BORDERLINE, ( CB_HEIGHT * 4 ) + TOP_BORDER, LABEL_WIDTH, CB_HEIGHT );
        mountLabel.setForeground( TEXT_COLOR );
        mount.setBounds( LABEL_WIDTH + BORDERLINE, ( CB_HEIGHT * 4 ) + TOP_BORDER, LABEL_WIDTH, CB_HEIGHT );
        organ1Label.setBounds( BORDERLINE, ( CB_HEIGHT * 5 ) + TOP_BORDER, LABEL_WIDTH, CB_HEIGHT );
        organ1Label.setForeground( TEXT_COLOR );
        organ1.setBounds( LABEL_WIDTH + BORDERLINE, ( CB_HEIGHT * 5 ) + TOP_BORDER, LABEL_WIDTH, CB_HEIGHT );
        organ2Label.setBounds( BORDERLINE, ( CB_HEIGHT * 6 ) + TOP_BORDER, LABEL_WIDTH, CB_HEIGHT );
        organ2Label.setForeground( TEXT_COLOR );
        organ2.setBounds( LABEL_WIDTH + BORDERLINE, ( CB_HEIGHT * 6 ) + TOP_BORDER, LABEL_WIDTH, CB_HEIGHT );
        clear.setBounds( ( 5 * BORDERLINE ) + LABEL_WIDTH, TOP_BORDER, BUTTON_WIDTH, CB_HEIGHT );
        doIt.setBounds( ( 5 * BORDERLINE ) + LABEL_WIDTH, TOP_BORDER + CB_HEIGHT, BUTTON_WIDTH, CB_HEIGHT );
        controlPanel.add( doIt );
        controlPanel.add( on );
        controlPanel.add( off );
        controlPanel.add( delim );
        controlPanel.add( mount );
        controlPanel.add( clear );
        controlPanel.add( organ1 );
        controlPanel.add( organ2 );
        controlPanel.add( delimLabel );
        controlPanel.add( mountLabel );
        controlPanel.add( organ1Label );
        controlPanel.add( organ2Label );
        controlPanel.add( clear );
        this.add( controlPanel );

        listPanel.setBounds( ( BORDERLINE * 4 ) + lootPanel.getWidth(), controlPanel.getHeight() + ( BORDERLINE * 4 ), ( LABEL_WIDTH * 2 ) + ( 2 * TOP_BORDER ), ( CB_HEIGHT * 12 ) + 3 );
        listPanel.setLayout( null );
        listPane.setBounds( BORDERLINE * 2, TOP_BORDER, listPanel.getWidth() - ( 4 * BORDERLINE ), listPanel.getHeight() - ( ( 2 * TOP_BORDER ) + CB_HEIGHT ) );
        add.setBounds( BORDERLINE * 2, listPane.getHeight() + CB_HEIGHT, BUTTON_WIDTH, CB_HEIGHT );
        lootItem.setBounds( BUTTON_WIDTH + ( BORDERLINE * 2 ), listPane.getHeight() + CB_HEIGHT, BUTTON_WIDTH, CB_HEIGHT );
        del.setBounds( ( BUTTON_WIDTH * 2 ) + ( BORDERLINE * 2 ), listPane.getHeight() + CB_HEIGHT, BUTTON_WIDTH, CB_HEIGHT );
        listPanel.add( listPane );
        listPanel.add( add );
        listPanel.add( lootItem );
        listPanel.add( del );
        this.add( listPanel );

    }


    @Override
    public void keyPressed( KeyEvent e ) {
        if (e.getSource() == lootLists) {
            if (lootLists.getSelectedIndex() > - 1) {
                if (e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                    listModel.remove( lootLists.getSelectedIndex() );
                    updateLoots();
                    persistAndUpdate();
                }
            }
        }
    }


    @Override
    public void keyReleased( KeyEvent e ) {
    }


    @Override
    public void keyTyped( KeyEvent e ) {
    }


    @Override
    public void changedUpdate( DocumentEvent e ) {
        updateDelimAndMountAffected();
        this.model.setDelim( delim.getText() );
        this.model.setMountHandle( mount.getText() );
        persistAndUpdate();
    }


    @Override
    public void insertUpdate( DocumentEvent arg0 ) {
        this.model.setDelim( delim.getText() );
        this.model.setMountHandle( mount.getText() );
        updateDelimAndMountAffected();
        persistAndUpdate();
    }


    @Override
    public void removeUpdate( DocumentEvent arg0 ) {
        this.model.setDelim( delim.getText() );
        this.model.setMountHandle( mount.getText() );
        updateDelimAndMountAffected();
        persistAndUpdate();
    }


    private void updateDelimAndMountAffected() {
        donate.setEffect( "get all from corpse" + getDelim() + "donate noeq" + getDelim() + "drop noeq" );
        eatCorpse.setEffect( "get corpse" + getDelim() + "eat corpse" );
        feedCorpseTo.setEffect( "get corpse" + getDelim() + "feed corpse to " + mount.getText() );
    }


    private void persistAndUpdate() {
        saveToModel();
        plugin.saveRipAction( makeRipString() );
    }


}
