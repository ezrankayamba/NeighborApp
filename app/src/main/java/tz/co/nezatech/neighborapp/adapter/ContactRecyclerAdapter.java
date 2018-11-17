package tz.co.nezatech.neighborapp.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;
import tz.co.nezatech.neighborapp.R;
import tz.co.nezatech.neighborapp.model.Contact;

import java.util.List;

public class ContactRecyclerAdapter extends RecyclerView.Adapter<ContactRecyclerAdapter.ListItemViewHolder> {
    private List<Contact> items;
    private ListUpdateListener listener;
    private final static int FADE_DURATION = 200;

    public interface ListUpdateListener {
        void selected(Contact contact);
    }

    public ContactRecyclerAdapter(List<Contact> modelData, ListUpdateListener listener) {
        this.listener = listener;
        if (modelData == null) {
            throw new IllegalArgumentException("modelData must not be null");
        }
        this.items = modelData;
    }
    private void setFadeAnimation(View view) {
        AlphaAnimation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(FADE_DURATION);
        view.startAnimation(anim);
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(
            ViewGroup viewGroup, int viewType) {
        View itemView = LayoutInflater.
                from(viewGroup.getContext()).
                inflate(R.layout.contacts_list_item_horizontal, viewGroup, false);

        return new ListItemViewHolder(itemView, new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                final Contact model = items.get(position);
                listener.selected(model);
            }
        });
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder viewHolder, int position) {
        final Contact model = items.get(position);
        viewHolder.name.setText(model.getName().split(" ")[0]);
        //setAnimation(viewHolder.itemView, position);
        //setFadeAnimation(viewHolder.itemView);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class ListItemViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        TextView name;
        private OnItemClickListener listener;
        //TextView msisdn;

        public ListItemViewHolder(View itemView, OnItemClickListener listener) {
            super(itemView);
            name = itemView.findViewById(R.id.name);
            this.listener = listener;
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View view) {
            listener.onItemClick(view, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View view) {
            listener.onItemClick(view, getAdapterPosition());
            return true;
        }
    }
}
