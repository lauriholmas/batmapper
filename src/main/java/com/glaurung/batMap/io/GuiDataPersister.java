package com.glaurung.batMap.io;

import java.awt.Dimension;
import java.awt.Point;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import com.glaurung.batMap.vo.GuiData;

public class GuiDataPersister {

    private final static String FILENAME = "batMap.conf";
    private final static String DIRNAME = "conf";


    public static void save( String baseDir, Point location, Dimension size ) {
        GuiData data = new GuiData( location, size );
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = new FileOutputStream( getFile( baseDir ) );
            ObjectOutputStream objectOutputStream = new ObjectOutputStream( fileOutputStream );
            objectOutputStream.writeObject( data );
            fileOutputStream.close();
        } catch (IOException e) {
            System.out.println( e );
        }


    }

    public static GuiData load( String basedir ) {
        try (FileInputStream fileInputStream = new FileInputStream( getFile( basedir ) );
             ObjectInputStream objectInputStream = new ObjectInputStream( fileInputStream )) {
            GuiData data = (GuiData) objectInputStream.readObject();
            return data;
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
