package com.emanuelef.remote_capture.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.emanuelef.remote_capture.R;

public class AboutActivitya extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tvAboutContent = findViewById(R.id.tv_about_content);
        tvAboutContent.setText("אפליקציית ניהול מכשירים (MDM)\n\n" +
                               "גרסה: 0.1\n" +
                               "יישום זה נועד לסייע בניהול הגדרות המכשיר והאפליקציות במכשירים מנוהלים. " +
                               "דורש הרשאת מנהל מכשיר.");
    }
}
