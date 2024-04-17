package dev.plytki.baseapi.inventories.util;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public class DataCompressor {

    // Compress data to a file with a specified compression level
    public void compressData(byte[] data, File file, int level) throws IOException {
        try (OutputStream fileStream = new FileOutputStream(file);
             DeflaterOutputStream out = new DeflaterOutputStream(fileStream, new Deflater(level))) {
            out.write(data);
        }
    }

    // Compress data to a byte array with a specified compression level
    public byte[] compressDataToByteArray(byte[] data, int level) throws IOException {
        try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
             DeflaterOutputStream out = new DeflaterOutputStream(byteStream, new Deflater(level))) {
            out.write(data);
            out.finish();
            return byteStream.toByteArray();
        }
    }

    // Decompress data from a file
    public byte[] decompressData(File file) throws IOException {
        try (InputStream fileStream = new FileInputStream(file);
             InflaterInputStream in = new InflaterInputStream(fileStream)) {
            return readFully(in);
        }
    }

    // Decompress data from a byte array
    public byte[] decompressData(byte[] data) throws IOException {
        try (InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(data))) {
            return readFully(in);
        }
    }

    // Utility method to read stream fully into byte array
    private byte[] readFully(InputStream inputStream) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            bout.write(buffer, 0, bytesRead);
        }
        return bout.toByteArray();
    }

}
