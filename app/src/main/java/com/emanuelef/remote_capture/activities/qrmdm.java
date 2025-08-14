package server.mdm;

import androidx.annotation.Nullable;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.Enumeration;
import java.util.StringTokenizer;
import android.graphics.Rect;
import android.app.ActionBar;
import android.graphics.drawable.GradientDrawable;
import android.view.View.OnTouchListener;
import android.view.MotionEvent;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import android.content.pm.PackageInfo;
import android.content.pm.Signature;
import java.security.MessageDigest;
import android.util.Base64;
import java.nio.charset.Charset;
import android.content.pm.PackageManager;
import com.emanuelef.remote_capture.R;

public class qrmdm extends Activity {
    ServerSocket ss;
    Socket soc;
    Context mcon=this;
    BufferedReader is;
    BufferedOutputStream os;
    PrintWriter out;
    String resul="";
    EditText edt;
    TextView tv,tvtitle,tvline,tvinstuctions;
    String ip="";
    boolean mavailable=false;
	String mstatecon="";
    String mbustatbar="";
	String mstatedown="";
    LinearLayout linl,lima;
    RelativeLayout rel;
    Button buinstructions,bubarcode,bubarwi,bubarhot;
    int mdeviwidth,mdeviheigth,mdeviwidthcal,mdeviheigthcal=0;
    HorizontalScrollView hscl;
    ScrollView scl;
    String patmdm="/storage/emulated/0/mdm.apk";
    
