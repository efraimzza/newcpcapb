package com.emanuelef.remote_capture.activities;

public class FileItem {
    public final String name;
    public final String path; // The full path/URI string for the item
    public final String mimeType;
    public final long size;
    public final long lastModified;

    public FileItem(String name, String path, String mimeType, long size, long lastModified) {
        this.name = name;
        this.path = path;
        this.mimeType = mimeType;
        this.size = size;
        this.lastModified = lastModified;
    }
}
