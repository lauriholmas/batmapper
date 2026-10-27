package com.glaurung.batMap.io;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashSet;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import com.glaurung.batMap.vo.Area;
import com.glaurung.batMap.vo.AreaSaveObject;
import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;

import edu.uci.ics.jung.graph.util.EdgeType;

public class AreaDataPersisterTest {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    @Test
    public void legacyAreaIsConvertedToJsonAndLoadsBack() throws Exception {
        String basedir = folder.getRoot().getPath();
        File areaDir = new File( new File( basedir, "conf" ), "batMapAreas" );
        assertTrue( areaDir.mkdirs() );

        Area area = new Area( "arelium" );
        Room hall = new Room( "Entrance hall", "id1", area );
        hall.setLongDesc( "blah blah" );
        hall.addExits( Arrays.asList( "west", "north" ) );
        hall.setIndoors( true );
        hall.setAreaEntrance( true );
        hall.setColor( new Color( 0x12, 0x34, 0x56 ) );
        hall.setNotes( "notes here" );
        hall.setLabel( "HALL" );
        hall.useExit( "north" );
        Room yard = new Room( "Yard", "id2", area );
        yard.setLongDesc( "a yard" );
        yard.addExit( "south" );

        AreaSaveObject legacy = new AreaSaveObject();
        legacy.getGraph().addEdge( new Exit( "north" ), hall, yard, EdgeType.DIRECTED );
        legacy.getGraph().addEdge( new Exit( "south" ), yard, hall, EdgeType.DIRECTED );
        legacy.getLocations().put( hall, new Point2D.Double( 10, 20 ) );
        legacy.getLocations().put( yard, new Point2D.Double( 10, -40.5 ) );
        try (ObjectOutputStream out = new ObjectOutputStream( new FileOutputStream( new File( areaDir, "arelium.batmap" ) ) )) {
            out.writeObject( legacy );
        }

        AreaDataPersister.convertLegacyFilesToJson( basedir );
        assertTrue( new File( areaDir, "arelium.json" ).exists() );
        assertFalse( new File( areaDir, "arelium.json.tmp" ).exists() );
        assertEquals( Arrays.asList( "arelium" ), AreaDataPersister.listAreaNames( basedir ) );

        // make sure the json is what gets loaded from now on
        assertTrue( new File( areaDir, "arelium.batmap" ).delete() );
        AreaSaveObject loaded = AreaDataPersister.loadData( basedir, "arelium" );

        assertEquals( 2, loaded.getGraph().getVertexCount() );
        assertEquals( 2, loaded.getGraph().getEdgeCount() );
        Room loadedHall = null;
        Room loadedYard = null;
        for (Room room : loaded.getGraph().getVertices()) {
            if (room.getId().equals( "id1" )) {
                loadedHall = room;
            } else {
                loadedYard = room;
            }
        }
        assertEquals( "Entrance hall", loadedHall.getShortDesc() );
        assertEquals( "blah blah", loadedHall.getLongDesc() );
        assertEquals( new HashSet<String>( Arrays.asList( "west", "north" ) ), loadedHall.getExits() );
        assertEquals( new HashSet<String>( Arrays.asList( "north" ) ), loadedHall.getUsedExits() );
        assertTrue( loadedHall.isIndoors() );
        assertTrue( loadedHall.isAreaEntrance() );
        assertEquals( new Color( 0x12, 0x34, 0x56 ), loadedHall.getColor() );
        assertEquals( "notes here", loadedHall.getNotes() );
        assertEquals( "HALL", loadedHall.getLabel() );
        assertEquals( "arelium", loadedHall.getArea().getName() );
        assertSame( loadedHall.getArea(), loadedYard.getArea() );
        assertNull( loadedYard.getColor() );
        assertFalse( loadedYard.isIndoors() );

        assertEquals( new Point2D.Double( 10, 20 ), loaded.getLocations().get( loadedHall ) );
        assertEquals( new Point2D.Double( 10, -40.5 ), loaded.getLocations().get( loadedYard ) );

        Exit north = loaded.getGraph().findEdge( loadedHall, loadedYard );
        assertEquals( "north", north.getExit() );
        assertEquals( EdgeType.DIRECTED, loaded.getGraph().getEdgeType( north ) );
        assertSame( loadedHall, loaded.getGraph().getSource( north ) );
        assertEquals( "south", loaded.getGraph().findEdge( loadedYard, loadedHall ).getExit() );
    }
}
