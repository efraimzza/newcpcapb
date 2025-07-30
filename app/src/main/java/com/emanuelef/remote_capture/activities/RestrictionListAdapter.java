package com.emanuelef.remote_capture.activities;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView; // הוסף
import android.widget.Switch; // שינוי מ-CheckBox
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
        final ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item_restriction, parent, false);
            holder = new ViewHolder();
            holder.restrictionIcon = (ImageView) convertView.findViewById(R.id.restriction_icon); // אתחול אייקון
            holder.restrictionName = (TextView) convertView.findViewById(R.id.restriction_name);
            holder.restrictionDescription = (TextView) convertView.findViewById(R.id.restriction_description); // אתחול תיאור
            holder.restrictionSwitch = (Switch) convertView.findViewById(R.id.restriction_switch); // שינוי ל-Switch
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final RestrictionItem restrictionItem = restrictionList.get(position);

        holder.restrictionIcon.setImageResource(restrictionItem.getIconResId()); // הגדרת אייקון
        holder.restrictionName.setText(restrictionItem.getName());
        holder.restrictionDescription.setText(restrictionItem.getDescription()); // הגדרת תיאור
        holder.restrictionSwitch.setChecked(restrictionItem.isEnabled());

        // **אין OnClickListener עבור ה-Switch/CheckBox עצמו**
        // הלחיצה מנוהלת על ידי ה-convertView (הפריט כולו)

        convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // שנה את מצב ה-switch ועדכן את ה-RestrictionItem
                    boolean newCheckedState = !holder.restrictionSwitch.isChecked();
                    holder.restrictionSwitch.setChecked(newCheckedState);
                    restrictionItem.setEnabled(newCheckedState);
                }
            });

        return convertView;
    }

    static class ViewHolder {
        ImageView restrictionIcon; // הוספה ל-ViewHolder
        TextView restrictionName;
        TextView restrictionDescription; // הוספה ל-ViewHolder
        Switch restrictionSwitch; // שינוי ל-Switch
    }
}
