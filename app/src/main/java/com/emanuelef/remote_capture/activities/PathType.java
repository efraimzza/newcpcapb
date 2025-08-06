package com.emanuelef.remote_capture.activities;

public enum PathType {
    MULTIMEDIA("מולטימדיה"),
    MULTIMEDIA_ACCESSIBILITY("מולטימדיה ונגישות"),
    EVERYTHING("הכל");

    private final String description;

    PathType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
