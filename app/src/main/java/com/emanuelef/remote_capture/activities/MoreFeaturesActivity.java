package com.emanuelef.remote_capture.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import com.emanuelef.remote_capture.R;

public class MoreFeaturesActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_more_features);

        // כפתור לדוגמה: הוספת קיצור דרך להגדרות מולטימדיה
        Button btnAddMultimediaSettingsShortcut = findViewById(R.id.btn_add_multimedia_settings_shortcut);
        if (btnAddMultimediaSettingsShortcut != null) {
            btnAddMultimediaSettingsShortcut.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        /*try {
                            Intent intent = new Intent(Settings.ACTION_SOUND_SETTINGS);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            Toast.makeText(MoreFeaturesActivity.this, "נפתח מסך הגדרות קול.", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(MoreFeaturesActivity.this, "לא ניתן לפתוח את הגדרות המולטימדיה.", Toast.LENGTH_SHORT).show();
                        }*/
                        try{
            ShortcutManager sm=(ShortcutManager) MoreFeaturesActivity.this.getSystemService(SHORTCUT_SERVICE);
            Icon ic= Icon.createWithResource(MoreFeaturesActivity.this,R.drawable.ic_settingsshort);
            ShortcutInfo si=new ShortcutInfo.Builder(MoreFeaturesActivity.this,"settings")
            .setShortLabel("הגדרות")
            .setIntent(MoreFeaturesActivity.this.getPackageManager().getLaunchIntentForPackage("com.android.settings"))
            .setIcon(ic)
            .build();
            sm.requestPinShortcut(si,null);
           } catch (Exception e){}
                    }
                });
                }
        Button btnprappmanag = findViewById(R.id.btn_prappmanag);
        if (btnprappmanag != null) {
            btnprappmanag.setOnClickListener(new View.OnClickListener() {
                    @Deprecated
                    @Override
                    public void onClick(View v) {
                        try{
                           Intent intent = new Intent(MoreFeaturesActivity.this, PrAppManagementActivity.class);
                           startActivity(intent);
                        } catch (Exception e){}
                    }
                });
        }
        Button btnenacc = findViewById(R.id.btn_enacc);
        if (btnenacc != null) {
            btnenacc.setOnClickListener(new View.OnClickListener() {
                    @Deprecated
                    @Override
                    public void onClick(View v) {
                        PasswordManager.requestPasswordAndSave(new Runnable() {
                                    @Override
                                    public void run() {
                                         try{
                                            Intent intent = new Intent(MoreFeaturesActivity.this, enactivityadbpair.class);
                                            startActivity(intent);
                                         } catch (Exception e){}
                                    }
                                },MoreFeaturesActivity.this);

                    }
                });
        }
    }
}
