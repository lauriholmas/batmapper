package com.glaurung.batMap.io;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.IOException;

import com.glaurung.batMap.vo.GuiData;

public class GuiDataPersister {

    private final static String FILENAME = "batMap.json";
    private final static String LEGACY_FILENAME = "batMap.conf";
    private final static String DIRNAME = "conf";

    private static class GuiJson {
        int x;
        int y;
        int width;
        int height;
    }


    public static void save( String baseDir, Point location, Dimension size ) {
        GuiJson json = new GuiJson();
        json.x = location.x;
        json.y = location.y;
        json.width = size.width;
        json.height = size.height;
        try {
            JsonFiles.write( getFile( baseDir, FILENAME ), json );
        } catch (IOException e) {
            System.out.println( e );
        }


    }

    public static GuiData load( String basedir ) {
        try {
            File file = getFile( basedir, FILENAME );
            if (file.exists()) {
                GuiJson json = JsonFiles.read( file, GuiJson.class );
                return new GuiData( new Point( json.x, json.y ), new Dimension( json.width, json.height ) );
            }
            return (GuiData) JsonFiles.readLegacy( getFile( basedir, LEGACY_FILENAME ) );
        } catch (IOException e) {
            System.out.println( e );
        } catch (ClassNotFoundException e) {
            System.out.println( e );
        }
        return null;

    }

    private static File getFile( String basedir, String fileName ) {
        File dirFile = new File( basedir, DIRNAME );
        File saveFile = new File( dirFile, fileName );
        return saveFile;
    }

}
