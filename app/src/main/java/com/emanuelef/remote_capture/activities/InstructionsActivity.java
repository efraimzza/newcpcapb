package com.emanuelef.remote_capture.activities;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;
import com.emanuelef.remote_capture.R;

public class InstructionsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instructions);

        TextView tvInstructions = findViewById(R.id.tv_instructions_content);
        tvInstructions.setText("כאן יופיעו הוראות הפעלה מפורטות לשימוש באפליקציית ה-MDM...\n\n" +
                               "1. הפעלת מנהל מכשיר ,adb connect pair ,ברקוד רגיל ,ברקוד ממכשיר למכשיר ,שורש(חשוב מאוד להסיר את הרוט למניעת מעקפים.),...\n" +
                               "2. הגדרת הגבלות, התקנה, הסרת התקנה, איפוס, ...\n" +
                               "3. ניהול הסתרת והתקנת אפליקציות...\n" +
                               "4. ניהול תעבורת אינטרנט(vpn), יש תעבורת אינטרנט רק לפי רשימה לבנה של אתרים...");
    }
}
