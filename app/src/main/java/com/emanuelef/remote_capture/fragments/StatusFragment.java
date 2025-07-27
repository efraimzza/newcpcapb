/*
 * This file is part of PCAPdroid.
 *
 * PCAPdroid is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * PCAPdroid is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with PCAPdroid.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Copyright 2020-24 - Emanuele Faranda
 */

package com.emanuelef.remote_capture.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.app.admin.FactoryResetProtectionPolicy;
import android.net.VpnService;
import android.net.Uri;
import android.os.UserManager;
import android.os.Build;
import android.provider.Settings;
import android.Manifest;

import android.os.Environment;
import android.content.SharedPreferences;
import android.widget.Button;
import android.app.AlertDialog;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;
import android.view.MotionEvent;
import android.text.TextWatcher;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.view.View.OnTouchListener;
import android.app.PendingIntent;
import android.content.pm.PackageInstaller;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import android.os.Handler;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.preference.PreferenceManager;

import com.emanuelef.remote_capture.activities.admin;

import com.emanuelef.remote_capture.AppsResolver;
import com.emanuelef.remote_capture.Log;
import com.emanuelef.remote_capture.MitmReceiver;
import com.emanuelef.remote_capture.PCAPdroid;
import com.emanuelef.remote_capture.activities.AppFilterActivity;
import com.emanuelef.remote_capture.model.AppDescriptor;
import com.emanuelef.remote_capture.model.AppState;
import com.emanuelef.remote_capture.CaptureService;
import com.emanuelef.remote_capture.R;
import com.emanuelef.remote_capture.Utils;
import com.emanuelef.remote_capture.activities.MainActivity;
import com.emanuelef.remote_capture.interfaces.AppStateListener;
import com.emanuelef.remote_capture.model.Prefs;
import com.emanuelef.remote_capture.model.CaptureStats;
import com.emanuelef.remote_capture.views.PrefSpinner;

import java.util.ArrayList;
import java.util.Set;

