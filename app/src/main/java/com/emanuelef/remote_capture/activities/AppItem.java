package com.emanuelef.remote_capture.activities;

import android.graphics.drawable.Drawable;

public class AppItem {
    private String name;
    private String packageName;
    private Drawable icon;
    private boolean isHidden;
    private boolean isSystemApp;
    private boolean hasLauncherIcon;
    private long lastUpdateTime;

    public AppItem(String name, String packageName, Drawable icon, boolean isHidden, boolean isSystemApp, boolean hasLauncherIcon, long lastUpdateTime) {
        this.name = name;
        this.packageName = packageName;
        this.icon = icon;
        this.isHidden = isHidden;
        this.isSystemApp = isSystemApp;
        this.hasLauncherIcon = hasLauncherIcon;
        this.lastUpdateTime = lastUpdateTime;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    public Drawable getIcon() {
        return icon;
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean hidden) {
        isHidden = hidden;
    }

    public boolean isSystemApp() {
        return isSystemApp;
    }

    public boolean hasLauncherIcon() {
        return hasLauncherIcon;
    }

    public long getLastUpdateTime() {
        return lastUpdateTime;
    }
}
