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

import com.emanuelef.remote_capture.Utils;
import com.emanuelef.remote_capture.CaptureService;
import com.emanuelef.remote_capture.R;

public class MDMActivity extends Activity {

    private DevicePolicyManager mDpm;
    private ComponentName mAdminComponentName;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdm);

        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = new ComponentName(this,admin.class);

        // אתחול כפתורים
        setupButton(R.id.btn_manage_restrictions, "ניהול הגבלות מכשיר", RestrictionManagementActivity.class);
        setupButton(R.id.btn_manage_apps, "ניהול אפליקציות", AppManagementActivity.class);
        setupButton(R.id.btn_manage_vpn, "ניהול vpn", MainActivity.class);
        setupButton(R.id.btn_change_password, "שנה סיסמה", null); // מטופל בלוגיקה נפרדת
        setupButton(R.id.btn_remove_mdm, "הסר ניהול מכשיר", null); // מטופל בלוגיקה נפרדת
        setupButton(R.id.btn_activate_mdm, "הפעל ניהול מכשיר", null); // מטופל בלוגיקה נפרדת
        setupButton(R.id.btn_update_mdm_app, "עדכון אפליקציית MDM", null); // מטופל בלוגיקה נפרדת
        setupButton(R.id.btn_select_route, "בחירת מסלול", null); // תצטרך אקטיביטי לזה
        setupButton(R.id.btn_refresh_website_list, "רענון רשימת אתרים", null); // תצטרך לוגיקה לזה
        setupButton(R.id.btn_update_whitelist, "עדכון לרשימת דומיינים לבנה", null); // תצטרך לוגיקה לזה
        setupButton(R.id.btn_more_features, "פיצ'רים נוספים", MoreFeaturesActivity.class); // אקטיביטי חדש
        setupButton(R.id.btn_instructions, "הוראות הפעלה", InstructionsActivity.class); // אקטיביטי חדש
        setupButton(R.id.btn_about, "אודות", AboutActivitya.class); // אקטיביטי חדש

        updateMdmActivationButtonText(); // עדכן טקסט כפתור הפעל/בטל MDM
    }

    private void setupButton(int buttonId, String text, final Class<?> targetActivity) {
        Button button = findViewById(buttonId);
        if (button != null) {
            button.setText(text);
            button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(final View v) {
                        // לוגיקה שדורשת אימות סיסמה לפני מעבר
                        if (v.getId() == R.id.btn_manage_restrictions ||
                            v.getId() == R.id.btn_manage_vpn ||
                            v.getId() == R.id.btn_change_password ||
                            v.getId() == R.id.btn_remove_mdm ||
                            v.getId() == R.id.btn_activate_mdm) { // הוסף כל כפתור הדורש סיסמה
                            PasswordManager.requestPasswordAndSave(new Runnable() {
                                    @Override
                                    public void run() {
                                        handleButtonClick(v.getId(), targetActivity);
                                    }
                                },MDMActivity.this);
                        } else {
                            // כפתורים שאינם דורשים סיסמה
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
            // טיפול בלוגיקה ספציפית לכפתורים ללא אקטיביטי יעד
            if (buttonId == R.id.btn_change_password) {
                PasswordManager. showSetPasswordDialog(MDMActivity.this);
            } else if (buttonId == R.id.btn_remove_mdm) {
                showRemoveMDMConfirmationDialog();
            } else if (buttonId == R.id.btn_activate_mdm) {
                toggleDeviceAdmin();
            } else if (buttonId == R.id.btn_update_mdm_app) {
                updateMdm();
            } else if (buttonId == R.id.btn_select_route) {
                //Toast.makeText(MDMActivity.this, "בחירת מסלול - נדרש יישום.", Toast.LENGTH_SHORT).show();
            } else if (buttonId == R.id.btn_refresh_website_list) {
                //Toast.makeText(MDMActivity.this, "רענון רשימת אתרים - נדרש יישום.", Toast.LENGTH_SHORT).show();
            } else if (buttonId == R.id.btn_update_whitelist) {
               if(CaptureService.isServiceActive()){
                   CaptureService.requestBlacklistsUpdate();
                   Toast.makeText(MDMActivity.this, "updating...",1).show();
               }
                //Toast.makeText(MDMActivity.this, "עדכון רשימת דומיינים לבנה - נדרש יישום.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showRemoveMDMConfirmationDialog() {
        new AlertDialog.Builder(this)
            .setTitle("הסר ניהול מכשיר")
            .setMessage("האם אתה בטוח שברצונך להסיר את אפליקציית ה-MDM כמנהל המכשיר?")
            .setPositiveButton("כן, הסר", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try{
                        mDpm.clearDeviceOwnerApp(getPackageName());
                    }catch(Exception e){}
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
                ed = "dpm set-device-owner com.emanuelef.remote_capture.activities/.MyDeviceAdminReceiver";
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
            });
        
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
}
