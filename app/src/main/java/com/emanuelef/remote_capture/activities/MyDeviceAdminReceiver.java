package com.emanuelef.remote_capture.activities;
import android.app.admin.DeviceAdminReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

// הגדר את ה-MyDeviceAdminReceiver שלך כפי שהוצג קודם
public class MyDeviceAdminReceiver extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        try{
            Toast.makeText(context,"מנהל הופעל",1).show();
            Intent inte=new Intent(context,MDMActivity.class);
            //inte.putExtra("recheck","recheck");
            
               inte.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if(MDMActivity.mlive==null){
            context.startActivity(inte);
            }else{
                MDMActivity.finres(MDMActivity.mlive);
                context.startActivity(inte);
            }
            
        }catch(Exception e){}
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        try{
        Toast.makeText(context,"מנהל הוסר",1).show();
        }catch(Exception e){}
    }
    
    // ניתן להשאיר ריק אם אין צורך בלוגיקה ספציפית כאן,
    // אך קובץ זה חיוני לרישום ה-MDM.
    
}
