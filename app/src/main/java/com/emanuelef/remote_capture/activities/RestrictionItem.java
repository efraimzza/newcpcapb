package com.emanuelef.remote_capture.activities;

import android.graphics.drawable.Drawable; // הוסף את הייבוא הזה
import com.emanuelef.remote_capture.R;

public class RestrictionItem {
    private String type;
    private String name;
    private String description; // חדש
    private String key;
    private boolean isEnabled;
    private int iconResId; // חדש: Resource ID של הדרבאל

    // קונסטרקטור חדש
    public RestrictionItem(String type,String name, String description, String key, boolean isEnabled, int iconResId) {
        this.type = type;
        this.name = name;
        this.description = description;
        this.key = key;
        this.isEnabled = isEnabled;
        this.iconResId = iconResId;
    }

    // קונסטרקטור קיים (למקרה של תאימות לאחור, אך מומלץ להשתמש בחדש)
    public RestrictionItem(String type,String name, String description, String key, boolean isEnabled) {
        this(type,name, description, key, isEnabled, R.drawable.ic_default_restriction); // ברירת מחדל לאייקון
    }
    public String getType() {
        return type;
    }
    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getKey() {
        return key;
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public void setEnabled(boolean enabled) {
        isEnabled = enabled;
    }

    public int getIconResId() {
        return iconResId;
    }
}
