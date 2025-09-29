package com.emanuelef.remote_capture.activities;

import android.app.Activity;
import android.view.View;
import android.content.Context;
import android.content.SharedPreferences;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Toast;
import android.text.method.PasswordTransformationMethod;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.emanuelef.remote_capture.R;

public class PrPasswordManager {
    private static final String PREFS_NAME = "MDMPrefs";
    private static final String KEY_PASSWORD_HASH = "pr_password_hash";
    private static final int MIN_PASSWORD_LENGTH = 4; // אורך סיסמה מינימלי
    public static boolean pwopen=false;
    /**
     * מגבבת (hashes) סיסמה באמצעות SHA-256 ושומרת אותה ב-SharedPreferences.
     *
     * @param context הקונטקסט.
     * @param password הסיסמה בטקסט רגיל.
     * @return true אם הסיסמה נשמרה בהצלחה, false אם הסיסמה קצרה מדי.
     */
    public static boolean setPassword(Context context, String password) {
        if (password == null || password.length() < MIN_PASSWORD_LENGTH) {
            return false; // הסיסמה קצרה מדי
        }

        String hashedPassword = hashPassword(password);
        if (hashedPassword != null) {
            SharedPreferences.Editor editor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
            editor.putString(KEY_PASSWORD_HASH, hashedPassword);
            editor.apply();
            return true;
        }
        return false; // שגיאה בגיבוב
    }

