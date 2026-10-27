package com.glaurung.batMap.web;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.glaurung.batMap.controller.MapperEngine;
import com.glaurung.batMap.controller.SearchEngine;
import com.glaurung.batMap.gui.RoomColors;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

/**
 * Serves the web map from localhost. The page gets map updates as server-sent events from /events
 * and sends user actions as json posts to /api.
 */
public class MapWebServer {

    public static final int DEFAULT_PORT = 8765;
    private static final int PORTS_TO_TRY = 10;
    private static final long KEEPALIVE_INTERVAL = 15000;
    private static final long UPDATE_COALESCE_MILLIS = 30;
    // browsers can't add custom headers to cross-site requests without a cors preflight, which we never allow
    private static final String ACTION_HEADER = "X-Batmap";
    private static final Map<String, String> STATIC_FILES = new HashMap<String, String>();

    static {
        STATIC_FILES.put( "/", "index.html" );
        STATIC_FILES.put( "/index.html", "index.html" );
        STATIC_FILES.put( "/map.css", "map.css" );
        STATIC_FILES.put( "/map.js", "map.js" );
    }

    private final Gson gson = new Gson();
    private final Map<String, MapperEngine> engines = new LinkedHashMap<String, MapperEngine>();
    private final Map<String, ViewChannel> channels = new HashMap<String, ViewChannel>();
    private HttpServer server;
    private ExecutorService executor;
    private int port;
    private volatile boolean running;

    public MapWebServer( MapperEngine mapEngine, SearchEngine searchEngine ) {
        engines.put( mapEngine.getViewName(), mapEngine );
        engines.put( searchEngine.getViewName(), searchEngine );
    }

    /**
     * Starts the server on the first free port starting from the given one
     */
    public void start( int firstPort ) throws IOException {
        IOException lastError = null;
        for (int candidate = firstPort; candidate < firstPort + PORTS_TO_TRY && server == null; candidate++) {
            try {
                server = HttpServer.create( new InetSocketAddress( InetAddress.getByName( "127.0.0.1" ), candidate ), 0 );
                port = candidate;
            } catch (BindException e) {
                lastError = e;
            }
        }
        if (server == null) {
            throw lastError;
        }
        executor = Executors.newCachedThreadPool();
        server.setExecutor( executor );
        server.createContext( "/", new StaticHandler() );
        server.createContext( "/events", new EventsHandler() );
        server.createContext( "/api", new ApiHandler() );
        running = true;
        for (Map.Entry<String, MapperEngine> entry : engines.entrySet()) {
            final ViewChannel channel = new ViewChannel( entry.getValue() );
            channels.put( entry.getKey(), channel );
            entry.getValue().addChangeListener( channel );
            Thread thread = new Thread( new Runnable() {
                public void run() {
                    channel.broadcastLoop();
                }
            }, "batMap web " + entry.getKey() );
            thread.setDaemon( true );
            thread.start();
        }
        server.start();
    }

    public void stop() {
        running = false;
        for (ViewChannel channel : channels.values()) {
            channel.close();
        }
        if (server != null) {
            server.stop( 0 );
        }
        if (executor != null) {
            executor.shutdownNow();
        }
    }

    public int getPort() {
        return port;
    }

    public String getUrl() {
        return "http://127.0.0.1:" + port + "/";
    }

    /**
     * Rejects requests that don't come addressed to us, so that other web sites can't reach the map
     * through dns rebinding.
     */
    private boolean isAllowedHost( HttpExchange exchange ) {
        String host = exchange.getRequestHeaders().getFirst( "Host" );
        return host != null && ( host.equals( "127.0.0.1:" + port ) || host.equals( "localhost:" + port ) );
    }

    private static void send( HttpExchange exchange, int status, String contentType, byte[] body ) throws IOException {
        exchange.getResponseHeaders().set( "Content-Type", contentType );
        exchange.getResponseHeaders().set( "Cache-Control", "no-store" );
        exchange.sendResponseHeaders( status, body.length == 0 ? - 1 : body.length );
        try (OutputStream out = exchange.getResponseBody()) {
            out.write( body );
        }
    }

    private static void sendText( HttpExchange exchange, int status, String text ) throws IOException {
        send( exchange, status, "text/plain; charset=utf-8", text.getBytes( StandardCharsets.UTF_8 ) );
    }

    private static String queryParam( URI uri, String name ) {
        String query = uri.getRawQuery();
        if (query == null) {
            return null;
        }
        for (String pair : query.split( "&" )) {
            int eq = pair.indexOf( '=' );
            if (eq > 0 && pair.substring( 0, eq ).equals( name )) {
                return pair.substring( eq + 1 );
            }
        }
        return null;
    }

