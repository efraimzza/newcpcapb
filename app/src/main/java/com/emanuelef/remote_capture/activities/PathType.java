package com.emanuelef.remote_capture.activities;

public enum PathType {
    MULTIMEDIA("מולטימדיה"),
    MULTIMEDIA_ACCESSIBILITY("מולטימדיה ונגישות"),
    EVERYTHING("הכל"),
    MAPS("מפות"),
    WAZE("וויז"),
    MAIL("מייל"),
    NAVIGATIONMUSICAPPS("ניווט ומוזיקה");

    private final String description;

    PathType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
