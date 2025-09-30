package com.emanuelef.remote_capture.activities;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class confirmationinstall extends Activity {
    @Deprecated
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent inte= getIntent().getParcelableExtra("inte");
        if(inte!=null){
            inte.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(inte);
        }
        new Handler().postDelayed(new Runnable(){
                @Deprecated
                @Override
                public void run() {
                    new Handler().postDelayed(new Runnable(){
                            @Deprecated
                            @Override
                            public void run() {
                                if (AppManagementActivity.progressDialog != null && AppManagementActivity.progressDialog.isShowing()) {
                                    AppManagementActivity.progressDialog.dismiss();
                                }
                            }
                        }, 3000);
                    finish();
                }
            }, 6000);
    }
    @Deprecated
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppManagementActivity.progressDialog.setMessage("req "+requestCode+" res "+resultCode);
        
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        finish();
        return super.onTouchEvent(event);
    }
   
}
