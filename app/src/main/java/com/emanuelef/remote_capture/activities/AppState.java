package com.emanuelef.remote_capture.activities;

public class AppState {
    private static AppState instance;
    private PathType currentPath;

    private AppState() {
        //default
        this.currentPath = PathType.MULTIMEDIA;
    }

    public static AppState getInstance() {
        if (instance == null) {
            instance = new AppState();
        }
        return instance;
    }

    public PathType getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(PathType path) {
        this.currentPath = path;
    }
}
