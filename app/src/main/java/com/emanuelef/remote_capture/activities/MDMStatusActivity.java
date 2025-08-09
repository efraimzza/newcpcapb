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

public class MDMStatusActivity extends Activity {
    
    public static DevicePolicyManager mDpm;
    public static ComponentName mAdminComponentName;
    SharedPreferences sp;
    SharedPreferences.Editor spe;
    public static final String modesp="mode";
    TextView tvstate,tvroute;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdm_status);
        mDpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
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
        tvroute=findViewById(R.id.act_stat_tvroute);
        
        TextView tvdescription=findViewById(R.id.act_stat_tvdescription);
        
        refresh();
        
    }
    private void refresh(){
        boolean mdmstate=mDpm.isDeviceOwnerApp(getPackageName());
        tvstate.setText("מצב mdm - "+(mdmstate?"פעיל":"כבוי"));
        tvroute.setText("המסלול הפעיל - "+AppState.getInstance().getCurrentPath().getDescription());
        boolean vpnenabled=false;
        String strpkgvpn= mDpm.getAlwaysOnVpnPackage(mAdminComponentName);
        if(strpkgvpn!=null){
            vpnenabled=strpkgvpn.equals(getPackageName());
        }
        
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
                        @Override
                        public void run() {
                            MDMSettingsActivity.showRemoveMDMConfirmationDialog(MDMStatusActivity.this);
                        }
                    },MDMStatusActivity.this);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}
}
