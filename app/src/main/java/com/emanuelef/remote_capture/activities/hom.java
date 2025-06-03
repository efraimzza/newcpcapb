package com.emanuelef.remote_capture.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.widget.Toast;
import java.util.List;
import android.content.Context;

public class hom extends Activity {
    Context mcon=this;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        String ap="";
        PackageManager pm = getPackageManager();
        List<ResolveInfo> homeActivities = pm.queryIntentActivities(intent, PackageManager.GET_RESOLVED_FILTER);

        for (ResolveInfo info : homeActivities) {
            String packageName = info.activityInfo.packageName;
            String className = info.activityInfo.name;
            ap+=info.loadLabel(pm);
            if(!packageName.equals(mcon.getPackageName())){
                Intent launchIntent = new Intent();
                launchIntent.setClassName(packageName, className);
                launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(launchIntent);
                finish();
                break;
                
            }
        }
        Toast.makeText(mcon, ""+ap, Toast.LENGTH_SHORT).show();
        
        
    }

}
