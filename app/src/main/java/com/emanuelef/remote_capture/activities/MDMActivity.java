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

import com.emanuelef.remote_capture.model.Prefs;
import com.emanuelef.remote_capture.fragments.StatusReceiver;
import com.emanuelef.remote_capture.Utils;
import com.emanuelef.remote_capture.CaptureService;
import com.emanuelef.remote_capture.R;

public class MDMActivity extends Activity {

    private DevicePolicyManager mDpm;
    private ComponentName mAdminComponentName;
    SharedPreferences mPrefs;
    SharedPreferences sp;
    SharedPreferences.Editor spe;
    public static final String modesp="mode";
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
        setContentView(R.layout.activity_mdm);
        try{
        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        handler = new Handler(Looper.getMainLooper());
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(monDownloadComplete, filter);
        }  catch(Exception e){
            Toast.makeText(this, e+"",1).show();
        }
        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = new ComponentName(this,admin.class);

        mPrefs = PreferenceManager.getDefaultSharedPreferences(MDMActivity.this);
        
        sp=this.getSharedPreferences(this.getPackageName(),this.MODE_PRIVATE);
        spe=sp.edit();

        if(sp.getString(modesp,"").equals("")){
            AppState.getInstance().setCurrentPath(PathType.MULTIMEDIA);
            spe.putString(modesp,AppState.getInstance().getCurrentPath().name());
            spe.commit();
            Toast.makeText(this,AppState.getInstance().getCurrentPath().name()+" is default",1).show();
        }else{
            try{
                AppState.getInstance().setCurrentPath(PathType.valueOf(sp.getString(modesp,"")));
                Toast.makeText(this, AppState.getInstance().getCurrentPath().name()+ " is now",1).show();
            }catch(Exception e){
                Toast.makeText(this, e+"",1).show();
            }
        }
        
        AppState.getInstance().setCurrentPath(AppState.getInstance().getCurrentPath());
        
        TextView tvcurroute=findViewById(R.id.tv_cur_route);
        tvcurroute.setText("המסלול הפעיל - "+AppState.getInstance().getCurrentPath().getDescription());
        
        // אתחול כפתורים
        setupButton(R.id.btn_manage_restrictions, "ניהול הגבלות מכשיר", RestrictionManagementActivity.class);
        setupButton(R.id.btn_manage_apps, "ניהול אפליקציות", AppManagementActivity.class);
        setupButton(R.id.btn_manage_vpn, "ניהול vpn", MainActivity.class);
        setupButton(R.id.btn_change_password, "שנה סיסמה", null);
        setupButton(R.id.btn_remove_mdm, "הסר ניהול מכשיר", null);
        setupButton(R.id.btn_activate_mdm, "הפעל ניהול מכשיר", null); 
        setupButton(R.id.btn_remove_frp, "הסר frp", null); 
        setupButton(R.id.btn_activate_frp, "הפעל frp", null); 
        setupButton(R.id.btn_update_mdm_app, "עדכון אפליקציית MDM", null); // מטופל בלוגיקה נפרדת
        setupButton(R.id.btn_select_route, "בחירת מסלול", null); // תצטרך אקטיביטי לזה
        setupButton(R.id.btn_refresh_website_list, "רענון רשימת אתרים", null); // תצטרך לוגיקה לזה
        setupButton(R.id.btn_update_whitelist, "עדכון לרשימת דומיינים לבנה", null); // תצטרך לוגיקה לזה
        setupButton(R.id.btn_more_features, "פיצ'רים נוספים", MoreFeaturesActivity.class); // אקטיביטי חדש
        setupButton(R.id.btn_instructions, "הוראות הפעלה", InstructionsActivity.class); // אקטיביטי חדש
        setupButton(R.id.btn_about, "אודות", AboutActivitya.class); // אקטיביטי חדש
        setupabodeb();

