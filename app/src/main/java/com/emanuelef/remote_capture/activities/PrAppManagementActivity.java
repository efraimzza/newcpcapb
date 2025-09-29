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
public class PrAppManagementActivity extends Activity {

    private DevicePolicyManager mDpm;
    private ComponentName mAdminComponentName;
    private ListView lvApps;
    private List<AppItem> mOriginalAppList;
    private List<AppItem> mFilteredAppList;
    private PrAppListAdapter mAdapter;

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
        setContentView(R.layout.practivity_app_management);

        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = new ComponentName(this, admin.class);

        lvApps = (ListView) findViewById(R.id.lv_apps);

        // טען את הרשימה באופן אסינכרוני
        new LoadAppsTask().execute();

        mFilteredAppList = new ArrayList<AppItem>();
        mAdapter = new PrAppListAdapter(this, mFilteredAppList);
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
                    //Toast.makeText(PrAppManagementActivity.this, "רשימת אפליקציות עודכנה.", Toast.LENGTH_SHORT).show();
                }
            });
        Button btnSaveAppChanges = (Button) findViewById(R.id.btn_save_app_changes);
        btnSaveAppChanges.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PrPasswordManager.requestPasswordAndSave(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                    applyAppVisibilityChanges();
                                }catch(Exception e){}
                            }
                        },PrAppManagementActivity.this);
                }
            });

        Button btnchangepwd = (Button) findViewById(R.id.btn_change_pwd);
        if (btnchangepwd != null) {
            btnchangepwd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        PrPasswordManager.requestPasswordAndSave(new Runnable() {
                            @Override
                            public void run() {
                                try{
                                     PrPasswordManager.showSetPasswordDialog(PrAppManagementActivity.this);
                                }catch(Exception e){}
                            }
                        },PrAppManagementActivity.this);
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
            progressDialog = new ProgressDialog(PrAppManagementActivity.this);
            progressDialog.getWindow().setBackgroundDrawableResource(R.drawable.roundbugreen);
            progressDialog.setMessage("טוען רשימת אפליקציות משתמש...");
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
                if((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
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
        View dialogView = inflater.inflate(R.layout.prdialog_filter_options, null);
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
    

    private boolean isAppInstalled(String packageName) {
        try {
            getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
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
        Toast.makeText(PrAppManagementActivity.this, "שינויים באפליקציות נשמרו!", Toast.LENGTH_SHORT).show();
        // רענן את הרשימה לאחר שמירה כדי לשקף שינויים (לדוגמה, בסינון 'מוסתרות')
        // טען מחדש את הרשימה המקורית מהמערכת
        new LoadAppsTask().execute();
        applyFiltersAndSort(); // סנן ומיין אותה מחדש
    }
    
    
    
}
