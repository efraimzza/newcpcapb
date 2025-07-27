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
        Button btnStartMdm = (Button) findViewById(R.id.btn_start_mdm);
        btnStartMdm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    
                }
            });
        Button btnRemoveMdm = (Button) findViewById(R.id.btn_remove_mdm);
        btnRemoveMdm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        mDpm.clearDeviceOwnerApp(getPackageName());
                    } catch (Exception e) {
                        Toast.makeText(MDMActivity. this, "" + e, 1).show();
                    }
                    try {
                        mDpm.removeActiveAdmin(mAdminComponentName);
                    } catch (Exception e) {
                        Toast.makeText(MDMActivity. this, "" + e, 1).show();
                    }
                }
            });
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
                            requestPasswordAndSave(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            applyAllRestrictions();
                                        } catch (Exception e) {
                                            Toast.makeText(MDMActivity. this, "" + e, 1).show();
                                        }
                                    }
                                });

                        }
                    });

            } else {
                Toast.makeText(this, "is not owner", 1).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "" + e, 1).show();
        }
        Button btnChangePassword = (Button) findViewById(R.id.btn_change_password); // הוסף כפתור כזה ל-layout
        if (btnChangePassword != null) {
            btnChangePassword.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestPasswordAndSave(new Runnable() {
                                @Override
                                public void run() {
                                 
                        showSetPasswordDialog(); // פותח את דיאלוג הגדרת/שינוי הסיסמה
                        }
                        });
                    }
                });
        }
        Button btnManageApps = (Button) findViewById(R.id.btn_manage_apps); // וודא ש-ID זה קיים ב-XML
        if (btnManageApps != null) {
            btnManageApps.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        requestPasswordAndSave(new Runnable() {
                                @Override
                                public void run() {
                                    Intent intent = new Intent(MDMActivity.this, AppManagementActivity.class);
                                    startActivity(intent);
                                }
                            });
                    }
                });
        }
    }

    private void loadRestrictions() {
        mRestrictionList = new ArrayList<RestrictionItem>();

        String[][] restr={
            {"vpn","21","הפעלת vpn",""},
            {"um","20","השבתת הגדרת נקודה חמה",UserManager.DISALLOW_CONFIG_TETHERING},
            {"um","20","השבתת הגדרת VPN",UserManager.DISALLOW_CONFIG_VPN},
            {"um","17","השבתת הגדרת Wi-Fi",UserManager.DISALLOW_CONFIG_WIFI},
            {"um","17","השבתת התקנת אפליקציות",UserManager.DISALLOW_INSTALL_APPS},
            {"um","17","השבתת התקנה ממקורות לא ידועים",UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES},
            {"um","20","מצב מפתחים",UserManager.DISALLOW_DEBUGGING_FEATURES},
            {"um","20","איפוס להגדרות יצרן",UserManager.DISALLOW_FACTORY_RESET},
            {"um","25","הוספת משתמש בבעלות",UserManager.DISALLOW_ADD_MANAGED_PROFILE},
            {"um","27","החלפת משתמש",UserManager.DISALLOW_USER_SWITCH},
            {"um","20","הוספת משתמשים",UserManager.DISALLOW_ADD_USER},
            {"um","25","בלוטות",UserManager.DISALLOW_BLUETOOTH},
            {"um","17","שינוי בלוטות",UserManager.DISALLOW_CONFIG_BLUETOOTH},
            {"um","28","dns",UserManager.DISALLOW_CONFIG_PRIVATE_DNS},
            {"um","17","הסרת התקנה",UserManager.DISALLOW_UNINSTALL_APPS},
            {"um","20","אס אם אס (sms)",UserManager.DISALLOW_SMS},
            {"um","25","שיתוף בלוטות",UserManager.DISALLOW_BLUETOOTH_SHARING},
            {"um","27","זמן",UserManager.DISALLOW_CONFIG_DATE_TIME},
            {"um","22","מצב בטוח",UserManager.DISALLOW_SAFE_BOOT},
            {"um","20","שיחות יוצאות",UserManager.DISALLOW_OUTGOING_CALLS},
            {"um","20","שינוי רשת סלולרית",UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS},
            {"um","17","הסרת משתמש",UserManager.DISALLOW_REMOVE_USER},
            {"um","20","שליטה באפליקציות",UserManager.DISALLOW_APPS_CONTROL},
            {"um","23","נדידת נתונים",UserManager.DISALLOW_DATA_ROAMING},
            {"um","17","usb",UserManager.DISALLOW_USB_FILE_TRANSFER}
            
        };

        for (String[] ret:restr) {
            if (ret[0].equals("um")) {
                if (Build.VERSION.SDK_INT > Integer.parseInt(ret[1]))
                    mRestrictionList.add(new RestrictionItem(
                                             ret[0],
                                             ret[2],
                                             ret[3],
                                             mDpm.getUserRestrictions(mAdminComponentName).getBoolean(ret[3])));
            } else {
                if (ret[0].equals("list")) {
                    mRestrictionList.add(new RestrictionItem(
                                             ret[0],
                                             ret[2],
                                             ret[3], // מפתח מיוחד עבור פריט זה
                                             false));
                } else if (ret[0].equals("vpn")) {
                    if (Build.VERSION.SDK_INT > Integer.parseInt(ret[1]))
                        mRestrictionList.add(new RestrictionItem(
                                                 ret[0],
                                                 ret[2],
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

    private void applyAllRestrictions() {
        for (RestrictionItem item : mRestrictionList) {
            // נתעלם מפריט ניהול האפליקציות כי הוא לא הגבלה ישירה
            //if (!APP_MANAGEMENT_ITEM_KEY.equals(item.getRestrictionKey())) {
            if (item.getType().equals("um")) {
                if (item.isChecked()) {
                    mDpm.addUserRestriction(mAdminComponentName, item.getRestrictionKey());
                } else {
                    mDpm.clearUserRestriction(mAdminComponentName, item.getRestrictionKey());
                }
            } else if (item.getType().equals("vpn")) {
                
                try {
                    mDpm.setAlwaysOnVpnPackage(mAdminComponentName,item.isChecked()? getPackageName():null, true);
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

    private void requestPasswordAndSave(final Runnable onPasswordCorrect) {
        final String storedPasswordHash = PasswordManager.getStoredPasswordHash(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("אימות סיסמה");

        final EditText input = new EditText(this);
        input.setHint("הכנס סיסמה");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("אשר", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String enteredPassword = input.getText().toString();
                    if (PasswordManager.checkPassword(MDMActivity.this, enteredPassword)) {
                        onPasswordCorrect.run();
                    } else {
                        Toast.makeText(MDMActivity.this, "סיסמה שגויה!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

        if (storedPasswordHash == null) {
            builder.setMessage("אין סיסמת אבטחה מוגדרת. האם ברצונך להגדיר אחת כעת?\n" +
                               "אורך מינימלי: " + PasswordManager.getMinPasswordLength() + " תווים.");
            builder.setNeutralButton("הגדר סיסמה", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showSetPasswordDialog();
                    }
                });
        }
        builder.show();
    }

    private void showSetPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("הגדר/שנה סיסמה");

        final EditText input = new EditText(this);
        input.setHint("הכנס סיסמה חדשה (מינימום " + PasswordManager.getMinPasswordLength() + " תווים)");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        builder.setView(input);

        builder.setPositiveButton("שמור", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newPassword = input.getText().toString();
                    if (PasswordManager.setPassword(MDMActivity.this, newPassword)) {
                        Toast.makeText(MDMActivity.this, "הסיסמה נשמרה בהצלחה!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MDMActivity.this, "שגיאה: הסיסמה קצרה מדי! (מינימום " + PasswordManager.getMinPasswordLength() + " תווים)", Toast.LENGTH_LONG).show();
                    }
                }
            });
        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        builder.show();
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
