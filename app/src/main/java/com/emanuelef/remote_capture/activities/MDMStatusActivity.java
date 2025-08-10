package com.emanuelef.remote_capture.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
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
import com.emanuelef.remote_capture.R;

public class MDMStatusActivity extends Activity {
    
    public static DevicePolicyManager mDpm;
    public static ComponentName mAdminComponentName;
    SharedPreferences sp;
    SharedPreferences.Editor spe;
    public static final String modesp="mode";
    LinearLayout linlactivate,linldetails;
    TextView tvstate,tvroute,tvdescription,tvremoveroot,tvstartbarcode;
    Button bucpcmd,busavebarcode,bustartroot;
    ImageView ivbarcode;
    Bitmap bmp;
    InputStream is;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdm_status);
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
                Toast.makeText(this, AppState.getInstance().getCurrentPath().name()+ " is now",1).show();
            }catch(Exception e){
                Toast.makeText(this, e+"",1).show();
            }
        }
        tvstate=findViewById(R.id.act_stat_tvstate);
        linlactivate=findViewById(R.id.act_stat_linlactivate);
        linldetails=findViewById(R.id.act_stat_linldetails);
        bucpcmd=findViewById(R.id.act_stat_bucpcmd);
        tvstartbarcode=findViewById(R.id.act_stat_tvstartbarcode);
        ivbarcode=findViewById(R.id.act_stat_ivbarcode);
        busavebarcode=findViewById(R.id.act_stat_busavebarcode);
        bustartroot=findViewById(R.id.act_stat_bustartroot);
        tvremoveroot=findViewById(R.id.act_stat_tvremove_root);
        tvroute=findViewById(R.id.act_stat_tvroute);
        tvdescription=findViewById(R.id.act_stat_tvdescription);
        
        bucpcmd.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View p1) {
                    ClipboardManager clbo= (ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                    clbo.setText("dpm set-device-owner com.emanuelef.remote_capture.debug/com.emanuelef.remote_capture.activities.admin");
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
    
    @Deprecated
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);

        switch(item.getItemId()) {
            case R.id.men_ite_sett:
                Intent intent = new Intent(MDMStatusActivity.this, MDMSettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.men_ite_instruct:
                intent = new Intent(MDMStatusActivity.this, InstructionsActivity.class);
                startActivity(intent);
                return true;
            case R.id.men_ite_about:
                intent = new Intent(MDMStatusActivity.this, AboutActivitya.class);
                startActivity(intent);
                return true;
            case R.id.men_ite_remove:
                PasswordManager.requestPasswordAndSave(new Runnable() {
                        @Deprecated
                        @Override
                        public void run() {
                            showRemoveMDMConfirmationDialog(MDMStatusActivity.this);
                        }
                    },MDMStatusActivity.this);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}
}
