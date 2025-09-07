package com.emanuelef.remote_capture.activities;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.os.AsyncTask; // הוסף את הייבוא הזה
import android.app.ProgressDialog; // הוסף את הייבוא הזה (לדיאלוג טעינה)
import java.util.zip.ZipInputStream; // וודא שזה מיובא
import java.util.zip.ZipEntry; // וודא שזה מיובא
import android.content.pm.PackageInstaller;

import com.emanuelef.remote_capture.R;

@Deprecated
public class AppManagementActivity extends Activity {

    private DevicePolicyManager mDpm;
    private ComponentName mAdminComponentName;
    private ListView lvApps;
    private List<AppItem> mOriginalAppList;
    private List<AppItem> mFilteredAppList;
    private AppListAdapter mAdapter;

    // משתנים לשמירת מצב הסינון והמיון הנוכחי
    private String currentSearchText = "";
    private String currentSearchPackage = ""; // לחיפוש לפי שם חבילה
    private int currentFilterOptionId = R.id.rb_filter_all_dialog; // ID של כפתור הרדיו הנבחר
    private int currentSortOptionId = R.id.rb_sort_name_dialog; // ID של כפתור הרדיו הנבחר
   
    private ProgressDialog progressDialog; // משתנה לדיאלוג התקדמות
    
    private static final int PICK_APK_REQUEST_CODE = 101;
    
