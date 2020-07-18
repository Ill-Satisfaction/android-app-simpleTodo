package com.example.myapplication;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.view.menu.MenuView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;

import java.util.List;

public class ItemsAdapter extends RecyclerView.Adapter<ItemsAdapter.ViewHolder>{

    public interface OnLongClickListener {
        void onItemLongClicked(int position);
    }

    public interface OnClickListener {
        void onItemClicked(int position);
    }

    List<String> items;
    OnLongClickListener longClickListener;
    OnClickListener clickListener;

    public ItemsAdapter( List<String> items, OnLongClickListener longClickListener, OnClickListener clickListener) {
        this.items = items;
        this.longClickListener = longClickListener;
        this.clickListener = clickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // use layout inflater to inflate a view
        //View todoView = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false); //this is the line methinks
        View todoView = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_list_item, parent, false);
        return new ViewHolder(todoView);
    }

    // bind data to a particular viewholder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        //grab item at position
        String item = items.get(position);
        // bind item
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    //  container to provided easy access to views for each row in list
    class ViewHolder extends RecyclerView.ViewHolder {

        TextView tvItem;
        ImageView tvImportanceIcon;
        ImageView tvTypeIcon;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItem = itemView.findViewById(R.id.listItemText);
            tvImportanceIcon = itemView.findViewById(R.id.listItemImportantIcon);
            tvTypeIcon = itemView.findViewById(R.id.listItemTypeIcon);
        }

        // update view inside view holder
        public void bind(String item) {
            if(item.length()>0) {
                // set icons
                switch (item.charAt(0)) {
                    case '!':
                        tvImportanceIcon.setImageResource(R.drawable.ic_baseline_priority_high_24);
                        break;
                    case '?':
                        tvImportanceIcon.setImageResource(R.drawable.ic_baseline_priority_high_24_inactive);
                        break;
                    default:
                        item = "#" + item;
                        tvImportanceIcon.setImageResource(R.drawable.ic_baseline_priority_high_24_inactive);
                }
                switch (item.charAt(1)) {
                    case '0':
                        tvTypeIcon.setImageResource(R.drawable.ic_baseline_watch_24);
                        break;
                    case '-':
                        tvTypeIcon.setImageResource(R.drawable.ic_baseline_notes_24);
                        break;
                    case '.':
                    default:
                        item = "#" + item;
                        tvTypeIcon.setImageResource(R.drawable.ic_baseline_fiber_manual_record_24);
                }
                // set text
                tvItem.setText(item.substring(2));
            }

            // add listeners
            tvItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    clickListener.onItemClicked(getAdapterPosition());
                }
            });

            tvItem.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    //notify the listener of the position of the long-pressed ite.
                    longClickListener.onItemLongClicked(getAdapterPosition());
                    return true;
                }
            });
        }
    }

}
