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
                               "יישום זה נועד לסייע בניהול הגדרות המכשיר והאפליקציות וניהול תעבורת אינטרנט במכשירים מנוהלים. " +
                               "דורש הרשאת מנהל מכשיר.\n"+
                               "האפליקציות הפתוחות במסלול הכי פתוח (השם של המסלול הוא - \"מולטימדיה\")"+
                               "מייל, דרייב, צ'אט, מפות בלי תמונות, זינג, מיוזיק ווליום, וייז, פנגו, מוביט, ביט, קול הלשון, ועוד");
    }
}
