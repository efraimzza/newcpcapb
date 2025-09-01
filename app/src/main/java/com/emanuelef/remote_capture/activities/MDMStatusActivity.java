package com.emanuelef.remote_capture.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.ComponentName;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import android.widget.LinearLayout;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import android.content.pm.PackageManager;
import android.os.UserManager;
import android.os.Build;
import android.Manifest;
import android.provider.Settings;
import android.os.Environment;
import android.net.Uri;
import android.net.VpnService;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.view.Gravity;
import androidx.core.view.MenuProvider;
import com.emanuelef.remote_capture.R;
import com.emanuelef.remote_capture.BuildConfig;
import com.emanuelef.remote_capture.Utils;

import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MDMStatusActivity extends Activity {
    
    public static DevicePolicyManager mDpm;
    public static ComponentName mAdminComponentName;
    SharedPreferences sp;
    SharedPreferences.Editor spe;
    public static final String modesp="mode";
    public static final String locksp="lock";
    LinearLayout linlactivate,linldetails;
    TextView tvappname,tvstate,tvroute,tvdescription,tvremoveroot,tvstartbarcode;
    Button bucpcmd,busavebarcode,bustartroot,buadbwifi,buqrmdm;
    ImageView ivbarcode;
    Bitmap bmp;
    InputStream is;
    Context mcon=this;
    EditText edtxd;
    AlertDialog alertDialogb;
    TextView tvtc,tvc;
    Button bud;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdm_status);
        if(!hasManageExternalStoragePermission(this)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requestManageExternalStoragePermission(this);
            } else if (!hasWriteExternalStoragePermission(this)) {
                requestWriteExternalStoragePermission(this);
            }
        }
        try{
            requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"},55);
        }catch(Exception e){}
        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        mAdminComponentName = new ComponentName(this,admin.class);
        sp=this.getSharedPreferences(this.getPackageName(),this.MODE_PRIVATE);
        spe=sp.edit();
        if(sp.getString(modesp,"").equals("")){
            AppState.getInstance().setCurrentPath(PathType.MULTIMEDIA);
            spe.putString(modesp,AppState.getInstance().getCurrentPath().name());
            spe.commit();
            Toast.makeText(this,AppState.getInstance().getCurrentPath().name()+" is default",1).show();
        }else{
            try{
                AppState.getInstance().setCurrentPath(PathType.valueOf(sp.getString(modesp,"")));
                //Toast.makeText(this, AppState.getInstance().getCurrentPath().name()+ " is now",1).show();
            }catch(Exception e){
                Toast.makeText(this, e+"",1).show();
            }
        }
        tvappname=findViewById(R.id.act_stat_tvappname);
        tvstate=findViewById(R.id.act_stat_tvstate);
        linlactivate=findViewById(R.id.act_stat_linlactivate);
        linldetails=findViewById(R.id.act_stat_linldetails);
        bucpcmd=findViewById(R.id.act_stat_bucpcmd);
        buadbwifi=findViewById(R.id.act_stat_buadbwifi);
        buqrmdm=findViewById(R.id.act_stat_buqrmdm);
        tvstartbarcode=findViewById(R.id.act_stat_tvstartbarcode);
        ivbarcode=findViewById(R.id.act_stat_ivbarcode);
        busavebarcode=findViewById(R.id.act_stat_busavebarcode);
        bustartroot=findViewById(R.id.act_stat_bustartroot);
        tvremoveroot=findViewById(R.id.act_stat_tvremove_root);
        tvroute=findViewById(R.id.act_stat_tvroute);
        tvdescription=findViewById(R.id.act_stat_tvdescription);
        
        tvappname.setText(getResources().getString(R.string.pcapdroid_app_name)+" "+Utils.getAppVersion(this));
        bucpcmd.setOnClickListener(new OnClickListener(){
                @Deprecated
                @Override
                public void onClick(View p1) {
                    ClipboardManager clbo= (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    clbo.setText("dpm set-device-owner com.emanuelef.remote_capture.debug/com.emanuelef.remote_capture.activities.admin");
                    Toast.makeText(MDMStatusActivity.this, "הועתק ללוח!",1).show();
                    
                }
            });
        buadbwifi.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View p1) {
                    Intent intent = new Intent(MDMStatusActivity.this, activityadbpair.class);
                    startActivity(intent);
                }
            });
        buqrmdm.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View p1) {
                    Intent intent = new Intent(MDMStatusActivity.this, actqrmdm.class);
                    startActivity(intent);
                }
            });
        try {
            is= getAssets().open("barcode.png");
            
            if (null != is) {bmp = BitmapFactory.decodeStream(is);}
            
        } catch (IOException e) {}
        finally{
            try {
                if(is!=null){
                is.close();
                }
            } catch (Exception e) {}
        }
        if(bmp!=null){
            ivbarcode.setImageBitmap(bmp);
        }
        busavebarcode.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View p1) {
                    
                    int read=0;
                    byte[] buf=new byte[1024];
                    try {
                        FileOutputStream fos= new FileOutputStream(new File("/storage/emulated/0/barcode.png"));
                        is= getAssets().open("barcode.png");
                    while ((read = is.read(buf))>0) {
                        fos.write(buf,0,read);
                    }
                    Toast.makeText(MDMStatusActivity.this, "נשמר באיחסון פנימי!",1).show();
                    }catch(Exception e){
                        
                    }finally{
                        try {
                            if(is!=null){
                                is.close();
                            }
                        } catch (Exception e) {}
                    }
                }
            });
        bustartroot.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View p1) {
                    startwithroot(MDMStatusActivity.this);
                }
            });
        refresh();
        
    }
    private void refresh(){
        boolean mdmstate=mDpm.isDeviceOwnerApp(getPackageName());
        tvstate.setText("מצב mdm - "+(mdmstate?"פעיל":"כבוי"));
        if(mdmstate){
            linlactivate.setVisibility(View.GONE);
            linldetails.setVisibility(View.VISIBLE);
            tvroute.setText("המסלול הפעיל - "+AppState.getInstance().getCurrentPath().getDescription());
            boolean vpnenabled=false;
            String strpkgvpn= mDpm.getAlwaysOnVpnPackage(mAdminComponentName);
            if(strpkgvpn!=null){
                vpnenabled=strpkgvpn.equals(getPackageName());
            }
            tvdescription.setText("מצב vpn - "+(vpnenabled?"פעיל":"כבוי"));
            if(vpnenabled){
                try {
                   VpnService.prepare(this);
                   p(mDpm, mAdminComponentName, this.getPackageName(), true);
                } catch (Exception e) {}
            }
        } else {
            linlactivate.setVisibility(View.VISIBLE);
            linldetails.setVisibility(View.GONE);
        }
    }
    private void startwithroot(final Activity activity) {
            // אם לא פעיל, נבקש להפעיל
            /*
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminComponentName);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "יישום זה דורש הרשאת מנהל מכשיר כדי ליישם מדיניות אבטחה.");
            startActivityForResult(intent, 0);
            */
            try {
                //String[] strar = {"/system/bin/sh","-c",""};
                String[] strar = {"su","-c",""};
                String ed="";
                //ed = edtx1.getText().toString();
                ed="dpm set-device-owner com.emanuelef.remote_capture.debug/com.emanuelef.remote_capture.activities.admin";
                //int i = 0;
                //ed.split(" ", i++);
                strar[2] = ed;
                //strar[i]=ed;
                String c ="";

                try {
                    Process exec=Runtime.getRuntime().exec(strar);
                    c += (exec.waitFor() == 0) ?"success:": "fail:";
                    exec.getOutputStream();
                    //c = exec.getInputStream().toString();
                    //c=exec.getOutputStream().toString();
                    BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(exec.getInputStream()));
                    BufferedReader in=bufferedReader;
                    String st;
                    StringBuilder edtx1=new StringBuilder();
                    do {
                        st = in.readLine();
                        if (st != null) {
                            edtx1.append(st);
                            edtx1.append(String.valueOf("\n"));
                            continue;
                        }
                    } while (st != null);
                    in.close();
                    c += edtx1.toString();
                    bufferedReader = new BufferedReader(new InputStreamReader(exec.getErrorStream()));
                    in = bufferedReader;
                    st = "";
                    edtx1 = new StringBuilder();
                    do {
                        st = in.readLine();
                        if (st != null) {
                            edtx1.append(st);
                            edtx1.append(String.valueOf("\n"));
                            continue;
                        }
                    } while (st != null);
                    in.close();
                    c += edtx1.toString();
                    Toast.makeText(activity, "" + c, Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    Toast.makeText(activity, "error" + e, Toast.LENGTH_LONG).show();
                }
            } catch (Exception e) {
                Toast.makeText(activity, "error" + e, Toast.LENGTH_LONG).show();
            }
    }
       public  static  void p(DevicePolicyManager devicePolicyManager, ComponentName componentName, String string, boolean bl) throws PackageManager.NameNotFoundException {
        //   try {
        devicePolicyManager.setAlwaysOnVpnPackage(componentName, string, bl);
        //  } catch (Exception e) {
        //   Toast.makeText(getApplicationContext(),""+e,Toast.LENGTH_SHORT).show();
        // }
    }
    public static void showRemoveMDMConfirmationDialog(final Activity activity) {
        new AlertDialog.Builder(activity)
            .setTitle("הסר ניהול מכשיר")
            .setMessage("האם אתה בטוח שברצונך להסיר את אפליקציית ה-MDM כמנהל המכשיר?")
            .setPositiveButton("כן, הסר", new DialogInterface.OnClickListener() {
                @Deprecated
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mDpm = (DevicePolicyManager) activity.getSystemService(Context.DEVICE_POLICY_SERVICE);
                    mAdminComponentName = new ComponentName(activity, admin.class);
                    MDMSettingsActivity.removefrp(activity);
                    try{
                        mDpm.clearDeviceOwnerApp(activity.getPackageName());
                        Toast.makeText(activity, "mdm removed", Toast.LENGTH_SHORT).show();
                    }catch(Exception e){
                        Toast.makeText(activity, "" + e, Toast.LENGTH_SHORT).show();
                    }
                    try {
                        StringBuilder stringBuilder = new StringBuilder("package:");
                        stringBuilder.append(activity.getPackageName());
                        Uri parse = Uri.parse(stringBuilder.toString());
                        activity.startActivity(new Intent(Intent.ACTION_UNINSTALL_PACKAGE, parse).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                    } catch (Exception e) {
                        Toast.makeText(activity, "" + e, Toast.LENGTH_SHORT).show();
                    }
                }
            })
            .setNegativeButton("ביטול", null)
            .show();
    }
    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_status, menu);
        return true;
    }
    /*@Override
    public void onCreateMenu(@NonNull Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.main_menu_status, menu);
    }
    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem item) {
        return false;
    }*/
    @Deprecated
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);
        Intent intent;
        switch(item.getItemId()) {
            case R.id.men_ite_sett:
                if(!sp.getBoolean(locksp,false)){
                    intent = new Intent(MDMStatusActivity.this, MDMSettingsActivity.class);
                    startActivity(intent);
                }
                return true;
            case R.id.men_ite_instruct:
                intent = new Intent(MDMStatusActivity.this, InstructionsActivity.class);
                startActivity(intent);
                return true;
            case R.id.men_ite_about:
                intent = new Intent(MDMStatusActivity.this, AboutActivitya.class);
                startActivity(intent);
                return true;
            case R.id.men_ite_mail:
                sendm();
                return true;
            case R.id.men_ite_remove:
                if(!sp.getBoolean(locksp,false)){
                    PasswordManager.requestPasswordAndSave(new Runnable() {
                        @Deprecated
                        @Override
                        public void run() {
                            showRemoveMDMConfirmationDialog(MDMStatusActivity.this);
                        }
                    },MDMStatusActivity.this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}
	// Check if Manage External Storage permission is granted (for Android 11+)
    public static boolean hasManageExternalStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return Environment.isExternalStorageManager();
        } else {
            // For below Android 11, use normal READ/WRITE permissions
            int writePermission = context.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, android.os.Process.myPid(), android.os.Process.myUid());
            return writePermission == PackageManager.PERMISSION_GRANTED;
        }
    }

    public static void requestManageExternalStoragePermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                intent.setData(Uri.parse("package:" + context.getPackageName()));
                context.startActivity(intent);
            } catch (Exception e) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                context.startActivity(intent);
            }
        }
    }
    public static boolean hasWriteExternalStoragePermission(Context context) {
        int permissionCheck = context.checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, android.os.Process.myPid(), android.os.Process.myUid());
        return permissionCheck == PackageManager.PERMISSION_GRANTED;
    }
    public static void requestWriteExternalStoragePermission(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }
    }
        public void msendmail(final String md_email, final String md_password,final String body,final String[] recipients) {
        //final String md_email="whitelistnetkosher@gmail.com";
        //final String md_password="ogrh baby ankk twcb";
        //String md_targetemail="whitelistnetkosher@gmail.com";
        //final String[] recipients = {"****@gmail.com", "hefraimzzxc@gmail.com"};
        new Thread(){public void run() {
                try {
                    Properties props = new Properties();
                    props.put("mail.smtp.user", md_email);
                    props.put("mail.smtp.host", "smtp.gmail.com");
                    props.put("mail.smtp.port", "587");
                    props.put("mail.smtp.starttls.enable", "true");
                    props.put("mail.smtp.auth", "true");
                    props.put("mail.smtp.socketFactory.port", "587");
                    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                    props.put("mail.smtp.socketFactory.fallback", "true");
                    try {
                        Authenticator auth = new javax.mail.Authenticator() {
                            protected PasswordAuthentication getPasswordAuthentication() {
                                return new PasswordAuthentication(md_email, md_password);
                            }
                        };
                        Session session = Session.getInstance(props, auth);
                        MimeMessage msg = new MimeMessage(session);
                        String sub =mcon.getResources().getString(R.string.mailsub);
                        msg.setSubject(sub);
                        msg.setText(body);
                        /*
                         BodyPart messageBodyPart1 = new MimeBodyPart();
                         messageBodyPart1.setText("update"); 
                         MimeBodyPart messageBodyPart2 = new MimeBodyPart();
                         String filename = "/storage/emulated/0/Download/a.csv";//change accordingly
                         DataSource source = new FileDataSource(filename);
                         messageBodyPart2.setDataHandler(new DataHandler(source));
                         messageBodyPart2.setFileName("a.csv"); 
                         //5) create Multipart object and add MimeBodyPart objects to this object    
                         Multipart multipart = new MimeMultipart();
                         multipart.addBodyPart(messageBodyPart1);
                         multipart.addBodyPart(messageBodyPart2); 
                         //6) set the multiplart object to the message object
                         msg.setContent(multipart ); 
                         */

                        msg.setFrom(new InternetAddress(md_email));
                        //msg.addRecipient(Message.RecipientType.TO, new InternetAddress(md_targetemail));

                        InternetAddress[] recipientAddresses = new InternetAddress[recipients.length];
                        for (int i = 0; i < recipients.length; i++) {
                            recipientAddresses[i] = new InternetAddress(recipients[i]);
                        }
                        msg.addRecipients(Message.RecipientType.TO, recipientAddresses);
                        Transport.send(msg);
                        mcon.getMainLooper().myLooper().prepare();
                        Toast.makeText(mcon, R.string.send_successful, 1).show();
                        mcon.getMainLooper().myLooper().loop();
                    } catch (MessagingException mex) {
                        mex.printStackTrace();
                        //res += mex;
                        mcon.getMainLooper().myLooper().prepare();
                        Toast.makeText(mcon, "" + mex, 1).show();
                        mcon.getMainLooper().myLooper().loop();
                    }

                } catch (Exception e) {
                    mcon.getMainLooper().myLooper().prepare();
                    Toast.makeText(mcon, "" + e, 1).show();
                    mcon.getMainLooper().myLooper().loop();
                }
            }}.start();
    }
    void sendm() {
        try {
            HorizontalScrollView hsv=new HorizontalScrollView(mcon);
            FrameLayout.LayoutParams flp=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
            flp.gravity=Gravity.CENTER;
            //flp.setMargins(20,0,20,0);
            ScrollView sv=new ScrollView(mcon);
            LinearLayout linl=new LinearLayout(mcon);
            linl.setOrientation(linl.VERTICAL);
            linl.setGravity(Gravity.CENTER);
            tvtc=new TextView(mcon);
            tvtc.setTextSize(30);
            tvtc.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_DialogWindowTitle);
            edtxd = new EditText(mcon);
            String mailbod =mcon.getResources().getString(R.string.mailbod);
            edtxd.setHint(mailbod);
            //edtxd.setInputType(2);
            tvc = new TextView(mcon);
            bud = new Button(mcon);
            bud.setText(R.string.send);
            linl.addView(tvtc);
            linl.addView(edtxd);
            linl.addView(tvc);
            linl.addView(bud);
            sv.addView(linl);
            hsv.addView(sv);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mcon);
            alertDialogBuilder.setView(hsv);
            alertDialogb = alertDialogBuilder.create();
            //alertDialoga.setContentView(hsv);
            //alertDialoga.setView(linl);
            
            bud.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View p1) {
                        if (edtxd == null) {
                        } else {
                            String resa=edtxd.getText().toString();
                            if (!resa.equals("")) {
                                alertDialogb.hide();
                                String md_email="";
                                String md_password="";
                                md_email = BuildConfig.md_mail;
                                md_password = BuildConfig.md_pwd;
                                
                                String ad="****@gmail.com";
                                String mail_to = "";
                                mail_to = BuildConfig.md_mail_to;
                                String[] recipients = { mail_to };
                                msendmail(md_email, md_password,resa,recipients);
                            } else {
                                tvc.setText(R.string.empty);
                                Toast.makeText(mcon, R.string.empty, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
                
                alertDialogb.show();
                hsv.setLayoutParams(flp);
                LinearLayout.LayoutParams llp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                
                edtxd.setLayoutParams(llp);
                //edtxd.setWidth(100);
                edtxd.setTextSize(20);
                tvtc.setLayoutParams(llp);
                tvc.setLayoutParams(llp);
                bud.setLayoutParams(llp);
                //linl.setLayoutParams(flp);
                tvtc.setText(R.string.send_mail);
                
        } catch (Exception e) {
            Toast.makeText(mcon, e + "", Toast.LENGTH_LONG).show();
            //finish();
        }
    }
}
