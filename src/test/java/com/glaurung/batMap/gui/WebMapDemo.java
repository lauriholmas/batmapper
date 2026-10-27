package com.glaurung.batMap.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashSet;

import com.glaurung.batMap.controller.MapperEngine;
import com.glaurung.batMap.controller.SearchEngine;
import com.glaurung.batMap.web.MapWebServer;

/**
 * Runs the web map without batclient. Walks a small test area, then reads moves from stdin as
 * "roomId exitUsed [indoors] [exits,comma,separated]".
 * Optional first argument is the base dir to save maps into, a temp dir is used otherwise.
 */
public class WebMapDemo {

    public static void main( String[] args ) throws Exception {
        String baseDir = args.length > 0 ? args[0] : Files.createTempDirectory( "batmap" ).toString();
        new File( baseDir, "conf" ).mkdirs();
        MapperEngine engine = new MapperEngine( null );
        SearchEngine searchEngine = new SearchEngine( null );
        engine.setBaseDir( baseDir );
        searchEngine.setBaseDir( baseDir );
        MapWebServer server = new MapWebServer( engine, searchEngine );
        server.start( MapWebServer.DEFAULT_PORT );
        System.out.println( "Map at " + server.getUrl() + " saving to " + baseDir );

        move( engine, "1", "", false, "Entrance to the test area", "n,e" );
        move( engine, "2", "n", false, "A path going north", "n,s,ne" );
        move( engine, "3", "n", true, "Inside a hut", "s,u,enter hole" );
        move( engine, "4", "u", true, "Hut attic", "d" );
        move( engine, "3", "d", true, "Inside a hut", "s,u,enter hole" );
        move( engine, "5", "enter hole", true, "A dark hole", "out" );
        move( engine, "3", "out", true, "Inside a hut", "s,u,enter hole" );
        move( engine, "2", "s", false, "A path going north", "n,s,ne" );
        move( engine, "6", "ne", false, "Grassy field", "sw,e,w,se" );
        move( engine, "6", "w", false, "Grassy field", "sw,e,w,se" );
        move( engine, "7", "e", false, "Edge of the field", "w,s,nw" );
        move( engine, "1", "teleport", false, "Entrance to the test area", "n,e" );
        engine.save();
        searchEngine.setMapperArea( "testArea" );

        BufferedReader in = new BufferedReader( new InputStreamReader( System.in ) );
        String line;
        while (( line = in.readLine() ) != null) {
            String[] parts = line.trim().split( " ", 4 );
            if (parts.length >= 2) {
                boolean indoors = parts.length > 2 && parts[2].equals( "1" );
                String exits = parts.length > 3 ? parts[3] : "";
                move( engine, parts[0], parts[1], indoors, "Room " + parts[0], exits );
            }
        }
    }

    private static void move( MapperEngine engine, String id, String exit, boolean indoors, String shortDesc, String exits ) {
        engine.moveToRoom( "testArea", id, exit, indoors, shortDesc, shortDesc + ". This is the long description of room " + id + ".",
                new HashSet<String>( Arrays.asList( exits.split( "," ) ) ) );
    }
}
