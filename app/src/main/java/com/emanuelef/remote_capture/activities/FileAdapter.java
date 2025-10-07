package com.emanuelef.remote_capture.activities;

import android.view.View;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.TextView;
import android.view.ViewGroup;
import android.graphics.drawable.Drawable;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.content.Context;
import android.widget.BaseAdapter;
import java.util.List;
import android.provider.DocumentsContract;
import java.util.Locale;
import java.io.File;

import com.emanuelef.remote_capture.R;

public class FileAdapter extends BaseAdapter {
    private final Context context;
    private final List<FileItem> fileList;

    public FileAdapter(Context context, List<FileItem> fileList) {
        this.context = context;
        this.fileList = fileList;
    }

    @Override
    public int getCount() {
        return fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    // View Holder Pattern
    static class ViewHolder {
        ImageView icon;
        TextView line1; // File name
        TextView line2; // Size and Time
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_file, parent, false);
            holder = new ViewHolder();
            holder.icon = (ImageView) convertView.findViewById(R.id.file_icon);
            holder.line1 = (TextView) convertView.findViewById(R.id.file_name);
            holder.line2 = (TextView) convertView.findViewById(R.id.file_details);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        FileItem item = fileList.get(position);

        // Line 1: Name
        holder.line1.setText(item.name);

        // Line 2: Size & Time conversion (No lambdas/streams)
        String sizeString = convertFileSize(item.size);
        String timeString = convertTime(item.lastModified);
        holder.line2.setText(sizeString + " | " + timeString);

        // Icon Logic
        Drawable icon = getFileIcon(item);
        holder.icon.setImageDrawable(icon);

        return convertView;
    }

    // --- Utility Methods (Outside of getView for clarity) ---

    private String convertFileSize(long size) {
        // Simple manual conversion logic (no lambdas/streams)
        if (size >= 1024 * 1024) {
            return String.format("%.2f MB", (double) size / (1024 * 1024));
        } else if (size >= 1024) {
            return String.format("%.2f KB", (double) size / 1024);
        } else {
            return size + " B";
        }
    }

    private String convertTime(long timestamp) {
        // Simple Date formatting (ymd hms) - no lambdas/streams
        // Requires importing java.util.Date and java.text.SimpleDateFormat
        Date date = new Date(timestamp);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(date);
    }
    @Deprecated
    private Drawable getFileIcon(FileItem item) {
        // Manual file type checking based on name/mimeType
        int iconResId;
        //if (item.mimeType.equals(DocumentsContract.Document.MIME_TYPE_DIR) || item.mimeType.equals("vnd.android.document/directory")) {
        if (new File(item.path).isDirectory()) {
            iconResId = R.drawable.ic_folder; // Replace with your actual drawable
        } else if (item.name.toLowerCase(Locale.ROOT).endsWith(".apk")||item.name.toLowerCase(Locale.ROOT).endsWith(".apks")||item.name.toLowerCase(Locale.ROOT).endsWith(".xapk")||item.name.toLowerCase(Locale.ROOT).endsWith(".apkm")) {
            iconResId = R.drawable.ic_apk; // Replace with your actual drawable
        } 
        // Add more manual checks for .xapk, .apks, etc.
        else {
            iconResId = R.drawable.ic_unknown; // Replace with your actual drawable
        }

        // Load the drawable (using Context.getDrawable() or Resources.getDrawable() depending on target API)
        // For broad compatibility, use Resources.getDrawable(id) on older APIs, or ContextCompat on newer APIs (but you restricted that)
        return context.getResources().getDrawable(iconResId);
    }
}
