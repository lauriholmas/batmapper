package com.glaurung.batMap.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.apache.commons.collections15.Transformer;

import com.glaurung.batMap.vo.Room;

public class RoomIconTransformer implements Transformer<Room, Icon> {

    private final Color PICKED = Color.CYAN;
    private final Color CURRENT = Color.RED;


    private Image nExitIn;
    private Image neExitIn;
    private Image eExitIn;
    private Image seExitIn;
    private Image sExitIn;
    private Image swExitIn;
    private Image wExitIn;
    private Image nwExitIn;

    private Image nWallIn;
    private Image neWallIn;
    private Image eWallIn;
    private Image seWallIn;
    private Image sWallIn;
    private Image swWallIn;
    private Image wWallIn;
    private Image nwWallIn;

    private Image middleIn;
    private Image middleSpecialIn;

    private Image nExitOut;
    private Image neExitOut;
    private Image eExitOut;
    private Image seExitOut;
    private Image sExitOut;
    private Image swExitOut;
    private Image wExitOut;
    private Image nwExitOut;

    private Image nWallOut;
    private Image neWallOut;
    private Image eWallOut;
    private Image seWallOut;
    private Image sWallOut;
    private Image swWallOut;
    private Image wWallOut;
    private Image nwWallOut;

    private Image middleOut;
    private Image middleSpecialOut;


    private final String PATH = "/images/";

    private Image[][] indoorsWalls = new Image[3][3];
    private Image[][] indoorsExits = new Image[3][3];

    private Image[][] outdoorsWalls = new Image[3][3];
    private Image[][] outdoorsExits = new Image[3][3];

    private Image[][] redWalls = new Image[3][3];
    private Image[][] redExits = new Image[3][3];

    private Image[][] yellowWalls = new Image[3][3];
    private Image[][] yellowExits = new Image[3][3];

    private Image[][] blueWalls = new Image[3][3];
    private Image[][] blueExits = new Image[3][3];

    private Image[][] pinkWalls = new Image[3][3];
    private Image[][] pinkExits = new Image[3][3];

    private Image[][] purpleWalls = new Image[3][3];
    private Image[][] purpleExits = new Image[3][3];

    private Image[][] orangeWalls = new Image[3][3];
    private Image[][] orangeExits = new Image[3][3];

    private Image[][] brownWalls = new Image[3][3];
    private Image[][] brownExits = new Image[3][3];

    private Image[][] turquoiseWalls = new Image[3][3];
    private Image[][] turquoiseExits = new Image[3][3];

    private Image[][] ivoryWalls = new Image[3][3];
    private Image[][] ivoryExits = new Image[3][3];

    public RoomIconTransformer() {
        nExitIn = loadAndRotateImage( "nIn.gif", null, null );
        wExitIn = loadAndRotateImage( "nIn.gif", 270d, null );
        sExitIn = loadAndRotateImage( "nIn.gif", 180d, null );
        eExitIn = loadAndRotateImage( "nIn.gif", 90d, null );
        neExitIn = loadAndRotateImage( "neIn.gif", null, null );
        nwExitIn = loadAndRotateImage( "neIn.gif", 270d, null );
        swExitIn = loadAndRotateImage( "neIn.gif", 180d, null );
        seExitIn = loadAndRotateImage( "neIn.gif", 90d, null );

        nWallIn = loadAndRotateImage( "nwallIn.gif", null, null );
        wWallIn = loadAndRotateImage( "nwallIn.gif", 270d, null );
        sWallIn = loadAndRotateImage( "nwallIn.gif", 180d, null );
        eWallIn = loadAndRotateImage( "nwallIn.gif", 90d, null );

        neWallIn = loadAndRotateImage( "newallIn.gif", null, null );
        nwWallIn = loadAndRotateImage( "newallIn.gif", 270d, null );
        swWallIn = loadAndRotateImage( "newallIn.gif", 180d, null );
        seWallIn = loadAndRotateImage( "newallIn.gif", 90d, null );


        middleIn = loadAndRotateImage( "middleIn.gif", null, null );
        middleSpecialIn = loadAndRotateImage( "middlespecialIn.gif", null, null );

        nExitOut = loadAndRotateImage( "nOut.gif", null, null );
        wExitOut = loadAndRotateImage( "nOut.gif", 270d, null );
        sExitOut = loadAndRotateImage( "nOut.gif", 180d, null );
        eExitOut = loadAndRotateImage( "nOut.gif", 90d, null );
        neExitOut = loadAndRotateImage( "neOut.gif", null, null );
        nwExitOut = loadAndRotateImage( "neOut.gif", 270d, null );
        swExitOut = loadAndRotateImage( "neOut.gif", 180d, null );
        seExitOut = loadAndRotateImage( "neOut.gif", 90d, null );

        nWallOut = loadAndRotateImage( "nwallOut.gif", null, null );
        wWallOut = loadAndRotateImage( "nwallOut.gif", 270d, null );
        sWallOut = loadAndRotateImage( "nwallOut.gif", 180d, null );
        eWallOut = loadAndRotateImage( "nwallOut.gif", 90d, null );

        neWallOut = loadAndRotateImage( "newallOut.gif", null, null );
        nwWallOut = loadAndRotateImage( "newallOut.gif", 270d, null );
        swWallOut = loadAndRotateImage( "newallOut.gif", 180d, null );
        seWallOut = loadAndRotateImage( "newallOut.gif", 90d, null );


        middleOut = loadImage( "middleOut.gif" );
        middleSpecialOut = loadImage( "middlespecialOut.gif" );

//		"normal","red","yellow","blue", "pink","purple","orange","brown","turquoise","ivory"
        loadIndoors();
        loadOutdoors();
        loadRed();
        loadColoredIcons( yellowWalls, yellowExits, RoomColors.YELLOW );
        loadColoredIcons( blueWalls, blueExits, RoomColors.BLUE );
        loadColoredIcons( pinkWalls, pinkExits, RoomColors.PINK );
        loadColoredIcons( purpleWalls, purpleExits, RoomColors.PURPLE );
        loadColoredIcons( orangeWalls, orangeExits, RoomColors.ORANGE );
        loadColoredIcons( brownWalls, brownExits, RoomColors.BROWN );
        loadColoredIcons( turquoiseWalls, turquoiseExits, RoomColors.TURQUOISE );
        loadColoredIcons( ivoryWalls, ivoryExits, RoomColors.IVORY );


    }


