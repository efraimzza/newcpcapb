package com.emanuelef.remote_capture.activities;

public class RestrictionItem {
    private String type;
    private String name;
    private String restrictionKey; // המפתח של ההגבלה ב-UserManager (לדוגמה: UserManager.DISALLOW_TETHERING)
    private boolean isChecked; // האם ה-CheckBox מסומן

    public RestrictionItem(String type,String name, String restrictionKey, boolean isChecked) {
        this.type = type;
        this.name = name;
        this.restrictionKey = restrictionKey;
        this.isChecked = isChecked;
    }

    // Getters and Setters
    public String getType() {
        return type;
    }
    public String getName() {
        return name;
    }

    public String getRestrictionKey() {
        return restrictionKey;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }
}
