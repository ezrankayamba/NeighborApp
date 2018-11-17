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
import tz.co.nezatech.neighborapp.model.Member;

import java.util.List;

public class MemberAdapter extends ArrayAdapter<Member> {

    private Context context;

    public MemberAdapter(@NonNull Context context, int resource, @NonNull List<Member> objects) {
        super(context, resource, objects);
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        Member contact = getItem(position);

        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.members_list_item, null, true);

            holder.tvname = convertView.findViewById(R.id.name);
            holder.tvnumber = convertView.findViewById(R.id.msisdn);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.tvname.setText(contact.getName());
        holder.tvnumber.setText(contact.getMsisdn());

        return convertView;
    }

    private class ViewHolder {
        protected TextView tvname, tvnumber;
    }
}
