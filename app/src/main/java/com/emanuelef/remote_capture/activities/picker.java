package com.emanuelef.remote_capture.activities;

import android.app.Activity;
import android.os.Bundle;
import android.os.Build;
import android.widget.ListView;
import android.widget.AdapterView;
import android.widget.Toast;
import android.provider.DocumentsContract;
import android.view.View;
import java.io.File;
import android.os.Environment;
import java.util.List;
import java.util.ArrayList;
import android.webkit.MimeTypeMap;
import android.net.Uri;
import android.database.Cursor;
import android.content.Intent;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import java.util.Collections;
import java.util.Comparator;
import android.view.MotionEvent;
import java.util.concurrent.locks.ReentrantLock;

import com.emanuelef.remote_capture.R;

public class picker extends Activity { 

    ListView listView;
    String mpath="";
    FileAdapter adapter;
    List<FileItem> fileList;
    //final ReentrantLock mLock = new ReentrantLock();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);
        //LogUtil.initlogpat(getExternalFilesDir("")+"/log.txt");
        try {
            //requestVolumeAccess();

            listView = findViewById(R.id.file_list_view);
            fileList = new ArrayList<>();
            adapter = new FileAdapter(picker.this, fileList);
            listView.setAdapter(adapter);
            mpath = Environment.getExternalStorageDirectory().getAbsolutePath();
            mpath = "/storage";
            //mpath="/storage";
            reloadlist(mpath);
            //LogUtil.logToFile("res");
            /*
             List<FileItem> fileList=new ArrayList<>();
             for(File fi: new File(mpath).listFiles()){
             String extensi="";
             //try{
             //String extens=fi.getAbsolutePath().substring(fi.getAbsolutePath().lastIndexOf(46) + 1).toLowerCase();
             //extensi=MimeTypeMap.getSingleton().getMimeTypeFromExtension(""+extens);
             //}catch(Exception e){}
             fileList.add(new FileItem(fi.getName(),fi.getAbsolutePath(),extensi,fi.length(),fi.lastModified()));
             }
             final FileAdapter adapter = new FileAdapter(this, fileList); // Assume fileList is loaded
             listView.setAdapter(adapter);
             */
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Deprecated
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        FileItem clickedItem = (FileItem) parent.getItemAtPosition(position);
                        //if (clickedItem.mimeType.equals(DocumentsContract.Document.MIME_TYPE_DIR)) {
                        if (new File(clickedItem.path).isDirectory()) {
                            reloadlist(clickedItem.path);
                            // Folder click: Navigate to the new path (load files from the new path/URI)
                            // You'd need to re-run your loadFilesFromUri/loadFilesFromFile method with the new path/URI
                            // e.g., loadFilesFromUri(Uri.parse(clickedItem.path));
                        } else {
                            // File click: Show the full path in a Toast
                            if (clickedItem.path.toLowerCase().endsWith(".apk") ||
                                clickedItem.path.toLowerCase().toLowerCase().endsWith(".apks") ||
                                clickedItem.path.toLowerCase().toLowerCase().endsWith(".xapk") ||
                                clickedItem.path.toLowerCase().toLowerCase().endsWith(".apkm")) {
                                    AppManagementActivity.pickedfilepath=clickedItem.path;
                                    finish();
                                }
                            //Toast.makeText(getApplicationContext(), "Path: " + clickedItem.path, Toast.LENGTH_LONG).show();
                        }

                    }
                });
        } catch (Exception e) {
            LogUtil.logToFile(e.toString());
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }

    }
    boolean mlock=false;
	void reloadlist(String path) {
        //mLock.lock();
        mlock=true;
        listView.setEnabled(false);
        mpath = path;
        new Thread(){public void run() {
                try {
                    
                    //List<FileItem> fileList=new ArrayList<>();
                    fileList.clear();
                    StorageManager sm=(StorageManager) getSystemService(STORAGE_SERVICE);
                    List<String> storages=new ArrayList<>();

                    if(Build.VERSION.SDK_INT>=24){
                    for (StorageVolume stv : sm.getStorageVolumes()) {

                        if (!stv.isEmulated()) {
                            storages.add(stv.getUuid());
                        }
                        //tv.append(stv.getUuid()+" "+stv.getState()+" "+stv.isEmulated()+" "+stv.isPrimary()+" "+stv.isRemovable());
                    }
                    }else{
                        try{
                            if(new File("/storage").list()!=null){
                            for(String st:new File("/storage").list()){
                                storages.add(st);
                            }
                            }
                        }catch(Exception e){}
                    }
                    if (mpath.equals("/storage") || mpath.equals("/mnt") || mpath.equals("/")) {
                        mpath = "/storage";
                        
                        fileList.add(new FileItem("emulated", mpath + "/emulated", "", 0, 0));
                        for (String s:storages) {
                            if (new File(mpath + "/" + s).isDirectory())
                                fileList.add(new FileItem(s, mpath + "/" + s, "", 0, 0));
                        }
                        fileList.add(new FileItem("/mnt/media_rw", "/mnt/media_rw", "", 0, 0));
                        fileList.add(new FileItem("/sdcard", "/sdcard", "", 0, 0));
                        fileList.add(new FileItem("/sdcard0", "/sdcard0", "", 0, 0));
                    } else if (mpath.equals("/storage/emulated")) {
                        fileList.add(new FileItem("0", mpath + "/0", "", 0, 0));

                    } else {
                        if (new File(mpath).listFiles() != null) {
                            List<String> stl=new ArrayList<>();
                            for (File fi: new File(mpath).listFiles()) {
                                stl.add(fi.getName());
                            }
                            String[] sta=new String[stl.size()];
                            int i=0;
                            for (String s:stl) {
                                sta[i] = s;
                                i++;
                            }
                            sta = sortnameapp(sta, mpath + "/");
                            File[] lf=new File[sta.length];
                            i = 0;
                            for (String s:sta) {
                                lf[i] = new File(mpath, s);
                                i++;
                                //LogUtil.logToFile(mpath + s);
                            }

                            //for(File fi: new File(path).listFiles()){
                            for (File fi: lf) {
                                String extensi="";
                                /*try{
                                 String extens=fi.getAbsolutePath().substring(fi.getAbsolutePath().lastIndexOf(46) + 1).toLowerCase();

                                 //extens="txt";
                                 extensi=MimeTypeMap.getSingleton().getMimeTypeFromExtension(""+extens);
                                 }catch(Exception e){}*/
                                fileList.add(new FileItem(fi.getName(), fi.getAbsolutePath(), extensi, fi.length(), fi.lastModified()));
                            }
                        } else {
                            fileList.add(new FileItem("permission denied", mpath + "/permission denied", "", 0, 0));
                        }

                    }
                    if (fileList.size() == 0) {
                        //fileList.add(new FileItem("permission denied",mpath+"/permission denied","",0,0));
                    }
                    
                    getMainExecutor().execute(new Runnable(){

                            @Override
                            public void run() {
                                //mLock.unlock();
                                mlock=false;
                                listView.setEnabled(true);
                                adapter.notifyDataSetChanged();

                                
                            }
                        });
                } catch (Exception e) {
                    LogUtil.logToFile(e.toString());
                }
            }}.start();
    }
    @Deprecated
    @Override
    public void onBackPressed() {
        if(!mlock){
        if (mpath.equals("/storage") || mpath.equals("/")) {
            super.onBackPressed();
        } else {
            reloadlist(new File(mpath).getParent());
        }
        }
    }
    public String[] sortnameapp(String[] fgh, String pat) {
        List<String> li=new ArrayList<>();
        List<String> lidi=new ArrayList<>();
        List<String> limp=new ArrayList<>();
        int i=0;

        while (i < fgh.length) {
            File acurrentdir = new File(pat + fgh[i]);
            String name=pat + fgh[i];
            //Toast.makeText(con, "jump to "+acurrentdir, Toast.LENGTH_SHORT).show();
            if (acurrentdir.isDirectory() == true) {
                lidi.add(fgh[i]);
                i++;
            } else {
                if (cmpextensions(name)) {
                    limp.add(fgh[i]);
                    i++;
                } else {
                    li.add(fgh[i]);
                    i++;
                }
            }

        }

        Comparator<String> comparator=null;
        //int sort=SharedPref.getInteger("sort",1);
        int sort=1;
        switch (sort) {
            case 1:
                comparator = filecomparator.getInstance(filecomparator.SORT_BY_NAME, pat);
                break;
            case 2:
                comparator = filecomparator.getInstance(filecomparator.SORT_BY_SIZE, pat);
                break;
            case 3:
                comparator = filecomparator.getInstance(filecomparator.SORT_BY_TIME, pat);
                break;
            case 4:
                comparator = filecomparator.getInstance(filecomparator.SORT_BY_TYPE, pat);

        }
        
        Collections.sort(lidi, comparator);
        Collections.sort(limp, comparator);
        Collections.sort(li, comparator);

        i = 0;
        String[] fghtdi ={};
        fghtdi = lidi.toArray(fghtdi);
        while (i < fghtdi.length) {
            limp.add(fghtdi[i]);
            //LogUtil.logToFile("dir=" + fghtdi[i]);
            i++;}
        i = 0;
        String[] fght ={};
        fght = li.toArray(fght);
        while (i < fght.length) {
            limp.add(fght[i]);
            i++;}
        fgh = limp.toArray(fgh);
        
        return fgh;
    }

    boolean cmpextensions(String cmp) {
        boolean ret=false;
        String[] extenames={};

        extenames = new String[]{"apk","apks","xapk","apkm"};

        ArrayList<String> extensions =new ArrayList<>();  
        for (String s:extenames) {
            extensions.add("." + s);
        }
        boolean added=false;
        for (String s:extensions) {
            if (cmp.toLowerCase().endsWith(s)) {
                added = true;

                //do if true
                ret = true;

            }
        }
        if (! added) {
            //do of false
            ret = false;

        }


        return ret;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        return super.onTouchEvent(event);

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
    
    
    private void requestVolumeAccess() {
        // 1. Get initial access to emulated storage and prompt for external
        // On modern Android (API 24+), use this to prompt the user to select a directory (like the SD card root)
        // The user must grant access via this dialog.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        // You might need to set flags depending on your target API level
        // intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);

        // Use a unique request code
        startActivityForResult(intent, 42); // 42 for PICK_ROOT_FOLDER_REQUEST
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 42 && resultCode == RESULT_OK) {
            if (data != null) {
                Uri treeUri = data.getData();
                // Store this URI persistently (e.g., in SharedPreferences)
                // Also, take persistent URI permission
                getContentResolver().takePersistableUriPermission(treeUri, 
                                                                  Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                // Now, you can use DocumentFile.fromTreeUri(this, treeUri) to list files
                // DocumentFile is a convenience wrapper and might still be restricted on some older platform versions
                // but is the standard way to handle SAF-based file access.
                // NOTE: DocumentFile is part of the platform in some versions, but you might need to check its availability 
                // without AndroidX if targeting very old SDKs. If DocumentFile is not available, you MUST use 
                // ContentResolver queries, which are much more complex.

                // For a pure Java approach without DocumentFile (if it's not available in your targeted platform without dependencies):
                // You must use ContentResolver directly with treeUri and DocumentContract.
                loadFilesFromUri(treeUri); 
            }
        }
    }
    private void loadFilesFromUri(final Uri treeUri) {
        new Thread(){public void run() {
                // Requires platform-level access to DocumentContract (API 19+)
                final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri));

                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(childrenUri, 
                                                        new String[]{
                                                            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                                                            DocumentsContract.Document.COLUMN_MIME_TYPE,
                                                            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                                                            DocumentsContract.Document.COLUMN_SIZE,
                                                            DocumentsContract.Document.COLUMN_DOCUMENT_ID // Key to build new URIs for subfolders
                                                        }, 
                                                        null, null, null);

                    /*if (cursor != null && cursor.moveToFirst()) {
                     // Process the cursor data into your custom file POJOs
                     String sn="";
                     for(String s:cursor.getColumnNames()){
                     sn+=s+",";
                     }
                     String mname= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                     String mmimetype= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                     String mlastmodified= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
                     String msize= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                     String mdocid= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                     sn=mname+","+mmimetype+","+mlastmodified+","+msize+","+mdocid;
                     //LogUtil.logToFile(sn);
                     }*/

                    if (cursor != null &&
                        cursor.moveToFirst())
                        do{
                            String sn="";

                            String mname= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                            String mmimetype= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                            String mlastmodified= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
                            String msize= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                            String mdocid= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));

                            //Uri t= DocumentsContract.buildTreeDocumentUri(treeUri.getAuthority(),mdocid);
                            Uri s = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, mdocid);

                            mloadFilesFromUri(s);
                            sn = "," + "," + cursor.getCount() + "," + cursor.getPosition() + "," + mname + "," + mmimetype + "," + mlastmodified + "," + msize + "," + mdocid + "," + DocumentsContract.getTreeDocumentId(DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, mdocid));

                            //loadFilesFromUri( DocumentsContract.buildDocumentUri(treeUri.getAuthority(),mdocid));
                            //loadFilesFromUri(DocumentsContract.buildChildDocumentsUriUsingTree(treeUri,mdocid));
                            //LogUtil.logToFile(sn);
                        }while(cursor.moveToNext());

                    //for(int i=0;i<cursor.getCount();i++){
                    /*while(cursor.moveToNext()){
                     String sn="";

                     String mname= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                     String mmimetype= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                     String mlastmodified= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
                     String msize= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                     String mdocid= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                     sn=cursor.getCount()+","+cursor.getPosition()+","+ mname+","+mmimetype+","+mlastmodified+","+msize+","+mdocid;
                     //LogUtil.logToFile(sn);
                     }*/
                } catch (Exception e) {
                    LogUtil.logToFile(e.toString());
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }}.start();
    }
    private void mloadFilesFromUri(final Uri treeUri) {
        new Thread(){public void run() {
                // Requires platform-level access to DocumentContract (API 19+)
                //final Uri childrenUri = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, DocumentsContract.getTreeDocumentId(treeUri));

                Cursor cursor = null;
                try {
                    cursor = getContentResolver().query(treeUri, 
                                                        new String[]{
                                                            DocumentsContract.Document.COLUMN_DISPLAY_NAME,
                                                            DocumentsContract.Document.COLUMN_MIME_TYPE,
                                                            DocumentsContract.Document.COLUMN_LAST_MODIFIED,
                                                            DocumentsContract.Document.COLUMN_SIZE,
                                                            DocumentsContract.Document.COLUMN_DOCUMENT_ID // Key to build new URIs for subfolders
                                                        }, 
                                                        null, null, null);

                    /*if (cursor != null && cursor.moveToFirst()) {
                     // Process the cursor data into your custom file POJOs
                     String sn="";
                     for(String s:cursor.getColumnNames()){
                     sn+=s+",";
                     }
                     String mname= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                     String mmimetype= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                     String mlastmodified= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
                     String msize= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                     String mdocid= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                     sn=mname+","+mmimetype+","+mlastmodified+","+msize+","+mdocid;
                     //LogUtil.logToFile(sn);
                     }*/

                    if (cursor != null &&
                        cursor.moveToFirst())
                        do{
                            String sn="";

                            String mname= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                            String mmimetype= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                            String mlastmodified= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
                            String msize= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                            String mdocid= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));

                            /*String pat ="";
                             File ff=new File(treeUri.getPath());
                             for(File f:ff.getCanonicalFile().listFiles()){
                             pat+=f.getCanonicalPath()+"\n";
                             }*/
                            //Uri t= DocumentsContract.buildTreeDocumentUri(treeUri.getAuthority(),mdocid);
                            if (mmimetype.equals(DocumentsContract.Document.MIME_TYPE_DIR)) {

                                Uri s = DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, mdocid);
                                mloadFilesFromUri(s);
                            }
                            sn = "," + "," + cursor.getCount() + "," + cursor.getPosition() + "," + mname + "," + mmimetype + "," + mlastmodified + "," + msize + "," + mdocid + "," + DocumentsContract.getTreeDocumentId(DocumentsContract.buildChildDocumentsUriUsingTree(treeUri, mdocid));

                            //loadFilesFromUri( DocumentsContract.buildDocumentUri(treeUri.getAuthority(),mdocid));
                            //loadFilesFromUri(DocumentsContract.buildChildDocumentsUriUsingTree(treeUri,mdocid));
                            //LogUtil.logToFile(sn);
                        }while(cursor.moveToNext());

                    //for(int i=0;i<cursor.getCount();i++){
                    /*while(cursor.moveToNext()){
                     String sn="";

                     String mname= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                     String mmimetype= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                     String mlastmodified= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
                     String msize= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                     String mdocid= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));
                     sn=cursor.getCount()+","+cursor.getPosition()+","+ mname+","+mmimetype+","+mlastmodified+","+msize+","+mdocid;
                     //LogUtil.logToFile(sn);
                     }*/
                } catch (Exception e) {
                    LogUtil.logToFile(e.toString());
                    e.printStackTrace();
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }}.start();
    }
    void reloadlistcursor(final Cursor cursor) {
        new Thread(){public void run() {
                /*try{
                 mpath=path;
                 List<FileItem> fileList=new ArrayList<>();
                 for(File fi: new File(path).listFiles()){
                 String extensi="";
                 String mname= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DISPLAY_NAME));
                 String mmimetype= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_MIME_TYPE));
                 String mlastmodified= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_LAST_MODIFIED));
                 String msize= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_SIZE));
                 String mdocid= cursor.getString(cursor.getColumnIndex(DocumentsContract.Document.COLUMN_DOCUMENT_ID));

                 fileList.add(new FileItem(mname,mdocid,mmimetype,Long.parseLong(msize),Long.parseLong(mlastmodified)));
                 }
                 final FileAdapter adapter = new FileAdapter(picker.this, fileList); // Assume fileList is loaded
                 getMainExecutor().execute(new Runnable(){

                 @Override
                 public void run() {
                 listView.setAdapter(adapter);
                 }
                 });
                 }catch(Exception e){

                 }*/
            }}.start();
    }

} 
