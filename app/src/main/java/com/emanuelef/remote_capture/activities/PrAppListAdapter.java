package com.emanuelef.remote_capture.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Switch; // שינוי מ-CheckBox
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import com.emanuelef.remote_capture.R;

public class PrAppListAdapter extends ArrayAdapter<AppItem> {

    private final Context context;
    private final List<AppItem> appList;
    private String mdmPackageName;

    public PrAppListAdapter(Context context, List<AppItem> appList) {
        super(context, R.layout.app_list_item, appList);
        this.context = context;
        this.appList = appList;
        this.mdmPackageName = context.getPackageName();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.app_list_item, parent, false);
            holder = new ViewHolder();
            holder.appIcon = (ImageView) convertView.findViewById(R.id.app_icon);
            holder.appName = (TextView) convertView.findViewById(R.id.app_name);
            holder.appPackage = (TextView) convertView.findViewById(R.id.app_package);
            holder.appLastUpdated = (TextView) convertView.findViewById(R.id.app_last_updated);
            holder.hideSwitch = (Switch) convertView.findViewById(R.id.hide_checkbox); // שינוי ל-Switch
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final AppItem appItem = appList.get(position);

        holder.appIcon.setImageDrawable(appItem.getIcon());
        holder.appName.setText(appItem.getName());
        holder.appPackage.setText(appItem.getPackageName());

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String lastUpdated = "עדכון אחרון: " + sdf.format(new Date(appItem.getLastUpdateTime()));
        holder.appLastUpdated.setText(lastUpdated);

        // הגדר את מצב ה-Switch ללא ליסנר כאן
        if (appItem.getPackageName().equals(mdmPackageName)) {
            holder.hideSwitch.setChecked(false); // תמיד לא מסומן עבור ה-MDM app
            holder.hideSwitch.setEnabled(false); // בטל את האפשרות ללחוץ עליו ישירות
        } else {
            holder.hideSwitch.setChecked(appItem.isHidden());
            holder.hideSwitch.setEnabled(true); // ודא שהוא מופעל אם לא ה-MDM
        }

        convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (appItem.getPackageName().equals(mdmPackageName)||appItem.getPackageName().equals("io.github.huskydg.magisk")) {
                        Toast.makeText(context, "לא ניתן לשנות אפליקציה זו.", Toast.LENGTH_SHORT).show();
                    } else {
                        boolean newCheckedState = !holder.hideSwitch.isChecked();
                        holder.hideSwitch.setChecked(newCheckedState);
                        appItem.setHidden(newCheckedState);
                    }
                }
            });

        return convertView;
    }

    static class ViewHolder {
        ImageView appIcon;
        TextView appName;
        TextView appPackage;
        TextView appLastUpdated;
        Switch hideSwitch; // שינוי ל-Switch
    }
}
