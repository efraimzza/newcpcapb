package com.emanuelef.remote_capture.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.widget.Toast;

import java.io.File;
import android.os.Handler;

public class InstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppUpdater.ACTION_INSTALL_COMPLETE.equals(intent.getAction())) {
            String packageName = AppUpdater.mainPackageName;
            //packageName=intent.getStringExtra(AppUpdater.EXTRA_PACKAGE_NAME);
            int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1);
            String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);

            // ודא שאתה מוחק את התיקיה הזמנית בכל מקרה
            File tempDir = new File(context.getFilesDir()+ "/cach/apks_temp");
            AppUpdater.deleteTempDir(tempDir);

            // בנוסף, אם ההתקנה בוטלה בגלל סיסמה שגויה, אנו צריכים למחוק את קובץ ה-APK המקורי
            // זה קצת מורכב כי ה-Receiver לא יודע איזה קובץ APK נבחר
            // דרך טובה יותר תהיה להעביר את נתיב הקובץ הזמני כ-Extra ל-InstallReceiver
            // למען הפשטות כרגע, ננקה רק את התיקיה הזמנית הכללית.

            switch (status) {
                case PackageInstaller.STATUS_PENDING_USER_ACTION:
                    /*Intent confirmationIntent = intent.getParcelableExtra(Intent.EXTRA_INTENT);
                    //if (confirmationIntent != null) {
                        try{
                        confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(confirmationIntent);
                        }catch(Exception e){}*/
                        try{
                    Intent inte=new Intent(context,confirmationinstall.class);
                    inte.putExtra("inte",intent.getParcelableExtra("android.intent.extra.INTENT"));
                    inte.addFlags(intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    context.startActivity(inte);
                        }catch(Exception e){}
                    //}
                    AppManagementActivity.progressDialog.setMessage("התקנה ממתינה לאישור משתמש עבור " + packageName + ": " + message);
                    Toast.makeText(context, "התקנה ממתינה לאישור משתמש עבור " + packageName, Toast.LENGTH_LONG).show();
                    break;
                case PackageInstaller.STATUS_SUCCESS:
                    AppManagementActivity.progressDialog.setMessage("התקנה/עדכון הושלם בהצלחה עבור " + packageName + ": " + message);
                    dismissprogress();
                    Toast.makeText(context, "התקנה/עדכון הושלם בהצלחה עבור " + packageName, Toast.LENGTH_LONG).show();
                    // ייתכן שתרצה לרענן את רשימת האפליקציות ב-UI (לדוגמה, לשלוח ברודקאסט חזרה ל-AppManagementActivity)
                    break;
                case PackageInstaller.STATUS_FAILURE:
                case PackageInstaller.STATUS_FAILURE_ABORTED:
                case PackageInstaller.STATUS_FAILURE_BLOCKED:
                case PackageInstaller.STATUS_FAILURE_CONFLICT:
                case PackageInstaller.STATUS_FAILURE_INCOMPATIBLE:
                case PackageInstaller.STATUS_FAILURE_INVALID:
                case PackageInstaller.STATUS_FAILURE_STORAGE:
                    Toast.makeText(context, "התקנה נכשלה עבור " + packageName + ": " + message, Toast.LENGTH_LONG).show();
                    AppManagementActivity.progressDialog.setMessage("התקנה נכשלה עבור " + packageName + ": " + message);
                    dismissprogress();
                    LogUtil.logToFile("התקנה נכשלה עבור " + packageName + ": " + message);
                    break;
                default:
                    Toast.makeText(context, "סטטוס התקנה לא ידוע: " + message, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
    static void dismissprogress(){
        new Handler().postDelayed(new Runnable(){

                @Override
                public void run() {
                    if (AppManagementActivity.progressDialog != null && AppManagementActivity.progressDialog.isShowing()) {
                        AppManagementActivity.progressDialog.dismiss();
                    }
                }
            }, 5000);
    }
}
