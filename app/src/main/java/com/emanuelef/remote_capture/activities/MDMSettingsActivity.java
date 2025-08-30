package com.emanuelef.remote_capture.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;
import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import android.content.pm.PackageInstaller;
import android.os.Handler;
import java.util.List;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.app.PendingIntent;
import android.content.pm.PackageInstaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.view.View.OnLongClickListener;
import android.widget.Switch;
import android.os.Build;
import android.app.admin.FactoryResetProtectionPolicy;
import java.util.List;
import java.util.ArrayList;
import android.text.method.PasswordTransformationMethod;
import android.widget.TextView;
import android.app.DownloadManager;
import android.net.Uri;
import java.io.File;
import android.os.Environment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Looper;
import android.os.UserManager;

import com.emanuelef.remote_capture.model.Prefs;
import com.emanuelef.remote_capture.fragments.StatusReceiver;
import com.emanuelef.remote_capture.Utils;
import com.emanuelef.remote_capture.CaptureService;
import com.emanuelef.remote_capture.R;

@Deprecated
public class MDMSettingsActivity extends Activity {

    public static DevicePolicyManager mDpm;
    public static ComponentName mAdminComponentName;
    SharedPreferences mPrefs;
    SharedPreferences sp;
    SharedPreferences.Editor spe;
    public static final String modesp="mode";
    public static final String locksp="lock";
    private DownloadManager downloadManager;
    private long downloadId;
    private ProgressDialog progressDialog;
    private Handler handler;
    private Runnable updateProgressRunnable;
    private boolean isDownloadCanceled = false;
    
