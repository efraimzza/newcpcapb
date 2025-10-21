package com.emanuelef.remote_capture.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.os.Handler;

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
   
    public static ProgressDialog progressDialog; // משתנה לדיאלוג התקדמות
    
    private static final int PICK_APK_REQUEST_CODE = 101;
    
    static String pickedfilepath="";
    
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
                                    if (pses != null&&pses.getInstallerPackageName()!=null) {
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
                        //Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        //intent.setType("*/*");
                        //String[] mimetypes = {"application/vnd.android.package-archive", "application/zip", "application/x-zip-compressed", "application/octet-stream","*/apk","*/apks","*/xapk","*/apkm"};
                        //intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
                        //intent.addCategory(Intent.CATEGORY_OPENABLE);
                        //startActivityForResult(Intent.createChooser(intent, "בחר קובץ APK/APKS"), PICK_APK_REQUEST_CODE);
                        pickedfilepath="";
                        Intent intent=new Intent(AppManagementActivity.this,picker.class);
                        startActivity(intent);  
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
    @Deprecated
    @Override
    protected void onResume() {
        super.onResume();
        if(pickedfilepath!=null&&!pickedfilepath.equals("")){
            AppUpdater. deleteTempDir(new File(AppManagementActivity.this.getFilesDir().toString() + "/cach"));
            mfilepath=pickedfilepath;
            pickedfilepath="";
            new Thread(){public void run() {

                    getMainExecutor().execute(new Runnable(){
                            @Deprecated
                            @Override
                            public void run() {
                                progressDialog = new ProgressDialog(AppManagementActivity.this);
                                progressDialog.getWindow().setBackgroundDrawableResource(R.drawable.roundbugreen);
                                progressDialog.setMessage("מתחיל.");
                                //progressDialog.setCancelable(false);
                                progressDialog.show();
                            }});
                    befsha1 = "";
                    aftsha1 = "";
                 
                        
                            try {
                               
                                befsha1 = getsha1(mfilepath);
                            } catch (Exception e) {
                                LogUtil.logToFile(e.toString());
                            }
                            //File tempFile = null;
                            File mselectedfile = null;
                            try {
                                // העתק את ה-URI לקובץ זמני.
                                // אם זה ZIP/APKS, זה עדיין יועתק כקובץ ZIP יחיד.
                                //tempFile = copyUriToTempFile(AppManagementActivity.this, sourceUri);
                                //LogUtil.logToFile("copied!");
                                mselectedfile = new File(mfilepath);
                                LogUtil.logToFile("selected " + mfilepath);
                                if (mselectedfile != null) {
                                    String detectedPackageName = null;
                                    File baseApkFile = null; // קובץ ה-APK הבסיסי שישמש לבדיקת שם חבילה
                                    // בדוק אם הקובץ הוא ארכיון (ZIP/APKS)
                                    //LogUtil.logToFile("3");
                                    if (mselectedfile.getName().toLowerCase().endsWith(".zip") ||
                                        mselectedfile.getName().toLowerCase().endsWith(".apks") ||
                                        mselectedfile.getName().toLowerCase().endsWith(".xapk") ||
                                        mselectedfile.getName().toLowerCase().endsWith(".apkm")) {

                                        // זהו ארכיון, חלץ את ה-APK הראשי כדי לקבל את שם החבילה
                                        File extractedTempDir = AppUpdater.createTempDir(AppManagementActivity.this); // השתמש במתודה מ-AppUpdater
                                        List<File> extractedApks = AppUpdater.extractApksFromZip(mselectedfile, extractedTempDir); // השתמש במתודה מ-AppUpdater
                                        //LogUtil.logToFile("1");
                                        if (!extractedApks.isEmpty()) {
                                            //LogUtil.logToFile("3");
                                            baseApkFile = AppUpdater.findBaseApk(AppManagementActivity.this, extractedApks); // מצא את ה-APK הבסיסי מתוך הרשימה
                                            if (baseApkFile != null) {
                                                //LogUtil.logToFile("4");
                                                detectedPackageName = AppUpdater.getApkPackageName(AppManagementActivity.this, baseApkFile.getAbsolutePath());
                                                if (detectedPackageName != null) {}
                                                else{
                                                    LogUtil.logToFile("retrying detect pn");
                                                    for (File apk : extractedApks) {
                                                        LogUtil.logToFile("for");
                                                        if (apk.getName().contains(".apk")) {
                                                            try {
                                                                detectedPackageName = AppUpdater.getApkPackageName(AppManagementActivity.this, apk.getAbsolutePath());
                                                                
                                                                if (detectedPackageName != null ) {
                                                                    LogUtil.logToFile("detected main pn - "+detectedPackageName);
                                                                    break;
                                                                }
                                                            } catch (Exception ee) {
                                                                
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            //LogUtil.logToFile("5");
                                        }
                                        //LogUtil.logToFile("6");
                                        // חשוב: נקה את התיקיה הזמנית של הקבצים המחולצים.
                                        // בדרך כלל הניקוי יקרה ב-InstallReceiver, אבל אם ההתקנה לא תצא לפועל
                                        // בגלל בדיקת סיסמה שנכשלה, צריך לנקות.
                                        // עם זאת, אם ההתקנה תצא לפועל, ה-InstallReceiver יטפל בזה.
                                        // במקרה הזה, נשאיר את הניקוי ל-InstallReceiver
                                        // או שנדאג לנקות את `extractedTempDir` אם ההתקנה מבוטלת פה.
                                        // לצורך הפשטות, נסמוך על ה-InstallReceiver לניקוי הכללי של `apks_temp`.

                                    } else if (mselectedfile.getName().toLowerCase().endsWith(".apk")) {
                                        // זהו קובץ APK בודד
                                        baseApkFile = mselectedfile; // הקובץ עצמו הוא ה-APK הבסיסי
                                        detectedPackageName = AppUpdater.getApkPackageName(AppManagementActivity.this, baseApkFile.getAbsolutePath());

                                        //LogUtil.logToFile("1");
                                    }
                                    //progressDialog.setMessage("session.");
                                    prgmsg(AppManagementActivity.this,"מצב סשן!",false);
                                    LogUtil.logToFile("now start session!");
                                    if (detectedPackageName != null) {
                                        if (isAppInstalled(detectedPackageName)) {
                                            // האפליקציה כבר מותקנת, זהו עדכון - אין צורך בסיסמה
                                            AppUpdater.startInstallSession(AppManagementActivity.this, mfilepath, mselectedfile, false); // העבר את קובץ ה-ZIP המקורי/APK בודד
                                        } else {
                                            // אפליקציה חדשה, דורש סיסמה
                                            showNewAppInstallPasswordDialog(mfilepath, mselectedfile); // העבר את קובץ ה-ZIP המקורי/APK בודד
                                        }
                                    } else {
                                        LogUtil.logToFile("not detected pkgnm");
                                        //progressDialog.setMessage("not detected pkgnm");
                                        //AppUpdater.dismissprogress(AppManagementActivity.this);
                                        prgmsg(AppManagementActivity.this,"לא נמצא שם חבילה(קובץ בעייתי או לא נתמך)",true);
                                        //Toast.makeText(AppManagementActivity.this, "לא ניתן לזהות את שם החבילה מהקובץ הנבחר.", Toast.LENGTH_LONG).show();
                                        // נקה קובץ זמני אם לא זיהינו שם חבילה
                                        //if (tempFile.exists()) tempFile.delete();
                                    }
                                } else {
                                    LogUtil.logToFile("not access to file");
                                    //progressDialog.setMessage("not access to file");
                                    //AppUpdater.dismissprogress(AppManagementActivity.this);
                                    prgmsg(AppManagementActivity.this,"אין גישה לקובץ",true);
                                    //Toast.makeText(AppManagementActivity.this, "לא ניתן לגשת לקובץ הנבחר או להעתיקו.", Toast.LENGTH_LONG).show();
                                }
                            } catch (Exception e) {
                                LogUtil.logToFile("exeption while accessing to file " + e);
                                //progressDialog.setMessage("exeption while accessing to file" + e);
                                //AppUpdater.dismissprogress(AppManagementActivity.this);
                                prgmsg(AppManagementActivity.this,"שגיאה בגישה לקובץ",true);
                                //Toast.makeText(AppManagementActivity.this, "שגיאה בטיפול בקובץ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                                //if (tempFile != null && tempFile.exists()) {
                                //tempFile.delete(); // נקה במקרה של שגיאה
                                //}
                            }
                        
                    
                }}.start();
        }
    }
    
    String mfilepath="";
    String befsha1="";
    String aftsha1="";
    
    String getsha1(String mfilename) {
        String res="";
        try {
            MessageDigest md= MessageDigest.getInstance("SHA-1");
            FileInputStream fis = new FileInputStream(new File(mfilename));
            int len;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) > 0) {
                md.update(buffer, 0, len);
            }
            byte[] bArr=md.digest();
            char[] aa = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
            char[] cArr = new char[bArr.length * 2];
            for (int i = 0; i < bArr.length; i++) {
                cArr[i * 2] = aa[(bArr[i] & 255) >>> 4];
                cArr[(i * 2) + 1] = aa[bArr[i] & 15];
            }
            res = new String(cArr);
            //LogUtil.logToFile(new String(cArr));
        } catch (Exception e) {
            LogUtil.logToFile(e.toString());
            Toast.makeText(this, "" + e, 1).show();
        }
        return res;
    }
    /*
    @Deprecated
    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        new Thread(){public void run() {
                
                getMainExecutor().execute(new Runnable(){
                        @Deprecated
                        @Override
                        public void run() {
                            progressDialog = new ProgressDialog(AppManagementActivity.this);
                            progressDialog.getWindow().setBackgroundDrawableResource(R.drawable.roundbugreen);
                            progressDialog.setMessage("מתחיל.");
                            //progressDialog.setCancelable(false);
                            progressDialog.show();
                        }});
                befsha1 = "";
                aftsha1 = "";
                if (requestCode == PICK_APK_REQUEST_CODE && resultCode == RESULT_OK) {
                    if (data != null && data.getData() != null) {
                        Uri sourceUri = data.getData();
                        try {
                            //LogUtil.logToFile(bgetFileNameFromUri(AppManagementActivity.this, data.getData()) + " " + new PathUtil().getPath(AppManagementActivity.this, data.getData()));
                            //mfilepath = new PathUtil().getPath(AppManagementActivity.this, data.getData());
                            //befsha1 = getsha1(new PathUtil().getPath(AppManagementActivity.this, data.getData()));
                        } catch (Exception e) {
                            LogUtil.logToFile(e.toString());
                        }
                        //File tempFile = null;
                        File mselectedfile = null;
                        try {
                            // העתק את ה-URI לקובץ זמני.
                            // אם זה ZIP/APKS, זה עדיין יועתק כקובץ ZIP יחיד.
                            //tempFile = copyUriToTempFile(AppManagementActivity.this, sourceUri);
                            //LogUtil.logToFile("copied!");
                            mselectedfile = new File(mfilepath);
                            LogUtil.logToFile("selected " + mfilepath);
                            if (mselectedfile != null) {
                                String detectedPackageName = null;
                                File baseApkFile = null; // קובץ ה-APK הבסיסי שישמש לבדיקת שם חבילה
                                // בדוק אם הקובץ הוא ארכיון (ZIP/APKS)
                                LogUtil.logToFile("3");
                                if (mselectedfile.getName().toLowerCase().endsWith(".zip") ||
                                    mselectedfile.getName().toLowerCase().endsWith(".apks") ||
                                    mselectedfile.getName().toLowerCase().endsWith(".xapk")) {

                                    // זהו ארכיון, חלץ את ה-APK הראשי כדי לקבל את שם החבילה
                                    File extractedTempDir = AppUpdater.createTempDir(AppManagementActivity.this); // השתמש במתודה מ-AppUpdater
                                    List<File> extractedApks = AppUpdater.extractApksFromZip(mselectedfile, extractedTempDir); // השתמש במתודה מ-AppUpdater
                                    LogUtil.logToFile("1");
                                    if (!extractedApks.isEmpty()) {
                                        LogUtil.logToFile("3");
                                        baseApkFile = AppUpdater.findBaseApk(AppManagementActivity.this, extractedApks); // מצא את ה-APK הבסיסי מתוך הרשימה
                                        if (baseApkFile != null) {
                                            LogUtil.logToFile("4");
                                            detectedPackageName = AppUpdater.getApkPackageName(AppManagementActivity.this, baseApkFile.getAbsolutePath());
                                        }
                                        LogUtil.logToFile("5");
                                    }
                                    LogUtil.logToFile("6");
                                    // חשוב: נקה את התיקיה הזמנית של הקבצים המחולצים.
                                    // בדרך כלל הניקוי יקרה ב-InstallReceiver, אבל אם ההתקנה לא תצא לפועל
                                    // בגלל בדיקת סיסמה שנכשלה, צריך לנקות.
                                    // עם זאת, אם ההתקנה תצא לפועל, ה-InstallReceiver יטפל בזה.
                                    // במקרה הזה, נשאיר את הניקוי ל-InstallReceiver
                                    // או שנדאג לנקות את `extractedTempDir` אם ההתקנה מבוטלת פה.
                                    // לצורך הפשטות, נסמוך על ה-InstallReceiver לניקוי הכללי של `apks_temp`.

                                } else if (mselectedfile.getName().toLowerCase().endsWith(".apk")) {
                                    // זהו קובץ APK בודד
                                    baseApkFile = mselectedfile; // הקובץ עצמו הוא ה-APK הבסיסי
                                    detectedPackageName = AppUpdater.getApkPackageName(AppManagementActivity.this, baseApkFile.getAbsolutePath());

                                    LogUtil.logToFile("1");
                                }
                                //progressDialog.setMessage("session.");
                                prgmsg(AppManagementActivity.this,"מצב סשן!",false);
                                LogUtil.logToFile("now start session!");
                                if (detectedPackageName != null) {
                                    if (isAppInstalled(detectedPackageName)) {
                                        // האפליקציה כבר מותקנת, זהו עדכון - אין צורך בסיסמה
                                        AppUpdater.startInstallSession(AppManagementActivity.this, mfilepath, mselectedfile, false); // העבר את קובץ ה-ZIP המקורי/APK בודד
                                    } else {
                                        // אפליקציה חדשה, דורש סיסמה
                                        showNewAppInstallPasswordDialog(mfilepath, mselectedfile); // העבר את קובץ ה-ZIP המקורי/APK בודד
                                    }
                                } else {
                                    LogUtil.logToFile("not detected pkgnm");
                                    //progressDialog.setMessage("not detected pkgnm");
                                    //AppUpdater.dismissprogress(AppManagementActivity.this);
                                    prgmsg(AppManagementActivity.this,"not detected pkgnm",true);
                                    //Toast.makeText(AppManagementActivity.this, "לא ניתן לזהות את שם החבילה מהקובץ הנבחר.", Toast.LENGTH_LONG).show();
                                    // נקה קובץ זמני אם לא זיהינו שם חבילה
                                    //if (tempFile.exists()) tempFile.delete();
                                }
                            } else {
                                LogUtil.logToFile("not access to file");
                                //progressDialog.setMessage("not access to file");
                                //AppUpdater.dismissprogress(AppManagementActivity.this);
                                prgmsg(AppManagementActivity.this,"not access to file",true);
                                //Toast.makeText(AppManagementActivity.this, "לא ניתן לגשת לקובץ הנבחר או להעתיקו.", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            LogUtil.logToFile("exeption while accessing to file " + e);
                            //progressDialog.setMessage("exeption while accessing to file" + e);
                            //AppUpdater.dismissprogress(AppManagementActivity.this);
                            prgmsg(AppManagementActivity.this,"exeption while accessing to file",true);
                            //Toast.makeText(AppManagementActivity.this, "שגיאה בטיפול בקובץ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                            //if (tempFile != null && tempFile.exists()) {
                            //tempFile.delete(); // נקה במקרה של שגיאה
                            //}
                        }
                    }
                } else {
                    //AppUpdater.dismissprogress(AppManagementActivity.this);
                    prgmsg(AppManagementActivity.this,"cancel!",true);
                }

            }}.start();
    }*/

    private boolean isAppInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private void showNewAppInstallPasswordDialog(final String mfilepath, final File apkFile) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(AppManagementActivity.this);
        builder.setTitle("התקנת אפליקציה חדשה");
        builder.setMessage("התקנת אפליקציה חדשה דורשת אימות סיסמה. האם ברצונך להמשיך?");

        builder.setPositiveButton("כן, המשך", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    PasswordManager.requestPasswordAndSave(new Runnable() {
                            @Override
                            public void run() {
                                // אם הסיסמה נכונה, התחל את ההתקנה
                                new Thread(){public void run() {
                                        //LogUtil.logToFile("inn 1");
                                        prgmsg(AppManagementActivity.this,"אפליקציה חדשה!",false);
                                        AppUpdater.startInstallSession(AppManagementActivity.this, mfilepath, apkFile, true); // true = password already checked
                                        //LogUtil.logToFile("inn 2");
                                        
                                    }}.start();
                            }
                        }, AppManagementActivity.this);
                }
            });
        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Toast.makeText(AppManagementActivity.this, "התקנת האפליקציה בוטלה.", Toast.LENGTH_SHORT).show();
                    //AppUpdater.dismissprogress(AppManagementActivity.this);
                    prgmsg(AppManagementActivity.this,"ביטול!",true);
                    // נקה את הקובץ הזמני אם ההתקנה בוטלה
                    //if (apkFile != null && apkFile.exists()) {
                    //apkFile.delete();
                    //}
                }
            });
        getMainExecutor().execute(new Runnable(){

                @Override
                public void run() {
                    builder.show();
                }
            });

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
    
/*
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
    */
        @Deprecated
        static void prgmsg(final Context context,final String msg,final boolean end){
        //context.getMainLooper().prepare();
        context. getMainExecutor().execute(new Runnable(){
                @Deprecated
                @Override
                public void run() {
                    if(progressDialog!=null){
                        if(progressDialog.isShowing()){
                            progressDialog.dismiss();
                        }
                    }
                    progressDialog = new ProgressDialog(context);
                    progressDialog.getWindow().setBackgroundDrawableResource(R.drawable.roundbugreen);
                    progressDialog.setMessage("מתחיל.");
                    //progressDialog.setCancelable(false);
                    progressDialog.show();
                    progressDialog.setMessage(msg);
                    if(end){
                        new Handler().postDelayed(new Runnable(){
                                @Deprecated
                                @Override
                                public void run() {
                                    if(progressDialog!=null){
                                        if(progressDialog.isShowing()){
                                            progressDialog.dismiss();
                                        }
                                    }
                                }
                            }, 5000);
                    }
                }});


        //context.getMainLooper().loop();
    }
}
