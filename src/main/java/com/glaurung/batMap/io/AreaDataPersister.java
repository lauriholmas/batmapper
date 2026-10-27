package com.glaurung.batMap.io;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import com.glaurung.batMap.vo.Area;
import com.glaurung.batMap.vo.AreaSaveObject;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class AreaDataPersister {

    private static final String SUFFIX = ".json";
    private static final String LEGACY_SUFFIX = ".batmap";
    private static final String PATH = "batMapAreas";
    private static final String NEW_PATH = "conf";
    private static final int FORMAT_VERSION = 1;

    // json file layout for one area, rooms are referenced from exits by room id
    private static class AreaJson {
        int version;
        List<RoomJson> rooms = new ArrayList<RoomJson>();
        List<ExitJson> exits = new ArrayList<ExitJson>();
    }

    private static class RoomJson {
        String id;
        String area;
        String shortDesc;
        String longDesc;
        String label;
        String notes;
        String color;
        boolean indoors;
        boolean areaEntrance;
        boolean current;
        boolean drawn;
        boolean picked;
        Set<String> exits;
        Set<String> usedExits;
        Double x;
        Double y;
    }

    private static class ExitJson {
        String exit;
        String from;
        String to;
        boolean directed;
        boolean currentExit;
    }


    public static void save( String basedir, SparseMultigraph<Room, Exit> graph, Layout<Room, Exit> layout ) throws IOException {
        AreaSaveObject saveObject = makeSaveObject( basedir, graph, layout );
        saveData( saveObject );

    }


    private static void saveData( AreaSaveObject saveObject ) throws IOException {
        JsonFiles.write( new File( saveObject.getFileName() ), toJson( saveObject ) );
    }


    public static AreaSaveObject loadData( String basedir, String areaName ) throws IOException, ClassNotFoundException {

        File dataFile = new File( getFileNameFrom( basedir, areaName ) );
//		System.out.println("\n\n+ndataFileForLoading\n\n\n"+dataFile);
        if (dataFile.exists()) {
            AreaSaveObject saveObject = fromJson( JsonFiles.read( dataFile, AreaJson.class ) );
            saveObject.setFileName( dataFile.getPath() );
            return saveObject;
        }
        File legacyFile = new File( getFileNameFrom( basedir, areaName, LEGACY_SUFFIX ) );
        if (legacyFile.exists()) {
            return (AreaSaveObject) JsonFiles.readLegacy( legacyFile );
        }
        throw new FileNotFoundException( dataFile.getPath() );
    }

    private static AreaJson toJson( AreaSaveObject saveObject ) {
        SparseMultigraph<Room, Exit> graph = saveObject.getGraph();
        AreaJson json = new AreaJson();
        json.version = FORMAT_VERSION;
        for (Room room : graph.getVertices()) {
            RoomJson roomJson = new RoomJson();
            roomJson.id = room.getId();
            roomJson.area = room.getArea() == null ? null : room.getArea().getName();
            roomJson.shortDesc = room.getShortDesc();
            roomJson.longDesc = room.getLongDesc();
            roomJson.label = room.getLabel();
            roomJson.notes = room.getNotes();
            roomJson.color = colorToString( room.getColor() );
            roomJson.indoors = room.isIndoors();
            roomJson.areaEntrance = room.isAreaEntrance();
            roomJson.current = room.isCurrent();
            roomJson.drawn = room.isDrawn();
            roomJson.picked = room.isPicked();
            roomJson.exits = room.getExits();
            roomJson.usedExits = room.getUsedExits();
            Point2D location = saveObject.getLocations().get( room );
            if (location != null) {
                roomJson.x = location.getX();
                roomJson.y = location.getY();
            }
            json.rooms.add( roomJson );
        }
        for (Exit exit : graph.getEdges()) {
            ExitJson exitJson = new ExitJson();
            exitJson.exit = exit.getExit();
            exitJson.from = graph.getSource( exit ) != null ? graph.getSource( exit ).getId() : graph.getEndpoints( exit ).getFirst().getId();
            exitJson.to = graph.getDest( exit ) != null ? graph.getDest( exit ).getId() : graph.getEndpoints( exit ).getSecond().getId();
            exitJson.directed = graph.getEdgeType( exit ) == EdgeType.DIRECTED;
            exitJson.currentExit = exit.isCurrentExit();
            json.exits.add( exitJson );
        }
        return json;
    }

    private static AreaSaveObject fromJson( AreaJson json ) throws IOException {
        AreaSaveObject saveObject = new AreaSaveObject();
        SparseMultigraph<Room, Exit> graph = saveObject.getGraph();
        Map<String, Area> areas = new HashMap<String, Area>();
        Map<String, Room> roomsById = new HashMap<String, Room>();
        for (RoomJson roomJson : json.rooms) {
            Area area = null;
            if (roomJson.area != null) {
                area = areas.get( roomJson.area );
                if (area == null) {
                    area = new Area( roomJson.area );
                    areas.put( roomJson.area, area );
                }
            }
            Room room = new Room( roomJson.id, area );
            room.setDescs( roomJson.shortDesc, roomJson.longDesc );
            room.setLabel( roomJson.label );
            room.setNotes( roomJson.notes );
            room.setColor( colorFromString( roomJson.color ) );
            room.setIndoors( roomJson.indoors );
            room.setAreaEntrance( roomJson.areaEntrance );
            room.setCurrent( roomJson.current );
            room.setDrawn( roomJson.drawn );
            room.setPicked( roomJson.picked );
            room.setExits( roomJson.exits != null ? roomJson.exits : new HashSet<String>() );
            room.setUsedExits( roomJson.usedExits != null ? roomJson.usedExits : new HashSet<String>() );
            graph.addVertex( room );
            roomsById.put( room.getId(), room );
            if (roomJson.x != null && roomJson.y != null) {
                saveObject.getLocations().put( room, new Point2D.Double( roomJson.x, roomJson.y ) );
            }
        }
        for (ExitJson exitJson : json.exits) {
            Room from = roomsById.get( exitJson.from );
            Room to = roomsById.get( exitJson.to );
            if (from == null || to == null) {
                throw new IOException( "Exit " + exitJson.exit + " refers to unknown room " + ( from == null ? exitJson.from : exitJson.to ) );
            }
            Exit exit = new Exit( exitJson.exit );
            exit.setCurrentExit( exitJson.currentExit );
            graph.addEdge( exit, from, to, exitJson.directed ? EdgeType.DIRECTED : EdgeType.UNDIRECTED );
        }
        return saveObject;
    }

    private static String colorToString( Color color ) {
        if (color == null) {
            return null;
        }
        if (color.getAlpha() == 255) {
            return String.format( "#%06x", color.getRGB() & 0xffffff );
        }
        return String.format( "#%08x", color.getRGB() );
    }

    private static Color colorFromString( String color ) throws IOException {
        if (color == null) {
            return null;
        }
        String hex = color.startsWith( "#" ) ? color.substring( 1 ) : color;
        try {
            if (hex.length() == 6) {
                return new Color( Integer.parseInt( hex, 16 ) );
            }
            if (hex.length() == 8) {
                return new Color( (int) Long.parseLong( hex, 16 ), true );
            }
        } catch (NumberFormatException e) {
            // fall through to the error below
        }
        throw new IOException( "Invalid color " + color );
    }

    public static List<String> listAreaNames( String basedir ) {
        File newDir = new File( basedir, NEW_PATH );
        newDir = new File( newDir, PATH );
        File folder = newDir;
        File[] files = folder.listFiles();
        // areas that haven't been converted from the old format yet are listed too
        Set<String> names = new LinkedHashSet<String>();
        if (files != null) {
            for (File file : files) {
                String extension = "." + FilenameUtils.getExtension( file.getName() );
                if (extension.equals( SUFFIX ) || extension.equals( LEGACY_SUFFIX )) {
//				System.out.println(FilenameUtils.getBaseName(file.getName()));
                    names.add(FilenameUtils.getBaseName(file.getName()));
                }
            }
        }
        return new LinkedList<String>( names );
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
        return getFileNameFrom( basedir, areaName, SUFFIX );
    }

    private static String getFileNameFrom( String basedir, String areaName, String suffix ) throws IOException {

        areaName = areaName.replaceAll( "'", "" );
        areaName = areaName.replaceAll( "/", "" );
        areaName = areaName + suffix;
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

    /**
     * Writes a json copy of every old java-serialized .batmap file that doesn't have one yet.
     * The old files are left in place untouched.
     */
    public static void convertLegacyFilesToJson( String basedir ) {
        for (String areaName : listAreaNames( basedir )) {
            try {
                File jsonFile = new File( getFileNameFrom( basedir, areaName ) );
                if (jsonFile.exists()) {
                    continue;
                }
                File legacyFile = new File( getFileNameFrom( basedir, areaName, LEGACY_SUFFIX ) );
                AreaSaveObject saveObject = (AreaSaveObject) JsonFiles.readLegacy( legacyFile );
                saveObject.setFileName( jsonFile.getPath() );
                saveData( saveObject );
                System.out.println( "Converted " + legacyFile + " to " + jsonFile );
            } catch (IOException | ClassNotFoundException | RuntimeException e) {
                System.out.println( "Unable to convert area " + areaName + " to json: " + e );
            }
        }
    }


}
