package com.emanuelef.remote_capture.activities;
 
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;
import android.graphics.Color;

import com.emanuelef.remote_capture.R;

public class activityadbpair extends Activity {

    private static final String TAG = "RootCommandExecutor";
    private String pkgname,hompat,filesdir,menv,cmddpm;
    private TextView outputTextView;
    private EditText edtxip,edtxport,edtxpwd;
    private EditText commandEditText;
    private Button bupair,bucon,bukill,bushell,buconmul,buconmult,buexecall;
    private ScrollView outputScrollView; 
    public interface CommandOutputListener {
        void onOutputReceived(String line);
        void onErrorReceived(String line);
        void onCommandFinished(int exitCode, String finalOutput, String finalError);
    }

    private CommandOutputListener commandListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_adb_pair);
        pkgname=activityadbpair.this.getPackageName();
        hompat=getDir("HOME", MODE_PRIVATE).getAbsolutePath();
        filesdir=getApplicationInfo().nativeLibraryDir;
        final String adb="adb.so";
        menv="\nPATH=$PATH:"+filesdir+"\nTMPDIR="+hompat+"\nHOME="+hompat+"\nTERM=screen\nexport PATH\nexport TMPDIR\n";
        cmddpm="\n"+adb+" shell dpm set-device-owner com.emanuelef.remote_capture.debug/com.emanuelef.remote_capture.activities.admin & exit\nexit\n";
        //Toast.makeText(this,getDir("HOME", MODE_PRIVATE).getAbsolutePath(),1).show();
        getDir("HOME", MODE_PRIVATE).getAbsolutePath();//important to automaticly create
        outputTextView = (TextView) findViewById(R.id.outputTextView);
        edtxip=findViewById(R.id.edtxip);
        edtxport=findViewById(R.id.edtxport);
        edtxpwd=findViewById(R.id.edtxpwd);
        commandEditText = (EditText) findViewById(R.id.commandEditText); // אתחול EditText
        bupair = findViewById(R.id.bupair);
        bucon = findViewById(R.id.bucon);
        buconmul = findViewById(R.id.buconmul);
        buconmult = findViewById(R.id.buconmult);
        bukill = findViewById(R.id.bukill);
        bushell = findViewById(R.id.bushell);
        buexecall = findViewById(R.id.buexecall);
        outputScrollView = (ScrollView) findViewById(R.id.outputScrollView); // אתחול ScrollView
        
        // הגדרת הליסטנר באמצעות Anonymous Inner Class
        commandListener = new CommandOutputListener() {
            @Override
            public void onOutputReceived(final String line) {
                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.d(TAG, "(Real-time output): " + line);
                            if (outputTextView != null) {
                                outputTextView.append("o: " + line + "\n");
                                if(line.toLowerCase().contains("success: device owner set to package")){
                                    outputTextView.setText("הפעלה הצליחה");
                                    outputTextView.setTextColor(Color.parseColor("#FF00FF00"));
                                }
                                // גלילה אוטומטית לתחתית
                                if (outputTextView != null && outputScrollView != null) {
                                    //outputTextView.append("Output: " + line + "\n");
                                    // scrill down
                                    outputScrollView.fullScroll(View.FOCUS_DOWN);
                                }
                            }
                        }
                    });
            }

            @Override
            public void onErrorReceived(final String line) {
                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.e(TAG, "(Error output): " + line);
                            if (outputTextView != null) {
                                outputTextView.append("e: " + line + "\n");
                                
                                if (outputTextView != null && outputScrollView != null) { // ודא ששניהם לא null
                                    //outputTextView.append("Output: " + line + "\n");
                                    
                                    outputScrollView.fullScroll(View.FOCUS_DOWN);
                                }
                            }
                        }
                    });
            }

            @Override
            public void onCommandFinished(final int exitCode, final String finalOutput, final String finalError) {
                runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //Log.d(TAG, "(Command finished). Exit Code: " + exitCode);
                            //Log.d(TAG, "(Final Output):\n" + finalOutput);
                            //Log.d(TAG, "(Final Error Output):\n" + finalError);

                            if (outputTextView != null) {
                                outputTextView.append("\n--- Command Finished ---\n");
                                outputTextView.append("Exit Code: " + exitCode + "\n");
                                //outputTextView.append("Final Output:\n" + finalOutput + "\n");
                                //outputTextView.append("Final Error:\n" + finalError + "\n");
                                
                                if (outputTextView != null && outputScrollView != null) { // ודא ששניהם לא null
                                    //outputTextView.append("Output: " + line + "\n");
                                    
                                    outputScrollView.fullScroll(View.FOCUS_DOWN);
                                }
                            }
                            
                            bupair.setEnabled(true);
                            bucon.setEnabled(true);
                            buconmul.setEnabled(true);
                            buconmult.setEnabled(true);
                            bukill.setEnabled(true);
                            bushell.setEnabled(true);
                            buexecall.setEnabled(true);
                        }
                    });
            }
        };
        //
        bupair.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //
                    outputTextView.setText("מבצע פקודה...\n");
                    bupair.setEnabled(false);
                    commandEditText.setText("/system/bin/sh -"+menv+adb+" kill-server\n"+adb+" pair "+edtxip.getText().toString()+":"+edtxport.getText().toString()+"\n"+edtxpwd.getText().toString()+cmddpm);
                    final String commandToExecute = commandEditText.getText().toString();
                    if (commandToExecute.isEmpty()) {
                        outputTextView.append("שגיאה: נא הכנס פקודה לביצוע.\n");
                        bupair.setEnabled(true);
                        return;
                    }
                    //
                    new Thread(new Runnable() {
                            @Override
                            public void run() {
                                executeRootCommandInternal(commandToExecute, commandListener);
                            }
                        }).start();
                }
            });
        bucon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // נקה את הפלט הקודם
                    outputTextView.setText("מבצע פקודה...\n");
                    // נטרל את הכפתור כדי למנוע לחיצות מרובות בזמן שהפקודה רצה
                    bucon.setEnabled(false);
                    //String mpropport = "setprop service.adb.tcp.port 5555\n";
                    //String mproprestart = "setprop ctl.restart adbd\n";
                    //String mproprestartb = "adb kill-server\nadb start-server\n";
                    commandEditText.setText("/system/bin/sh -"+menv+adb+" kill-server\nadb.so disconnect\nadb.so connect "+edtxip.getText().toString()+":"+edtxport.getText().toString()+"\nadb.so disconnect\nadb.so connect "+edtxip.getText().toString()+":"+edtxport.getText().toString()+cmddpm);
                    final String commandToExecute = commandEditText.getText().toString();
                    if (commandToExecute.isEmpty()) {
                        outputTextView.append("שגיאה: נא הכנס פקודה לביצוע.\n");
                        bucon.setEnabled(true); // הפוך את הכפתור ללחיץ בחזרה
                        return;
                    }

                    //Log.d(TAG, "Button clicked, executing command: " + commandToExecute);
                    // הפעלת הפקודה על Thread נפרד
                    new Thread(new Runnable() {
                            @Override
                            public void run() {
                                executeRootCommandInternal(commandToExecute, commandListener);
                            }
                        }).start();
                }
            });
        buconmul.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // נקה את הפלט הקודם
                    outputTextView.setText("מבצע פקודה...\n");
                    // נטרל את הכפתור כדי למנוע לחיצות מרובות בזמן שהפקודה רצה
                    buconmul.setEnabled(false);
                    String mpropport = "setprop service.adb.tcp.port 5555\n";
                    String mproprestart = "setprop ctl.restart adbd\n"+adb+" kill-server\n"+adb+" disconnect\n"+adb+" devices\n";
                    //String mproprestartb = "adb kill-server\nadb start-server\n";
                    commandEditText.setText("/system/bin/sh -"+menv+mpropport+mproprestart+adb+" connect localhost:5555"+"\n"+adb+" devices"+cmddpm);
                    final String commandToExecute = commandEditText.getText().toString();
                    if (commandToExecute.isEmpty()) {
                        outputTextView.append("שגיאה: נא הכנס פקודה לביצוע.\n");
                        buconmul.setEnabled(true);
                        return;
                    }

                    //Log.d(TAG, "Button clicked, executing command: " + commandToExecute);
                    
                    new Thread(new Runnable() {
                            @Override
                            public void run() {
                                executeRootCommandInternal(commandToExecute, commandListener);
                            }
                        }).start();
                }
            });
        buconmult.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //
                    outputTextView.setText("מבצע פקודה...\n");
                    // נטרל את הכפתור כדי למנוע לחיצות מרובות בזמן שהפקודה רצה
                    buconmult.setEnabled(false);
                    String mpropport = "setprop service.adb.tcp.port 5555\n";
                    String mproprestart = "setprop ctl.restart adbd\n"+adb+" disconnect\n"+adb+" devices\n";
                    //String mproprestartb = "adb kill-server\nadb start-server\n";
                    String patadb = "/data/user/0/com.emanuelef.remote_capture.debug/files/adb ";
                    patadb = adb+" ";
                    String multcmd = "/system/bin/sh -\nTMPDIR=/storage/emulated/0/\nHOME=/storage/emulated/0/\nTERM=screen\necho $TMPDIR$HOME\nsetprop service.adb.tcp.port 5555\nsetprop ctl.restart adbd\n"+patadb+"kill-server\n"+patadb+"disconnect\n"+patadb+"devices\n"+patadb+"connect localhost:5555\n"+patadb+"devices\n"+patadb+"shell dpm set-device-owner com.emanuelef.remote_capture.debug/com.emanuelef.remote_capture.activities.admin & exit\nexit\n";
                    multcmd = "/system/bin/sh -\nPATH=$PATH:"+filesdir+"\nTMPDIR=/storage/emulated/0/\nexport PATH\nexport TMPDIR\nHOME=/storage/emulated/0/\nTERM=screen\necho $TMPDIR$HOME\nsetprop service.adb.tcp.port 5555\nsetprop ctl.restart adbd\nadb.so kill-server\nadb.so disconnect\nadb.so devices\nadb.so connect localhost:5555\nadb.so devices\nadb.so disconnect\nadb.so connect localhost:5555\n#adb.so\nadb.so devices -l\nadb -t 1\nadb.so shell dpm set-device-owner com.emanuelef.remote_capture.debug/com.emanuelef.remote_capture.activities.admin & exit\nexit\n";
                    //"TERM=screen\nexport TMPDIR\nexport PATH\nadb.so kill-server\nadb.so disconnect\nadb.so connect localhost:5555\nadb.so disconnect\nadb.so connect localhost:5555\n#adb.so\nadb.so devices -l\nadb -t 1\nadb.so shell dpm set-device-owner com.emanuelef.remote_capture.debug/com.emanuelef.remote_capture.activities.admin & exit\nexit\n";
                    commandEditText.setText(multcmd);
                    final String commandToExecute = commandEditText.getText().toString();
                    if (commandToExecute.isEmpty()) {
                        outputTextView.append("שגיאה: נא הכנס פקודה לביצוע.\n");
                        buconmult.setEnabled(true);
                        return;
                    }
                    //Log.d(TAG, "Button clicked, executing command: " + commandToExecute);
                    
                    new Thread(new Runnable() {
                            @Override
                            public void run() {
                                executeRootCommandInternal(commandToExecute, commandListener);
                            }
                        }).start();
                }
            });
        bukill.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // נקה את הפלט הקודם
                    outputTextView.setText("מבצע פקודה...\n");
                    // נטרל את הכפתור כדי למנוע לחיצות מרובות בזמן שהפקודה רצה
                    bukill.setEnabled(false);
                    commandEditText.setText("/system/bin/sh -"+menv+adb+" kill-server\n"+adb+" connect "+edtxip.getText().toString()+":"+edtxport.getText().toString()+cmddpm);
                    final String commandToExecute = commandEditText.getText().toString();
                    if (commandToExecute.isEmpty()) {
                        outputTextView.append("שגיאה: נא הכנס פקודה לביצוע.\n");
                        bukill.setEnabled(true); // הפוך את הכפתור ללחיץ בחזרה
                        return;
                    }

                    //Log.d(TAG, "Button clicked, executing command: " + commandToExecute);
                    // הפעלת הפקודה על Thread נפרד
                    new Thread(new Runnable() {
                            @Override
                            public void run() {
                                executeRootCommandInternal(commandToExecute, commandListener);
                            }
                        }).start();
                }
            });
        bushell.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    
                    outputTextView.setText("מבצע פקודה...\n");
                    
                    bushell.setEnabled(false);
                    commandEditText.setText("/system/bin/sh -"+menv+adb+" shell dpm set-device-owner com.emanuelef.remote_capture.debug/com.emanuelef.remote_capture.activities.admin & exit\nexit\n");
                    final String commandToExecute = commandEditText.getText().toString();
                    if (commandToExecute.isEmpty()) {
                        outputTextView.append("שגיאה: נא הכנס פקודה לביצוע.\n");
                        bushell.setEnabled(true);
                        return;
                    }

                    //Log.d(TAG, "Button clicked, executing command: " + commandToExecute);
                    
                    new Thread(new Runnable() {
                            @Override
                            public void run() {
                                executeRootCommandInternal(commandToExecute, commandListener);
                            }
                        }).start();
                }
            });
        buexecall.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // נקה את הפלט הקודם
                    outputTextView.setText("מבצע פקודה...\n");
                    // נטרל את הכפתור כדי למנוע לחיצות מרובות בזמן שהפקודה רצה
                    buexecall.setEnabled(false);
                    //commandEditText.setText("/system/bin/sh -"+menv+"adb shell dpm set-device-owner com.emanuelef.remote_capture.debug/com.emanuelef.remote_capture.activities.admin & exit\nexit\n");
                    final String commandToExecute = commandEditText.getText().toString();
                    if (commandToExecute.isEmpty()) {
                        outputTextView.append("שגיאה: נא הכנס פקודה לביצוע.\n");
                        buexecall.setEnabled(true); // הפוך את הכפתור ללחיץ בחזרה
                        return;
                    }

                    //Log.d(TAG, "Button clicked, executing command: " + commandToExecute);
                    // הפעלת הפקודה על Thread נפרד
                    new Thread(new Runnable() {
                            @Override
                            public void run() {
                                executeRootCommandInternal(commandToExecute, commandListener);
                            }
                        }).start();
                }
            });
        // פקודה לדוגמה שמוצגת ב-EditText בהתחלה
        initalcommand();
        edtxip.setText(wifiip);
        commandEditText.setText("/system/bin/sh -"+menv+"echo $TMP\nexport\nexport HOME\nexport $HOME\nexport TMPDIR\nexport $TMPDIR\n"+adb+" connect "+"localhost:5555"+cmddpm);
        // או פקודת dd לדוגמה
        // commandEditText.setText("dd if=/dev/zero of=/sdcard/test_dd_output_dynamic.bin bs=1M count=1 2>&1");
        try {
            //InputStream adbb= getAssets().open("adb");
            //File fi=new File(getFilesDir()+"/adb");
            // fi=new File("/data/local/tmp"+"/adb");

            File fil=new File(getDataDir()+"/");
            File filb=new File(getFilesDir()+"/");
            File filc=new File(getFilesDir()+"/home/");
            filc.mkdir();

            //pcopyFile(adbb,fi);
            //fi.setExecutable(true,false);
            //fi.setWritable(true,false);
            //fi.setReadable(true,false);
            fil.setExecutable(true,false);
            fil.setWritable(true,false);
            fil.setReadable(true,false);
            filb.setExecutable(true,false);
            filb.setWritable(true,false);
            filb.setReadable(true,false);
            filc.setExecutable(true,false);
            filc.setWritable(true,false);
            filc.setReadable(true,false);
            /*try {
             Os.chmod(fi.getPath(), 777);
             } catch (ErrnoException e) {}*/
        } catch (Exception e) {}
    }
    public static String wifiip="";
    //int pport=0;
    public void initalcommand(){//added
        wifiip=getWifiIp();
        //main();
        //mSettings.inicom("PATH=$PATH:/data/user/0/mdm.adb/files\nadb connect "+Term.wifiip+":"+pport+"\nadb shell dpm set-device-owner com.kdroid.filter/.listener.AdminListener");

    }
    @Deprecated
    private String getWifiIp() {
        final WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
            int ip = mWifiManager.getConnectionInfo().getIpAddress();
            return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 24) & 0xFF);
        }
        return "";
    }
    public void pcopyFile(InputStream var0, File var1) throws IOException {
        int var3;
        
        InputStream var4 = var0;
        FileOutputStream var6 = new FileOutputStream(var1);
        byte[] var5 = new byte[1024];

        while(true) {
            var3 = var4.read(var5);
            if(var3 <= 0) {
                var4.close();
                var6.close();
                break;
            }
            var6.write(var5, 0, var3);

        }
    }
    

    /**
     * פונקציה פנימית לביצוע פקודות באופן אסינכרוני.
     *
     * @param command הפקודה לביצוע
     * @param listener הליסטנר לקבלת עדכונים על פלט
     */
    Process process = null;
    private void executeRootCommandInternal(final String command, final CommandOutputListener listener) {
        
        DataOutputStream os = null;
        final StringBuilder finalOutput = new StringBuilder();
        final StringBuilder finalErrorOutput = new StringBuilder();
        int exitCode = -1;

        try {
            process = Runtime.getRuntime().exec("sh");
            os = new DataOutputStream(process.getOutputStream());

            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();

            final AtomicBoolean processFinished = new AtomicBoolean(false);

            // Thread לקריאת Standard Output
            Thread outputReaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                            String line;
                            while ((line = reader.readLine()) != null || !processFinished.get()) {
                                if (line != null) {
                                    if (listener != null) {
                                        listener.onOutputReceived(line);
                                    }
                                    finalOutput.append(line).append("\n");
                                } else {
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        break;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            if (listener != null) {
                                listener.onErrorReceived("Output Stream Read Error: " + e.getMessage());
                            }
                            finalErrorOutput.append("Output Stream Read Error: ").append(e.getMessage()).append("\n");
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) { /* ignore */ }
                            }
                        }
                    }
                });

            // Thread לקריאת Standard Error
            Thread errorReaderThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        BufferedReader reader = null;
                        try {
                            reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
                            String line;
                            while ((line = reader.readLine()) != null || !processFinished.get()) {
                                if (line != null) {
                                    if (listener != null) {
                                        listener.onErrorReceived(line);
                                    }
                                    finalErrorOutput.append(line).append("\n");
                                } else {
                                    try {
                                        Thread.sleep(50);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                        break;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            if (listener != null) {
                                listener.onErrorReceived("Error Stream Read Error: " + e.getMessage());
                            }
                            finalErrorOutput.append("Error Stream Read Error: ").append(e.getMessage()).append("\n");
                        } finally {
                            if (reader != null) {
                                try {
                                    reader.close();
                                } catch (IOException e) { /* ignore */ }
                            }
                        }
                    }
                });

            outputReaderThread.start();
            errorReaderThread.start();

            exitCode = process.waitFor();
            processFinished.set(true);

            outputReaderThread.join();
            errorReaderThread.join();

        } catch (Exception e) {
            if (listener != null) {
                listener.onErrorReceived("Execution Exception: " + e.getMessage());
            }
            finalErrorOutput.append("Execution Exception: ").append(e.getMessage()).append("\n");
        } finally {
            try {
                bupair.setEnabled(true);
                bucon.setEnabled(true);
                buconmul.setEnabled(true);
                buconmult.setEnabled(true);
                bukill.setEnabled(true);
                bushell.setEnabled(true);
                buexecall.setEnabled(true);
                if (os != null) os.close();
                if (process != null) process.destroy();
            } catch (Exception e) {
                if (listener != null) {
                    listener.onErrorReceived("Resource Close Error: " + e.getMessage());
                }
                finalErrorOutput.append("Resource Close Error: ").append(e.getMessage()).append("\n");
            }
            if (listener != null) {
                listener.onCommandFinished(exitCode, finalOutput.toString(), finalErrorOutput.toString());
            }
        }
    }
}
