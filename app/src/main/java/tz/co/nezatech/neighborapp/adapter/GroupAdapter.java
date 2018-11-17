package tz.co.nezatech.neighborapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import tz.co.nezatech.neighborapp.R;
import tz.co.nezatech.neighborapp.model.Contact;
import tz.co.nezatech.neighborapp.model.Group;

import java.util.List;

public class GroupAdapter extends ArrayAdapter<Group> {

    private Context context;

    public GroupAdapter(@NonNull Context context, int resource, @NonNull List<Group> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        Group group = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.groups_list_item, null, true);

            holder.tvname = convertView.findViewById(R.id.name);
            holder.tvdesc = convertView.findViewById(R.id.desc);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvname.setText(group.getName());
        holder.tvdesc.setText(group.getMembers().size() + " neighbors");
        return convertView;
    }

    private class ViewHolder {
        protected TextView tvname, tvdesc;
    }
}
