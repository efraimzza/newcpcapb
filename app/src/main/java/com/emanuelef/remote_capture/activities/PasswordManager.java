package com.emanuelef.remote_capture.activities;

import android.content.Context;
import android.content.SharedPreferences;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordManager {
    private static final String PREFS_NAME = "MDMPrefs";
    private static final String KEY_PASSWORD_HASH = "admin_password_hash";
    private static final int MIN_PASSWORD_LENGTH = 4; // אורך סיסמה מינימלי

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
    
    public static void requestPasswordAndSave(final Runnable onPasswordCorrect,final Context mcont) {
        final String storedPasswordHash = PasswordManager.getStoredPasswordHash(mcont);

        AlertDialog.Builder builder = new AlertDialog.Builder(mcont);
        builder.setTitle("אימות סיסמה");
        
        final EditText input = new EditText(mcont);
        input.setHint("הכנס סיסמה");
        input.setInputType(android.text.InputType.TYPE_CLASS_TEXT | android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        if (storedPasswordHash != null) {
        builder.setView(input);
        
        builder.setPositiveButton("אשר", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String enteredPassword = input.getText().toString();
                    if (PasswordManager.checkPassword(mcont, enteredPassword)) {
                        onPasswordCorrect.run();
                    } else {
                        Toast.makeText(mcont, "סיסמה שגויה!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            builder.setMessage("אין סיסמת אבטחה מוגדרת. האם ברצונך להגדיר אחת כעת?\n" +
                               "אורך מינימלי: " + PasswordManager.getMinPasswordLength() + " תווים.");
            builder.setPositiveButton("הגדר סיסמה", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        showSetPasswordDialog(mcont);
                    }
                });
        }
        builder.setNegativeButton("ביטול", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
        
        builder.show();
    }

    public static void showSetPasswordDialog(final Context mcont) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mcont);
        builder.setTitle("הגדר/שנה סיסמה");

        final EditText input = new EditText(mcont);
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
                    if (PasswordManager.setPassword(mcont, newPassword)) {
                        Toast.makeText(mcont, "הסיסמה נשמרה בהצלחה!", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mcont, "שגיאה: הסיסמה קצרה מדי! (מינימום " + PasswordManager.getMinPasswordLength() + " תווים)", Toast.LENGTH_LONG).show();
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
}
