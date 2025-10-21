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
import android.os.Handler;
import android.os.Looper;
import android.app.ProgressDialog;

import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.enums.CompressionMethod;
import net.lingala.zip4j.exception.ZipException;

public class AppUpdater {

    public static final String ACTION_INSTALL_COMPLETE = "com.your.package.name.ACTION_INSTALL_COMPLETE";
    public static final String EXTRA_PACKAGE_NAME = "packageName";
    public static final String EXTRA_INSTALL_STATUS = "installStatus";
    static String mainPackageName = null;
    static String reserr="";
    static Context mcontext;
    // הוספת פרמטר isPasswordAlreadyChecked
    @Deprecated
    public static void startInstallSession(Context context, String mfilepath, File sourceFile, boolean isPasswordAlreadyChecked) {
        reserr="";
        mcontext=context;
        if (sourceFile == null || !sourceFile.exists()) {
            //Toast.makeText(context, "קובץ התקנה לא נמצא או לא חוקי.", Toast.LENGTH_LONG).show();
            //AppManagementActivity.progressDialog.setMessage("not faund or not possible" + sourceFile.getName());
            //dismissprogress(context);
            AppManagementActivity.prgmsg(context,"לא נמצא או לא אפשרי" + sourceFile.getName(),true);
            LogUtil.logToFile("not faund or not possible");
            return;
        }
        //LogUtil.logToFile("in 1");
        PackageManager pm = context.getPackageManager();
        PackageInstaller packageInstaller = pm.getPackageInstaller();
        PackageInstaller.Session session = null;

        List<File> apksToInstall = new ArrayList<File>();

        Signature[] mainApkSignatures = null;

        try {
            if (sourceFile.getName().toLowerCase().endsWith(".zip") ||
                sourceFile.getName().toLowerCase().endsWith(".apks") ||
                sourceFile.getName().toLowerCase().endsWith(".xapk") ||
                sourceFile.getName().toLowerCase().endsWith(".apkm")) {
                //LogUtil.logToFile("in 1");
                File tempDir = createTempDir(context);
                apksToInstall = extractApksFromZip(sourceFile, tempDir);
                //LogUtil.logToFile("in 2");
                if (apksToInstall.isEmpty()) {
                    //AppManagementActivity.progressDialog.setMessage("לא נמצאו קבצי APK בארכיון ה-ZIP." + sourceFile.getName());
                    //dismissprogress(context);
                    AppManagementActivity.prgmsg(context,"לא נמצאו קבצי APK בארכיון ה-ZIP." + sourceFile.getName(),true);
                    //Toast.makeText(context, "לא נמצאו קבצי APK בארכיון ה-ZIP.", Toast.LENGTH_LONG).show();
                    return;
                }

                File baseApk = findBaseApk(context, apksToInstall);
                if (baseApk != null) {
                    try {
                        mainPackageName = getApkPackageName(context, baseApk.getAbsolutePath());
                        mainApkSignatures = getApkSignature(context, baseApk.getAbsolutePath());
                        if (mainPackageName != null && mainApkSignatures != null) {
                            LogUtil.logToFile("detected main pn");
                        }
                    } catch (Exception e) {
                        LogUtil.logToFile("e "+e);
                        for (File apk : apksToInstall) {
                            LogUtil.logToFile("for");
                            if (apk.getName().contains(".apk")) {
                                try {
                                    mainPackageName = getApkPackageName(context, apk.getAbsolutePath());
                                    mainApkSignatures = getApkSignature(context, apk.getAbsolutePath());
                                    if (mainPackageName != null && mainApkSignatures != null) {
                                        LogUtil.logToFile("detected main pn");
                                        break;//breaking the for loop
                                    }
                                } catch (Exception ee) {}
                            }
                        }
                    }
                    if (mainPackageName == null) {
                        //AppManagementActivity.progressDialog.setMessage("לא ניתן לזהות את ה-APK הבסיסי בארכיון." + sourceFile.getName());
                        //dismissprogress(context);
                        //AppManagementActivity.prgmsg(context,"לא ניתן לזהות את ה-APK הבסיסי בארכיון." + sourceFile.getName(),true);
                        //return;
                        LogUtil.logToFile("retrying detect pn");
                        for (File apk : apksToInstall) {
                            LogUtil.logToFile("for");
                            if (apk.getName().contains(".apk")) {
                                try {
                                    mainPackageName = getApkPackageName(context, apk.getAbsolutePath());
                                    mainApkSignatures = getApkSignature(context, apk.getAbsolutePath());
                                    if (mainPackageName != null && mainApkSignatures != null) {
                                        LogUtil.logToFile("detected main pn - "+mainPackageName);
                                        break;
                                    }
                                } catch (Exception e) {
                                    LogUtil.logToFile(e.toString());
                                }
                            }
                        }
                    }
                } else {
                    for (File apk : apksToInstall) {
                        LogUtil.logToFile("for");
                        if (apk.getName().contains(".apk")) {
                            try {
                                mainPackageName = getApkPackageName(context, apk.getAbsolutePath());
                                mainApkSignatures = getApkSignature(context, apk.getAbsolutePath());
                                if (mainPackageName != null && mainApkSignatures != null) {
                                    LogUtil.logToFile("detected main pn");
                                    break;
                                }
                            } catch (Exception e) {
                                LogUtil.logToFile(e.toString());
                            }
                        }
                    }
                    if (mainPackageName == null) {
                        //AppManagementActivity.progressDialog.setMessage("לא ניתן לזהות את ה-APK הבסיסי בארכיון." + sourceFile.getName());
                        //dismissprogress(context);
                        AppManagementActivity.prgmsg(context,"לא ניתן לזהות את ה-APK הבסיסי בארכיון." + sourceFile.getName(),true);
                        return;
                    }
                }
            } else if (sourceFile.getName().toLowerCase().endsWith(".apk")) {
                apksToInstall.add(sourceFile);
                //LogUtil.logToFile("in 1");
                LogUtil.logToFile(sourceFile.getAbsolutePath());
                mainPackageName = getApkPackageName(context, sourceFile.getAbsolutePath());
                LogUtil.logToFile(mainPackageName);
                mainApkSignatures = getApkSignature(context, sourceFile.getAbsolutePath());
                //LogUtil.logToFile("in 2");
            } else {
                //AppManagementActivity.progressDialog.setMessage("פורמט קובץ לא נתמך: " + sourceFile.getName());
                //dismissprogress(context);
                AppManagementActivity.prgmsg(context,"פורמט קובץ לא נתמך: " + sourceFile.getName(),true);
                //Toast.makeText(context, "פורמט קובץ לא נתמך: " + sourceFile.getName(), Toast.LENGTH_LONG).show();
                return;
            }
            if (mainPackageName == null || mainApkSignatures == null || mainApkSignatures.length == 0) {
                //AppManagementActivity.progressDialog.setMessage("שגיאה: לא ניתן לקרוא שם חבילה או חתימה מקובץ ה-APK הראשי.");
                //dismissprogress(context);
                AppManagementActivity.prgmsg(context,"שגיאה: לא ניתן לקרוא שם חבילה או חתימה מקובץ ה-APK הראשי."+"pn="+(mainPackageName==null)+"s="+(mainApkSignatures==null),true);
                //Toast.makeText(context, "שגיאה: לא ניתן לקרוא שם חבילה או חתימה מקובץ ה-APK הראשי.", Toast.LENGTH_LONG).show();
                return;
            }
            
            // בדיקת חתימה מול האפליקציה המותקנת
            
            try {
                PackageInfo existingPackage;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    existingPackage = pm.getPackageInfo(mainPackageName, PackageManager.GET_SIGNING_CERTIFICATES);
                } else {
                    existingPackage = pm.getPackageInfo(mainPackageName, PackageManager.GET_SIGNATURES);
                }
                //LogUtil.logToFile("in 4");
                Signature[] existingSignatures = getSignaturesFromPackageInfo(existingPackage);

                if (!signaturesMatch(mainApkSignatures, existingSignatures)) {
                    //LogUtil.logToFile("in 5");
                    //AppManagementActivity.progressDialog.setMessage("שגיאה: חתימות האפליקציה אינן תואמות. העדכון בוטל.");
                    AppManagementActivity.prgmsg(context,"שגיאה: חתימות האפליקציה אינן תואמות. העדכון בוטל.",true);
                    //Toast.makeText(context, "שגיאה: חתימות האפליקציה אינן תואמות. העדכון בוטל.", Toast.LENGTH_LONG).show();
                    //session.abandon();//null...
                    return;
                }
            } catch (PackageManager.NameNotFoundException e) {
                try {
                    //if (AppManagementActivity.progressDialog != null && AppManagementActivity.progressDialog.isShowing())
                        //AppManagementActivity.progressDialog.setMessage("" + e.toString());
                    LogUtil.logToFile(e.toString());
                } catch (Exception ee) {
                    LogUtil.logToFile(e.toString());
                }
                //dismissprogress();
                // האפליקציה אינה מותקנת.
                // אם זו התקנה חדשה, הסיסמה כבר נבדקה מראש ב-AppManagementActivity
                // לכן אין צורך בבדיקה נוספת כאן.
                // אם רוצים לאכוף שרק אפליקציות חתומות ספציפית יוכלו להיות מותקנות,
                // יש לבדוק את ה-mainApkSignatures מול רשימת חתימות "לבנות" ידועות כאן.
            }
            //AppManagementActivity.progressDialog.setMessage("session create");
            AppManagementActivity.prgmsg(context,"יצור סשן",false);
            //LogUtil.logToFile("in 6");
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

            //boolean deltempDir = new File(context.getCacheDir()+"/").delete();
            deleteTempDir(new File(context.getFilesDir().toString() + "/cach"));

            if (!sourceFile.getName().toLowerCase().endsWith(".apk")) {
                if (! addzipToSession(session, new File(mfilepath))) {
                    try {
                        
                        if (session != null)
                            session.abandon();
                        //AppManagementActivity.prgmsg(context,reserr,true);
                        //dismissprogress(context);
                    } catch (Exception e) {
                        LogUtil.logToFile("e3" + e);
                    }
                    return;
                }
            } else {
                if (!addApkToSession(session, new File(mfilepath))) {
                    
                    if (session != null)
                        session.abandon();
                    //AppManagementActivity.prgmsg(context,reserr,true);
                    //dismissprogress(context);
                    
                    return;
                }
            }

            /*for (File apk : apksToInstall) {
             addApkToSession(session, apk);
             }*/
            //LogUtil.logToFile("in 5");
            LogUtil.logToFile(mainPackageName);
            Intent callbackIntent = new Intent(context, InstallReceiver.class);
            callbackIntent.setAction(ACTION_INSTALL_COMPLETE);
            //callbackIntent.putExtra(EXTRA_PACKAGE_NAME, mainPackageName);

            int flags = 0;
            if (Build.VERSION.SDK_INT >= 31) {
                flags = android.app.PendingIntent.FLAG_IMMUTABLE;
                //flags = android.app.PendingIntent.FLAG_MUTABLE;
                flags = 33554432;
            } else {
                flags = android.app.PendingIntent.FLAG_UPDATE_CURRENT;
            }

            android.app.PendingIntent pendingIntent = android.app.PendingIntent.getBroadcast(
                context, 0, callbackIntent, flags);

            session.commit(pendingIntent.getIntentSender());
            //AppManagementActivity.progressDialog.setMessage("session commit");
            AppManagementActivity.prgmsg(context,"ביצוע סשן",false);
            LogUtil.logToFile("starting installation...");
            //Toast.makeText(context, "מתחיל התקנת/עדכון APK...", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            LogUtil.logToFile("failed to starting installation - " + e);
            //AppManagementActivity.progressDialog.setMessage("התחלת התקנה נכשלה " +reserr+ e);
            //dismissprogress(context);
            AppManagementActivity.prgmsg(context,"התחלת התקנה נכשלה " +reserr+ e,true);
            //Toast.makeText(context, "שגיאה בהתחלת התקנה/עדכון: " + e.getMessage(), Toast.LENGTH_LONG).show();
            if (session != null) {
                session.abandon();
            }
            e.printStackTrace();
        }
    }
    @Deprecated
    static void dismissprogress(final Context context) {
        /*context.getMainLooper().prepare();
        new Handler().postDelayed(new Runnable(){

                @Override
                public void run() {
                    if (AppManagementActivity.progressDialog != null && AppManagementActivity.progressDialog.isShowing()) {
                        AppManagementActivity.progressDialog.dismiss();
                    }
                }
            }, 0);
        context.getMainLooper().loop();*/
        AppManagementActivity.prgmsg(context,"ביטול!",true);
    }
    //static ProgressDialog progressDialog;
    /*static void prgmsg(final Context context,final String msg,final boolean end){
        context.getMainLooper().prepare();
            context. getMainExecutor().execute(new Runnable(){
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
                        progressDialog.setCancelable(false);
                        progressDialog.show();
                        progressDialog.setMessage(msg);
                        if(end){
                        new Handler().postDelayed(new Runnable(){

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
       
        
        context.getMainLooper().loop();
    }*/
    // ... (מתודות עזר: createTempDir, extractApksFromZip, findBaseApk, addApkToSession - ללא שינוי)

    /**
     * מקבל חתימות מקובץ APK. משתמש ב-GET_SIGNING_CERTIFICATES עבור API 28+
     * וב-GET_SIGNATURES עבור גרסאות ישנות יותר.
     */
     @Deprecated
    private static Signature[] getApkSignature(Context context, String apkFilePath) {
        try {
            PackageManager pm = context.getPackageManager();
            PackageInfo packageInfo;
            //if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) { // Android 9 (API 28)
                //packageInfo = pm.getPackageArchiveInfo(apkFilePath, PackageManager.GET_SIGNING_CERTIFICATES);
                
            //} else {
                packageInfo = pm.getPackageArchiveInfo(apkFilePath, PackageManager.GET_SIGNATURES | PackageManager.GET_META_DATA | PackageManager.GET_SIGNING_CERTIFICATES);
            //}
            return getSignaturesFromPackageInfo(packageInfo);
        } catch (Exception e) {
            e.printStackTrace();
            AppManagementActivity.prgmsg(context,"err " + e,true);
            LogUtil.logToFile("err "+e);
            
        }
        return null;
    }

    /**
     * חולץ חתימות מאובייקט PackageInfo, תוך התחשבות ב-API 28+
     * וב-GET_SIGNING_CERTIFICATES.
     */
     @Deprecated
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
            LogUtil.logToFile(e.toString());
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
        File tempDir = new File(context.getFilesDir() + "/cach/apks_temp/");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        return tempDir;
    }
    @Deprecated
    public static List<File> extractApksFromZipo(File zipFile, File outputDir) throws IOException {
        List<File> extractedApks = new ArrayList<File>();
        byte[] buffer = new byte[1024];
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry;
            int i=0;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String fileName = zipEntry.getName();
                if (fileName.toLowerCase().endsWith(".apk") && !zipEntry.isDirectory()) {
                    i++;
                    File newFile = new File(outputDir, fileName);
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        extractedApks.add(newFile);
                        AppManagementActivity.prgmsg(mcontext,"extrected " + i,false);
                    } catch (Exception e) {
                        LogUtil.logToFile("" + e);
                        //AppManagementActivity.progressDialog.setMessage("extrected e " + e);
                        AppManagementActivity.prgmsg(mcontext,"extrected e " + e,true);
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
    
    private static final int BUFFER_SIZE = 4096;
    @Deprecated
    public static List<File> extractApksFromZip(File zipFilep, File destDir) throws IOException {
        if (!destDir.exists()) {
                destDir.mkdirs();
            }
        List<File> extractedApks = new ArrayList<File>();
        InputStream is = null;
        OutputStream os = null;
        try {
             ZipFile zipFile = new ZipFile(zipFilep);
            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
            if (fileHeaders == null || fileHeaders.isEmpty()) {
                    System.out.println("The ZIP file is empty.");
                    return null;
            }
                int i=0;
                for (FileHeader header : fileHeaders) {
                    String fileName = header.getFileName();

                    // בדיקה אם הקובץ הוא קובץ (ולא תיקייה) והאם הוא מסתיים ב-.apk
                    if (!header.isDirectory() && fileName.toLowerCase().endsWith(".apk")) {
                          i++;
                        System.out.printf("Extracting: %s%n", fileName);

                        // 3. חילוץ הקובץ הספציפי לתיקיית היעד
                        // Zip4j יודעת לחלץ Entry בודד באמצעות השיטה extractFile(FileHeader, String destinationPath)
                        //zipFile.extractFile(header, destDirectoryPath);
                        
                        is = zipFile.getInputStream(header);
                        os=new FileOutputStream(new File(destDir.getAbsolutePath()+"/"+fileName));
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int len;

                        // 2. לולאת קריאה/כתיבה: קוראים מה-InputStream של Zip4j 
                        // וכותבים ישירות ל-OutputStream של הסשן.
                        while ((len = is.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                        extractedApks.add(new File(destDir+"/"+fileName));
                        AppManagementActivity.prgmsg(mcontext,"מחלץ " + i,false);
                    }
                }

                System.out.println("Selective extraction complete.");

            } catch (Exception e) {
            LogUtil.logToFile("" + e);
                System.err.println("Zip4j Error: " + e.getMessage());
                //throw new IOException("Failed to extract specific files: " + e.getMessage(), e);
            } finally {
            // 3. סגירת ה-InputStream
            try{
            if (is != null) {
                is.close();
            }
            }catch(Exception e){
                LogUtil.logToFile("" + e);
            }
            try{
                os.close();
            }catch(Exception e){
                LogUtil.logToFile("" + e);
            }
            }
/*
            zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry;
            int i=0;
            while ((zipEntry = zis.getNextEntry()) != null) {
                String fileName = zipEntry.getName();
                if (fileName.toLowerCase().endsWith(".apk") && !zipEntry.isDirectory()) {
                    i++;
                    File newFile = new File(outputDir, fileName);
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(newFile);
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                        extractedApks.add(newFile);
                        AppManagementActivity.prgmsg(mcontext,"extrected " + i,false);
                    } catch (Exception e) {
                        LogUtil.logToFile("" + e);
                        //AppManagementActivity.progressDialog.setMessage("extrected e " + e);
                        AppManagementActivity.prgmsg(mcontext,"extrected e " + e,true);
                    } finally {
                        if (fos != null) fos.close();
                    }
                }
                zis.closeEntry();
            }
        } finally {
            if (zis != null) zis.close();
        }
        */
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
    @Deprecated
    private static boolean addApkToSession(PackageInstaller.Session session, File apkFile) throws Exception {
        boolean suc=true;
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
        } catch (Exception e) {
            LogUtil.logToFile("" + e);
            //AppManagementActivity.progressDialog.setMessage("סשן ש " + e);
            AppManagementActivity.prgmsg(mcontext,"סשן ש " + e,true);
            if (session != null)
                session.abandon();

            suc = false;
        } finally {
            if (in != null) in.close();
            if (out != null) out.close();
        }
        return suc;
    }
    @Deprecated
    private static boolean addzipToSessiono(PackageInstaller.Session session, File zipFile) throws Exception {
        //OutputStream out = null;
        //InputStream in = null;
        // try {
        boolean suc=true;
        byte[] buffer = new byte[1024];
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(zipFile));
            ZipEntry zipEntry;
            int i=0;
            while ((zipEntry = zis.getNextEntry()) != null && suc) {
                String fileName = zipEntry.getName();
                if (fileName.toLowerCase().endsWith(".apk") && !zipEntry.isDirectory()) {
                    //File newFile = new File(outputDir, fileName);
                    i++;
                    OutputStream os = null;
                    try {
                        os = session.openWrite(fileName, 0, zipEntry.getSize());
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            os.write(buffer, 0, len);
                        }
                        session.fsync(os);
                        AppManagementActivity.prgmsg(mcontext,"מתקין " + i,false);
                    } catch (Exception e) {
                        if (os != null) os.close();
                        LogUtil.logToFile("" + e);
                        if (session != null)
                            session.abandon();
                        suc = false;
                        //AppManagementActivity.progressDialog.setMessage("session e " + e);
                        AppManagementActivity.prgmsg(mcontext,"סשן ש " + e,true);
                        reserr=e.toString();
                    } finally {
                        if (os != null) os.close();
                    }
                }
                zis.closeEntry();
            }
        }catch(Exception e){
            if (zis != null) zis.close();
            LogUtil.logToFile("in 8"+e);
        } finally {
            
            if (zis != null) zis.close();
            LogUtil.logToFile("in 9");
            if (!suc) {
                LogUtil.logToFile("e1");
                if (session != null)
                    session.abandon();
                LogUtil.logToFile("e2");
                suc = false;
                return suc;
            }
        }
        return suc;
        // השם של הקובץ בסשן חשוב! ל-Split APKs, זה יכול להיות גם משהו כמו "split_config.xxx.apk"
        // הפורמט של PackageInstaller לוקח את השם מה-ZipEntry, כאן אנחנו רק מעבירים את שם הקובץ
        /*out = session.openWrite(apkFile.getName(), 0, apkFile.length());
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
         }*/
    }
    @Deprecated
    private static boolean addzipToSession(PackageInstaller.Session session, File zipFilep) {
        //OutputStream out = null;
        //InputStream in = null;
        // try {
        boolean suc=true;
       
        InputStream is = null;
        OutputStream os = null;
        try {
            ZipFile zipFile = new ZipFile(zipFilep);
            List<FileHeader> fileHeaders = zipFile.getFileHeaders();
            if (fileHeaders == null || fileHeaders.isEmpty()) {
                System.out.println("The ZIP file is empty.");
                return false;
            }
            int i=0;
            for (FileHeader header : fileHeaders) {
                String fileName = header.getFileName();

                // בדיקה אם הקובץ הוא קובץ (ולא תיקייה) והאם הוא מסתיים ב-.apk
                if (!header.isDirectory() && fileName.toLowerCase().endsWith(".apk")) {
                    i++;
                    System.out.printf("Extracting: %s%n", fileName);

                    // 3. חילוץ הקובץ הספציפי לתיקיית היעד
                    // Zip4j יודעת לחלץ Entry בודד באמצעות השיטה extractFile(FileHeader, String destinationPath)
                    //zipFile.extractFile(header, destDirectoryPath);

                    is = zipFile.getInputStream(header);
                    os=session.openWrite(fileName, 0, header.getUncompressedSize());
                    byte[] buffer = new byte[BUFFER_SIZE];
                    int len;

                    // 2. לולאת קריאה/כתיבה: קוראים מה-InputStream של Zip4j 
                    // וכותבים ישירות ל-OutputStream של הסשן.
                    while ((len = is.read(buffer)) > 0) {
                        os.write(buffer, 0, len);
                    }
                    session.fsync(os);
                    if (is != null) is.close();
                    if (os != null) os.close();
                    AppManagementActivity.prgmsg(mcontext,"מתקין - " + i,false);
                }
            }

            System.out.println("Selective extraction complete.");

        } catch (Exception e) {
            LogUtil.logToFile("ses err" + e);
            System.err.println("Zip4j Error: " + e.getMessage());
            try{
            if (os != null) os.close();
            }catch(Exception ee){}
            //LogUtil.logToFile("" + e);
            if (session != null)
                session.abandon();
            suc = false;
            //AppManagementActivity.progressDialog.setMessage("session e " + e);
            AppManagementActivity.prgmsg(mcontext,"סשן ש " + e,true);
            reserr=e.toString();
            //throw new IOException("Failed to extract specific files: " + e.getMessage(), e);
        } finally {
            try{
            if (is != null) is.close();
            }catch(Exception e){}
            LogUtil.logToFile("in 9");
            if (!suc) {
                LogUtil.logToFile("e1");
                if (session != null)
                    session.abandon();
                LogUtil.logToFile("e2");
                suc = false;
                return suc;
            }
            // 3. סגירת ה-InputStream
            try{
                if (is != null) {
                    is.close();
                }
            }catch(Exception e){
                LogUtil.logToFile("" + e);
            }
            try{
                os.close();
            }catch(Exception e){
                LogUtil.logToFile("" + e);
            }
        }
        
        return suc;
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
