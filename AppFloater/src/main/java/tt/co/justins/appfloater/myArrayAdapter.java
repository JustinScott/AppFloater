package tt.co.justins.appfloater;


import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class myArrayAdapter extends ArrayAdapter {

    Context context;
    List list;
    int rowId;
    int textId;

    public myArrayAdapter(Context context, int resourceId, int textId, List list) {
        super(context, resourceId, list);
        this.context = context;
        this.rowId = resourceId;
        this.textId = textId;
        this.list = list;
    }

    class ViewHolder {
        public TextView textView;

        ViewHolder(View row) {
            textView = (TextView) row.findViewById(textId);
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder viewHolder;

        if(row == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(rowId, parent, false);

            viewHolder = new ViewHolder(row);
            row.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) row.getTag();
        }

        String name = list.get(position).toString();
        viewHolder.textView.setText(name);

        return row;
    }
}
