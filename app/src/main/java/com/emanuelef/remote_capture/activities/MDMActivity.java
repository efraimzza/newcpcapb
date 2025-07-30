package com.emanuelef.remote_capture.activities;

import android.app.Activity;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import android.os.Build;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PackageManager; // לצורך הדיאלוג
import com.emanuelef.remote_capture.R;

public class MDMActivity extends Activity {

    private DevicePolicyManager mDpm;
    private ComponentName mAdminComponentName;
    private ListView lvRestrictions;
    private RestrictionListAdapter mAdapter;
    private List<RestrictionItem> mRestrictionList;
    Context mcon=this;
    // קבוע עבור הפריט המיוחד של ניהול אפליקציות
    
    private static final int REQUEST_CODE_ENABLE_ADMIN = 1; // קוד לבקשת הפעלת אדמין
    public static MDMActivity mlive=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mlive=this;
        setContentView(R.layout.activity_mdm_listview); // קובץ XML עבור ListView
       /* if(getIntent().getStringExtra("recheck")!=null){
            if(getIntent().getStringExtra("recheck").equals("recheck")){
                //finish();
                startActivity(new Intent(this,MDMActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        }*/
        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = new ComponentName(this, admin.class);
        
        // וודא שהאפליקציה היא אדמין
        //requestDeviceAdmin();
        try {
            if (mDpm.isDeviceOwnerApp(getPackageName())) {
                lvRestrictions = (ListView) findViewById(R.id.lv_restrictions);
                loadRestrictions();


                Button btnSaveAll = (Button) findViewById(R.id.btn_save_all);
                btnSaveAll.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            PasswordManager.requestPasswordAndSave(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            applyAllRestrictions();
                                        } catch (Exception e) {
                                            Toast.makeText(MDMActivity. this, "" + e, 1).show();
                                        }
                                    }
                               },MDMActivity.this);

                        }
                    });

            } else {
                Toast.makeText(this, "לא מנהל מכשיר", 1).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "" + e, 1).show();
        }
        
        Button btnManageApps = (Button) findViewById(R.id.btn_manage_apps); // וודא ש-ID זה קיים ב-XML
        if (btnManageApps != null) {
            btnManageApps.setOnClickListener(new View.OnClickListener() {
                    @Deprecated
                    @Override
                    public void onClick(View v) {
                        //requestPasswordAndSave(new Runnable() {
                         //       @Override
                        //        public void run() {
                                    Intent intent = new Intent(MDMActivity.this, AppManagementActivity.class);
                                    startActivity(intent);
                        //        }
                         //   });
                    }
                });
        }
    }
    @Deprecated
    private void loadRestrictions() {
        mRestrictionList = new ArrayList<RestrictionItem>();

        String[][] restr={
            {"vpn","21","הפעלת vpn","",""+R.drawable.ic_restriction_vpn},
            {"um","20","השבתת הגדרת נקודה חמה",UserManager.DISALLOW_CONFIG_TETHERING,""+R.drawable.ic_restriction_tethering},
            {"um","20","השבתת הגדרת VPN",UserManager.DISALLOW_CONFIG_VPN,""+R.drawable.ic_restriction_vpn},
            {"um","17","השבתת הגדרת Wi-Fi",UserManager.DISALLOW_CONFIG_WIFI,""+R.drawable.ic_restriction_wifi},
            {"um","17","השבתת התקנת אפליקציות",UserManager.DISALLOW_INSTALL_APPS,""+R.drawable.ic_restriction_install_apps},
            {"um","17","השבתת התקנה ממקורות לא ידועים",UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES,""+R.drawable.ic_restriction_unknown_sources},
            {"um","20","מצב מפתחים",UserManager.DISALLOW_DEBUGGING_FEATURES,""+R.drawable.ic_restriction_developer_mode},
            {"um","20","איפוס להגדרות יצרן",UserManager.DISALLOW_FACTORY_RESET,""+R.drawable.ic_restriction_factory_reset},
            {"um","25","הוספת משתמש בבעלות",UserManager.DISALLOW_ADD_MANAGED_PROFILE,""+R.drawable.ic_restriction_add_user},
            {"um","27","החלפת משתמש",UserManager.DISALLOW_USER_SWITCH,""+R.drawable.ic_restriction_add_user},
            {"um","20","הוספת משתמשים",UserManager.DISALLOW_ADD_USER,""+R.drawable.ic_restriction_add_user},
            {"um","25","בלוטות",UserManager.DISALLOW_BLUETOOTH,""+R.drawable.ic_restriction_bluetooth},
            {"um","17","שינוי בלוטות",UserManager.DISALLOW_CONFIG_BLUETOOTH,""+R.drawable.ic_restriction_bluetooth},
            {"um","28","dns",UserManager.DISALLOW_CONFIG_PRIVATE_DNS,""+R.drawable.ic_restriction_dns},
            {"um","17","הסרת התקנה",UserManager.DISALLOW_UNINSTALL_APPS,""+R.drawable.ic_restriction_uninstall_apps},
            {"um","20","אס אם אס (sms)",UserManager.DISALLOW_SMS,""+R.drawable.ic_restriction_sms},
            {"um","25","שיתוף בלוטות",UserManager.DISALLOW_BLUETOOTH_SHARING,""+R.drawable.ic_restriction_bluetooth},
            {"um","27","זמן",UserManager.DISALLOW_CONFIG_DATE_TIME,""+R.drawable.ic_restriction_date_time},
            {"um","22","מצב בטוח",UserManager.DISALLOW_SAFE_BOOT,""+R.drawable.ic_restriction_safe_boot},
            {"um","20","שיחות יוצאות",UserManager.DISALLOW_OUTGOING_CALLS,""+R.drawable.ic_restriction_outgoing_calls},
            {"um","20","שינוי רשת סלולרית",UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS,""+R.drawable.ic_restriction_mobile_networks},
            {"um","17","הסרת משתמש",UserManager.DISALLOW_REMOVE_USER,""+R.drawable.ic_restriction_remove_user},
            {"um","20","שליטה באפליקציות",UserManager.DISALLOW_APPS_CONTROL,""+R.drawable.ic_restriction_apps_control},
            {"um","23","נדידת נתונים",UserManager.DISALLOW_DATA_ROAMING,""+R.drawable.ic_restriction_data_roaming},
            {"um","17","usb",UserManager.DISALLOW_USB_FILE_TRANSFER,""+R.drawable.ic_restriction_usb_file_transfer}

        };

        for (String[] ret:restr) {
            if (ret[0].equals("um")) {  //um - usermanager
                if (Build.VERSION.SDK_INT > Integer.parseInt(ret[1]))
                    mRestrictionList.add(new RestrictionItem(
                                             ret[0],
                                             ret[2],
                                             getDescriptionForKey( ret[3]),
                                             ret[3],
                                             mDpm.getUserRestrictions(mAdminComponentName).getBoolean(ret[3]),
                                             Integer.parseInt( ret[4])));
            } else {
                if (ret[0].equals("list")) {
                    mRestrictionList.add(new RestrictionItem(
                                             ret[0],
                                             ret[2],
                                             getDescriptionForKey( ret[3]),
                                             ret[3], // מפתח מיוחד עבור פריט זה
                                             false));
                } else if (ret[0].equals("vpn")) { // vpn key
                    if (Build.VERSION.SDK_INT > Integer.parseInt(ret[1]))
                        mRestrictionList.add(new RestrictionItem(
                                                 ret[0],
                                                 ret[2],
                                                 getDescriptionForKey( ret[3]),
                                                 ret[3],
                                                 mDpm.isAlwaysOnVpnLockdownEnabled(mAdminComponentName)));
                }
            }
        }

        // הוספת הגבלות למערך
        // נבדוק את הסטטוס הנוכחי של ההגבלות במכשיר כדי לאתחל את ה-CheckBoxes


        mAdapter = new RestrictionListAdapter(this, mRestrictionList);
        lvRestrictions.setAdapter(mAdapter);

        // טיפול בלחיצות על פריטי ה-ListView (במיוחד עבור "ניהול אפליקציות")
        lvRestrictions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    RestrictionItem item = mRestrictionList.get(position);

                }
            });
    }
    private String getDescriptionForKey(String key) {
        switch (key) {
            case UserManager.DISALLOW_CONFIG_TETHERING: return "מונע מהמשתמש להפעיל נקודה חמה ניידת.";
            case UserManager.DISALLOW_CONFIG_VPN: return "מונע מהמשתמש להגדיר חיבורי VPN.";
            case UserManager.DISALLOW_CONFIG_WIFI: return "מונע מהמשתמש לשנות את הגדרות ה-Wi-Fi.";
            case UserManager.DISALLOW_INSTALL_APPS: return "מונע התקנת אפליקציות חדשות על המכשיר.";
            case UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES: return "מונע התקנת אפליקציות ממקורות שאינם חנות האפליקציות הרשמית.";
            case UserManager.DISALLOW_DEBUGGING_FEATURES: return "מונע שימוש בתכונות איתור באגים (כמו USB debugging).";
            case UserManager.DISALLOW_FACTORY_RESET: return "מונע איפוס המכשיר להגדרות היצרן.";
            case UserManager.DISALLOW_ADD_MANAGED_PROFILE: return "מונע הוספת פרופילים מנוהלים חדשים (לדוגמה, פרופיל עבודה).";
            case UserManager.DISALLOW_USER_SWITCH: return "מונע החלפה בין משתמשים שונים במכשיר.";
            case UserManager.DISALLOW_ADD_USER: return "מונע הוספת משתמשים חדשים למכשיר.";
            case UserManager.DISALLOW_BLUETOOTH: return "משבית את תכונת ה-Bluetooth במכשיר.";
            case UserManager.DISALLOW_CONFIG_BLUETOOTH: return "מונע מהמשתמש לשנות את הגדרות ה-Bluetooth.";
            case UserManager.DISALLOW_CONFIG_PRIVATE_DNS: return "מונע שינוי הגדרות DNS פרטי.";
            case UserManager.DISALLOW_UNINSTALL_APPS: return "מונע הסרת התקנה של אפליקציות.";
            case UserManager.DISALLOW_SMS: return "מונע שליחה וקבלה של הודעות SMS.";
            case UserManager.DISALLOW_BLUETOOTH_SHARING: return "מונע שיתוף קבצים באמצעות Bluetooth.";
            case UserManager.DISALLOW_CONFIG_DATE_TIME: return "מונע שינוי תאריך ושעה במכשיר.";
            case UserManager.DISALLOW_SAFE_BOOT: return "מונע כניסה למצב בטוח (Safe Mode).";
            case UserManager.DISALLOW_OUTGOING_CALLS: return "מונע ביצוע שיחות יוצאות.";
            case UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS: return "מונע שינוי הגדרות רשת סלולרית.";
            case UserManager.DISALLOW_REMOVE_USER: return "מונע הסרת משתמשים מהמכשיר.";
            case UserManager.DISALLOW_APPS_CONTROL: return "מונע גישה והתאמה אישית של הגדרות אפליקציות.";
            case UserManager.DISALLOW_DATA_ROAMING: return "מונע שימוש בנדידת נתונים (roaming).";
            case UserManager.DISALLOW_USB_FILE_TRANSFER: return "מונע העברת קבצים באמצעות כבל USB.";
            default: return "תיאור הגבלה לא ידוע.";
        }
    }
    private void applyAllRestrictions() {
        for (RestrictionItem item : mRestrictionList) {
            // נתעלם מפריט ניהול האפליקציות כי הוא לא הגבלה ישירה
            //if (!APP_MANAGEMENT_ITEM_KEY.equals(item.getRestrictionKey())) {
            if (item.getType().equals("um")) {
                if (item.isEnabled()) {
                    mDpm.addUserRestriction(mAdminComponentName, item.getKey());
                } else {
                    mDpm.clearUserRestriction(mAdminComponentName, item.getKey());
                }
            } else if (item.getType().equals("vpn")) {

                try {
                    mDpm.setAlwaysOnVpnPackage(mAdminComponentName, item.isEnabled() ? getPackageName(): null, true);
                } catch (PackageManager.NameNotFoundException e) {}

            }
        }
        Toast.makeText(this, "ההגבלות נשמרו!", Toast.LENGTH_SHORT).show();
    }

    // וודא שאתה מטפל בהפעלת האדמין (Device Admin)
    // אם האפליקציה לא אדמין, בקש מהמשתמש להפעיל אותה.
    private void requestDeviceAdmin() {
        if (!mDpm.isAdminActive(mAdminComponentName)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "יישום זה דורש הרשאות מנהל מכשיר לניהול הגדרות.");
            startActivityForResult(intent, REQUEST_CODE_ENABLE_ADMIN);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ENABLE_ADMIN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "הרשאות מנהל מכשיר הופעלו!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "הרשאות מנהל מכשיר לא הופעלו. חלק מהפונקציות לא יפעלו.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mlive=null;
    }
    static void finres(Activity act){
        act.finish();
    }
}
