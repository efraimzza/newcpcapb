package com.emanuelef.remote_capture.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageInstaller;
import android.content.pm.Signature;
import android.content.pm.SigningInfo;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class AppUpdater {

    public static final String ACTION_INSTALL_COMPLETE = "com.your.package.name.ACTION_INSTALL_COMPLETE";
    public static final String EXTRA_PACKAGE_NAME = "packageName";
    public static final String EXTRA_INSTALL_STATUS = "installStatus";

    // הוספת פרמטר isPasswordAlreadyChecked
    public static void startInstallSession(Context context, File sourceFile, boolean isPasswordAlreadyChecked) {
        if (sourceFile == null || !sourceFile.exists()) {
            Toast.makeText(context, "קובץ התקנה לא נמצא או לא חוקי.", Toast.LENGTH_LONG).show();
            return;
        }

        PackageManager pm = context.getPackageManager();
        PackageInstaller packageInstaller = pm.getPackageInstaller();
        PackageInstaller.Session session = null;

        List<File> apksToInstall = new ArrayList<File>();
        String mainPackageName = null;
        Signature[] mainApkSignatures = null;

        try {
            if (sourceFile.getName().toLowerCase().endsWith(".zip") ||
                sourceFile.getName().toLowerCase().endsWith(".apks") ||
                sourceFile.getName().toLowerCase().endsWith(".xapk")) {

                File tempDir = createTempDir(context);
                apksToInstall = extractApksFromZip(sourceFile, tempDir);

                if (apksToInstall.isEmpty()) {
                    Toast.makeText(context, "לא נמצאו קבצי APK בארכיון ה-ZIP.", Toast.LENGTH_LONG).show();
                    return;
                }

                File baseApk = findBaseApk(context, apksToInstall);
                if (baseApk == null) {
                    Toast.makeText(context, "לא ניתן לזהות את ה-APK הבסיסי בארכיון.", Toast.LENGTH_LONG).show();
                    return;
                }
                mainPackageName = getApkPackageName(context, baseApk.getAbsolutePath());
                mainApkSignatures = getApkSignature(context, baseApk.getAbsolutePath());

            } else if (sourceFile.getName().toLowerCase().endsWith(".apk")) {
                apksToInstall.add(sourceFile);
                mainPackageName = getApkPackageName(context, sourceFile.getAbsolutePath());
                mainApkSignatures = getApkSignature(context, sourceFile.getAbsolutePath());

            } else {
                Toast.makeText(context, "פורמט קובץ לא נתמך: " + sourceFile.getName(), Toast.LENGTH_LONG).show();
                return;
            }

            if (mainPackageName == null || mainApkSignatures == null || mainApkSignatures.length == 0) {
                Toast.makeText(context, "שגיאה: לא ניתן לקרוא שם חבילה או חתימה מקובץ ה-APK הראשי.", Toast.LENGTH_LONG).show();
                return;
            }

            // יצירת סשן ההתקנה
            PackageInstaller.SessionParams params = new PackageInstaller.SessionParams(
                PackageInstaller.SessionParams.MODE_FULL_INSTALL);
            // אם isPasswordAlreadyChecked הוא true, זה אומר שהמשתמש אישר והסיסמה נבדקה (במקרה של התקנה חדשה)
            // לכן, ניתן לדרוש שההתקנה תתבצע ללא אינטראקציה נוספת אם האפליקציה מאושרת
            if (isPasswordAlreadyChecked) {
                // דגל INSTALL_REPLACE_EXISTING רלוונטי רק אם זהו עדכון לאפליקציה קיימת
                // אם זו התקנה ראשונית, הוא פשוט יתקין אותה.
                // אם אתה רוצה לאפשר התקנה שקטה לחלוטין (ללא דיאלוג התקנה למשתמש),
                // נדרשות הרשאות מערכת/MDM מתקדמות יותר ושיטות ספציפיות למכשיר.
                // עבור מצב רגיל, המערכת עדיין עשויה לבקש אישור.
                // params.setInstallerPackageName(context.getPackageName()); // יציין שהאפליקציה שלך היא המקור
            }

            params.setAppPackageName(mainPackageName);

            int sessionId = packageInstaller.createSession(params);
            session = packageInstaller.openSession(sessionId);

            for (File apk : apksToInstall) {
                addApkToSession(session, apk);
            }

            // בדיקת חתימה מול האפליקציה המותקנת
            try {
                PackageInfo existingPackage;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    existingPackage = pm.getPackageInfo(mainPackageName, PackageManager.GET_SIGNING_CERTIFICATES);
                } else {
                    existingPackage = pm.getPackageInfo(mainPackageName, PackageManager.GET_SIGNATURES);
                }

                Signature[] existingSignatures = getSignaturesFromPackageInfo(existingPackage);

                if (!signaturesMatch(mainApkSignatures, existingSignatures)) {
                    Toast.makeText(context, "שגיאה: חתימות האפליקציה אינן תואמות. העדכון בוטל.", Toast.LENGTH_LONG).show();
                    session.abandon();
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
                // האפליקציה אינה מותקנת.
                // אם זו התקנה חדשה, הסיסמה כבר נבדקה מראש ב-AppManagementActivity
                // לכן אין צורך בבדיקה נוספת כאן.
                // אם רוצים לאכוף שרק אפליקציות חתומות ספציפית יוכלו להיות מותקנות,
                // יש לבדוק את ה-mainApkSignatures מול רשימת חתימות "לבנות" ידועות כאן.
            }

            Intent callbackIntent = new Intent(context, InstallReceiver.class);
            callbackIntent.setAction(ACTION_INSTALL_COMPLETE);
            callbackIntent.putExtra(EXTRA_PACKAGE_NAME, mainPackageName);

            int flags = 0;
            if (Build.VERSION.SDK_INT >= 31) {
                flags = android.app.PendingIntent.FLAG_IMMUTABLE;
            } else {
                flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT;
            }

            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                context, 0, callbackIntent, flags);

            session.commit(pendingIntent.getIntentSender());
            Toast.makeText(context, "מתחיל התקנת/עדכון APK...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            Toast.makeText(context, "שגיאה בהתחלת התקנה/עדכון: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (session != null) {
                session.abandon();
            }
            e.printStackTrace();
        }
    }

    // ... (מתודות עזר: createTempDir, extractApksFromZip, findBaseApk, addApkToSession - ללא שינוי)

    /**
     * מקבל חתימות מקובץ APK. משתמש ב-GET_SIGNING_CERTIFICATES עבור API 28+
     * וב-GET_SIGNATURES עבור גרסאות ישנות יותר.
     */
    private static Signature[] getApkSignature(Context context, String apkFilePath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // Android 9 (API 28)
                packageInfo = pm.getPackageArchiveInfo(apkFilePath, PackageManager.GET_SIGNING_CERTIFICATES);
            } else {
                packageInfo = pm.getPackageArchiveInfo(apkFilePath, PackageManager.GET_SIGNATURES);
            }
            return getSignaturesFromPackageInfo(packageInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * חולץ חתימות מאובייקט PackageInfo, תוך התחשבות ב-API 28+
     * וב-GET_SIGNING_CERTIFICATES.
     */
    private static Signature[] getSignaturesFromPackageInfo(PackageInfo packageInfo) {
        if (packageInfo == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // Android 9 (API 28)
            SigningInfo signingInfo = packageInfo.signingInfo;
            if (signingInfo != null) {
                if (signingInfo.hasMultipleSigners()) {
                    // אם יש מספר חותמים, זהו מערך החתימות
                    return signingInfo.getApkContentsSigners();
                } else {
                    // אם יש חותם אחד, זהו מערך של חתימה בודדת
                    return signingInfo.getSigningCertificateHistory();
                }
            }
        } else {
            // לגרסאות ישנות יותר, השתמש בשדה signatures המיושן
            // אזהרה: ב-API 28+ זה יכול להחזיר רק את חתימת האפליקציה המותקנת כרגע
            // ולא את כל היסטוריית החתימות.
            if (packageInfo.signatures != null && packageInfo.signatures.length > 0) {
                return packageInfo.signatures;
            }
        }
        return null;
    }

    // קבלת שם החבילה (ללא שינוי)
    public static String getApkPackageName(Context context, String apkFilePath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo = pm.getPackageArchiveInfo(apkFilePath, 0);
            if (packageInfo != null) {
                return packageInfo.packageName;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * משווה שתי קבוצות של חתימות. המתודה מטפלת במקרים של Multi-Signers
     * ובשינויי חתימה לאורך זמן (אם מוגדר במניפסט).
     */
    private static boolean signaturesMatch(Signature[] sigs1, Signature[] sigs2) {
        if (sigs1 == null || sigs2 == null) {
            return false;
        }
        if (sigs1.length == 0 && sigs2.length == 0) { // שניהם ללא חתימות (נדיר/בעייתי)
            return true; 
        }
        if (sigs1.length == 0 || sigs2.length == 0) { // אחד מהם ריק, השני לא
            return false;
        }

        // אם ב-API 28 ומעלה, getSigningCertificateHistory/getApkContentsSigners
        // כבר מספקים את כל שרשרת החתימות או את החותמים הנוכחיים.
        // השוואה פשוטה של כל החתימות אמורה להיות מספקת.
        if (sigs1.length != sigs2.length) {
            return false;
        }

        // מיון החתימות כדי לוודא סדר זהה לפני ההשוואה
        // במקרה של מספר חותמים, הסדר לא מובטח
        // הדרך הנכונה היא להשוות סטים של חתימות
        List<Signature> list1 = new ArrayList<Signature>();
        List<Signature> list2 = new ArrayList<Signature>();
        Collections.addAll(list1, sigs1);
        Collections.addAll(list2, sigs2);

        // השוואת סטים (HashSet) כדי להתעלם מהסדר
        return new java.util.HashSet<Signature>(list1).equals(new java.util.HashSet<Signature>(list2));
    }
    
    

    // --- מתודות עזר חדשות לטיפול ב-ZIP (APKS) ---

    public static File createTempDir(Context context) {
        File tempDir = new File(context.getCacheDir(), "apks_temp");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        return tempDir;
    }

    public static List<File> extractApksFromZip(File zipFile, File outputDir) throws IOException {
        List<File> extractedApks = new ArrayList<File>();
        byte[] buffer = new byte[1024];
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String fileName = zipEntry.getName();
                if (fileName.toLowerCase().endsWith(".apk") && !zipEntry.isDirectory()) {
                    File newFile = new File(outputDir, fileName);
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        extractedApks.add(newFile);
                    } finally {
                        if (fos != null) fos.close();
                    }
                }
                zis.closeEntry();
            }
        } finally {
            if (zis != null) zis.close();
        }
        return extractedApks;
    }

    public static File findBaseApk(Context context, List<File> apks) {
        // חפש את "base.apk" אם קיים, אחרת בחר את הגדול ביותר כ"ראשי"
        for (File apk : apks) {
            if (apk.getName().equalsIgnoreCase("base.apk")) {
                return apk;
            }
        }
        // אם אין base.apk, בחר את ה-APK הגדול ביותר
        if (!apks.isEmpty()) {
            Collections.sort(apks, new Comparator<File>() {
                    @Override
                    public int compare(File f1, File f2) {
                        if (f1.length() > f2.length()) {
                            return -1;
                        } else if (f1.length() < f2.length()) {
                            return 1;
                        } else {
                            return 0;
                        }
                    }
                });
            return apks.get(0);
        }
        return null;
    }

    // --- מתודות קיימות (ללא שינוי, רק העתקה לצורך קונטקסט) ---

    private static void addApkToSession(PackageInstaller.Session session, File apkFile) throws Exception {
        OutputStream out = null;
        InputStream in = null;
        try {
            // השם של הקובץ בסשן חשוב! ל-Split APKs, זה יכול להיות גם משהו כמו "split_config.xxx.apk"
            // הפורמט של PackageInstaller לוקח את השם מה-ZipEntry, כאן אנחנו רק מעבירים את שם הקובץ
            out = session.openWrite(apkFile.getName(), 0, apkFile.length());
            in = new FileInputStream(apkFile);
            byte[] buffer = new byte[65536];
            int c;
            while ((c = in.read(buffer)) != -1) {
                out.write(buffer, 0, c);
            }
            session.fsync(out);
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
        }
    }

    

    

    

    // מתודת עזר למחיקת תיקיה ותוכן (אחרי סיום ההתקנה)
    public static void deleteTempDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                File child = new File(dir, children[i]);
                if (child.isDirectory()) {
                    deleteTempDir(child);
                } else {
                    child.delete();
                }
            }
            dir.delete();
        }
    }
}
