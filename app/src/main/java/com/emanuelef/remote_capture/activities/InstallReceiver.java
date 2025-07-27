package com.emanuelef.remote_capture.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.widget.Toast;

import java.io.File;

public class InstallReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppUpdater.ACTION_INSTALL_COMPLETE.equals(intent.getAction())) {
            String packageName = intent.getStringExtra(AppUpdater.EXTRA_PACKAGE_NAME);
            int status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, -1);
            String message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE);

            // ודא שאתה מוחק את התיקיה הזמנית בכל מקרה
            File tempDir = new File(context.getCacheDir(), "apks_temp");
            AppUpdater.deleteTempDir(tempDir);

            // בנוסף, אם ההתקנה בוטלה בגלל סיסמה שגויה, אנו צריכים למחוק את קובץ ה-APK המקורי
            // זה קצת מורכב כי ה-Receiver לא יודע איזה קובץ APK נבחר
            // דרך טובה יותר תהיה להעביר את נתיב הקובץ הזמני כ-Extra ל-InstallReceiver
            // למען הפשטות כרגע, ננקה רק את התיקיה הזמנית הכללית.

            switch (status) {
                case PackageInstaller.STATUS_PENDING_USER_ACTION:
                    Intent confirmationIntent = (Intent) intent.getParcelableExtra(Intent.EXTRA_INTENT);
                    if (confirmationIntent != null) {
                        confirmationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(confirmationIntent);
                    }
                    Toast.makeText(context, "התקנה ממתינה לאישור משתמש עבור " + packageName, Toast.LENGTH_LONG).show();
                    break;
                case PackageInstaller.STATUS_SUCCESS:
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
                    break;
                default:
                    Toast.makeText(context, "סטטוס התקנה לא ידוע: " + message, Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }
}
