package com.glaurung.batMap.io;

import java.io.File;
import java.io.IOException;

import com.glaurung.batMap.gui.corpses.CorpseModel;

public class CorpseHandlerDataPersister {

    private final static String FILENAME = "corpseHandler.json";
    private final static String LEGACY_FILENAME = "corpseHandler.conf";
    private final static String DIRNAME = "conf";


    public static void save( String baseDir, CorpseModel model ) {
        try {
            JsonFiles.write( getFile( baseDir, FILENAME ), model );
        } catch (IOException e) {
            System.out.println( e );
        }


    }

    public static CorpseModel load( String basedir ) {
        try {
            File file = getFile( basedir, FILENAME );
            if (file.exists()) {
                return JsonFiles.read( file, CorpseModel.class );
            }
            return (CorpseModel) JsonFiles.readLegacy( getFile( basedir, LEGACY_FILENAME ) );
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
