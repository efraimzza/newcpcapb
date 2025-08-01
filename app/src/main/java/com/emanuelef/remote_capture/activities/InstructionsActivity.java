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
                               "1. הפעלת מנהל מכשיר...\n" +
                               "2. הגדרת הגבלות...\n" +
                               "3. ניהול אפליקציות...");
    }
}
