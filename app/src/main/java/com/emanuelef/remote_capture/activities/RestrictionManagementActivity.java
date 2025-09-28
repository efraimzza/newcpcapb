package com.emanuelef.remote_capture.activities;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.UserManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import android.content.pm.PackageManager;
import android.content.SharedPreferences;
import com.emanuelef.remote_capture.R;

public class RestrictionManagementActivity extends Activity {

    private DevicePolicyManager mDpm;
    private ComponentName mAdminComponentName;
    private List<RestrictionItem> mRestrictionList;
    private RestrictionListAdapter mAdapter;
    private ListView lvRestrictions;
    SharedPreferences sp;
    SharedPreferences.Editor spe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restriction_management); // שם קובץ layout חדש
        
        sp = this.getSharedPreferences(this.getPackageName(), this.MODE_PRIVATE);
        spe = sp.edit();
        
        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = new ComponentName(this, admin.class);
        if(mDpm.isDeviceOwnerApp(getPackageName())){
        lvRestrictions = findViewById(R.id.lv_restrictions);
        loadRestrictions();
        mAdapter = new RestrictionListAdapter(this, mRestrictionList);
        lvRestrictions.setAdapter(mAdapter);

        Button btnSaveRestrictions = findViewById(R.id.btn_save_restrictions);
        btnSaveRestrictions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    PasswordManager.requestPasswordAndSave(new Runnable(){

                            @Override
                            public void run() {
                                applyRestrictions();
                            }
                        }, RestrictionManagementActivity.this);
                    
                }
            });
            }
    }
    @Deprecated
    private void loadRestrictions() {
        mRestrictionList = new ArrayList<>();

        // Helper method to add restriction if API level is met
        // addRestrictionIfApplicable(String name, String key, int minApi, boolean isEnabled, int iconResId)
        
        boolean vpnenabled=false;
        String strpkgvpn= mDpm.getAlwaysOnVpnPackage(mAdminComponentName);
        if(strpkgvpn!=null){
            vpnenabled=strpkgvpn.equals(getPackageName());
        }
        if (Build.VERSION.SDK_INT >= 24) {
            mRestrictionList.add(new RestrictionItem("",
                                     "הפעלת VPN תמידי",
                                     "מונע עקיפת VPN על ידי חסימת כל תעבורת הרשת מחוץ ל-VPN.",
                                     "DISALLOW_ALWAYS_ON_VPN", // זה לא UserManager restriction key, תצטרך לטפל בזה באופן נפרד ב-applyRestrictions
                                     vpnenabled,
                                     R.drawable.ic_restriction_vpn
                                 ));
        }
        addRestrictionIfApplicable(
            "השבתת הגדרת VPN", UserManager.DISALLOW_CONFIG_VPN, 20,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_CONFIG_VPN),
            R.drawable.ic_restriction_vpn
        );
        addRestrictionIfApplicable(
            "שינוי זמן", UserManager.DISALLOW_CONFIG_DATE_TIME, 27,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_CONFIG_DATE_TIME),
            R.drawable.ic_restriction_date_time
        );
        addRestrictionIfApplicable(
            "השבתת הגדרת נקודה חמה", UserManager.DISALLOW_CONFIG_TETHERING, 20,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_CONFIG_TETHERING),
            R.drawable.ic_restriction_tethering
        );
        
        addRestrictionIfApplicable(
            "השבתת הגדרת Wi-Fi", UserManager.DISALLOW_CONFIG_WIFI, 17,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_CONFIG_WIFI),
            R.drawable.ic_restriction_wifi
        );
                
        addRestrictionIfApplicable(
            "מצב מפתחים", UserManager.DISALLOW_DEBUGGING_FEATURES, 20,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_DEBUGGING_FEATURES),
            R.drawable.ic_restriction_developer_mode
        );
        addRestrictionIfApplicable(
            "השבתת התקנת אפליקציות", UserManager.DISALLOW_INSTALL_APPS, 17,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_INSTALL_APPS),
            R.drawable.ic_restriction_install_apps
        );
        addRestrictionIfApplicable(
            "השבתת התקנה ממקורות לא ידועים", UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, 17,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES),
            R.drawable.ic_restriction_unknown_sources
        );

        addRestrictionIfApplicable(
            "הסרת התקנה", UserManager.DISALLOW_UNINSTALL_APPS, 17,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_UNINSTALL_APPS),
            R.drawable.ic_restriction_uninstall_apps
        );
        addRestrictionIfApplicable(
            "שליטה באפליקציות", UserManager.DISALLOW_APPS_CONTROL, 20,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_APPS_CONTROL),
            R.drawable.ic_restriction_apps_control
        );
        addRestrictionIfApplicable(
            "איפוס להגדרות יצרן", UserManager.DISALLOW_FACTORY_RESET, 20,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_FACTORY_RESET),
            R.drawable.ic_restriction_factory_reset
        );
        addRestrictionIfApplicable(
            "הוספת משתמש בבעלות", UserManager.DISALLOW_ADD_MANAGED_PROFILE, 25,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_ADD_MANAGED_PROFILE),
            R.drawable.ic_restriction_add_user // ניתן להשתמש באותו אייקון להוספת משתמש
        );
        addRestrictionIfApplicable(
            "החלפת משתמש", UserManager.DISALLOW_USER_SWITCH, 27,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_USER_SWITCH),
            R.drawable.ic_restriction_add_user // ניתן להשתמש באותו אייקון
        );
        addRestrictionIfApplicable(
            "הוספת משתמשים", UserManager.DISALLOW_ADD_USER, 20,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_ADD_USER),
            R.drawable.ic_restriction_add_user
        );
        addRestrictionIfApplicable(
            "מצב בטוח", UserManager.DISALLOW_SAFE_BOOT, 22,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_SAFE_BOOT),
            R.drawable.ic_restriction_safe_boot
        );
        addRestrictionIfApplicable(
            "הסרת משתמש", UserManager.DISALLOW_REMOVE_USER, 17,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_REMOVE_USER),
            R.drawable.ic_restriction_remove_user
        );     
        
        addRestrictionIfApplicable(
            "השבתת בלוטות'", UserManager.DISALLOW_BLUETOOTH, 25,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_BLUETOOTH),
            R.drawable.ic_restriction_bluetooth
        );
        addRestrictionIfApplicable(
            "שינוי בלוטות'", UserManager.DISALLOW_CONFIG_BLUETOOTH, 17,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_CONFIG_BLUETOOTH),
            R.drawable.ic_restriction_bluetooth
        );
        addRestrictionIfApplicable(
            "שיתוף בלוטות'", UserManager.DISALLOW_BLUETOOTH_SHARING, 25,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_BLUETOOTH_SHARING),
            R.drawable.ic_restriction_bluetooth // ניתן להשתמש באותו אייקון
        );
        addRestrictionIfApplicable(
            "השבתת DNS פרטי", UserManager.DISALLOW_CONFIG_PRIVATE_DNS, 28,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_CONFIG_PRIVATE_DNS),
            R.drawable.ic_restriction_dns
        );

        addRestrictionIfApplicable(
            "אס אם אס (SMS)", UserManager.DISALLOW_SMS, 20,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_SMS),
            R.drawable.ic_restriction_sms
        );


        addRestrictionIfApplicable(
            "שיחות יוצאות", UserManager.DISALLOW_OUTGOING_CALLS, 20,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_OUTGOING_CALLS),
            R.drawable.ic_restriction_outgoing_calls
        );
        addRestrictionIfApplicable(
            "שינוי רשת סלולרית", UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS, 20,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS),
            R.drawable.ic_restriction_mobile_networks
        );

        
        addRestrictionIfApplicable(
            "נדידת נתונים", UserManager.DISALLOW_DATA_ROAMING, 23,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_DATA_ROAMING),
            R.drawable.ic_restriction_data_roaming
        );
        addRestrictionIfApplicable(
            "העברת קבצי USB", UserManager.DISALLOW_USB_FILE_TRANSFER, 17,
            mDpm.getUserRestrictions(mAdminComponentName).getBoolean(UserManager.DISALLOW_USB_FILE_TRANSFER),
            R.drawable.ic_restriction_usb_file_transfer
        );
        mRestrictionList.add(new RestrictionItem("",
                                  "השבתת מצלמה",
                                  "מונע אפשרות צילום והסרטה",
                                  "DISALLOW_CAMERA",
                                  mDpm.getCameraDisabled(mAdminComponentName),
                                  R.drawable.ic_restriction_vpn
                               ));
        try{
           if (Build.VERSION.SDK_INT >= 23) {
                mRestrictionList.add(new RestrictionItem("",
                                   "השבתת סטטוס בר",
                                   "מונע אפשרות לראות סטטוס בר",
                                   "DISALLOW_STATUSBAR",
                                   sp.getBoolean("dis_statusbar",false),
                                   R.drawable.ic_restriction_vpn
                                ));
           }
        }catch(Exception e){
            Toast.makeText(this, "c"+e, Toast.LENGTH_SHORT).show();
        }
        
    }

    private void addRestrictionIfApplicable(String name, String key, int minApi, boolean isEnabled, int iconResId) {
        if (Build.VERSION.SDK_INT >= minApi) {
            mRestrictionList.add(new RestrictionItem("",name, getDescriptionForKey(key), key, isEnabled, iconResId));
        }
    }
    @Deprecated
    private String getDescriptionForKey(String key) {
        // ... (המתודה הקיימת, ללא שינוי)
        switch (key) {
            case UserManager.DISALLOW_CONFIG_TETHERING: return "מונע מהמשתמש להגדיר נקודה חמה (Hotspot).";
            case UserManager.DISALLOW_CONFIG_VPN: return "מונע מהמשתמש להגדיר חיבורי VPN.";
            case UserManager.DISALLOW_CONFIG_WIFI: return "מונע מהמשתמש להגדיר חיבורי Wi-Fi.";
            case UserManager.DISALLOW_INSTALL_APPS: return "מונע מהמשתמש להתקין אפליקציות.";
            case UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES: return "מונע התקנת אפליקציות ממקורות לא ידועים (מחוץ ל-Google Play).";
            case UserManager.DISALLOW_DEBUGGING_FEATURES: return "מגביל גישה לאפשרויות מפתחים וניפוי באגים.";
            case UserManager.DISALLOW_FACTORY_RESET: return "מונע מהמשתמש לבצע איפוס להגדרות יצרן.";
            case UserManager.DISALLOW_ADD_MANAGED_PROFILE: return "מונע הוספת פרופיל מנוהל חדש.";
            case UserManager.DISALLOW_USER_SWITCH: return "מונע מעבר בין משתמשים קיימים.";
            case UserManager.DISALLOW_ADD_USER: return "מונע הוספת משתמשים חדשים למכשיר.";
            case UserManager.DISALLOW_BLUETOOTH: return "משבית את פונקציונליות הבלוטות'.";
            case UserManager.DISALLOW_CONFIG_BLUETOOTH: return "מונע מהמשתמש לשנות הגדרות בלוטות'.";
            case UserManager.DISALLOW_CONFIG_PRIVATE_DNS: return "מונע מהמשתמש להגדיר DNS פרטי.";
            case UserManager.DISALLOW_UNINSTALL_APPS: return "מונע הסרת התקנה של אפליקציות.";
            case UserManager.DISALLOW_SMS: return "משבית פונקציונליות שליחת/קבלת הודעות SMS.";
            case UserManager.DISALLOW_BLUETOOTH_SHARING: return "מונע שיתוף קבצים באמצעות בלוטות'.";
            case UserManager.DISALLOW_CONFIG_DATE_TIME: return "מונע מהמשתמש לשנות את התאריך והשעה במכשיר.";
            case UserManager.DISALLOW_SAFE_BOOT: return "מונע הפעלת המכשיר במצב בטוח (Safe Mode).";
            case UserManager.DISALLOW_OUTGOING_CALLS: return "מונע ביצוע שיחות יוצאות מהמכשיר.";
            case UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS: return "מונע מהמשתמש לשנות הגדרות רשת סלולרית.";
            case UserManager.DISALLOW_REMOVE_USER: return "מונע הסרת משתמשים מהמכשיר.";
            case UserManager.DISALLOW_APPS_CONTROL: return "מונע גישה להגדרות בקרת אפליקציות.";
            case UserManager.DISALLOW_DATA_ROAMING: return "מונע הפעלת נדידת נתונים סלולרית.";
            case UserManager.DISALLOW_USB_FILE_TRANSFER: return "מונע העברת קבצים באמצעות חיבור USB.";
            case "DISALLOW_ALWAYS_ON_VPN": return "כופה שימוש ב-VPN תמידי, מונע תעבורה מחוץ ל-VPN.";
            default: return "תיאור לא זמין.";
        }
    }

    private void applyRestrictions() {
        for (RestrictionItem item : mRestrictionList) {
            // טיפול מיוחד עבור DISALLOW_ALWAYS_ON_VPN מכיוון שהוא לא UserManager restriction
            if (item.getKey().equals("DISALLOW_ALWAYS_ON_VPN")) {
                if (Build.VERSION.SDK_INT >= 24) {
                    try {
                        mDpm.setAlwaysOnVpnPackage(mAdminComponentName, item.isEnabled() ? getPackageName() : null, item.isEnabled());
                        // שנה ל-package name של אפליקציית ה-VPN שלך!
                        //Toast.makeText(this, "הגדרת VPN תמידי: " + (item.isEnabled() ? "הופעל" : "בוטל"), Toast.LENGTH_SHORT).show();
                    } catch (PackageManager.NameNotFoundException e) {
                        Toast.makeText(this, "שגיאה: אפליקציית ה-VPN לא נמצאה.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    //Toast.makeText(this, "אפשרות VPN תמידי זמינה מ-Android 7.0 (API 24) ואילך.", Toast.LENGTH_SHORT).show();
                }
            } else if (item.getKey().equals("DISALLOW_CAMERA")) {
                if (Build.VERSION.SDK_INT >= 14) {
                    try {
                        mDpm.setCameraDisabled(mAdminComponentName, item.isEnabled());
                    } catch (Exception e) {
                        Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
                    }
                }
            } else if (item.getKey().equals("DISALLOW_STATUSBAR")) {
                if (Build.VERSION.SDK_INT >= 23) {
                    try {
                        mDpm.setStatusBarDisabled(mAdminComponentName, item.isEnabled());
                        spe.putBoolean("dis_statusbar",item.isEnabled());
                        spe.commit();
                    } catch (Exception e) {
                        Toast.makeText(this, ""+e, Toast.LENGTH_SHORT).show();
                    }
                }
            }
            else {
                // עבור שאר הגבלות UserManager
                if (item.isEnabled()) {
                    mDpm.addUserRestriction(mAdminComponentName, item.getKey());
                } else {
                    mDpm.clearUserRestriction(mAdminComponentName, item.getKey());
                }
            }
        }
        Toast.makeText(this, "ההגבלות נשמרו בהצלחה!", Toast.LENGTH_SHORT).show();
    }
}
