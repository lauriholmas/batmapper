package com.glaurung.batMap.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.glaurung.batMap.gui.corpses.CorpseModel;

public class CorpseHandlerDataPersister {

    private final static String FILENAME = "corpseHandler.conf";
    private final static String DIRNAME = "conf";


    public static void save( String baseDir, CorpseModel model ) {
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream( getFile( baseDir ) );
            ObjectOutputStream objectOutputStream = new ObjectOutputStream( fileOutputStream );
            objectOutputStream.writeObject( model );
            fileOutputStream.close();
        } catch (IOException e) {
            System.out.println( e );
        }


    }

    public static CorpseModel load( String basedir ) {
        try (FileInputStream fileInputStream = new FileInputStream( getFile( basedir ) );
             ObjectInputStream objectInputStream = new ObjectInputStream( fileInputStream )) {
            CorpseModel model = (CorpseModel) objectInputStream.readObject();
            return model;
        } catch (IOException e) {
            System.out.println( e );
        } catch (ClassNotFoundException e) {
            System.out.println( e );
        }
        return null;

    }

    private static File getFile( String basedir ) {
        File dirFile = new File( basedir, DIRNAME );
        File saveFile = new File( dirFile, FILENAME );
        return saveFile;
    }
}
