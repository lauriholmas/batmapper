package com.glaurung.batMap.gui;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.geom.Point2D;

import org.apache.commons.collections15.Predicate;
import org.apache.commons.collections15.Transformer;

import com.glaurung.batMap.vo.Exit;
import com.glaurung.batMap.vo.Room;

import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.util.SelfLoopEdgePredicate;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.Context;
import edu.uci.ics.jung.graph.util.Pair;
import edu.uci.ics.jung.visualization.Layer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.transform.BidirectionalTransformer;

public class ExitPaintTransformer implements Transformer<Exit, Paint> {

    private static final Color INDOOR = new Color( 50, 50, 50 );
    private static final Color OUTDOOR = new Color( 8, 208, 55 );
    private VisualizationViewer<Room, Exit> vv;
    protected BidirectionalTransformer transformer;
    protected Predicate<Context<Graph<Room, Exit>, Exit>> selfLoop = new SelfLoopEdgePredicate<Room, Exit>();

    public ExitPaintTransformer( VisualizationViewer<Room, Exit> vv ) {
        this.vv = vv;
        this.transformer = vv.getRenderContext().getMultiLayerTransformer().getTransformer( Layer.LAYOUT );
    }

    public Paint transform( Exit exit ) {
        Layout<Room, Exit> layout = vv.getGraphLayout();
        Pair<Room> pair = layout.getGraph().getEndpoints( exit );
        Room begin = pair.getFirst();
        Room end = pair.getSecond();
        Point2D beginPoint = transformer.transform( layout.transform( begin ) );
        Point2D endPoint = transformer.transform( layout.transform( end ) );
        float xFirst = (float) beginPoint.getX();
        float yFirst = (float) beginPoint.getY();
        float xEnd = (float) endPoint.getX();
        float yEnd = (float) endPoint.getY();

        if (selfLoop.evaluate( Context.<Graph<Room, Exit>, Exit>getInstance( layout.getGraph(), exit ) )) {
            xEnd += 50;
            yEnd += 50;
        }

        return new GradientPaint( xFirst, yFirst, getColorFor( begin ), xEnd, yEnd, getColorFor( end ), true );
    }


    private Color getColorFor( Room room ) {
        if (room.isIndoors())
            return INDOOR;
        return OUTDOOR;
    }

}