    private class StaticHandler implements HttpHandler {
        @Override
        public void handle( HttpExchange exchange ) throws IOException {
            try {
                if (! isAllowedHost( exchange )) {
                    sendText( exchange, 403, "Forbidden" );
                    return;
                }
                String file = STATIC_FILES.get( exchange.getRequestURI().getPath() );
                if (file == null || ! exchange.getRequestMethod().equals( "GET" )) {
                    sendText( exchange, 404, "Not found" );
                    return;
                }
                send( exchange, 200, contentTypeFor( file ), readResource( "/web/" + file ) );
            } finally {
                exchange.close();
            }
        }

        private String contentTypeFor( String file ) {
            if (file.endsWith( ".css" )) {
                return "text/css; charset=utf-8";
            }
            if (file.endsWith( ".js" )) {
                return "application/javascript; charset=utf-8";
            }
            return "text/html; charset=utf-8";
        }

        private byte[] readResource( String path ) throws IOException {
            try (InputStream in = MapWebServer.class.getResourceAsStream( path )) {
                if (in == null) {
                    throw new IOException( "Missing resource " + path );
                }
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int read;
                while (( read = in.read( buffer ) ) != - 1) {
                    bytes.write( buffer, 0, read );
                }
                return bytes.toByteArray();
            }
        }
    }

    private class EventsHandler implements HttpHandler {
        @Override
        public void handle( HttpExchange exchange ) throws IOException {
            ViewChannel channel = channels.get( queryParam( exchange.getRequestURI(), "view" ) );
            if (! isAllowedHost( exchange ) || channel == null) {
                sendText( exchange, isAllowedHost( exchange ) ? 404 : 403, "Not available" );
                exchange.close();
                return;
            }
            exchange.getResponseHeaders().set( "Content-Type", "text/event-stream; charset=utf-8" );
            exchange.getResponseHeaders().set( "Cache-Control", "no-store" );
            exchange.sendResponseHeaders( 200, 0 );
            // the exchange is kept open, the channel writes all further updates into it
            channel.addClient( exchange );
        }
    }

    private class ApiHandler implements HttpHandler {
        @Override
        public void handle( HttpExchange exchange ) throws IOException {
            try {
                if (! isAllowedHost( exchange ) || exchange.getRequestHeaders().getFirst( ACTION_HEADER ) == null) {
                    sendText( exchange, 403, "Forbidden" );
                    return;
                }
                if (! exchange.getRequestMethod().equals( "POST" )) {
                    sendText( exchange, 405, "POST only" );
                    return;
                }
                JsonObject request;
                try {
                    request = new JsonParser().parse( new InputStreamReader( exchange.getRequestBody(), StandardCharsets.UTF_8 ) ).getAsJsonObject();
                } catch (JsonParseException | IllegalStateException e) {
                    sendText( exchange, 400, "Bad json" );
                    return;
                }
                MapperEngine engine = engines.get( getString( request, "view" ) );
                if (engine == null) {
                    sendText( exchange, 404, "Unknown view" );
                    return;
                }
                Map<String, Object> response = new HashMap<String, Object>();
                try {
                    handleAction( engine, getString( request, "action" ), request, response );
                } catch (IllegalArgumentException | NullPointerException | ClassCastException | IllegalStateException e) {
                    sendText( exchange, 400, "Bad request: " + e );
                    return;
                }
                send( exchange, 200, "application/json; charset=utf-8", gson.toJson( response ).getBytes( StandardCharsets.UTF_8 ) );
            } finally {
                exchange.close();
            }
        }
    }