        updateMdmActivationButtonText(); // עדכן טקסט כפתור הפעל/בטל MDM
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
                            v.getId() == R.id.btn_manage_vpn ||
                            v.getId() == R.id.btn_change_password ||
                            v.getId() == R.id.btn_remove_mdm ||
                            v.getId() == R.id.btn_activate_mdm ||
                            v.getId() == R.id.btn_remove_frp ||
                            v.getId() == R.id.btn_activate_frp ||
                            v.getId() ==  R.id.btn_select_route) { 
                            PasswordManager.requestPasswordAndSave(new Runnable() {
                                    @Override
                                    public void run() {
                                        handleButtonClick(v.getId(), targetActivity);
                                    }
                                },MDMActivity.this);
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
            Intent intent = new Intent(MDMActivity.this, targetActivity);
            startActivity(intent);
        } else {
            // without target activity
            if (buttonId == R.id.btn_change_password) {
                PasswordManager. showSetPasswordDialog(MDMActivity.this);
            } else if (buttonId == R.id.btn_remove_mdm) {
                showRemoveMDMConfirmationDialog();
            } else if (buttonId == R.id.btn_activate_mdm) {
                toggleDeviceAdmin();
            }else if (buttonId == R.id.btn_remove_frp) {
                removefrp();
            } else if (buttonId == R.id.btn_activate_frp) {
                activatefrp();
            } else if (buttonId == R.id.btn_update_mdm_app) {
                updateMdm();
                //startDownload();
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
                            
                            Toast.makeText(MDMActivity.this, "המסלול שנבחר: " + selectedPath.getDescription(), Toast.LENGTH_SHORT).show();
                        }
                    });
                builder.create().show();  
            } else if (buttonId == R.id.btn_refresh_website_list) {
                //Toast.makeText(MDMActivity.this, "רענון רשימת אתרים - נדרש יישום.", Toast.LENGTH_SHORT).show();
            } else if (buttonId == R.id.btn_update_whitelist) {
               if(CaptureService.isServiceActive()){
                   CaptureService.requestBlacklistsUpdate();
                   Toast.makeText(MDMActivity.this, "updating...",1).show();
               }
            }
        }
    }
    private void removefrp(){
        if (Build.VERSION.SDK_INT > 29) {
           try {
              List<String> arrayList = new ArrayList<>();
              FactoryResetProtectionPolicy frp=new FactoryResetProtectionPolicy.Builder()
                .setFactoryResetProtectionAccounts(arrayList)
                .setFactoryResetProtectionEnabled(false)
                 .build();
                 mDpm.setFactoryResetProtectionPolicy(mAdminComponentName, frp);
           } catch (Exception e) {
                Toast.makeText(MDMActivity.this, "e-frp" , Toast.LENGTH_SHORT).show();
           }
        }
           try {
              Bundle bundle = new Bundle();
              bundle = null;
              String str = "com.google.android.gms";
              mDpm=(DevicePolicyManager)MDMActivity.this.getSystemService("device_policy");
              mDpm.setApplicationRestrictions(mAdminComponentName, str, bundle);
              Intent intent = new Intent("com.google.android.gms.auth.FRP_CONFIG_CHANGED");
              intent.setPackage(str);
              intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
              MDMActivity.this.sendBroadcast(intent);
              Toast.makeText(MDMActivity.this, "frp removed", Toast.LENGTH_SHORT).show();
           } catch (Exception e) {
             Toast.makeText(MDMActivity.this, "" + e, Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(MDMActivity.this, "e-frp"+e , Toast.LENGTH_SHORT).show();
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
            MDMActivity.this.sendBroadcast(intent);
            Toast.makeText(MDMActivity.this, "frp..", Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
		    Toast.makeText(MDMActivity.this, "e-frp2"+e , Toast.LENGTH_SHORT).show();
		}
    }
    private void setupabodeb(){
        Button but=findViewById(R.id.btn_about);
        but.setOnLongClickListener(new OnLongClickListener(){

                @Override
                public boolean onLongClick(View p1) {
                    PasswordManager.requestPasswordAndSave(new Runnable() {
                            @Override
                            public void run() {
                                AlertDialog.Builder builder = new AlertDialog.Builder(MDMActivity.this);
                                builder.setTitle("debuging");

                                final Switch swi =new Switch(MDMActivity.this);
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
                        },MDMActivity.this);
                    return true;
                }
            });
    }

    private void showRemoveMDMConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("הסר ניהול מכשיר")
            .setMessage("האם אתה בטוח שברצונך להסיר את אפליקציית ה-MDM כמנהל המכשיר?")
            .setPositiveButton("כן, הסר", new DialogInterface.OnClickListener() {
                @Deprecated
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    removefrp();
                    try{
                        
                    mDpm.clearDeviceOwnerApp(getPackageName());
                        Toast.makeText(MDMActivity.this, "mdm removed", Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                        Toast.makeText(MDMActivity.this, "" + e, Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("ביטול", null)
            .show();
    }

    private void toggleDeviceAdmin() {
        if (mDpm.isDeviceOwnerApp(getPackageName())) {
            // אם פעיל, ננסה לבטל את ההרשאה
            showRemoveMDMConfirmationDialog(); // נשתמש באותו דיאלוג אישור להסרה
        } else {
            // אם לא פעיל, נבקש להפעיל
            /*
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "יישום זה דורש הרשאת מנהל מכשיר כדי ליישם מדיניות אבטחה.");
            startActivityForResult(intent, 0);
            */
            try {
                //String[] strar = {"/system/bin/sh","-c",""};
                String[] strar = {"su","-c",""};
                String ed="";
                //ed = edtx1.getText().toString();
                ed="dpm set-device-owner com.emanuelef.remote_capture.debug/com.emanuelef.remote_capture.activities.admin";
                //int i = 0;
                //ed.split(" ", i++);
                strar[2] = ed;
                //strar[i]=ed;
                String c ="";

                try {
                    Process exec=Runtime.getRuntime().exec(strar);
                    c += (exec.waitFor() == 0) ?"success:": "fail:";
                    exec.getOutputStream();
                    //c = exec.getInputStream().toString();
                    //c=exec.getOutputStream().toString();
                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(exec.getInputStream()));
                    BufferedReader in=bufferedReader;
                    String st;
                    StringBuilder edtx1=new StringBuilder();
                    do {
                        st = in.readLine();
                        if (st != null) {
                            edtx1.append(st);
                            edtx1.append(String.valueOf("\n"));
                            continue;
                        }
                    } while (st != null);
                    in.close();
                    c += edtx1.toString();
                    bufferedReader = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
                    in = bufferedReader;
                    st = "";
                    edtx1 = new StringBuilder();
                    do {
                        st = in.readLine();
                        if (st != null) {
                            edtx1.append(st);
                            edtx1.append(String.valueOf("\n"));
                            continue;
                        }
                    } while (st != null);
                    in.close();
                    c += edtx1.toString();
                    Toast.makeText(this, "" + c, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(this, "error" + e, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(this, "error" + e, Toast.LENGTH_LONG).show();
            }
        }
    }
    boolean succ=false;
    boolean mend=false;
    @Deprecated
    private void updateMdm(){
    //first abandon all old sessions
    try {
       List<PackageInstaller.SessionInfo> lses= MDMActivity.this.getPackageManager().getPackageInstaller().getAllSessions();
       if (lses != null) {
         for (PackageInstaller.SessionInfo pses:lses) {
             if (pses != null) {
                try {
                    if (pses.getInstallerPackageName().equals(MDMActivity.this.getPackageName())) {
                       MDMActivity.this.getPackageManager().getPackageInstaller().abandonSession(pses.getSessionId());
                    }
                } catch (Exception e) {  
                    Toast.makeText(MDMActivity.this, "" + e, 0).show();
                }
             }
         }
      }
      } catch (Exception e) {
         Toast.makeText(MDMActivity.this, "" + e, 0).show();
      }
      try{
         startDownload();
      } catch (Exception e) {
         Toast.makeText(MDMActivity.this, "" + e, 0).show();
      }
        /*
        new Thread(){public void run(){
        succ= Utils.downloadFile("https://raw.githubusercontent.com/efraimzz/whitelist/refs/heads/main/whitelistbeta.apk", MDMActivity.this.getFilesDir()+"/updatebeta.apk");
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
                    appone(MDMActivity.this.getFilesDir()+"/updatebeta.apk");
                    }
                        Toast.makeText(MDMActivity.this, ""+succ, 1).show();
                        mend=false;
                        succ=false;
                    }
                }
            });*/
        
    }
    private void updateMdmActivationButtonText() {
        Button btnActivateMDM = findViewById(R.id.btn_activate_mdm);
        if (btnActivateMDM != null) {
            if (mDpm.isDeviceOwnerApp(getPackageName())) {
                btnActivateMDM.setText("בטל ניהול מכשיר");
                btnActivateMDM.setBackgroundResource(R.drawable.red_button_background); // כפתור אדום לביטול
            } else {
                btnActivateMDM.setText("הפעל ניהול מכשיר");
                btnActivateMDM.setBackgroundResource(R.drawable.green_button_background); // כפתור ירוק להפעלה
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        updateMdmActivationButtonText(); // עדכן את הכפתור לאחר חזרה מפעילות הפעלת מנהל
    }
    PackageInstaller.Session openses;
    void appone(String mappath) {
        String editable;
        try {
            PackageInstaller packageInstaller = MDMActivity.this.getPackageManager().getPackageInstaller();

            PackageInstaller. SessionParams sessionParams = new PackageInstaller. SessionParams(1);
            openses = packageInstaller.openSession(packageInstaller.createSession(sessionParams));
           // editable = edtx1.getText().toString();
            editable = mappath;
            if (editable.equals("")) {
                Toast.makeText(MDMActivity.this, "write the path!", 1).show();
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
                            Intent intent  = new Intent(MDMActivity.this, StatusReceiver.class);
                            openses.commit(PendingIntent.getBroadcast(MDMActivity.this, 0, intent, PendingIntent.FLAG_MUTABLE).getIntentSender());
                            return;
                        } catch (Throwable e) {}
                    }
                }
            }
            Toast.makeText(MDMActivity.this, "not exsist or not readable!", 1).show();
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
            Toast.makeText(MDMActivity.this, ""+e2+editable, 1).show();
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
        progressDialog = new ProgressDialog(MDMActivity.this);
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
                    Toast.makeText(MDMActivity.this, "ההורדה בוטלה.", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(MDMActivity.this, "הורדה בוטלה או לא נמצאה.", Toast.LENGTH_SHORT).show();
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
                            appone(MDMActivity.this.getExternalFilesDir("")+"/updatebeta.apk");
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
