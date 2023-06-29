package com.glaurung.batMap.io;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.glaurung.batMap.vo.AreaSaveObject;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.SparseMultigraph;

public class AreaDataPersister {

    private static final String SUFFIX = ".batmap";
    private static final String PATH = "batMapAreas";
    private static final String NEW_PATH = "conf";


    public static void save( String basedir, SparseMultigraph<Room, Exit> graph, Layout<Room, Exit> layout ) throws IOException {
        AreaSaveObject saveObject = makeSaveObject( basedir, graph, layout );
        saveData( saveObject );

    }


    private static void saveData( AreaSaveObject saveObject ) throws IOException {
	String baseName = saveObject.getFileName();
	File baseFile = new File( saveObject.getFileName() );
	File target = null;
	long timestamp = Long.MAX_VALUE;
	if (baseFile.exists()) {
	    for (int i = 0; i < 5; i++) {
		File backup = new File ( saveObject.getFileName() + "." + i + ".bk" );
		if (!backup.exists()) {
		    target = backup;
		    break;
		} else if (backup.lastModified() < timestamp) {
		    timestamp = backup.lastModified();
		    target = backup;
		}
	    }
	    System.out.println("Renaming " + baseFile + " to " + target);
	    baseFile.renameTo(target);
	}

        try (FileOutputStream fileOutputStream = new FileOutputStream(new File(saveObject.getFileName()));
             ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream)) {
            objectOutputStream.writeObject(saveObject);
        }
    }


    public static AreaSaveObject loadData( String basedir, String areaName ) throws IOException, ClassNotFoundException {

        File dataFile = new File( getFileNameFrom( basedir, areaName ) );
//		System.out.println("\n\n+ndataFileForLoading\n\n\n"+dataFile);
        try (FileInputStream fileInputStream = new FileInputStream(dataFile);
             ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream)) {
            return (AreaSaveObject) objectInputStream.readObject();
        }
    }

    public static List<String> listAreaNames( String basedir ) {
        File newDir = new File( basedir, NEW_PATH );
        newDir = new File( newDir, PATH );
        File folder = newDir;
        File[] files = folder.listFiles();
        LinkedList<String> names = new LinkedList<String>();
        if (files != null) {
            for (File file : files) {
                if (FilenameUtils.getExtension(file.getName()).equals("batmap")) {
//				System.out.println(FilenameUtils.getBaseName(file.getName()));
                    names.add(FilenameUtils.getBaseName(file.getName()));
                }
            }
        }
        return names;
    }

    private static AreaSaveObject makeSaveObject( String basedir, SparseMultigraph<Room, Exit> graph, Layout<Room, Exit> layout ) throws IOException {
        AreaSaveObject saveObject = new AreaSaveObject();
        saveObject.setGraph( graph );
        Map<Room, Point2D> locations = saveObject.getLocations();
        for (Room room : graph.getVertices()) {
            Point2D coord = layout.transform( room );
            locations.put( room, coord );
        }
        saveObject.setFileName( getFileNameFrom( basedir, graph.getVertices().iterator().next().getArea().getName() ) );
//		System.out.println("\n\n+nsaveobjectdone\n\n\n"+saveObject.getFileName());
        return saveObject;
    }

    private static String getFileNameFrom( String basedir, String areaName ) throws IOException {

        areaName = areaName.replaceAll( "'", "" );
        areaName = areaName.replaceAll( "/", "" );
        areaName = areaName + SUFFIX;
        File newDir = new File( basedir, NEW_PATH );
        newDir = new File( newDir, PATH );
//		File pathFile = new File(PATH);
        if (! newDir.exists()) {
            if (! newDir.mkdir()) {
                throw new IOException( PATH + " doesn't exist" );
            }
        }

        return new File( newDir, areaName ).getPath();
    }

    public static void migrateFilesToNewLocation( String basedir ) {
        File oldDir = new File( PATH );
        File newDir = new File( basedir, NEW_PATH );
        newDir = new File( newDir, PATH );
        if (! oldDir.exists())
            return;
        Collection<File> oldDirFiles = FileUtils.listFiles( oldDir, null, false );

        try {
            if (oldDirFiles.size() == 0) {
                FileUtils.deleteDirectory( oldDir );
                return;
            }
            FileUtils.forceMkdir( newDir );
            for (File mapfile : oldDirFiles) {
                if (! FileUtils.directoryContains( newDir, mapfile )) {
                    FileUtils.moveFileToDirectory( mapfile, newDir, true );
                } else {
                }
            }
            //all files moved to new place now, can safely delete old directory
            if (FileUtils.listFiles( oldDir, null, false ).size() == 0) {
                FileUtils.deleteDirectory( oldDir );
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }


}