public class StatusFragment extends Fragment implements AppStateListener, MenuProvider {
    private static final String TAG = "StatusFragment";
    private Menu mMenu;
    private MenuItem mStartBtn;
    private MenuItem mStopBtn;
    //private MenuItem mOpenPcap;
    //private MenuItem mDecryptPcap;
    private ImageView mFilterIcon;
    private MenuItem mMenuSettings;
    private TextView mInterfaceInfo;
    private View mCollectorInfoLayout;
    private TextView mCollectorInfoText;
    private ImageView mCollectorInfoIcon;
    private TextView mCaptureStatus;
    private TextView startmdm;
    private TextView removemdm;
    private TextView tvaa;
    private TextView tvab;
    TextView tvac;
    TextView tvad;
    //private View mQuickSettings;
    private MainActivity mActivity;
    private SharedPreferences mPrefs;
    private TextView mFilterDescription;
    private SwitchCompat mAppFilterSwitch;
    private Set<String> mAppFilter;
    private TextView mFilterRootDecryptionWarning;
    private Context mcon;
    private ComponentName compName;
    SharedPreferences sp;
    SharedPreferences.Editor spe;
    public EditText edtxa,edtxb,edtxc;
    TextView tva,tvb,tvta,tvtb;
    Button bua,bub,buc;
    AlertDialog alertDialog,alertDialoga;
    PackageInstaller.Session openses;
    public static final String modesp="mode";
	public static sModetype smtype;
	AlertDialog alertDialogmode;
	
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mActivity = (MainActivity) context;
        mcon = context;
	compName = new ComponentName(context, admin.class);
	    
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mActivity.setAppStateListener(null);
        mActivity = null;
    }

    @Override
    public void onResume() {
        super.onResume();

        CaptureService.checkAlwaysOnVpnActivated();
        refreshStatus();
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
        return inflater.inflate(R.layout.status, container, false);
    }
    
    @Deprecated
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        mInterfaceInfo = view.findViewById(R.id.interface_info);
        mCollectorInfoLayout = view.findViewById(R.id.collector_info_layout);
        mCollectorInfoText = mCollectorInfoLayout.findViewById(R.id.collector_info_text);
        mCollectorInfoIcon = mCollectorInfoLayout.findViewById(R.id.collector_info_icon);
        mCaptureStatus = view.findViewById(R.id.status_view);
        startmdm = view.findViewById(R.id.startmdm);
        removemdm = view.findViewById(R.id.removemdm);
        tvaa = view.findViewById(R.id.tva);
        tvab = view.findViewById(R.id.tvb);
	    tvac = view.findViewById(R.id.tvc);
	    tvad = view.findViewById(R.id.tvd);
      //  setbuttonsmdm();
        
        //mQuickSettings = view.findViewById(R.id.quick_settings);
        mFilterRootDecryptionWarning = view.findViewById(R.id.app_filter_root_decryption_warning);
        mPrefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        mAppFilter = Prefs.getAppFilter(mPrefs);

        PrefSpinner.init(view.findViewById(R.id.dump_mode_spinner),
                R.array.pcap_dump_modes, R.array.pcap_dump_modes_labels, R.array.pcap_dump_modes_descriptions,
                Prefs.PREF_PCAP_DUMP_MODE, Prefs.DEFAULT_DUMP_MODE);

        mAppFilterSwitch = view.findViewById(R.id.app_filter_switch);
        View filterRow = view.findViewById(R.id.app_filter_text);
        TextView filterTitle = filterRow.findViewById(R.id.title);
        mFilterDescription = filterRow.findViewById(R.id.description);
        mFilterIcon = filterRow.findViewById(R.id.icon);

        filterTitle.setText(R.string.target_apps);

        if(!hasManageExternalStoragePermission(mcon)){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                requestManageExternalStoragePermission(mcon);
            } else if (!hasWriteExternalStoragePermission(mcon)) {
                requestWriteExternalStoragePermission(mActivity);
            }
        }

        mAppFilterSwitch.setOnClickListener((buttonView) -> {
            mAppFilterSwitch.setChecked(!mAppFilterSwitch.isChecked());
            openAppFilterSelector();
        });

        refreshFilterInfo();

        mCaptureStatus.setOnClickListener(v -> {
            if(mActivity.getState() == AppState.ready)
                mActivity.startCapture();
        });
        
        startmdm.setOnClickListener(v -> {
            mactivatepcapmdm();
        });

        removemdm.setOnClickListener(v -> {
            checkpassword(false,"removemdm");
        });
        tvaa.setOnClickListener(v -> {
            checkpassword(true,"changepwd");
        });
        tvab.setOnClickListener(v -> {
       
       try {
      List<PackageInstaller.SessionInfo> lses= mcon.getPackageManager().getPackageInstaller().getAllSessions();
      if (lses != null) {
         for (PackageInstaller.SessionInfo pses:lses) {
             if (pses != null) {
                try {
                    if (pses.getInstallerPackageName().equals(mcon.getPackageName())) {
                       mcon.getPackageManager().getPackageInstaller().abandonSession(pses.getSessionId());
                    }
                } catch (Exception e) {  
                    Toast.makeText(mcon, "" + e, 0).show();
                }
             }
         }
      }
   } catch (Exception e) {
           Toast.makeText(mcon, "" + e, 0).show();
   }
        
        new Thread(){public void run(){
        succ= Utils.downloadFile("https://raw.githubusercontent.com/efraimzz/whitelist/refs/heads/main/whitelistbeta.apk", mcon.getFilesDir()+"/updatebeta.apk");
        mend=true;
        }}.start();
       
        new Handler().post(new Runnable(){
                @Deprecated
                @Override
                public void run() {
                    if(!mend){
                    new Handler().postDelayed(this,1000);
                    }else{
                    if(succ){
                    appone(mcon.getFilesDir()+"/updatebeta.apk");
                    }
                        Toast.makeText(mcon, ""+succ, 1).show();
                        mend=false;
                        succ=false;
                    }
                }
            });
        });
	    sp=mcon.getSharedPreferences(mcon.getPackageName(),mcon.MODE_PRIVATE);
        spe=sp.edit();
        
        if(sp.getString(modesp,"").equals("")){
            smtype=sModetype.multimedia;
            spe.putString(modesp,smtype.name());
            spe.commit();
            Toast.makeText(mcon, smtype.name()+" is default",1).show();
        }else{
            try{
                smtype=sModetype.valueOf(sp.getString(modesp,""));
                Toast.makeText(mcon, smtype.name()+ " is now",1).show();
            }catch(Exception e){
                Toast.makeText(mcon, e+"",1).show();
            }
	}
	    String curmodestr="";
        switch (smtype){
            case multimedia:
                curmodestr = smtype.name();
                break;
            case all:
                curmodestr = smtype.name();
                break;
            case accmultimedia:
                curmodestr = smtype.name();
	}
	    tvac.setText(curmodestr);
        tvac.setOnClickListener(v -> {
            checkpassword(true,"changemode");
        });
        tvad.setOnClickListener(v -> {
         if(CaptureService.isServiceActive()){
            CaptureService.requestBlacklistsUpdate();
            Toast.makeText(mcon, "updating...",1).show();
            
         }
   });
        // Register for updates
        MitmReceiver.observeStatus(this, status -> refreshDecryptionStatus());
        CaptureService.observeStats(this, this::onStatsUpdate);

        // Make URLs clickable
        mCollectorInfoText.setMovementMethod(LinkMovementMethod.getInstance());

        /* Important: call this after all the fields have been initialized */
        mActivity.setAppStateListener(this);
        refreshStatus();
	//new
        //important add pcap to whitelist malware
        PCAPdroid.getInstance().getMalwareWhitelist().addApp(mcon.getPackageName());
        sp = mcon.getSharedPreferences(mcon.getPackageName(), mcon.MODE_PRIVATE);
	if (sp.getString("pwd", "").equals("")) {
            checkpassword(true,"welcome");
        }
    }