    /**
     * מחזירה את הגיבוב של הסיסמה השמורה.
     *
     * @param context הקונטקסט.
     * @return גיבוב הסיסמה, או null אם לא הוגדרה סיסמה.
     */
    public static String getStoredPasswordHash(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PASSWORD_HASH, null);
    }

    /**
     * בודקת אם הסיסמה שהוזנה תואמת לסיסמה השמורה (המגובבת).
     *
     * @param context הקונטקסט.
     * @param enteredPassword הסיסמה שהמשתמש הזין.
     * @return true אם הסיסמה נכונה, false אחרת.
     */
    public static boolean checkPassword(Context context, String enteredPassword) {
        String storedHash = getStoredPasswordHash(context);

        // אם אין סיסמה שמורה, כל סיסמה נחשבת נכונה (מצב "ללא סיסמה").
        // בבקשתך צוין שאם אין סיסמה, זה "like at welcome add it",
        // כלומר, המצב הראשוני הוא ללא סיסמה.
        if (storedHash == null) {
            return true;
        }

        String enteredPasswordHash = hashPassword(enteredPassword);
        return enteredPasswordHash != null && enteredPasswordHash.equals(storedHash);
    }

    /**
     * מגבבת סיסמה באמצעות אלגוריתם SHA-256.
     *
     * @param password הסיסמה בטקסט רגיל.
     * @return מחרוזת של הגיבוב, או null במקרה של שגיאה.
     */
    private static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * מחזירה את אורך הסיסמה המינימלי הנדרש.
     */
    public static int getMinPasswordLength() {
        return MIN_PASSWORD_LENGTH;
    }
    public static void requestPasswordAndSave(final Runnable action,final Activity activity) {
        if(pwopen){
            action.run();
            return;
        }
        final String storedPasswordHash = PrPasswordManager.getStoredPasswordHash(activity);
        
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("אימות סיסמה");
        final View passwordLayout = activity. getLayoutInflater().inflate(R.drawable.dialog_password_input, null);
        final android.widget.EditText etPassword = passwordLayout.findViewById(R.id.et_admin_password);
        final android.widget.ImageView showHidePassword = passwordLayout.findViewById(R.id.show_hide_password);
        
        showHidePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // // נבדוק את הסוג הנוכחי של הקלט
                    if (etPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                        // // אם זה סיסמה (מוסתר), נשנה אותו לטקסט רגיל (גלוי)
                        etPassword.setTransformationMethod(null);
                        showHidePassword.setImageResource(R.drawable.ic_visibility_off); // // תמונה של עין סגורה
                    } else {
                        // // אם זה טקסט רגיל (גלוי), נשנה אותו לסיסמה (מוסתר)
                        etPassword.setTransformationMethod(new PasswordTransformationMethod());
                        showHidePassword.setImageResource(R.drawable.ic_visibility); // // תמונה של עין פתוחה
                    }
                    // // חשוב להזיז את הסמן לסוף הטקסט כדי למנוע קפיצה של הסמן
                    etPassword.setSelection(etPassword.getText().length());
                }
            });

        if (storedPasswordHash != null) {
            builder.setView(passwordLayout);

            builder.setPositiveButton("אשר", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String enteredPassword = etPassword.getText().toString();
                        if (PrPasswordManager.checkPassword(activity, enteredPassword)) {
                        
                            action.run();
                        } else {
                            Toast.makeText(activity, "סיסמה שגויה!", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        }else{
            builder.setMessage("אין סיסמת אבטחה מוגדרת. האם ברצונך להגדיר אחת כעת?\n" +
                               "אורך מינימלי: " + PrPasswordManager.getMinPasswordLength() + " תווים.");
            builder.setPositiveButton("הגדר סיסמה", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showSetPasswordDialog(activity);
                    }
                });
        }
        builder.setNegativeButton("ביטול", null);
        builder.show();
    }

    public static void showSetPasswordDialog(final Activity activity) {
        // ... (הקוד הקיים לשינוי סיסמה, אולי תרצה לשפר אותו עם דיאלוג מותאם אישית)
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("שינוי סיסמת משתמש");
        final View passwordLayout = activity. getLayoutInflater().inflate(R.drawable.dialog_password_input, null); // השתמש באותו layout
        final android.widget.EditText etNewPassword = passwordLayout.findViewById(R.id.et_admin_password);
        final android.widget.EditText etNewPasswordb = passwordLayout.findViewById(R.id.et_admin_passwordb);
        final android.widget.ImageView showHidePassword = passwordLayout.findViewById(R.id.show_hide_password);
        final android.widget.ImageView showHidePasswordb = passwordLayout.findViewById(R.id.show_hide_passwordb);
        showHidePassword.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // // נבדוק את הסוג הנוכחי של הקלט
                    if (etNewPassword.getTransformationMethod() instanceof PasswordTransformationMethod) {
                        // // אם זה סיסמה (מוסתר), נשנה אותו לטקסט רגיל (גלוי)
                        etNewPassword.setTransformationMethod(null);
                        showHidePassword.setImageResource(R.drawable.ic_visibility_off); // // תמונה של עין סגורה
                    } else {
                        // // אם זה טקסט רגיל (גלוי), נשנה אותו לסיסמה (מוסתר)
                        etNewPassword.setTransformationMethod(new PasswordTransformationMethod());
                        showHidePassword.setImageResource(R.drawable.ic_visibility); // // תמונה של עין פתוחה
                    }
                    // // חשוב להזיז את הסמן לסוף הטקסט כדי למנוע קפיצה של הסמן
                    etNewPassword.setSelection(etNewPassword.getText().length());
                }
            });
        showHidePasswordb.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // // נבדוק את הסוג הנוכחי של הקלט
                    if (etNewPasswordb.getTransformationMethod() instanceof PasswordTransformationMethod) {
                        // // אם זה סיסמה (מוסתר), נשנה אותו לטקסט רגיל (גלוי)
                        etNewPasswordb.setTransformationMethod(null);
                        showHidePasswordb.setImageResource(R.drawable.ic_visibility_off); // // תמונה של עין סגורה
                    } else {
                        // // אם זה טקסט רגיל (גלוי), נשנה אותו לסיסמה (מוסתר)
                        etNewPasswordb.setTransformationMethod(new PasswordTransformationMethod());
                        showHidePasswordb.setImageResource(R.drawable.ic_visibility); // // תמונה של עין פתוחה
                    }
                    // // חשוב להזיז את הסמן לסוף הטקסט כדי למנוע קפיצה של הסמן
                    etNewPasswordb.setSelection(etNewPasswordb.getText().length());
                }
            });
        LinearLayout linlnewb=passwordLayout.findViewById(R.id.linlnewb);
        linlnewb.setVisibility(View.VISIBLE);
        etNewPassword.setHint("הכנס סיסמה חדשה (מינימום " + PrPasswordManager.getMinPasswordLength() + " תווים)");
        etNewPasswordb.setHint("הכנס שוב את הסיסמה לאימות");
        builder.setView(passwordLayout);

        builder.setPositiveButton("שנה", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String newPassword = etNewPassword.getText().toString();
                    String newPasswordb = etNewPasswordb.getText().toString();
                    if (newPassword.equals(newPasswordb)) {
                        if (PrPasswordManager.setPassword(activity, newPassword)) {
                            Toast.makeText(activity, "הסיסמה נשמרה בהצלחה!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(activity, "שגיאה: הסיסמה קצרה מדי! (מינימום " + PrPasswordManager.getMinPasswordLength() + " תווים)", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(activity, "שגיאה: הסיסמאות אינם תואמות", Toast.LENGTH_LONG).show();
                    }
                }
            });
        builder.setNegativeButton("ביטול", null);
        builder.show();
    }
    
}
