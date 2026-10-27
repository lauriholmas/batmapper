package com.glaurung.batMap.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;

/**
 * Shared helpers for reading and writing the json save files, plus reading the old java-serialized files
 * so existing data can still be loaded.
 */
class JsonFiles {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Writes to a temp file first and then moves it over the target, so a failed write never leaves
     * the target file half written.
     */
    static void write( File file, Object data ) throws IOException {
        File tempFile = new File( file.getPath() + ".tmp" );
        try {
            try (Writer writer = new OutputStreamWriter( new FileOutputStream( tempFile ), StandardCharsets.UTF_8 )) {
                GSON.toJson( data, writer );
            } catch (JsonIOException e) {
                throw new IOException( "Unable to write " + file, e );
            }
            try {
                Files.move( tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE );
            } catch (AtomicMoveNotSupportedException e) {
                Files.move( tempFile.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING );
            }
        } finally {
            Files.deleteIfExists( tempFile.toPath() );
        }
    }

    static <T> T read( File file, Class<T> type ) throws IOException {
        try (Reader reader = new InputStreamReader( new FileInputStream( file ), StandardCharsets.UTF_8 )) {
            T data = GSON.fromJson( reader, type );
            if (data == null) {
                throw new IOException( file + " is empty" );
            }
            return data;
        } catch (JsonParseException e) {
            throw new IOException( "Unable to parse " + file, e );
        }
    }

    static Object readLegacy( File file ) throws IOException, ClassNotFoundException {
        try (FileInputStream fileInputStream = new FileInputStream( file );
             ObjectInputStream objectInputStream = new ObjectInputStream( fileInputStream )) {
            return objectInputStream.readObject();
        }
    }
}
