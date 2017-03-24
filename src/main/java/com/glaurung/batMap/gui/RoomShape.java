package com.glaurung.batMap.gui;

import java.awt.Shape;
import java.awt.geom.Area;

import org.apache.commons.collections15.Transformer;

import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;

import edu.uci.ics.jung.graph.SparseMultigraph;
import edu.uci.ics.jung.visualization.decorators.AbstractVertexShapeTransformer;

public class RoomShape extends AbstractVertexShapeTransformer<Room> implements Transformer<Room, Shape> {

    public RoomShape( SparseMultigraph<Room, Exit> graph ) {
    }

    public Shape transform( Room room ) {

        setSizeTransformer( new Transformer<Room, Integer>() {
            public Integer transform( Room room ) {
                return DrawingUtils.ROOM_SIZE;
            }
        } );

        Area roomGfx = new Area( factory.getRectangle( room ) );
        return roomGfx;
    }


}
