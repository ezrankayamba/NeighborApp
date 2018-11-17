package tz.co.nezatech.neighborapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import tz.co.nezatech.neighborapp.R;
import tz.co.nezatech.neighborapp.model.Contact;

import java.util.List;

public class ContactAdapter extends ArrayAdapter<Contact> {

    private Context context;

    public ContactAdapter(@NonNull Context context, int resource, @NonNull List<Contact> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        Contact contact = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.contacts_list_item, null, true);

            holder.tvname = convertView.findViewById(R.id.name);
            holder.tvnumber = convertView.findViewById(R.id.msisdn);
            holder.imtick = convertView.findViewById(R.id.tick);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvname.setText(contact.getName());
        holder.tvnumber.setText(contact.getMsisdn());
        holder.imtick.setVisibility(contact.isSelected() ? View.VISIBLE : View.GONE);
        return convertView;
    }

    private class ViewHolder {
        protected TextView tvname, tvnumber;
        private ImageView imtick;
    }
}