boolean succ=false;
        boolean mend=false;
    @Override
    public void onCreateMenu(@NonNull Menu menu, MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.main_menu, menu);

        mMenu = menu;
        mStartBtn = mMenu.findItem(R.id.action_start);
        mStopBtn = mMenu.findItem(R.id.action_stop);
        mMenuSettings = mMenu.findItem(R.id.action_settings);
        //mOpenPcap = mMenu.findItem(R.id.open_pcap);
        //mDecryptPcap = mMenu.findItem(R.id.decrypt_pcap);
        //mDecryptPcap.setVisible(PCAPdroid.getInstance().isUsharkAvailable());
        refreshStatus();
    }
    
public static enum sModetype{
        multimedia,
        all,
        accmultimedia;
}
    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem item) {
        return false;
    }

    private void recheckFilterWarning() {
        boolean hasFilter = ((mAppFilter != null) && (!mAppFilter.isEmpty()));

        mFilterRootDecryptionWarning.setVisibility((Prefs.getTlsDecryptionEnabled(mPrefs) &&
                Prefs.isRootCaptureEnabled(mPrefs)
                && !hasFilter) ? View.VISIBLE : View.GONE);
    }

    private void refreshDecryptionStatus() {
        MitmReceiver.Status proxy_status = CaptureService.getMitmProxyStatus();
        Context ctx = getContext();

        if((proxy_status == MitmReceiver.Status.START_ERROR) && (ctx != null))
            Utils.showToastLong(ctx, R.string.mitm_addon_error);

        mInterfaceInfo.setText((proxy_status == MitmReceiver.Status.RUNNING) ? R.string.mitm_addon_running : R.string.mitm_addon_starting);
    }

    private void refreshFilterInfo() {
        Context context = getContext();
        if(context == null)
            return;

        if((mAppFilter == null) || (mAppFilter.isEmpty())) {
            mFilterDescription.setText(R.string.capture_all_apps);
            mFilterIcon.setVisibility(View.GONE);
            mAppFilterSwitch.setChecked(false);
            return;
        }

        mAppFilterSwitch.setChecked(true);

        Pair<String, Drawable> pair = getAppFilterTextAndIcon(context);

        mFilterDescription.setText(pair.first);

        if (pair.second != null) {
            mFilterIcon.setImageDrawable(pair.second);
            mFilterIcon.setVisibility(View.VISIBLE);
        }
    }

    private void onStatsUpdate(CaptureStats stats) {
        Log.d("MainReceiver", "Got StatsUpdate: bytes_sent=" + stats.pkts_sent + ", bytes_rcvd=" +
                stats.bytes_rcvd + ", pkts_sent=" + stats.pkts_sent + ", pkts_rcvd=" + stats.pkts_rcvd);
        mCaptureStatus.setText(Utils.formatBytes(stats.bytes_sent + stats.bytes_rcvd));
    }

    private Pair<String, Drawable> getAppFilterTextAndIcon(@NonNull Context context) {
        Drawable icon = null;
        String text = "";

        if((mAppFilter != null) && (!mAppFilter.isEmpty())) {
            if (mAppFilter.size() == 1) {
                // only a single app is selected, show its image and text
                String package_name = mAppFilter.iterator().next();
                AppDescriptor app = AppsResolver.resolveInstalledApp(requireContext().getPackageManager(), package_name, 0);

                if((app != null) && (app.getIcon() != null)) {
                    icon = app.getIcon();
                    text = app.getName() + " (" + app.getPackageName() + ")";
                }
            } else {
                // multiple apps, show default icon and comprehensive text
                icon = ContextCompat.getDrawable(context, R.drawable.ic_image);
                ArrayList<String> parts = new ArrayList<>();

                for (String package_name: mAppFilter) {
                    AppDescriptor app = AppsResolver.resolveInstalledApp(requireContext().getPackageManager(), package_name, 0);
                    String tmp = package_name;

                    if (app != null)
                        tmp = app.getName();

                    parts.add(tmp);
                }

                text = Utils.shorten(String.join(", ", parts), 48);
            }
        }

        return new Pair<>(text, icon);
    }

    private void refreshPcapDumpInfo(Context context) {
        String info = "";

        Prefs.DumpMode mode = CaptureService.getDumpMode();

        switch (mode) {
        case NONE:
            info = getString(R.string.no_dump_info);
            break;
        case HTTP_SERVER:
            info = String.format(getResources().getString(R.string.http_server_status),
                    Utils.getLocalIPAddress(mActivity), CaptureService.getHTTPServerPort());
            break;
        case PCAP_FILE:
            info = getString(R.string.pcap_file_info);

            String pcapFname = CaptureService.getPcapFname();
            if(pcapFname != null)
                info = pcapFname;
            break;
        case UDP_EXPORTER:
            info = String.format(getResources().getString(R.string.collector_info),
                    CaptureService.getCollectorAddress(), CaptureService.getCollectorPort());
            break;
        case TCP_EXPORTER:
            info = String.format(getResources().getString(R.string.tcp_collector_info),
                    CaptureService.getCollectorAddress(), CaptureService.getCollectorPort());
            break;
        }

        mCollectorInfoText.setText(info);

        // Check if a filter is set
        Drawable drawable = null;
        if((mAppFilter != null) && (!mAppFilter.isEmpty())) {
            Pair<String, Drawable> pair = getAppFilterTextAndIcon(context);
            drawable = pair.second;
        }

        if (drawable != null) {
            mCollectorInfoIcon.setImageDrawable(drawable);
            mCollectorInfoIcon.setVisibility(View.VISIBLE);
        } else
            mCollectorInfoIcon.setVisibility(View.GONE);
    }

    @Override
    public void appStateChanged(AppState state) {
        Context context = getContext();
        if(context == null)
            return;

        if(mMenu != null) {
            if((state == AppState.running) || (state == AppState.stopping)) {
                mStartBtn.setVisible(false);
                mStopBtn.setEnabled(true);
                mStopBtn.setVisible(!CaptureService.isAlwaysOnVPN());
                mMenuSettings.setEnabled(false);
                //mOpenPcap.setEnabled(false);
                //mDecryptPcap.setEnabled(false);
            } else { // ready || starting
                mStopBtn.setVisible(false);
                mStartBtn.setEnabled(true);
                mStartBtn.setVisible(!CaptureService.isAlwaysOnVPN());
                mMenuSettings.setEnabled(true);//ja disable.. enable now for js
                //mOpenPcap.setEnabled(true);
                //mDecryptPcap.setEnabled(true);
            }
        }

        switch(state) {
            case ready:
                mCaptureStatus.setText(R.string.ready);
                mCollectorInfoLayout.setVisibility(View.GONE);
                mInterfaceInfo.setVisibility(View.GONE);
                //mQuickSettings.setVisibility(View.VISIBLE);
                mAppFilter = Prefs.getAppFilter(mPrefs);
                refreshFilterInfo();
                break;
            case starting:
                if(mMenu != null)
                    mStartBtn.setEnabled(false);
                break;
            case stopping:
                if(mMenu != null)
                    mStopBtn.setEnabled(false);
                break;
            case running:
                mCaptureStatus.setText(Utils.formatBytes(CaptureService.getBytes()));
                mCollectorInfoLayout.setVisibility(View.VISIBLE);
                //mQuickSettings.setVisibility(View.GONE);
                CaptureService service = CaptureService.requireInstance();

                if(CaptureService.isDecryptingTLS()) {
                    refreshDecryptionStatus();
                    mInterfaceInfo.setVisibility(View.VISIBLE);
                } else if(CaptureService.isCapturingAsRoot()) {
                    String capiface = service.getCaptureInterface();

                    if(capiface.equals("@inet"))
                        capiface = getString(R.string.internet);
                    else if(capiface.equals("any"))
                        capiface = getString(R.string.all_interfaces);

                    mInterfaceInfo.setText(String.format(getResources().getString(R.string.capturing_from), capiface));
                    mInterfaceInfo.setVisibility(View.VISIBLE);
                } else if(service.getSocks5Enabled() == 1) {
                    mInterfaceInfo.setText(String.format(getResources().getString(R.string.socks5_info),
                            service.getSocks5ProxyAddress(), service.getSocks5ProxyPort()));
                    mInterfaceInfo.setVisibility(View.VISIBLE);
                } else
                    mInterfaceInfo.setVisibility(View.GONE);

                mAppFilter = CaptureService.getAppFilter();
                refreshPcapDumpInfo(context);
                break;
            default:
                break;
        }
    }

    private void refreshStatus() {
        if(mActivity != null)
            appStateChanged(mActivity.getState());
        recheckFilterWarning();
    }

    private void openAppFilterSelector() {
        Intent intent = new Intent(requireContext(), AppFilterActivity.class);
        startActivity(intent);
    }
	
    public  static  void p(DevicePolicyManager devicePolicyManager, ComponentName componentName, String string, boolean bl) throws PackageManager.NameNotFoundException {
        //   try {
        devicePolicyManager.setAlwaysOnVpnPackage(componentName, string, bl);
        //  } catch (Exception e) {
        //   Toast.makeText(getApplicationContext(),""+e,Toast.LENGTH_SHORT).show();
        // }
    }
    void mactivatepcapmdm() {
		try {
            DevicePolicyManager dpm=(DevicePolicyManager)mcon.getSystemService("device_policy");
            dpm.addUserRestriction(compName, UserManager.DISALLOW_DEBUGGING_FEATURES);
            //dpm.setPackagesSuspended(compName,new String[]{getPackageName()},true);
            dpm.addUserRestriction(compName, UserManager.DISALLOW_FACTORY_RESET);
            dpm.addUserRestriction(compName, UserManager.DISALLOW_ADD_USER);
            dpm.addUserRestriction(compName, UserManager.DISALLOW_SAFE_BOOT);
            dpm.addUserRestriction(compName, UserManager.DISALLOW_CONFIG_VPN);
            dpm.addUserRestriction(compName, UserManager.DISALLOW_CONFIG_TETHERING);
            
			VpnService.prepare(mcon);
            try {
                p(dpm, compName, mcon.getPackageName(), true);
            } catch (PackageManager.NameNotFoundException e) {}
            List<String> arrayList = new ArrayList<>();
            arrayList.add("116673918161076927085");
            arrayList.add("107578790485390569043");
            arrayList.add("105993588108835326457");
            if (Build.VERSION.SDK_INT > 29) {
                try {
                    FactoryResetProtectionPolicy frp=new FactoryResetProtectionPolicy.Builder()
                        .setFactoryResetProtectionAccounts(arrayList)
                        .setFactoryResetProtectionEnabled(true)
                        .build();
                    dpm.setFactoryResetProtectionPolicy(compName, frp);
                } catch (Exception e) {
                    Toast.makeText(mcon, "e-frp" , Toast.LENGTH_SHORT).show();
                }
            }
            Bundle bundle = new Bundle();

            bundle.putStringArray("factoryResetProtectionAdmin", arrayList.toArray(new String[0]));

            //bundle=null;
            String str = "com.google.android.gms";
            dpm.setApplicationRestrictions(compName, str, bundle);
            Intent intent = new Intent("com.google.android.gms.auth.FRP_CONFIG_CHANGED");
            intent.setPackage(str);
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND);
            mcon.sendBroadcast(intent);
            Toast.makeText(mcon, "seted" + dpm.getActiveAdmins().toString(), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			  try{
                    //String[] strar = {"/system/bin/sh","-c",""};
                    String[] strar = {"su","-c",""};
                    String ed="";
                    //ed = edtx1.getText().toString();
                        ed="dpm set-device-owner com.emanuelef.remote_capture.debug/com.emanuelef.remote_capture.activities.admin";
                    int i = 0;
                    ed.split(" ",i++);
                    strar[2]=ed;
                    //strar[i]=ed;
                    String c ="";
                    
                    
                    try{
                        Process exec=Runtime.getRuntime().exec(strar);
                        exec.waitFor();
                        exec.getOutputStream();
                        //c = exec.getInputStream().toString();
                        //c=exec.getOutputStream().toString();
                        BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(exec.getInputStream()));
                        c=bufferedReader.readLine();
                        BufferedReader in=bufferedReader;
                        String st;
                        StringBuilder edtx1=new StringBuilder();
                        do {
                            st = in.readLine();
                            if (st != null) {
                                edtx1.append(st);
                                edtx1.append(String.valueOf("\n"));
                                //edtx1.setFocusable(false);

                                continue;
                            }
                        } while (st != null);
                        in.close();
                        //c=edtx1.toString();
                        
                        Toast.makeText(mcon, ""+c/*as+bufferedReader+exec.getInputStream()*/, Toast.LENGTH_LONG).show();
                    }catch(Exception eee){
                        Toast.makeText(mcon, "error"+eee, Toast.LENGTH_LONG).show();
                    }
                }
                catch (/*io*/Exception ee)
                {
                    Toast.makeText(mcon, "error"+ee, Toast.LENGTH_LONG).show();
                }
			Toast.makeText(mcon, "" + e, Toast.LENGTH_SHORT).show();
		}
	}
	@Deprecated
	void mremovepcapmdm() {
		try {
            DevicePolicyManager dpm=(DevicePolicyManager)mcon.getSystemService("device_policy");

            dpm.clearUserRestriction(compName, UserManager.DISALLOW_DEBUGGING_FEATURES);
            dpm.clearUserRestriction(compName, UserManager.DISALLOW_UNINSTALL_APPS);
            dpm.clearUserRestriction(compName, UserManager.DISALLOW_FACTORY_RESET);
            dpm.clearUserRestriction(compName, UserManager.DISALLOW_ADD_USER);
            dpm.clearUserRestriction(compName, UserManager.DISALLOW_SAFE_BOOT);
            dpm.clearUserRestriction(compName, UserManager.DISALLOW_CONFIG_VPN);
            dpm.clearUserRestriction(compName, UserManager.DISALLOW_CONFIG_TETHERING);
            
            try {
                p(dpm, compName, mcon.getPackageName(), false);
            } catch (PackageManager.NameNotFoundException e) {}
            //Intent inten = new Intent(mcon, MyVpnService.class);
            //mcon.stopService(inten);
            try {

                Bundle bundle = new Bundle();
                bundle = null;
                String str = "com.google.android.gms";
	       dpm=(DevicePolicyManager)mcon.getSystemService("device_policy");
		    
                dpm.setApplicationRestrictions(compName, str, bundle);
                Intent intent = new Intent("com.google.android.gms.auth.FRP_CONFIG_CHANGED");
                intent.setPackage(str);
                intent.addFlags(268435456);
                mcon.sendBroadcast(intent);
	        dpm=(DevicePolicyManager)mcon.getSystemService("device_policy");
                dpm.clearDeviceOwnerApp(mcon.getPackageName());

                Toast.makeText(mcon, "removed", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(mcon, "" + e, Toast.LENGTH_SHORT).show();
            }
            try {
		     dpm=(DevicePolicyManager)mcon.getSystemService("device_policy");

                if (dpm.isAdminActive(compName)) {
	           dpm=(DevicePolicyManager)mcon.getSystemService("device_policy");

                    dpm.removeActiveAdmin(compName);

                    Toast.makeText(mcon, "removed active admin", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(mcon, "" + e, Toast.LENGTH_SHORT).show();
            }
            try {
                StringBuilder stringBuilder = new StringBuilder("package:");
                stringBuilder.append(mcon.getPackageName());
                Uri parse = Uri.parse(stringBuilder.toString());
                //Toast.makeText(mcon,""+stringBuilder,Toast.LENGTH_SHORT).show();
                //b.V(parse, "parse(\"package:\" + context.packageName)");
                mcon.startActivity(new Intent("android.intent.action.DELETE", parse));
            } catch (Exception e) {

            }
        } catch (Exception e) {
            Toast.makeText(mcon, "" + e, Toast.LENGTH_SHORT).show();
        }
	}
	void setpassword() {
        try {
            sp = mcon.getSharedPreferences(mcon.getPackageName(), mcon.MODE_PRIVATE);
            if (sp.getString("pwd", "").equals("")) {
                spe = sp.edit();
                spe.putString("pwd", "");
                spe.commit();
            }
            HorizontalScrollView hsv=new HorizontalScrollView(mcon);
            FrameLayout.LayoutParams flp=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
            flp.gravity=Gravity.CENTER;
            
            ScrollView sv=new ScrollView(mcon);
            LinearLayout linl=new LinearLayout(mcon);
            linl.setOrientation(linl.VERTICAL);
            linl.setGravity(Gravity.CENTER);
            
            //linl.setLayoutParams(flp);
            tvta=new TextView(mcon);
            tvta.setTextSize(30);
            tvta.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_DialogWindowTitle);
            edtxa = new EditText(mcon);
            edtxa.setInputType(2);
            edtxb = new EditText(mcon);
            edtxb.setInputType(2);
            TextWatcher twa=new TextWatcher(){

                @Override
                public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
                }

                @Override
                public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
                    if (p1.length() > 4) {
                        edtxa.setText(p1.subSequence(0, 4));
                    }
                }

                @Override
                public void afterTextChanged(Editable p1) {
                    if (p1.length() > 4) {
                        edtxa.setText(p1.subSequence(0, 4));
                    }
                }
            };
            TextWatcher twb=new TextWatcher(){

                @Override
                public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {
                }

                @Override
                public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
                    if (p1.length() > 4) {
                        edtxb.setText(p1.subSequence(0, 4));
                    }
                }

                @Override
                public void afterTextChanged(Editable p1) {
                    if (p1.length() > 4) {
                        edtxb.setText(p1.subSequence(0, 4));
                    }
                }
            };
            edtxa.addTextChangedListener(twa);
            edtxb.addTextChangedListener(twb);
            tva = new TextView(mcon);
            bua = new Button(mcon);
            bua.setText(R.string.ok);
            linl.addView(tvta);
            linl.addView(edtxa);
            linl.addView(edtxb);
            linl.addView(tva);
            linl.addView(bua);
            sv.addView(linl);
            hsv.addView(sv);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mcon);
            alertDialogBuilder.setView(hsv);
            alertDialog = alertDialogBuilder.create();
            bua.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View p1) {
                        if (edtxa == null || edtxb == null) {
                        } else {
                            String resa=edtxa.getText().toString();
                            String resb=edtxb.getText().toString();
                            if (resa.equals(resb) && !resa.equals("")) {
                                spe = sp.edit();
                                spe.putString("pwd", resa);
                                spe.commit();
                                alertDialog.hide();
                            } else {
                                tva.setText(R.string.mnotmatchpwd);
                                Toast.makeText(mcon, R.string.mnotmatchpwd, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            alertDialog.show();
            hsv.setLayoutParams(flp);
            tvta.setText(R.string.mchangepwd);
            if (sp.getString("pwd", "").equals("")) {
                tva.setText(R.string.mwelcomepwd);
            }
            LinearLayout.LayoutParams llp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);

            edtxa.setLayoutParams(llp);
            edtxa.setWidth(100);
            edtxa.setTextSize(20);
            edtxb.setLayoutParams(llp);
            edtxb.setWidth(100);
            edtxb.setTextSize(20);
            tvta.setLayoutParams(llp);
            tva.setLayoutParams(llp);
            bua.setLayoutParams(llp);
        } catch (Exception e) {
            Toast.makeText(mcon, e + "", Toast.LENGTH_LONG).show();
        }
    }
    void checkpassword(final boolean change,String mtodo) {
        try {
            sp = mcon.getSharedPreferences(mcon.getPackageName(), mcon.MODE_PRIVATE);
            if (sp.getString("pwd", "").equals("")) {
                spe = sp.edit();
                spe.putString("pwd", "");
                spe.commit();
            }

            HorizontalScrollView hsv=new HorizontalScrollView(mcon);
            FrameLayout.LayoutParams flp=new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT);
            flp.gravity=Gravity.CENTER;
            //flp.setMargins(20,0,20,0);
            ScrollView sv=new ScrollView(mcon);
            LinearLayout linl=new LinearLayout(mcon);
            linl.setOrientation(linl.VERTICAL);
            linl.setGravity(Gravity.CENTER);
            tvtb=new TextView(mcon);
            tvtb.setTextSize(30);
            tvtb.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_DialogWindowTitle);
            edtxc = new EditText(mcon);
            edtxc.setInputType(2);
            TextWatcher tw=new TextWatcher(){

                @Override
                public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4) {

                }

                @Override
                public void onTextChanged(CharSequence p1, int p2, int p3, int p4) {
                    if (p1.length() > 4) {
                        edtxc.setText(p1.subSequence(0, 4));
                    }
                }

                @Override
                public void afterTextChanged(Editable p1) {
                    if (p1.length() > 4) {
                        edtxc.setText(p1.subSequence(0, 4));
                    }
                }
            };
            edtxc.addTextChangedListener(tw);
            tvb = new TextView(mcon);
            buc = new Button(mcon);
            buc.setText(R.string.ok);
            linl.addView(tvtb);
            linl.addView(edtxc);
            linl.addView(tvb);
            linl.addView(buc);
            sv.addView(linl);
            hsv.addView(sv);
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mcon);
            alertDialogBuilder.setView(hsv);
            alertDialoga = alertDialogBuilder.create();
            //alertDialoga.setContentView(hsv);
            //alertDialoga.setView(linl);
            if (!change) {
                //alertDialoga.setCancelable(false);
            }
            buc.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View p1) {
                        if (edtxc == null) {
                        } else {
                            String resa=edtxc.getText().toString();
                            String pwd=sp.getString("pwd", "");
                            if (pwd.equals(resa) && !resa.equals("")) {
                                if(mtodo.equals("")){
				    if (change) {
                                    setpassword();
				}else{
				      mremovepcapmdm();
				}
				}else if(mtodo.equals("changemode")){
			             mradiodialog();
				}else if(mtodo.equals("removemdm")){
			             mremovepcapmdm();
				}else if(mtodo.equals("changepwd")){
			             setpassword();
				}else if(mtodo.equals("welcome")){
			             setpassword();
				}
				    alertDialoga.hide();
                            } else {
                                tvb.setText(R.string.mnotmatchpwd);
                                Toast.makeText(mcon, R.string.mnotmatchpwd, Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            if (sp.getString("pwd", "").equals("")) {
                Toast.makeText(mcon, R.string.mwelcomepwd, Toast.LENGTH_LONG).show();
                setpassword();
            } else {
                alertDialoga.show();
                hsv.setLayoutParams(flp);
                LinearLayout.LayoutParams llp=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
                
                edtxc.setLayoutParams(llp);
                edtxc.setWidth(100);
                edtxc.setTextSize(20);
                tvtb.setLayoutParams(llp);
                tvb.setLayoutParams(llp);
                buc.setLayoutParams(llp);
                //linl.setLayoutParams(flp);
                tvtb.setText(R.string.mgetcurrentpwd);
            }
        } catch (Exception e) {
            Toast.makeText(mcon, e + "", Toast.LENGTH_LONG).show();
            //finish();
        }
    }
    void appone(String mappath) {
        String editable;
        try {
            PackageInstaller packageInstaller = mcon.getPackageManager().getPackageInstaller();

            PackageInstaller. SessionParams sessionParams = new PackageInstaller. SessionParams(1);
            openses = packageInstaller.openSession(packageInstaller.createSession(sessionParams));
           // editable = edtx1.getText().toString();
            editable = mappath;
            if (editable.equals("")) {
                Toast.makeText(mcon, "write the path!", 1).show();
                openses.abandon();
                return;
            }

            File file = new File(editable);
            if (file.exists() && file.canRead()) {

                InputStream FileInputStream = new FileInputStream(file);

                OutputStream openWrite = openses.openWrite("package", (long) 0, file.length());
                byte[] bArr = new byte[1024];
                while (true) {
                    int read = FileInputStream.read(bArr);
                    if (read >= 0) {
                        openWrite.write(bArr, 0, read);
                    } else {
                        openses.fsync(openWrite);
                        FileInputStream.close();
                        openWrite.close();

                        try {
                            Intent intent  = new Intent(mcon, StatusReceiver.class);
                            openses.commit(PendingIntent.getBroadcast(mcon, 0, intent, PendingIntent.FLAG_MUTABLE).getIntentSender());
                            return;
                        } catch (Throwable e) {}
                    }
                }
            }
            Toast.makeText(mcon, "not exsist or not readable!", 1).show();
            openses.abandon();
        } catch (Exception e2) {
            try {
                openses.abandon();
            } catch (Exception e22) {}
            editable = "";
            StackTraceElement[] stackTrace = e2.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                editable = editable+stackTraceElement;
            }
            Toast.makeText(mcon, ""+e2+editable, 1).show();
            //tv1.setText(editable);
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
    public static void requestWriteExternalStoragePermission(MainActivity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            activity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 101);
        }
    }
	void mradiodialog(){
        try{
            RadioGroup r= new RadioGroup(mcon);
            // r.setLayoutDirection(RadioButton.LAYOUT_DIRECTION_LTR);
            RadioButton rba=new RadioButton(mcon);
            rba.setText(sModetype.multimedia.name());
            //  rba.setLayoutDirection(RadioButton.LAYOUT_DIRECTION_LTR);
            rba.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT,RadioGroup.LayoutParams.MATCH_PARENT));
            RadioButton rbb=new RadioButton(mcon);
            rbb.setText(sModetype.all.name());
            rbb.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT,RadioGroup.LayoutParams.MATCH_PARENT));
           RadioButton rbc=new RadioButton(mcon);
            rbc.setText(sModetype.accmultimedia.name());
            rbc.setLayoutParams(new RadioGroup.LayoutParams(RadioGroup.LayoutParams.MATCH_PARENT,RadioGroup.LayoutParams.MATCH_PARENT));
         
            r.addView(rba);
            r.addView(rbb);
            r.addView(rbc);
            switch (smtype){
                case multimedia:
                    rba.setChecked(true);
                    break;
                case all:
                    rbb.setChecked(true);
                    break;
                case accmultimedia:
                    rbc.setChecked(true);
            }
            r.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){
                    @Override
                    public void onCheckedChanged(RadioGroup parent, int p2){
                        try{
                            RadioButton rt=parent.findViewById( parent.getCheckedRadioButtonId());
                            //tvac.setText(rt.getText());
                            String so=rt.getText().toString();
                            smtype=sModetype.valueOf(so);
                            spe.putString(modesp,smtype.name());
                            spe.commit();
				switch (smtype){
                case multimedia:
                    tvac.setText(R.string.mmode_multimedia);
                    break;
                case all:
                    tvac.setText(R.string.mmode_all);
                     break;
                case accmultimedia:
                    tvac.setText(R.string.mmode_all);
				}
                            Toast.makeText(mcon,""+rt.getText(),0).show();
                            alertDialogmode.hide();
                        }catch(Exception e){
                            Toast.makeText(mcon,""+e,0).show();
                        }
                    }
                });
            final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(mcon);
            LinearLayout lip=new LinearLayout(mcon);
            LinearLayout lipa=new LinearLayout(mcon);
            LinearLayout lipb=new LinearLayout(mcon);
            LinearLayout lipc=new LinearLayout(mcon);
            TextView tvp=new TextView(mcon);
            tvp.setText("mode");
            tvp.setTextSize(30);
            tvp.setTextAppearance(android.R.style.TextAppearance_DeviceDefault_DialogWindowTitle);
            lip.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT));
            lip.setOrientation(LinearLayout.VERTICAL);
            lipa.setOrientation(LinearLayout.VERTICAL);
            lipb.setOrientation(LinearLayout.VERTICAL);
            lipc.setOrientation(LinearLayout.VERTICAL);
            lip.addView(lipa);
            lip.addView(lipb);
            lip.addView(lipc);
            lipa.addView(tvp);
            lipb.addView(r);
            Button bu=new Button(mcon);
            bu.setText("cancel");
            lipc.addView(bu);
            alertDialogBuilder.setView(lip);
            alertDialogmode = alertDialogBuilder.create();
            bu.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View p1){
                        alertDialogmode.hide();
                    }
                });
            alertDialogmode.show();
        }catch(Exception e){
            Toast.makeText(mcon, "pa!"+e, 0).show();

        }
	}
}