    @Deprecated
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
        setContentView(R.layout.activity_mdm_settings);
        try{
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        handler = new Handler(Looper.getMainLooper());
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(monDownloadComplete, filter, Context.RECEIVER_EXPORTED);
        } else {
            registerReceiver(monDownloadComplete, filter);
        }
        }  catch(Exception e){
            MDMSettingsActivity.this.requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 55);
            LogUtil.logToFile(""+e);
            Toast.makeText(this, e+"",1).show();
        }
        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = new ComponentName(this,admin.class);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(MDMSettingsActivity.this);
        
        sp=this.getSharedPreferences(this.getPackageName(),this.MODE_PRIVATE);
        spe=sp.edit();


        TextView tvcurroute=findViewById(R.id.tv_cur_route);
        tvcurroute.setText("המסלול הפעיל - "+AppState.getInstance().getCurrentPath().getDescription());
        
        // אתחול כפתורים
        setupButton(R.id.btn_manage_restrictions, "ניהול הגבלות מכשיר", RestrictionManagementActivity.class);
        setupButton(R.id.btn_manage_apps, "ניהול אפליקציות", AppManagementActivity.class);
        setupButton(R.id.btn_manage_vpn, "ניהול vpn", MainActivity.class);
        setupButton(R.id.btn_change_password, "שנה סיסמה", null);
        setupButton(R.id.btn_remove_frp, "הסר frp", null); 
        setupButton(R.id.btn_activate_frp, "הפעל frp", null); 
        setupButton(R.id.btn_update_mdm_app, "עדכון אפליקציית MDM", null);
        setupButton(R.id.btn_lock_mdm, "נעילת הגדרות והסרה", null);
        setupButton(R.id.btn_select_route, "בחירת מסלול", null); // תצטרך אקטיביטי לזה
        setupButton(R.id.btn_def_rest_multi, "השבתות מומלצות למולטימדיה", null);
        setupButton(R.id.btn_def_rest_cube, "השבתות מומלצות לקוביית אנדרואיד", null);
        setupButton(R.id.btn_update_whitelist, "עדכון לרשימת דומיינים לבנה", null); // תצטרך לוגיקה לזה
        setupButton(R.id.btn_more_features, "פיצ'רים נוספים", MoreFeaturesActivity.class); // אקטיביטי חדש
        setupabodeb();
        }  catch(Exception e){
            Toast.makeText(this, e+"",1).show();
        }
        }

    private void setupButton(int buttonId, String text, final Class<?> targetActivity) {
        Button button = findViewById(buttonId);
        if (button != null) {
            button.setText(text);
            button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        // with password
                        if (v.getId() == R.id.btn_manage_restrictions ||
                            v.getId() == R.id.btn_change_password ||
                            v.getId() == R.id.btn_remove_frp ||
                            v.getId() == R.id.btn_activate_frp ||
                            v.getId() == R.id.btn_lock_mdm ||
                            v.getId() ==  R.id.btn_select_route ||
                            v.getId() == R.id.btn_def_rest_multi ||
                            v.getId() == R.id.btn_def_rest_cube) { 
                            PasswordManager.requestPasswordAndSave(new Runnable() {
                                    @Override
                                    public void run() {
                                        handleButtonClick(v.getId(), targetActivity);
                                    }
                                },MDMSettingsActivity.this);
                        } else {
                            // without password
                            handleButtonClick(v.getId(), targetActivity);
                        }
                    }
                });
        }
    }

    private void handleButtonClick(int buttonId, Class<?> targetActivity) {
        if (targetActivity != null) {
            Intent intent = new Intent(MDMSettingsActivity.this, targetActivity);
            startActivity(intent);
        } else {
            // without target activity
            if (buttonId == R.id.btn_change_password) {
                PasswordManager. showSetPasswordDialog(MDMSettingsActivity.this);
            }else if (buttonId == R.id.btn_remove_frp) {
                removefrp(MDMSettingsActivity.this);
            } else if (buttonId == R.id.btn_activate_frp) {
                activatefrp();
            } else if (buttonId == R.id.btn_update_mdm_app) {
                updateMdm();
                //startDownload();
            } else if (buttonId == R.id.btn_lock_mdm) {
                showLockMDMConfirmationDialog(MDMSettingsActivity.this);
            } else if (buttonId == R.id.btn_select_route) {
                final PathType[] paths = PathType.values();
                String[] pathNames = new String[paths.length];

                for (int i = 0; i < paths.length; i++) {
                    pathNames[i] = paths[i].getDescription();
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("בחר מסלול");
                builder.setItems(pathNames, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            PathType selectedPath = paths[which];
                            AppState.getInstance().setCurrentPath(selectedPath);
                            spe.putString(modesp,AppState.getInstance().getCurrentPath().name());
                            spe.commit();
                            TextView tvcurroute=findViewById(R.id.tv_cur_route);
                            tvcurroute.setText("המסלול הפעיל - "+AppState.getInstance().getCurrentPath().getDescription());
                            
                            Toast.makeText(MDMSettingsActivity.this, "המסלול שנבחר: " + selectedPath.getDescription(), Toast.LENGTH_SHORT).show();
                        }
                    });
                builder.create().show();  
            } else if (buttonId == R.id.btn_def_rest_multi) {
                boolean mdmstate=mDpm.isDeviceOwnerApp(MDMSettingsActivity.this.getPackageName());
                if(mdmstate){
                try{
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_CONFIG_TETHERING);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_DEBUGGING_FEATURES);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_FACTORY_RESET);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_ADD_MANAGED_PROFILE);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_USER_SWITCH);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_ADD_USER);
                //mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_UNINSTALL_APPS);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_REMOVE_USER);
                //mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_APPS_CONTROL);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_INSTALL_APPS);//all!
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_CONFIG_WIFI);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_SAFE_BOOT);
                
                
                //mDpm.setApplicationHidden(mAdminComponentName, "com.dofun.carsetting", true);//carsettings
                mDpm.setApplicationHidden(mAdminComponentName, "com.android.vending", true);//Google play
                mDpm.setApplicationHidden(mAdminComponentName, "com.android.chrome", true);//chrome
                mDpm.setApplicationHidden(mAdminComponentName, "com.dofun.market", true);//market
                mDpm.setApplicationHidden(mAdminComponentName, "io.github.huskydg.magisk", true);//kitsun
                mDpm.setApplicationHidden(mAdminComponentName, "com.google.android.googlequicksearchbox", true);//Google
                
                //mDpm.setApplicationHidden(mAdminComponentName, "com.google.android.apps.maps", true);//maps
                
                
                Toast.makeText(MDMSettingsActivity.this, "הופעלו השבתות מומלצות למולטימדיה!", Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    Toast.makeText(MDMSettingsActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                }
                    
                }else{
                    Toast.makeText(MDMSettingsActivity.this, "אין עדיין הרשאות ניהול מכשיר", Toast.LENGTH_SHORT).show();
                }
            }else if (buttonId == R.id.btn_def_rest_cube) {
                boolean mdmstate=mDpm.isDeviceOwnerApp(MDMSettingsActivity.this.getPackageName());
                if(mdmstate){
                try{
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_CONFIG_TETHERING);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_DEBUGGING_FEATURES);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_FACTORY_RESET);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_ADD_MANAGED_PROFILE);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_USER_SWITCH);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_ADD_USER);
                //mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_UNINSTALL_APPS);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_REMOVE_USER);
                //mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_APPS_CONTROL);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_INSTALL_APPS);//all!
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_CONFIG_WIFI);
                mDpm.addUserRestriction(mAdminComponentName, UserManager.DISALLOW_SAFE_BOOT);
                
                
                mDpm.setApplicationHidden(mAdminComponentName, "com.android.vending", true);//Google play
                mDpm.setApplicationHidden(mAdminComponentName, "com.android.chrome", true);//chrome
                mDpm.setApplicationHidden(mAdminComponentName, "com.google.android.googlequicksearchbox", true);//Google
                mDpm.setApplicationHidden(mAdminComponentName, "com.google.android.apps.maps", true);//maps
                mDpm.setApplicationHidden(mAdminComponentName, "com.suding.apkinstaller", true);//market
                mDpm.setApplicationHidden(mAdminComponentName, "com.google.android.apps.youtube.music", true);//YouTube music
                
                
                
                Toast.makeText(MDMSettingsActivity.this, "הופעלו השבתות מומלצות לקוביית אנדרואיד!", Toast.LENGTH_SHORT).show();
                } catch (Exception e){
                    Toast.makeText(MDMSettingsActivity.this, ""+e, Toast.LENGTH_SHORT).show();
                }
                    
                }else{
                    Toast.makeText(MDMSettingsActivity.this, "אין עדיין הרשאות ניהול מכשיר", Toast.LENGTH_SHORT).show();
                }
            } else if (buttonId == R.id.btn_update_whitelist) {
               if(CaptureService.isServiceActive()){
                   CaptureService.requestBlacklistsUpdate();
                   Toast.makeText(MDMSettingsActivity.this, "updating...",1).show();
               }
            }
        }
    }
    public static void removefrp(final Activity activity){
        if (Build.VERSION.SDK_INT > 29) {
           try {
              List<String> arrayList = new ArrayList<>();
              FactoryResetProtectionPolicy frp=new FactoryResetProtectionPolicy.Builder()
                .setFactoryResetProtectionAccounts(arrayList)
                .setFactoryResetProtectionEnabled(false)
                 .build();
                 mDpm.setFactoryResetProtectionPolicy(mAdminComponentName, frp);
           } catch (Exception e) {
                Toast.makeText(activity, "e-frp" , Toast.LENGTH_SHORT).show();
           }
        }
           try {
              Bundle bundle = new Bundle();
              bundle = null;
              String str = "com.google.android.gms";
              mDpm=(DevicePolicyManager)activity.getSystemService("device_policy");
              mDpm.setApplicationRestrictions(mAdminComponentName, str, bundle);
              Intent intent = new Intent("com.google.android.gms.auth.FRP_CONFIG_CHANGED");
              intent.setPackage(str);
              intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
              activity.sendBroadcast(intent);
              Toast.makeText(activity, "frp removed", Toast.LENGTH_SHORT).show();
           } catch (Exception e) {
             Toast.makeText(activity, "" + e, Toast.LENGTH_SHORT).show();
           }
                    
    }
    private void activatefrp(){
        try {
            
            List<String> arrayList = new ArrayList<>();
            arrayList.add("116673918161076927085");
            arrayList.add("107578790485390569043");
            arrayList.add("105993588108835326457");
            if (Build.VERSION.SDK_INT > 29) {
                try {
                    FactoryResetProtectionPolicy frp=new FactoryResetProtectionPolicy.Builder()
                        .setFactoryResetProtectionAccounts(arrayList)
                        .setFactoryResetProtectionEnabled(true)
                        .build();
                    mDpm.setFactoryResetProtectionPolicy(mAdminComponentName, frp);
                } catch (Exception e) {
                    Toast.makeText(MDMSettingsActivity.this, "e-frp"+e , Toast.LENGTH_SHORT).show();
                }
            }
            Bundle bundle = new Bundle();

            bundle.putStringArray("factoryResetProtectionAdmin", arrayList.toArray(new String[0]));

            //bundle=null;
            String str = "com.google.android.gms";
            mDpm.setApplicationRestrictions(mAdminComponentName, str, bundle);
            Intent intent = new Intent("com.google.android.gms.auth.FRP_CONFIG_CHANGED");
            intent.setPackage(str);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            MDMSettingsActivity.this.sendBroadcast(intent);
            Toast.makeText(MDMSettingsActivity.this, "frp..", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
		    Toast.makeText(MDMSettingsActivity.this, "e-frp2"+e , Toast.LENGTH_SHORT).show();
		}
    }
    private void setupabodeb(){
        Button but=findViewById(R.id.btn_more_features);
        but.setOnLongClickListener(new OnLongClickListener(){

                @Override
                public boolean onLongClick(View p1) {
                    PasswordManager.requestPasswordAndSave(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MDMSettingsActivity.this);
                                builder.setTitle("debuging");

                                final Switch swi =new Switch(MDMSettingsActivity.this);
                                swi.setText("debug");
                                swi.setChecked(Prefs.isdebug(mPrefs));
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                                    LinearLayout.LayoutParams.MATCH_PARENT,
                                    LinearLayout.LayoutParams.MATCH_PARENT);
                                swi.setLayoutParams(lp);
                                builder.setView(swi);

                                builder.setPositiveButton("אישור", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Prefs.setdebugp(mPrefs,swi.isChecked());
                                            CaptureService.setdebug(Prefs.isdebug(mPrefs));
                                            dialog.cancel();
                                        }
                                    });
                                builder.show();
                            }
                        },MDMSettingsActivity.this);
                    return true;
                }
            });
    }

    boolean succ=false;
    boolean mend=false;
    @Deprecated
    private void updateMdm(){
    //first abandon all old sessions
    try {
       List<PackageInstaller.SessionInfo> lses= MDMSettingsActivity.this.getPackageManager().getPackageInstaller().getAllSessions();
       if (lses != null) {
         for (PackageInstaller.SessionInfo pses:lses) {
             if (pses != null) {
                try {
                    if (pses.getInstallerPackageName().equals(MDMSettingsActivity.this.getPackageName())) {
                       MDMSettingsActivity.this.getPackageManager().getPackageInstaller().abandonSession(pses.getSessionId());
                    }
                } catch (Exception e) {  
                    Toast.makeText(MDMSettingsActivity.this, "" + e, 0).show();
                }
             }
         }
      }
      } catch (Exception e) {
         Toast.makeText(MDMSettingsActivity.this, "" + e, 0).show();
      }
      try{
         startDownload();
      } catch (Exception e) {
         Toast.makeText(MDMSettingsActivity.this, "" + e, 0).show();
      }
        /*
        new Thread(){public void run(){
        succ= Utils.downloadFile("https://raw.githubusercontent.com/efraimzz/whitelist/refs/heads/main/whitelistbeta.apk", MDMSettingsActivity.this.getFilesDir()+"/updatebeta.apk");
        mend=true;
        }}.start();
       
        new Handler().post(new Runnable(){
                @Deprecated
                @Override
                public void run() {
                    if(!mend){
                    new Handler().postDelayed(this,1000);
                    }else{
                    if(succ){
                    appone(MDMSettingsActivity.this.getFilesDir()+"/updatebeta.apk");
                    }
                        Toast.makeText(MDMSettingsActivity.this, ""+succ, 1).show();
                        mend=false;
                        succ=false;
                    }
                }
            });*/
        
    }
    

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }
    PackageInstaller.Session openses;
    void appone(String mappath) {
        String editable;
        try {
            PackageInstaller packageInstaller = MDMSettingsActivity.this.getPackageManager().getPackageInstaller();

            PackageInstaller. SessionParams sessionParams = new PackageInstaller. SessionParams(1);
            openses = packageInstaller.openSession(packageInstaller.createSession(sessionParams));
           // editable = edtx1.getText().toString();
            editable = mappath;
            if (editable.equals("")) {
                Toast.makeText(MDMSettingsActivity.this, "write the path!", 1).show();
                openses.abandon();
                return;
            }

            File file = new File(editable);
            if (file.exists() && file.canRead()) {

                InputStream FileInputStream = new FileInputStream(file);

                OutputStream openWrite = openses.openWrite("package", (long) 0, file.length());
                byte[] bArr = new byte[1024];
                while (true) {
                    int read = FileInputStream.read(bArr);
                    if (read >= 0) {
                        openWrite.write(bArr, 0, read);
                    } else {
                        openses.fsync(openWrite);
                        FileInputStream.close();
                        openWrite.close();

                        try {
                            Intent intent  = new Intent(MDMSettingsActivity.this, StatusReceiver.class);
                            openses.commit(PendingIntent.getBroadcast(MDMSettingsActivity.this, 0, intent, PendingIntent.FLAG_MUTABLE).getIntentSender());
                            return;
                        } catch (Throwable e) {}
                    }
                }
            }
            Toast.makeText(MDMSettingsActivity.this, "not exsist or not readable!", 1).show();
            openses.abandon();
        } catch (Exception e2) {
            try {
                openses.abandon();
            } catch (Exception e22) {}
            editable = "";
            StackTraceElement[] stackTrace = e2.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                editable = editable+stackTraceElement;
            }
            Toast.makeText(MDMSettingsActivity.this, ""+e2+editable, 1).show();
            //tv1.setText(editable);
        }
    }
    private void startDownload() {
        
        Uri uri = Uri.parse("https://raw.githubusercontent.com/efraimzz/whitelist/refs/heads/main/whitelistbeta.apk");
        DownloadManager.Request request = new DownloadManager.Request(uri);

        // הגדר כותרת ותיאור עבור ההתראה
        request.setTitle("הורדת קובץ");
        request.setDescription("מוריד עדכון");

        // אפשר הורדה דרך רשת סלולרית ו-Wi-Fi
        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);

        new File(getExternalFilesDir("")+"/updatebeta.apk").delete();
        File destinationFile = new File(getExternalFilesDir("")+"/updatebeta.apk");
        request.setDestinationUri(Uri.fromFile(destinationFile));
        // הפוך את ההורדה לגלוי ביישום ההורדות ובשורת ההתראות
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // אפס את דגל הביטול לפני התחלת הורדה חדשה
        isDownloadCanceled = false;

        // הכנס את ההורדה לתור וקבל את מזהה ההורדה
        downloadId = downloadManager.enqueue(request);

        showProgressDialog();
    }
    @Deprecated
    private void showProgressDialog() {
        progressDialog = new ProgressDialog(MDMSettingsActivity.this);
        progressDialog.setTitle("הורדת קובץ");
        progressDialog.setMessage("מתחיל הורדה...");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCancelable(false); // המשתמש אינו יכול לבטל אותו (דרך כפתור החזרה)

        // הוספת כפתור שלילי (ביטול) לדיאלוג
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "ביטול", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // סמן שההורדה בוטלה על ידי המשתמש
                    isDownloadCanceled = true;
                    // ביטול ההורדה באמצעות DownloadManager
                    downloadManager.remove(downloadId);
                    // עצור את עדכון ההתקדמות
                    handler.removeCallbacks(updateProgressRunnable);
                    // סגור את הדיאלוג
                    dialog.dismiss();
                    Toast.makeText(MDMSettingsActivity.this, "ההורדה בוטלה.", Toast.LENGTH_SHORT).show();
                }
            });

        progressDialog.show();

        updateProgressRunnable = new Runnable() {
            @Deprecated
            @Override
            public void run() {
                // אם ההורדה כבר בוטלה על ידי המשתמש, אין צורך להמשיך לעדכן
                if (isDownloadCanceled) {
                    handler.removeCallbacks(this);
                    return;
                }

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);
                Cursor cursor = downloadManager.query(query);
                if (cursor != null && cursor.moveToFirst()) {
                    int statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int bytesDownloadedColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR);
                    int totalBytesColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES);

                    int status = cursor.getInt(statusColumnIndex);
                    long bytesDownloaded = cursor.getLong(bytesDownloadedColumnIndex);
                    long totalBytes = cursor.getLong(totalBytesColumnIndex);

                    if (totalBytes > 0) {
                        int progress = (int) ((bytesDownloaded * 100) / totalBytes);
                        progressDialog.setProgress(progress);
                        progressDialog.setMessage("הורדה: " + progress + "%");
                    }

                    // אם ההורדה הושלמה בהצלחה או נכשלה, הפסק לעדכן
                    if (status == DownloadManager.STATUS_SUCCESSFUL || status == DownloadManager.STATUS_FAILED) {
                        handler.removeCallbacks(this); // הפסק לעדכן התקדמות
                        progressDialog.dismiss();
                        //appone(getExternalFilesDir("")+"/updatebeta.apk");
                    } else {
                        handler.postDelayed(this, 1000); // עדכן כל שנייה
                    }
                    cursor.close();
                } else {
                    // טפל במקרה שבו הסמן הוא null או ריק (ההורדה אולי הוסרה או בוטלה)
                    handler.removeCallbacks(this);
                    progressDialog.dismiss();
                    // אם לא בוטל על ידי המשתמש, ייתכן שהייתה בעיה אחרת
                    if (!isDownloadCanceled) {
                        Toast.makeText(MDMSettingsActivity.this, "הורדה בוטלה או לא נמצאה.", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };
        handler.post(updateProgressRunnable); // התחל את עדכון ההתקדמות
    }

    public BroadcastReceiver monDownloadComplete = new BroadcastReceiver() {
        @Deprecated
        @Override
        public void onReceive(Context context, Intent intent) {
        try{
            long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            // ודא שהאירוע מתייחס להורדה הנוכחית ושהיא לא בוטלה על ידי המשתמש
            if (downloadId == id && !isDownloadCanceled) {
                // בטל את דיאלוג ההתקדמות אם הוא עדיין מוצג
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                    handler.removeCallbacks(updateProgressRunnable);
                }

                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(id);
                Cursor cursor = downloadManager.query(query);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        int statusColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                        int localUriColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI);
                        int reasonColumnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);

                        int status = cursor.getInt(statusColumnIndex);
                        String localUriString = cursor.getString(localUriColumnIndex);
                        int reason = cursor.getInt(reasonColumnIndex);

                        if (status == DownloadManager.STATUS_SUCCESSFUL) {
                            Toast.makeText(context, "הורדה הושלמה בהצלחה!", Toast.LENGTH_LONG).show();
                            // התקן את הקובץ שהורד
                            //installApk(localUriString);
                            appone(MDMSettingsActivity.this.getExternalFilesDir("")+"/updatebeta.apk");
                        } else if (status == DownloadManager.STATUS_FAILED) {
                            Toast.makeText(context, "הורדה נכשלה: " + reason, Toast.LENGTH_LONG).show();
                        }
   
                    }
                    cursor.close();
                }
            }
        }  catch(Exception e){
            Toast.makeText(context, e+"",1).show();
        }
        }
    };

    private void installApk(String fileUriString) {
        if (fileUriString == null) {
            Toast.makeText(this, "קובץ לא נמצא להתקנה.", Toast.LENGTH_SHORT).show();
            return;
        }


        Uri apkUri = Uri.parse(fileUriString);
        if (apkUri.getScheme().equals("file")) {
            // נתיב זה מיועד לגרסאות אנדרואיד ישנות יותר (לפני API 24)
            File apkFile = new File(apkUri.getPath());
            apkUri = Uri.fromFile(apkFile); // עדיין מביא ל-URI של file://
        }

        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // נדרש להפעלת פעילות מחוץ להקשר האפליקציה הנוכחי
        // עבור API 24+, תצטרך גם FLAG_GRANT_READ_URI_PERMISSION אם משתמשים ב-FileProvider
        // installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        try {
            startActivity(installIntent);
        } catch (Exception e) {
            Toast.makeText(this, "לא ניתן להתקין קובץ: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }
    public void showLockMDMConfirmationDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
            .setTitle("נעילת הגדרות ניהול מכשיר ונעילת הסרה")
            .setMessage("האם אתה בטוח שברצונך להסיר אפשרות ניהול של כל ההגדרות mdm לצמיתות ונעילת אפשרות הסרת mdm לצמיתות?")
            .setPositiveButton("כן, נעל!", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    spe.putBoolean(locksp,true);
                    spe.commit();
                }
            })
            .setNegativeButton("ביטול", null)
            .show();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // בטל רישום של ה-BroadcastReceiver כדי למנוע דליפות זיכרון
        unregisterReceiver(monDownloadComplete);
        // עצור עדכוני התקדמות ממתינים
        if (handler != null && updateProgressRunnable != null) {
            handler.removeCallbacks(updateProgressRunnable);
        }
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}