    private void handleAction( MapperEngine engine, String action, JsonObject request, Map<String, Object> response ) {
        switch (action) {
            case "pick":
                engine.pickRoom( getString( request, "room" ), request.has( "toggle" ) && request.get( "toggle" ).getAsBoolean() );
                break;
            case "pickMany":
                engine.pickRooms( getStrings( request, "rooms" ), request.get( "clear" ).getAsBoolean() );
                break;
            case "move": {
                Map<String, Point2D> newLocations = new HashMap<String, Point2D>();
                JsonObject positions = request.getAsJsonObject( "positions" );
                for (Map.Entry<String, com.google.gson.JsonElement> entry : positions.entrySet()) {
                    JsonObject position = entry.getValue().getAsJsonObject();
                    newLocations.put( entry.getKey(), new Point2D.Double( position.get( "x" ).getAsDouble(), position.get( "y" ).getAsDouble() ) );
                }
                engine.moveRooms( newLocations );
                break;
            }
            case "color": {
                int index = request.get( "index" ).getAsInt();
                Color[] colors = RoomColors.getColors();
                if (index < 0 || index >= colors.length) {
                    throw new IllegalArgumentException( "no such color" );
                }
                engine.changeRoomColor( colors[index] );
                break;
            }
            case "deleteRooms":
                engine.deletePickedRooms();
                break;
            case "deleteExit":
                engine.deleteExit( request.get( "exit" ).getAsInt() );
                break;
            case "renameExit":
                engine.renameExit( request.get( "exit" ).getAsInt(), getString( request, "name" ) );
                break;
            case "notes":
                engine.setNotes( getString( request, "room" ), getString( request, "notes" ) );
                break;
            case "dirs":
                response.put( "dirs", engine.getDirsTo( getString( request, "room" ), false ) );
                response.put( "shortDirs", engine.getDirsTo( getString( request, "room" ), true ) );
                break;
            case "sendToMud":
                engine.sendToMud( getString( request, "text" ) );
                break;
            case "sendToParty":
                engine.sendToParty( getString( request, "text" ) );
                break;
            case "save":
                engine.save();
                break;
            case "clear":
                engine.clearCurrentArea();
                break;
            case "snap":
                engine.setRoomSnapping( request.get( "enabled" ).getAsBoolean() );
                break;
            case "maze":
                engine.setMazeMode( request.get( "enabled" ).getAsBoolean() );
                break;
            case "revs":
                engine.setReversableDirsMode( request.get( "enabled" ).getAsBoolean() );
                break;
            case "search":
                searchEngine( engine ).searchForRoomsWith( getString( request, "text" ).trim() );
                break;
            case "showRoom":
                searchEngine( engine ).showRoom( getString( request, "area" ), getString( request, "room" ) );
                break;
            default:
                throw new IllegalArgumentException( "unknown action " + action );
        }
        response.put( "ok", true );
        response.put( "version", engine.getVersion() );
    }

    private static SearchEngine searchEngine( MapperEngine engine ) {
        if (! ( engine instanceof SearchEngine )) {
            throw new IllegalArgumentException( "only available in search view" );
        }
        return (SearchEngine) engine;
    }

    private static String getString( JsonObject request, String name ) {
        if (! request.has( name ) || request.get( name ).isJsonNull()) {
            return null;
        }
        return request.get( name ).getAsString();
    }

    private static List<String> getStrings( JsonObject request, String name ) {
        List<String> values = new ArrayList<String>();
        for (com.google.gson.JsonElement element : request.getAsJsonArray( name )) {
            values.add( element.getAsString() );
        }
        return values;
    }

    /**
     * Pushes the state of one map view to all browsers watching it. Changes are coalesced, so a burst of
     * changes is sent as one update.
     */
    private class ViewChannel implements Runnable {

        private final MapperEngine engine;
        private final List<HttpExchange> clients = new CopyOnWriteArrayList<HttpExchange>();
        private boolean dirty = false;

        ViewChannel( MapperEngine engine ) {
            this.engine = engine;
        }

        // called by the engine whenever the map changes
        @Override
        public void run() {
            synchronized (this) {
                dirty = true;
                notifyAll();
            }
        }

        void addClient( HttpExchange exchange ) {
            byte[] message = stateMessage();
            try {
                OutputStream out = exchange.getResponseBody();
                out.write( message );
                out.flush();
                clients.add( exchange );
            } catch (IOException e) {
                exchange.close();
            }
        }

        void close() {
            synchronized (this) {
                notifyAll();
            }
            for (HttpExchange client : clients) {
                client.close();
            }
            clients.clear();
        }

        private byte[] stateMessage() {
            String json = gson.toJson( engine.getState() );
            return ( "event: state\ndata: " + json + "\n\n" ).getBytes( StandardCharsets.UTF_8 );
        }

        void broadcastLoop() {
            while (running) {
                boolean sendState;
                synchronized (this) {
                    if (! dirty) {
                        try {
                            wait( KEEPALIVE_INTERVAL );
                        } catch (InterruptedException e) {
                            return;
                        }
                    }
                    sendState = dirty;
                }
                if (! running) {
                    return;
                }
                if (sendState) {
                    try {
                        Thread.sleep( UPDATE_COALESCE_MILLIS );
                    } catch (InterruptedException e) {
                        return;
                    }
                    synchronized (this) {
                        dirty = false;
                    }
                }
                if (clients.isEmpty()) {
                    continue;
                }
                byte[] message = sendState ? stateMessage() : ": keepalive\n\n".getBytes( StandardCharsets.UTF_8 );
                for (HttpExchange client : clients) {
                    try {
                        OutputStream out = client.getResponseBody();
                        out.write( message );
                        out.flush();
                    } catch (IOException e) {
                        clients.remove( client );
                        client.close();
                    }
                }
            }
        }
    }
}
