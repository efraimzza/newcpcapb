package com.emanuelef.remote_capture.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.List;
import com.emanuelef.remote_capture.R;

public class RestrictionListAdapter extends ArrayAdapter<RestrictionItem> {

    private final Context context;
    private final List<RestrictionItem> restrictionList;

    public RestrictionListAdapter(Context context, List<RestrictionItem> restrictionList) {
        super(context, R.layout.list_item_restriction, restrictionList);
        this.context = context;
        this.restrictionList = restrictionList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder; // הפוך את holder ל-final

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_restriction, parent, false);
            holder = new ViewHolder();
            holder.restrictionName = (TextView) convertView.findViewById(R.id.tv_restriction_name);
            holder.restrictionCheckbox = (CheckBox) convertView.findViewById(R.id.cb_restriction);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final RestrictionItem restrictionItem = restrictionList.get(position);
        
        holder.restrictionName.setText(restrictionItem.getName());

        // **חשוב**: הגדר את מצב הצ'קבוקס ללא ליסנר כאן
        holder.restrictionCheckbox.setChecked(restrictionItem.isChecked());

        // **הסר את ה-OnClickListener מה-CheckBox עצמו!**
        // holder.restrictionCheckbox.setOnClickListener(...) - הסר את הבלוק הזה לחלוטין

        // הגדר OnClickListener עבור כל ה-Item (ה-View הראשי)
        convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // שנה את מצב ה-checkbox ועדכן את ה-RestrictionItem
                    boolean newCheckedState = !holder.restrictionCheckbox.isChecked();
                    holder.restrictionCheckbox.setChecked(newCheckedState);
                    restrictionItem.setChecked(newCheckedState);
                }
            });

        return convertView;
    }

    static class ViewHolder {
        TextView restrictionName;
        CheckBox restrictionCheckbox;
    }
}