    @Override
    public Icon transform( Room room ) {
        return new ImageIcon( assembleImageBasedOn( room ) );
    }

    private BufferedImage loadAndRotateImage( String filename, Double rotation, Color color ) {
        BufferedImage newImage = null;
        BufferedImage originalImage = null;

        if (color != null) {
            newImage = loadImage( filename );
            newImage = changeColor( newImage, color );
        } else {
            newImage = loadImage( filename );
        }

        if (rotation != null) {
            originalImage = newImage;
            newImage = new BufferedImage( originalImage.getWidth(), originalImage.getHeight(), originalImage.getType() );

            Graphics2D graphics = newImage.createGraphics();

            graphics.rotate( Math.toRadians( rotation ), originalImage.getWidth() / 2, originalImage.getHeight() / 2 );
            graphics.drawImage( originalImage, 0, 0, originalImage.getWidth(), originalImage.getHeight(), 0, 0, originalImage.getWidth(), originalImage.getHeight(), null );
            graphics.dispose();
        }
        return newImage;
    }


    private BufferedImage loadImage( String filename ) {
        BufferedImage image;
        try {
            URL imgURL = getClass().getResource( PATH + filename );
            image = ImageIO.read( imgURL );
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private BufferedImage assembleImageBasedOn( Room room ) {
        Image[][] walls = null;
        Image[][] exits = null;
        boolean[][] sectionHasExit = getExitGrid( room.getExits() );// x,y going left to right, up to down
        if (room.getColor() != null) {
            if (room.getColor().equals( RoomColors.RED )) {
                walls = redWalls;
                exits = redExits;
            } else if (room.getColor().equals( RoomColors.YELLOW )) {
                walls = yellowWalls;
                exits = yellowExits;
            } else if (room.getColor().equals( RoomColors.BLUE )) {
                walls = blueWalls;
                exits = blueExits;
            } else if (room.getColor().equals( RoomColors.PINK )) {
                walls = pinkWalls;
                exits = pinkExits;
            } else if (room.getColor().equals( RoomColors.PURPLE )) {
                walls = purpleWalls;
                exits = purpleExits;
            } else if (room.getColor().equals( RoomColors.ORANGE )) {
                walls = orangeWalls;
                exits = orangeExits;
            } else if (room.getColor().equals( RoomColors.BROWN )) {
                walls = brownWalls;
                exits = brownExits;
            } else if (room.getColor().equals( RoomColors.TURQUOISE )) {
                walls = turquoiseWalls;
                exits = turquoiseExits;
            } else if (room.getColor().equals( RoomColors.IVORY )) {
                walls = ivoryWalls;
                exits = ivoryExits;
            }

        } else {
            if (! room.isIndoors()) {
                walls = indoorsWalls;
                exits = indoorsExits;

            } else if (room.isIndoors()) {
                walls = outdoorsWalls;
                exits = outdoorsExits;
            }
        }


        BufferedImage roomImage = new BufferedImage( 90, 90, BufferedImage.TYPE_4BYTE_ABGR );
        Graphics2D paint = roomImage.createGraphics();
        paint.setBackground( Color.WHITE );
        for (int i = 0; i < 3; ++ i) {
            for (int j = 0; j < 3; ++ j) {
                if (sectionHasExit[i][j]) {
                    paint.drawImage( exits[i][j], i * 30, j * 30, null );
                } else {
                    paint.drawImage( walls[i][j], i * 30, j * 30, null );
                }
            }
        }
        paint.dispose();

        if (room.isPicked()) {
            BufferedImage tempImage = new BufferedImage( 96, 96, BufferedImage.TYPE_4BYTE_ABGR );
            Graphics2D gfx = tempImage.createGraphics();
            gfx.setColor( PICKED );
            gfx.fillRect( 0, 0, 96, 96 );
            gfx.drawImage( roomImage, 3, 3, null );
            gfx.dispose();
            roomImage = tempImage;
        }

        if (room.isCurrent()) {
            BufferedImage tempImage = new BufferedImage( 102, 102, BufferedImage.TYPE_4BYTE_ABGR );
            Graphics2D gfx = tempImage.createGraphics();
            gfx.setColor( CURRENT );
            gfx.fillRect( 0, 0, 102, 102 );
            if (room.isPicked()) {
                gfx.drawImage( roomImage, 3, 3, null );
            } else {
                gfx.drawImage( roomImage, 6, 6, null );
            }
            gfx.dispose();
            roomImage = tempImage;
        }
        return roomImage;

    }


    private boolean[][] getExitGrid( Set<String> exits ) {
        boolean[][] grid = new boolean[3][3];
        for (int i = 0; i < 3; ++ i) {
            for (int j = 0; j < 3; ++ j) {
                grid[i][j] = false;
            }
        }

        for (String exit : exits) {
            if (exit.equalsIgnoreCase( "n" ) || exit.equalsIgnoreCase( "north" )) {
                grid[1][0] = true;
            } else if (exit.equalsIgnoreCase( "e" ) || exit.equalsIgnoreCase( "east" )) {
                grid[2][1] = true;
            } else if (exit.equalsIgnoreCase( "s" ) || exit.equalsIgnoreCase( "south" )) {
                grid[1][2] = true;
            } else if (exit.equalsIgnoreCase( "w" ) || exit.equalsIgnoreCase( "west" )) {
                grid[0][1] = true;
            } else if (exit.equalsIgnoreCase( "ne" ) || exit.equalsIgnoreCase( "northeast" )) {
                grid[2][0] = true;
            } else if (exit.equalsIgnoreCase( "nw" ) || exit.equalsIgnoreCase( "northwest" )) {
                grid[0][0] = true;
            } else if (exit.equalsIgnoreCase( "se" ) || exit.equalsIgnoreCase( "southeast" )) {
                grid[2][2] = true;
            } else if (exit.equalsIgnoreCase( "sw" ) || exit.equalsIgnoreCase( "southwest" )) {
                grid[0][2] = true;
            } else {
                grid[1][1] = true;
            }
        }
        return grid;
    }


    private void loadOutdoors() {
        outdoorsWalls[0][0] = nwWallOut;
        outdoorsWalls[1][0] = nWallOut;
        outdoorsWalls[2][0] = neWallOut;

        outdoorsWalls[0][1] = wWallOut;
        outdoorsWalls[1][1] = middleOut;
        outdoorsWalls[2][1] = eWallOut;

        outdoorsWalls[0][2] = swWallOut;
        outdoorsWalls[1][2] = sWallOut;
        outdoorsWalls[2][2] = seWallOut;

        outdoorsExits[0][0] = nwExitOut;
        outdoorsExits[1][0] = nExitOut;
        outdoorsExits[2][0] = neExitOut;

        outdoorsExits[0][1] = wExitOut;
        outdoorsExits[1][1] = middleSpecialOut;
        outdoorsExits[2][1] = eExitOut;

        outdoorsExits[0][2] = swExitOut;
        outdoorsExits[1][2] = sExitOut;
        outdoorsExits[2][2] = seExitOut;

    }


    private void loadIndoors() {
        indoorsWalls[0][0] = nwWallIn;
        indoorsWalls[1][0] = nWallIn;
        indoorsWalls[2][0] = neWallIn;

        indoorsWalls[0][1] = wWallIn;
        indoorsWalls[1][1] = middleIn;
        indoorsWalls[2][1] = eWallIn;

        indoorsWalls[0][2] = swWallIn;
        indoorsWalls[1][2] = sWallIn;
        indoorsWalls[2][2] = seWallIn;

        indoorsExits[0][0] = nwExitIn;
        indoorsExits[1][0] = nExitIn;
        indoorsExits[2][0] = neExitIn;

        indoorsExits[0][1] = wExitIn;
        indoorsExits[1][1] = middleSpecialIn;
        indoorsExits[2][1] = eExitIn;

        indoorsExits[0][2] = swExitIn;
        indoorsExits[1][2] = sExitIn;
        indoorsExits[2][2] = seExitIn;

    }

    private void loadColoredIcons( Image[][] walls, Image[][] exits, Color color ) {
        if (walls == null) {
            walls = new Image[3][3];
        }
        if (exits == null) {
            exits = new Image[3][3];
        }
        walls[0][0] = loadAndRotateImage( "newall.gif", 270d, color );
        walls[1][0] = loadAndRotateImage( "nwall.gif", null, color );
        walls[2][0] = loadAndRotateImage( "newall.gif", null, color );

        walls[0][1] = loadAndRotateImage( "nwall.gif", 270d, color );
        walls[1][1] = loadAndRotateImage( "middle.gif", null, color );
        walls[2][1] = loadAndRotateImage( "nwall.gif", 90d, color );

        walls[0][2] = loadAndRotateImage( "newall.gif", 180d, color );
        walls[1][2] = loadAndRotateImage( "nwall.gif", 180d, color );
        walls[2][2] = loadAndRotateImage( "newall.gif", 90d, color );

        exits[0][0] = loadAndRotateImage( "ne.gif", 270d, color );
        exits[1][0] = loadAndRotateImage( "n.gif", null, color );
        exits[2][0] = loadAndRotateImage( "ne.gif", null, color );

        exits[0][1] = loadAndRotateImage( "n.gif", 270d, color );
        exits[1][1] = loadAndRotateImage( "special.gif", null, color );
        exits[2][1] = loadAndRotateImage( "n.gif", 90d, color );

        exits[0][2] = loadAndRotateImage( "ne.gif", 180d, color );
        exits[1][2] = loadAndRotateImage( "n.gif", 180d, color );
        exits[2][2] = loadAndRotateImage( "ne.gif", 90d, color );
    }

    private void loadRed() {

        redWalls[0][0] = loadAndRotateImage( "newall.gif", 270d, RoomColors.RED );
        redWalls[1][0] = loadAndRotateImage( "nwall.gif", null, RoomColors.RED );
        redWalls[2][0] = loadAndRotateImage( "newall.gif", null, RoomColors.RED );

        redWalls[0][1] = loadAndRotateImage( "nwall.gif", 270d, RoomColors.RED );
        redWalls[1][1] = loadAndRotateImage( "middle.gif", null, RoomColors.RED );
        redWalls[2][1] = loadAndRotateImage( "nwall.gif", 90d, RoomColors.RED );

        redWalls[0][2] = loadAndRotateImage( "newall.gif", 180d, RoomColors.RED );
        redWalls[1][2] = loadAndRotateImage( "nwall.gif", 180d, RoomColors.RED );
        redWalls[2][2] = loadAndRotateImage( "newall.gif", 90d, RoomColors.RED );

        redExits[0][0] = loadAndRotateImage( "ne.gif", 270d, RoomColors.RED );
        redExits[1][0] = loadAndRotateImage( "n.gif", null, RoomColors.RED );
        redExits[2][0] = loadAndRotateImage( "ne.gif", null, RoomColors.RED );

        redExits[0][1] = loadAndRotateImage( "n.gif", 270d, RoomColors.RED );
        redExits[1][1] = loadAndRotateImage( "special.gif", null, RoomColors.RED );
        redExits[2][1] = loadAndRotateImage( "n.gif", 90d, RoomColors.RED );

        redExits[0][2] = loadAndRotateImage( "ne.gif", 180d, RoomColors.RED );
        redExits[1][2] = loadAndRotateImage( "n.gif", 180d, RoomColors.RED );
        redExits[2][2] = loadAndRotateImage( "ne.gif", 90d, RoomColors.RED );
    }

    private BufferedImage changeColor( BufferedImage image, Color color ) {
        for (int i = 0; i < image.getWidth(); ++ i) {
            for (int j = 0; j < image.getHeight(); ++ j) {
                if (image.getRGB( i, j ) == Color.WHITE.getRGB()) {
                    image.setRGB( i, j, color.getRGB() );
                }
            }
        }
        return image;
    }


}