    @Deprecated
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        permi();
        try {
            if (!new File("/storage/emulated/0/").listFiles()[0].canWrite()) {
                goManagerFileAccess();
            }
        } catch (Exception e) {
            goManagerFileAccess();
        }
        DisplayMetrics dm=new DisplayMetrics();
        Point poi=new Point();
        ((WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getRealMetrics(dm);
        ((WindowManager) getApplicationContext().getSystemService(WINDOW_SERVICE)).getDefaultDisplay().getSize(poi);
        getActionBar().hide();
        mdeviwidth = dm.widthPixels;
        mdeviheigth = poi.y;
        
        if (dm.widthPixels <= dm.heightPixels) {
            mdeviwidthcal = poi.x;
            mdeviheigthcal = poi.y - 50;
        } else {
            mdeviwidthcal = poi.y - 200;
            mdeviheigthcal = poi.x - 50;
        }
        hscl = new HorizontalScrollView(mcon);
        scl = new ScrollView(mcon);
        scl.setLayoutParams(new FrameLayout.LayoutParams(mdeviwidth, FrameLayout.LayoutParams.MATCH_PARENT));
        hscl.setLayoutParams(new LinearLayout.LayoutParams(mdeviwidth, FrameLayout.LayoutParams.MATCH_PARENT));
        linl = new LinearLayout(mcon);
        lima = new LinearLayout(mcon);
        rel = new RelativeLayout(mcon);
        linl.setGravity(Gravity.CENTER);
        rel.setGravity(Gravity.CENTER);
        buinstructions = new Button(mcon);
        bubarcode = new Button(mcon);
        bubarwi = new Button(mcon);
        bubarhot = new Button(mcon);
        edt = new EditText(mcon);
        tv = new TextView(mcon);
        tvtitle = new TextView(mcon);
        tvline = new TextView(mcon);
        tvinstuctions = new TextView(mcon);
        tv.setTextSize(20);
        tvtitle.setTextAlignment(4);
        tv.setTextAppearance(android.R.style.TextAppearance_Material_Title);
        tvtitle.setTextAppearance(android.R.style.TextAppearance_Material_Title);
        tvline.setTextAppearance(android.R.style.TextAppearance_Material_Title);
        tvinstuctions.setTextAppearance(android.R.style.TextAppearance_Material_Title);
        tvtitle.setTextSize(35);
        tvline.setTextSize(33);
        tvinstuctions.setTextSize(15);
        linl.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        linl.setOrientation(linl.VERTICAL);
        scl.addView(linl);
        hscl.addView(scl);
        setContentView(hscl);
        try {
            tha();
            mai();
        } catch (Exception e) {
            Toast.makeText(this, "" + e, Toast.LENGTH_LONG).show();
        }
    }
    public void permi() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 55);
            }
        }
    }
    private void goManagerFileAccess() {
        if (Build.VERSION.SDK_INT >= 30) {
            Intent intent = new Intent("android.settings.MANAGE_APP_ALL_FILES_ACCESS_PERMISSION");
            intent.setData(Uri.parse(new StringBuffer().append("package:").append(getPackageName()).toString()));
            startActivity(intent);

        }
    }
    String statelinl="home";
    @Deprecated
    @Override
    public void onBackPressed() {
        if (!statelinl.equals("home")) {
            statelinl = "home";
            linl.removeAllViews();
            rel.removeAllViews();
            scl.scrollTo(0,0);
            mai();
        } else if (statelinl.equals("home")) {
            super.onBackPressed();
        }
    }
    @Deprecated
    private void mai() {
        
        lima.removeAllViews();
        hscl.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.FILL_PARENT));
        scl.setLayoutParams(new FrameLayout.LayoutParams(mdeviwidth, LinearLayout.LayoutParams.FILL_PARENT));
        linl.setLayoutParams(new FrameLayout.LayoutParams(mdeviwidth, LinearLayout.LayoutParams.FILL_PARENT));
        lima.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        lima.setGravity(Gravity.CENTER);
        linl.addView(lisq());
        rel.removeAllViews();
        rel.setLayoutParams(new RelativeLayout.LayoutParams(mdeviwidth, RelativeLayout.LayoutParams.WRAP_CONTENT));
        tvtitle.setText("הוראות שימוש");
        rel.setGravity(Gravity.CENTER);
        RelativeLayout.MarginLayoutParams rlpti=new RelativeLayout.MarginLayoutParams(400, 100);
        rlpti.setMargins(0, 0, (mdeviwidth / 2) - 200, 0);
        tvtitle.setLayoutParams(rlpti);

        RelativeLayout.MarginLayoutParams rlp=new RelativeLayout.MarginLayoutParams(400, 100);
        rlp.setMargins(0, 15, (mdeviwidth / 2) - 200, 0);
        tvline.setLayoutParams(rlp);
        tvline.setText("______________");
        tvline.setTextAlignment(4);
        RelativeLayout.MarginLayoutParams rlpinstru=new RelativeLayout.MarginLayoutParams(mdeviwidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
        rlpinstru.setMargins(0, 110, 0, 0);
        tvinstuctions.setLayoutParams(rlpinstru);
        //LinearLayout litt= litv("כאן תוכלו להתקין את אפליקציית קיידרואיד על מכשיר חדש או מכשיר מאופס להגדרות יצרן.\n\n'-לפניכם 2 אפשרויות התקנה:'-\n\n'-התקנה דרך נקודה חמה'-\nמיועדת להתקנה ישירות ממכשיר זה, במידה והוא מחובר לאינטרנט.\n\n'-התקנה דרך רשת Wi-Fi'-\nמיועדת להתקנה דרך רשת Wi-Fi משותפת למכשיר זה ולמכשיר החדש או המאופס (הנקודה חמה צריכה להיות כבויה).\n\nלאחר שבחרתם את שיטת ההתקנה המועדפת עליכם, האפליקציה תיצור עבורכם ברקוד מתאים.\nכעת, בצעו את הפעולות הבאות:\n\nהפעילו את המכשיר החדש / המאופס.\nהקישו 6 פעמים רצופות על מסך הפתיחה, עד שהמצלמה תיפתח.\nסרקו את הברקוד שמופיע על המכשיר השולט.\nהתחברו לרשת ה-Wi-Fi או לנקודה החמה שהוקמה במכשיר השולט.\nהמשיכו לפעול על פי ההוראות שתופענה על המסך.\nמזל טוב! יש לכם מכשיר קיידרואיד חדש.\n\nשימו לב:\nייתכן ואפליקציית קיידרואיד לא תעבור אוטומטית למכשיר החדש. במקרה כזה, פשוט התקינו אותה בעצמכם – ההרשאה כבר הוקמה.\n\nנ.ב. המכשיר אינו מוגן עד להפעלת קיידרואיד בפועל.\n");
        LinearLayout litt= litv(getResources().getString(R.string.in2));
        litt.setLayoutParams(rlpinstru);
        tvinstuctions.setTextAlignment(4);
        rel.setLayoutParams(new RelativeLayout.LayoutParams(mdeviwidth, RelativeLayout.LayoutParams.WRAP_CONTENT));
        rel.addView(tvtitle);
        rel.addView(tvline);
        rel.addView(litt);
        linl.addView(rel);
        lima.addView(bubarcode);
        bubarcode.setBackground(grd(bubarcode));
        bubarcode.setText("יצור הברקוד");
        bubarcode.setTextColor(Color.WHITE);
        bubarcode.setOnClickListener(new OnClickListener(){

                @Override
                public void onClick(View p1) {
                    linl.removeAllViews();
                    scl.scrollTo(0,0);
                    statelinl = "barcode";
                    getDeviceIpAddressc();
                }
            });
        
        linl.addView(lima);
    }
    String actualConnectedToNetwork = null;
    Bitmap bitm;
    @Deprecated
    private String getDeviceIpAddressc() {
        actualConnectedToNetwork = null;
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        
        if (TextUtils.isEmpty(actualConnectedToNetwork)) {
            if (getNetworkInterfaceIpAddress() != null && !getNetworkInterfaceIpAddress().equals(getWifiIp())) {
                actualConnectedToNetwork = "" + getNetworkInterfaceIpAddress();
                resul += "hotspot" + actualConnectedToNetwork;
                ip = actualConnectedToNetwork;
                mavailable = true;
                mstatecon = "hotspot";
            }
        }
        if (connManager != null && TextUtils.isEmpty(actualConnectedToNetwork)) {
            NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (mWifi.isConnected()) {
                if (getWifiIp() != null) {
                    //tv.setText("wb");
                    actualConnectedToNetwork = "" + getWifiIp();
                    resul += "wifi" + actualConnectedToNetwork;
                    ip = actualConnectedToNetwork;
                    mavailable = true;
                    mstatecon = "wifi";
                }
            }
        }

        if (TextUtils.isEmpty(actualConnectedToNetwork)||!new File(patmdm).canRead()) {
           actualConnectedToNetwork = "פתח נקודה חמה או חבר לאותו ויפי ושים קובץ mdm.apk בזיכרון פנימי";
            resul = actualConnectedToNetwork;
            tv.setText(resul);
            mavailable = false;
            linl.addView(tv);
            linl.setGravity(Gravity.CENTER);
            Button bu=new Button(mcon);
            bu.setText("רענון...");
            bu.setTextColor(Color.WHITE);
            bu.setBackground(grd(bu));
            bu.setOnClickListener(new OnClickListener(){

                    @Override
                    public void onClick(View p1) {
                        linl.removeAllViews();
                        getDeviceIpAddressc();
                    }
                });
            linl.addView(bu);
            
        }
        if (mavailable) {

            bitm = enco(ip);
            ImageView img=new ImageView(mcon);
            img.setImageBitmap(bitm);
            linl.addView(img, mdeviwidthcal, mdeviwidthcal);
            
            Button busave=new Button(mcon);
            busave.setText("שמור");
            busave.setTextColor(Color.WHITE);
            busave.setBackground(grd(busave));
            LinearLayout libu=new LinearLayout(mcon);
            libu.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            libu.setGravity(Gravity.CENTER);
            libu.addView(busave);
            
            busave.setOnClickListener(new OnClickListener(){

                    @Override
                    public void onClick(View p1) {
                        try {
                            Bitmap.CompressFormat g= Bitmap.CompressFormat.PNG;
                            OutputStream gg=new FileOutputStream("/storage/emulated/0/" + "ברקוד" + ".png");
                            bitm.compress(g, 100, gg);
                            gg.close();
                            Toast.makeText(getApplicationContext(), "הברקוד נשמר ב " + "/storage/emulated/0/" + "ברקוד" + ".png!", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "err is= " + e, 0).show();
                        }
                    }
                });
            Button bu=new Button(mcon);
            bu.setText("רענון...");
            bu.setTextColor(Color.WHITE);
            bu.setBackground(grd(bu));
            bu.setOnClickListener(new OnClickListener(){

                    @Override
                    public void onClick(View p1) {
                        linl.removeAllViews();
                        getDeviceIpAddressc();

                    }
                });

            libu.addView(bu);
            linl.addView(libu);
            linl.setGravity(Gravity.CENTER);
            if (mstatecon.equals("hotspot")) {
                tv.setText("מצב נקודה חמה");
            } else if (mstatecon.equals("wifi")) {
                tv.setText("מצב ויפי");
            }
            linl.addView(tv);
            rel.removeAllViews();
            rel.setLayoutParams(new RelativeLayout.LayoutParams(mdeviwidth, RelativeLayout.LayoutParams.WRAP_CONTENT));
            tvtitle.setText("המשך");
            rel.setGravity(Gravity.CENTER);
            RelativeLayout.MarginLayoutParams rlpti=new RelativeLayout.MarginLayoutParams(400, 100);
            rlpti.setMargins(0, 0, (mdeviwidth / 2) - 200, 0);
            tvtitle.setLayoutParams(rlpti);

            RelativeLayout.MarginLayoutParams rlp=new RelativeLayout.MarginLayoutParams(400, 100);
            rlp.setMargins(0, 15, (mdeviwidth / 2) - 200, 0);
            tvline.setLayoutParams(rlp);
            tvline.setText("______");
            tvline.setTextAlignment(4);
            RelativeLayout.MarginLayoutParams rlpinstru=new RelativeLayout.MarginLayoutParams(mdeviwidth, RelativeLayout.LayoutParams.WRAP_CONTENT);
            rlpinstru.setMargins(0, 110, 0, 0);
            tvinstuctions.setLayoutParams(rlpinstru);
            
            LinearLayout litt= litv(getResources().getString(R.string.in3));
            litt.setLayoutParams(rlpinstru);
            tvinstuctions.setTextAlignment(4);
            rel.setLayoutParams(new RelativeLayout.LayoutParams(mdeviwidth, RelativeLayout.LayoutParams.WRAP_CONTENT));
            rel.addView(tvtitle);
            rel.addView(tvline);
            rel.addView(litt);
            linl.addView(rel);
        }
        return actualConnectedToNetwork;
    }
    @Deprecated
    private String getWifiIp() {
        final WifiManager mWifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (mWifiManager != null && mWifiManager.isWifiEnabled()) {
            int ip = mWifiManager.getConnectionInfo().getIpAddress();
            return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "."
                + ((ip >> 24) & 0xFF);
        }
        return null;
    }
    String name="";
    @Nullable
    public String getNetworkInterfaceIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface networkInterface = en.nextElement();
                
                if (networkInterface.getName().equals("ap0")) {
                    for (Enumeration<InetAddress> enumIpAddr = networkInterface.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            String host = inetAddress.getHostAddress();
                            if (!TextUtils.isEmpty(host) && !host.equals(getWifiIp())) {
                                return host;
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            resul += ex;
        }
        return null;
    }

    @Override
    protected void onDestroy() {
        try {
            ss.close();
        } catch (Exception e) {}
        super.onDestroy();
    }
	
    void tha() {
        new Thread(){
            @Override
            public void run() {
                try {
                    ss = new ServerSocket(7777);
                    while (true) {
                        ss.setReuseAddress(true);
                        Socket socket=ss.accept();
                        soc = socket;
                        //resul += soc.getInetAddress() + "\n";
                        String add="";
                        for (byte b:soc.getInetAddress().getAddress()) {
                            add += b;
                        }
                        //resul += add;
                        thold();
                    }
                } catch (Exception e) {
                    resul += e;
                }
            }
            @Override
            public void start() {
                super.start();
            }
        }.start();
    }
    
	void thold() {
        String fileRequested = null;
        BufferedReader
            in = null;
        try {
            out = new PrintWriter(soc.getOutputStream());
            os = new BufferedOutputStream(soc.getOutputStream());
			DataOutputStream dos=new DataOutputStream(soc.getOutputStream());
            in = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            String input = in.readLine();
            if (input == null) {
                return;}
            StringTokenizer parse = new StringTokenizer(input);
            String method = parse.nextToken().toUpperCase(); // we get the HTTP method of the client
            fileRequested = parse.nextToken().toLowerCase();
            if (fileRequested.contains("?"))
                fileRequested = fileRequested.split("\\?")[0];
            
            if (fileRequested.equals("/mdm.apk")) {
                resul += "\nrequest is mdm.apk";
				mstatedown = "זוהתה בקשת הורדת mdm";
				tv.setText(mstatedown);
                resul += "\ntype - " + getMimeType(fileRequested, mcon);
                
                FileInputStream i= new FileInputStream(new File(patmdm));
                byte[] fileData=new byte[i.available()];
                i.read(fileData);
                int fileLength = fileData.length;
                out.println("HTTP/1.1 200 OK");
                out.println("Server: Java HTTP Server from SSaurel : 1.0");
                out.println("Date: " + new Date());
                out.println("Content-type: " + getMimeType(fileRequested, mcon));
                out.println("Content-length: " + fileLength);
                //out.println("Content-Disposition: " + "attachment; filename=\"mdm.apk\"");
                out.println(); // blank line between headers and content, very important !
                out.flush();  // flush character output stream buffer

				os.write(fileData, 0, fileLength);
				mstatedown = "נשלח בהצלחה";
				tv.setText(mstatedown);
                os.flush();
				
                resul += "\nsended succesfuly";
            }
        } catch (FileNotFoundException fnfe) {
            resul += fnfe;
        } catch (IOException ioe) {
            resul += ioe;
            System.err.println("Server error : " + ioe);
        } finally {
            try {
                in.close();
                out.close();
                os.close();
                soc.close(); // we close socket connection
            } catch (Exception e) {
                resul += e;
                System.err.println("Error closing stream : " + e.getMessage());
            }}
    }
    public String getMimeType(String mAbsolutePath, Context context) {
        String fileName = getFileName(mAbsolutePath);
        String extension = getFileExtension(fileName);
        if (extension == null) {
            return "unknown_ext_null_mimeType";
        }
        String mimeType = null;
        if (mimeType == null || (mimeType != null && mimeType.endsWith("3gpp"))) {
            String[] projection = new String[]{"mime_type"};
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(MediaStore.Files.getContentUri("external"), projection, "_data = ?", new String[]{mAbsolutePath}, null);
                if (cursor != null && cursor.moveToNext()) {
                    String type = cursor.getString(cursor.getColumnIndexOrThrow("mime_type"));
                    if (type != null) {
                        mimeType = type;
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                resul += e;

                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                resul += th;
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (mimeType == null) {
            mimeType = f(extension);
        }

        if (mimeType == null) {
            mimeType = f(extension);
            return "unknown_ext_mimeType";
        }
        return mimeType;
    }

    public static String f(String str) {
        String toLowerCase = str.toLowerCase();
        try {
            str = MimeTypeMap.getSingleton().getMimeTypeFromExtension(toLowerCase);
            return str;
        } catch (RuntimeException e) {
            return null;
        }
    }
    public static String getFileExtension(String fileName) {
        if (fileName == null) {
            return null;
        }
        String extension = null;
        int lastDot = fileName.lastIndexOf(46);
        if (lastDot >= 0) {
            extension = fileName.substring(lastDot + 1).toLowerCase();
        }
        return extension;
    }
    public static String getFileName(String absolutePath) {
        int sepIndex = absolutePath.lastIndexOf("/");
        if (sepIndex >= 0) {
            return absolutePath.substring(sepIndex + 1);
        }
        return absolutePath;
    }
    public Bitmap enco(String ipres) {
        String checksum=mdmchecksum(patmdm);
        Bitmap i=encodeToQrCode("{\n    \"android.app.extra.PROVISIONING_DEVICE_ADMIN_COMPONENT_NAME\":\n    \"com.secureguard.mdm/.SecureGuardDeviceAdminReceiver\",\n\"android.app.extra.PROVISIONING_DEVICE_ADMIN_SIGNATURE_CHECKSUM\":\n\""+checksum+"\",\n\"android.app.extra.PROVISIONING_DEVICE_ADMIN_PACKAGE_DOWNLOAD_LOCATION\":\n\"http://" + ipres + ":7777/mdm.apk\",\n    \"android.app.extra.PROVISIONING_SKIP_ENCRYPTION\": true,\n    \"android.app.extra.PROVISIONING_LEAVE_ALL_SYSTEM_APPS_ENABLED\": true\n}", 1200, 1200);
        return i;
    }
    public static Bitmap encodeToQrCode(String text, int width, int height) {
        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = null;
        try {
            matrix = writer.encode(text, BarcodeFormat.QR_CODE, 1200, 1200);
        } catch (WriterException ex) {
            ex.printStackTrace();
        }
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bmp.setPixel(x, y, matrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }
        return bmp;
    }
    public TextView tvpo(String tv) {
        TextView tvp=new TextView(mcon);
        tvp.setTextAppearance(android.R.style.TextAppearance_Material_Title);
        tvp.setTextAlignment(4);
        tvp.setTextSize(25);
        tvp.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        tvp.setText(tv);
        return tvp;
    }
    public TextView tvnorm(String text) {
        TextView tvp2=new TextView(mcon);
        tvp2.setTextAppearance(android.R.style.TextAppearance_Material_Title);
        tvp2.setTextAlignment(4);
        tvp2.setTextSize(20);
        tvp2.setText(text);
        return tvp2;
    }
    public TextView tvline(String text) {
        TextView tvl=new TextView(mcon);
        tvl.setTextAlignment(4);
        tvl.setText(text);
        return tvl;
    }
    boolean po;
    public LinearLayout litv(String text) {
        po = false;
        LinearLayout lit=new LinearLayout(mcon);
        lit.setOrientation(lit.VERTICAL);
        lit.setGravity(Gravity.CENTER);
        
        lit.setLayoutParams(new LinearLayout.LayoutParams(mdeviwidth, LinearLayout.LayoutParams.WRAP_CONTENT));
        String[] strr= text.split("--");
        String re="" + strr.length;
        TextView tv=new TextView(mcon);
        tv.setTextAppearance(android.R.style.TextAppearance_Material_Title);
        tv.setText(re);
        
        for (String a:strr) {
            if (po) {
               
                lit.addView(tvpo(a));
                po = false;
            } else {

                if (a.contains("\n")) {
                    a = a.replaceFirst("\n", "");
                }
                if (a.contains("\n")) {
                    a = a.substring(0, a.lastIndexOf("\n"));
                }
                lit.addView(tvnorm(a));
                po = true;
            }
        }

        return lit;
    }
    public LinearLayout litvold(String text) {
        po = false;
        LinearLayout lit=new LinearLayout(mcon);
        lit.setOrientation(lit.VERTICAL);
        lit.setGravity(Gravity.CENTER);
        //lit.addView(tvnorm(text));
        lit.setLayoutParams(new LinearLayout.LayoutParams(mdeviwidth, LinearLayout.LayoutParams.WRAP_CONTENT));
        String[] strr= text.split("'-");
        String re="" + strr.length;
        TextView tv=new TextView(mcon);
        tv.setTextAppearance(android.R.style.TextAppearance_Material_Title);
        tv.setText(re);
        for (String a:strr) {
            if (po) {
                lit.addView(tvpo(a));
                po = false;
            } else {
                if (a.contains("\n")) {
                    a = a.replaceFirst("\n", "");
                }
                if (a.contains("\n")) {
                    a = a.substring(0, a.lastIndexOf("\n"));
                }
                lit.addView(tvnorm(a));
                po = true;
            }
        }

        return lit;
    }
    @Deprecated
    public GradientDrawable grd(View mview) {
        
        final GradientDrawable gdMenuBody = new GradientDrawable();
        mview.setOnTouchListener(new OnTouchListener(){
                @Deprecated
                @Override
                public boolean onTouch(final View p1, MotionEvent p2) {
                    gdMenuBody.setColor(Color.parseColor("#ff77ff92"));
                    new Handler().postDelayed(new Runnable(){

                            @Override
                            public void run() {
                                gdMenuBody.setColor(Color.parseColor("#ff006D52"));
                            }
                        }, 300);
                    
                    return false;
                }
            });
        gdMenuBody.setCornerRadius(35); //Set corner
        gdMenuBody.setColor(Color.parseColor("#ffD0AD69")); //Set background color
        gdMenuBody.setColor(Color.argb(165, 208, 173, 105));
        gdMenuBody.setColor(Color.parseColor("#ff6effff"));
        gdMenuBody.setColor(Color.parseColor("#ff006D52"));
        
        gdMenuBody.setShape(gdMenuBody.RECTANGLE);
        
        return gdMenuBody;
    }
    LinearLayout lisq() {
        LinearLayout lil=new LinearLayout(mcon);
        lil.setOrientation(lil.VERTICAL);
        lil.setLayoutParams(new FrameLayout.LayoutParams(mdeviwidth, LinearLayout.LayoutParams.FILL_PARENT));
        TextView tvtit=new TextView(mcon);
        tvtit.setText("הבהרה:");

        tvtit.setTextAppearance(android.R.style.TextAppearance_Material_Title);
        tvtit.setTextSize(25);
        tvtit.setTextAlignment(4);
        
        TextView tv=new TextView(mcon);
		tv.setText(getResources().getString(R.string.sq));
        
        tv.setTextAppearance(android.R.style.TextAppearance_Material_Title);
        tv.setTextSize(20);
        tv.setTextAlignment(4);
        lil.setGravity(Gravity.CENTER);
        lil.addView(tv);
        lil.setBackground(grdsq(lil));
        return lil;
    }
    public GradientDrawable grdsq(View mview) {

        final GradientDrawable gdMenuBody = new GradientDrawable();
        gdMenuBody.setCornerRadius(35); //Set corner
        gdMenuBody.setColor(Color.parseColor("#ffD0AD69")); //Set background color
        gdMenuBody.setColor(Color.argb(165, 208, 173, 105));
        gdMenuBody.setColor(Color.parseColor("#ffCEEBDE"));
        
        gdMenuBody.setShape(gdMenuBody.RECTANGLE);
        
        return gdMenuBody;
    }
    @Deprecated
    private String mdmchecksum(String pat){
        String res="";
        try{
        PackageInfo p= getPackageManager().getPackageArchiveInfo(pat,PackageManager.GET_SIGNATURES);
        
        String str="";
        try {
            Signature[] signatureArr = p.signatures;
            MessageDigest instance = MessageDigest.getInstance("SHA256");
            
            for (Signature toByteArray : signatureArr) {
                instance.update(toByteArray.toByteArray());
            }
            byte[] digest = instance.digest();
            String encode = Base64.encodeToString(digest, 0);
            StringBuffer stringBuffer = new StringBuffer();
            for (byte b : digest) {
                stringBuffer.append(Integer.toHexString(b & 255));
            }
            str = stringBuffer.toString();
            str=encode;
        } catch (Exception unused) {
            str = null;
        }
        byte[] decode = Base64.decode(str, 0);
        res=new String(decode, Charset.forName("UTF-8"));
        res=str;
        res= res.replace("+","-");
        res=res.replace("=","");
        res=res.replace("/","_");
        res=res.subSequence(0,res.length()-1).toString();
        }catch(Exception e){}
        return res;
        
    }
}
    