    @Deprecated
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_management);

        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = new ComponentName(this, admin.class);

        lvApps = (ListView) findViewById(R.id.lv_apps);

        // טען את הרשימה באופן אסינכרוני
        new LoadAppsTask().execute();

        mFilteredAppList = new ArrayList<AppItem>();
        mAdapter = new AppListAdapter(this, mFilteredAppList);
        lvApps.setAdapter(mAdapter);

        Button btnShowFilterOptions = (Button) findViewById(R.id.btn_filter_apps);
        btnShowFilterOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showFilterOptionsDialog();
                }
            });
        Button btnRefreshApps = findViewById(R.id.btn_refresh_apps);
        btnRefreshApps.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new LoadAppsTask().execute();
                    //Toast.makeText(AppManagementActivity.this, "רשימת אפליקציות עודכנה.", Toast.LENGTH_SHORT).show();
                }
            });
        Button btnSaveAppChanges = (Button) findViewById(R.id.btn_save_app_changes);
        btnSaveAppChanges.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PasswordManager.requestPasswordAndSave(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    applyAppVisibilityChanges();
                                }catch(Exception e){}
                            }
                        },AppManagementActivity.this);
                }
            });

        Button btnInstallApk = (Button) findViewById(R.id.btn_install_apk);
        if (btnInstallApk != null) {
            btnInstallApk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            List<PackageInstaller.SessionInfo> lses= getPackageManager().getPackageInstaller().getAllSessions();
                            if (lses != null) {
                                for (PackageInstaller.SessionInfo pses:lses) {
                                    if (pses != null) {
                                        try {
                                            if (pses.getInstallerPackageName().equals(getPackageName())) {
                                                getPackageManager().getPackageInstaller().abandonSession(pses.getSessionId());
                                            }
                                        } catch (Exception e) {
                                            LogUtil.logToFile(""+e);
                                            Toast.makeText(AppManagementActivity.this, "" + e, 0).show();
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            LogUtil.logToFile(""+e);
                            Toast.makeText(AppManagementActivity.this, "" + e, 0).show();
                        }
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setType("*/*");
                        String[] mimetypes = {"*/apk","*/apks","*/xapk","application/vnd.android.package-archive", "application/zip", "application/x-zip-compressed", "application/octet-stream"};
                        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);

                        intent.addCategory(Intent.CATEGORY_OPENABLE);
                        startActivityForResult(Intent.createChooser(intent, "בחר קובץ APK/APKS"), PICK_APK_REQUEST_CODE);
                    }
                });
        }
    }

    // הוסף AsyncTask חדש לטעינת אפליקציות
    @Deprecated
    private class LoadAppsTask extends AsyncTask<Void, Void, List<AppItem>> {
        
        @Deprecated
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(AppManagementActivity.this);
            progressDialog.getWindow().setBackgroundDrawableResource(R.drawable.roundbugreen);
            progressDialog.setMessage("טוען רשימת אפליקציות...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected List<AppItem> doInBackground(Void... voids) {
            PackageManager pm = getPackageManager();
            List<ApplicationInfo> installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA | PackageManager.MATCH_UNINSTALLED_PACKAGES);
            List<AppItem> appList = new ArrayList<AppItem>();

            for (ApplicationInfo appInfo : installedApps) {
                boolean isHiddenByMDM =false;
                try{
                    isHiddenByMDM = mDpm.isApplicationHidden(mAdminComponentName, appInfo.packageName);
                }catch(Exception e){}
                long lastUpdateTime = 0;
                try {
                    PackageInfo packageInfo = pm.getPackageInfo(appInfo.packageName, 0);
                    lastUpdateTime = packageInfo.lastUpdateTime;
                } catch (PackageManager.NameNotFoundException e) {
                    LogUtil.logToFile(""+e);
                    e.printStackTrace();
                }

                appList.add(new AppItem(
                                appInfo.loadLabel(pm).toString(),
                                appInfo.packageName,
                                appInfo.loadIcon(pm),
                                isHiddenByMDM,
                                (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0,
                                hasLauncherIcon(pm, appInfo.packageName),
                                lastUpdateTime
                            ));
            }
            return appList;
        }
        
        @Deprecated
        @Override
        protected void onPostExecute(List<AppItem> result) {
            super.onPostExecute(result);
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
            mOriginalAppList = result; // עדכן את הרשימה המקורית
            applyFiltersAndSort(); // בצע סינון ומיון ראשוני לאחר הטעינה
        }
    }

    // ... (loadAppList, hasLauncherIcon)


    private void showFilterOptionsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_filter_options, null);
        builder.setView(dialogView);

        final EditText etSearchApps = dialogView.findViewById(R.id.et_search_apps_dialog);
        final EditText etSearchPackage = dialogView.findViewById(R.id.et_search_package_dialog); // מצא את ה-EditText החדש
        final RadioGroup rgFilterApps = dialogView.findViewById(R.id.rg_filter_apps_dialog);
        final RadioGroup rgSortApps = dialogView.findViewById(R.id.rg_sort_apps_dialog);

        // הגדר את הערכים הנוכחיים בדיאלוג
        etSearchApps.setText(currentSearchText);
        etSearchPackage.setText(currentSearchPackage); // הגדר את טקסט חיפוש החבילה
        rgFilterApps.check(currentFilterOptionId);
        rgSortApps.check(currentSortOptionId);

        // עדכן את טקסט חיפוש השם כאשר המשתמש מקליד
        etSearchApps.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchText = s.toString();
                }
                @Override public void afterTextChanged(Editable s) {}
            });

        // עדכן את טקסט חיפוש החבילה כאשר המשתמש מקליד
        etSearchPackage.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearchPackage = s.toString();
                }
                @Override public void afterTextChanged(Editable s) {}
            });

        rgFilterApps.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    currentFilterOptionId = checkedId;
                }
            });

        rgSortApps.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    currentSortOptionId = checkedId;
                }
            });

        builder.setPositiveButton("החל סינון", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    applyFiltersAndSort();
                }
            });
        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // אין צורך לעשות כלום, הדיאלוג ייסגר
                }
            });

        builder.show();
    }

    private void applyFiltersAndSort() {
        mFilteredAppList.clear();
        String searchTextLower = currentSearchText.toLowerCase();
        String searchPackageLower = currentSearchPackage.toLowerCase(); // טקסט חיפוש חבילה ב-lowercase

        // 1. סינון
        for (AppItem appItem : mOriginalAppList) {
            boolean matchesFilter = false;
            boolean isSystemApp = appItem.isSystemApp();
            boolean isHidden = appItem.isHidden();
            boolean hasLauncher = appItem.hasLauncherIcon();

            if (currentFilterOptionId == R.id.rb_filter_all_dialog) {
                matchesFilter = true;
            } else if (currentFilterOptionId == R.id.rb_filter_user_dialog) {
                matchesFilter = !isSystemApp;
            } else if (currentFilterOptionId == R.id.rb_filter_system_dialog) {
                matchesFilter = isSystemApp;
            } else if (currentFilterOptionId == R.id.rb_filter_hidden_dialog) {
                matchesFilter = isHidden;
            } else if (currentFilterOptionId == R.id.rb_filter_launcher_dialog) {
                matchesFilter = hasLauncher;
            }

            // שילוב חיפוש לפי שם אפליקציה וגם לפי שם חבילה
            boolean matchesName = appItem.getName().toLowerCase().contains(searchTextLower);
            boolean matchesPackage = appItem.getPackageName().toLowerCase().contains(searchPackageLower);

            if (matchesFilter && matchesName && matchesPackage) { // חייב להתאים לשניהם
                mFilteredAppList.add(appItem);
            }
        }

        // 2. מיון
        if (currentSortOptionId == R.id.rb_sort_name_dialog) {
            Collections.sort(mFilteredAppList, new Comparator<AppItem>() {
                    @Override
                    public int compare(AppItem item1, AppItem item2) {
                        return item1.getName().compareToIgnoreCase(item2.getName());
                    }
                });
        } else if (currentSortOptionId == R.id.rb_sort_last_installed_dialog) {
            Collections.sort(mFilteredAppList, new Comparator<AppItem>() {
                    @Override
                    public int compare(AppItem item1, AppItem item2) {
                        if (item1.getLastUpdateTime() > item2.getLastUpdateTime()) {
                            return -1;
                        } else if (item1.getLastUpdateTime() < item2.getLastUpdateTime()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                });
        }

        mAdapter.notifyDataSetChanged();
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_APK_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri sourceUri = data.getData();
                File tempFile = null;
                try {
                    // העתק את ה-URI לקובץ זמני.
                    // אם זה ZIP/APKS, זה עדיין יועתק כקובץ ZIP יחיד.
                    tempFile = copyUriToTempFile(this, sourceUri);

                    if (tempFile != null) {
                        String detectedPackageName = null;
                        File baseApkFile = null; // קובץ ה-APK הבסיסי שישמש לבדיקת שם חבילה

                        // בדוק אם הקובץ הוא ארכיון (ZIP/APKS)
                        if (tempFile.getName().toLowerCase().endsWith(".zip") ||
                            tempFile.getName().toLowerCase().endsWith(".apks") ||
                            tempFile.getName().toLowerCase().endsWith(".xapk")) {

                            // זהו ארכיון, חלץ את ה-APK הראשי כדי לקבל את שם החבילה
                            File extractedTempDir = AppUpdater.createTempDir(this); // השתמש במתודה מ-AppUpdater
                            List<File> extractedApks = AppUpdater.extractApksFromZip(tempFile, extractedTempDir); // השתמש במתודה מ-AppUpdater

                            if (!extractedApks.isEmpty()) {
                                baseApkFile = AppUpdater.findBaseApk(this, extractedApks); // מצא את ה-APK הבסיסי מתוך הרשימה
                                if (baseApkFile != null) {
                                    detectedPackageName = AppUpdater.getApkPackageName(this, baseApkFile.getAbsolutePath());
                                }
                            }
                            // חשוב: נקה את התיקיה הזמנית של הקבצים המחולצים.
                            // בדרך כלל הניקוי יקרה ב-InstallReceiver, אבל אם ההתקנה לא תצא לפועל
                            // בגלל בדיקת סיסמה שנכשלה, צריך לנקות.
                            // עם זאת, אם ההתקנה תצא לפועל, ה-InstallReceiver יטפל בזה.
                            // במקרה הזה, נשאיר את הניקוי ל-InstallReceiver
                            // או שנדאג לנקות את `extractedTempDir` אם ההתקנה מבוטלת פה.
                            // לצורך הפשטות, נסמוך על ה-InstallReceiver לניקוי הכללי של `apks_temp`.

                        } else if (tempFile.getName().toLowerCase().endsWith(".apk")) {
                            // זהו קובץ APK בודד
                            baseApkFile = tempFile; // הקובץ עצמו הוא ה-APK הבסיסי
                            detectedPackageName = AppUpdater.getApkPackageName(this, baseApkFile.getAbsolutePath());
                        }

                        if (detectedPackageName != null) {
                            if (isAppInstalled(detectedPackageName)) {
                                // האפליקציה כבר מותקנת, זהו עדכון - אין צורך בסיסמה
                                AppUpdater.startInstallSession(this, tempFile, false); // העבר את קובץ ה-ZIP המקורי/APK בודד
                            } else {
                                // אפליקציה חדשה, דורש סיסמה
                                showNewAppInstallPasswordDialog(tempFile); // העבר את קובץ ה-ZIP המקורי/APK בודד
                            }
                        } else {
                            Toast.makeText(this, "לא ניתן לזהות את שם החבילה מהקובץ הנבחר.", Toast.LENGTH_LONG).show();
                            // נקה קובץ זמני אם לא זיהינו שם חבילה
                            if (tempFile.exists()) tempFile.delete();
                        }
                    } else {
                        Toast.makeText(this, "לא ניתן לגשת לקובץ הנבחר או להעתיקו.", Toast.LENGTH_LONG).show();
                    }
                } catch (Exception e) {
                    LogUtil.logToFile(""+e);
                    Toast.makeText(this, "שגיאה בטיפול בקובץ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    e.printStackTrace();
                    if (tempFile != null && tempFile.exists()) {
                        tempFile.delete(); // נקה במקרה של שגיאה
                    }
                }
            }
        }
    }

    private boolean isAppInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void showNewAppInstallPasswordDialog(final File apkFile) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("התקנת אפליקציה חדשה");
        builder.setMessage("התקנת אפליקציה חדשה דורשת אימות סיסמה. האם ברצונך להמשיך?");

        builder.setPositiveButton("כן, המשך", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PasswordManager.requestPasswordAndSave(new Runnable() {
                            @Override
                            public void run() {
                                // אם הסיסמה נכונה, התחל את ההתקנה
                                AppUpdater.startInstallSession(AppManagementActivity.this, apkFile, true); // true = password already checked
                            }
                        },AppManagementActivity.this);
                }
            });
        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(AppManagementActivity.this, "התקנת האפליקציה בוטלה.", Toast.LENGTH_SHORT).show();
                    // נקה את הקובץ הזמני אם ההתקנה בוטלה
                    if (apkFile != null && apkFile.exists()) {
                        apkFile.delete();
                    }
                }
            });
        builder.show();
    }

    
    private boolean hasLauncherIcon(PackageManager pm, String packageName) {
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<android.content.pm.ResolveInfo> resolveInfoList = pm.queryIntentActivities(intent, 0);
        for (android.content.pm.ResolveInfo resolveInfo : resolveInfoList) {
            if (resolveInfo.activityInfo != null && packageName.equals(resolveInfo.activityInfo.packageName)) {
                return true;
            }
        }
        return false;
    }

    

    private void applyAppVisibilityChanges() {
        for (AppItem appItem : mOriginalAppList) { // עבר על הרשימה המקורית
            // אם מצב ה-hidden השתנה עבור האפליקציה הזו
            boolean currentHiddenState = mDpm.isApplicationHidden(mAdminComponentName, appItem.getPackageName());
            if (currentHiddenState != appItem.isHidden()) {
                mDpm.setApplicationHidden(mAdminComponentName, appItem.getPackageName(), appItem.isHidden());
            }
        }
        Toast.makeText(AppManagementActivity.this, "שינויים באפליקציות נשמרו!", Toast.LENGTH_SHORT).show();
        // רענן את הרשימה לאחר שמירה כדי לשקף שינויים (לדוגמה, בסינון 'מוסתרות')
        // טען מחדש את הרשימה המקורית מהמערכת
        new LoadAppsTask().execute();
        applyFiltersAndSort(); // סנן ומיין אותה מחדש
    }
    

    // מתודת עזר להעתקת URI לקובץ זמני
    private File copyUriToTempFile(Context context, Uri uri) {
        File tempFile = null;
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            String fileName = getFileNameFromUri(context, uri); // פונקציית עזר לקבלת שם קובץ
            if (fileName == null) {
                fileName = "temp_package"; // שם ברירת מחדל
            }
            if (!fileName.contains(".")) { // הוסף סיומת אם חסרה
                fileName += ".apk"; // נניח APK כברירת מחדל אם לא זוהה
            }

            tempFile = new File(context.getCacheDir(), fileName);
            inputStream = context.getContentResolver().openInputStream(uri);
            outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            return tempFile;
        } catch (Exception e) {
            LogUtil.logToFile(""+e);
            e.printStackTrace();
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete(); // מחיקת קובץ חלקי במקרה של שגיאה
            }
            return null;
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                LogUtil.logToFile(""+e);
                e.printStackTrace();
            }
        }
    }

    // פונקציית עזר לקבלת שם קובץ מ-URI (לא תמיד אמין לחלוטין)
    private String getFileNameFromUri(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            android.database.Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }
    
    
}
