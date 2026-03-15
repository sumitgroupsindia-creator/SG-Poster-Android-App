package com.sgdigitalposter.app.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.util.Pair;

import com.sgdigitalposter.app.R;
import com.sgdigitalposter.app.listener.StartDragListener;
import com.sgdigitalposter.app.ui.stickers.RelStickerView;
import com.sgdigitalposter.app.ui.stickers.text.AutofitTextRel;
import com.woxthebox.draglistview.DragItemAdapter;

import java.util.ArrayList;

public class NewDragDrop extends DragItemAdapter<Pair<Long, View>, NewDragDrop.ViewHolder>{

    public boolean mDragOnLongPress;
    public int mGrabHandleId;
    private int mLayoutId;
    public Activity activity;
    StartDragListener startDragListener;

    public NewDragDrop(Activity activity2, ArrayList<Pair<Long, View>> arrayList,
                              int i, int i2, boolean z, StartDragListener startDragListener) {
        this.mLayoutId = i;
        this.mGrabHandleId = i2;
        this.activity = activity2;
        this.mDragOnLongPress = z;
        this.startDragListener = startDragListener;
        setItemList(arrayList);
    }

    class ViewHolder extends DragItemAdapter.ViewHolder {
        public TextView mTitle;
        View rowView;
        ImageView ivDrag, iv_close, iv_hide, iv_main, iv_lock;

        public void onItemClicked(View view) {
        }

        public boolean onItemLongClicked(View view) {
            return true;
        }

        ViewHolder(View view) {
            super(view, mGrabHandleId, mDragOnLongPress);
            rowView = itemView;
            mTitle = itemView.findViewById(R.id.tvText);
            ivDrag = itemView.findViewById(R.id.ivDrag);
            iv_close = itemView.findViewById(R.id.ivRemove);
            iv_main = itemView.findViewById(R.id.ivPosterImg);
            iv_lock = itemView.findViewById(R.id.ivLock);
            iv_hide = itemView.findViewById(R.id.ivVisibility);
        }
    }

    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(this.mLayoutId, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        // Get the associated view from the item list
        View view = (View) ((Pair) this.mItemList.get(position)).second;

        // Reset visibility states for iv_main and mTitle
        holder.iv_main.setVisibility(View.GONE);
        holder.mTitle.setVisibility(View.VISIBLE);

        if (view instanceof AutofitTextRel) {
            // AutofitTextRel-specific bindings
            holder.mTitle.setText(((AutofitTextRel) view).getText().toString());
            holder.mTitle.setVisibility(View.VISIBLE);
            holder.iv_main.setVisibility(View.GONE);

            if (((AutofitTextRel) view).getVisibility() == View.VISIBLE) {
                holder.iv_hide.setImageResource(R.drawable.s_visible);
            } else {
                holder.iv_hide.setImageResource(R.drawable.s_hide);
            }

            if (((AutofitTextRel) view).isMultiTouchEnabled) {
                holder.iv_lock.setImageResource(R.drawable.s_unlock);
            } else {
                holder.iv_lock.setImageResource(R.drawable.s_lock);
            }
        } else if (view instanceof RelStickerView) {
            // RelStickerView-specific bindings
            holder.mTitle.setVisibility(View.GONE);
            holder.iv_main.setVisibility(View.VISIBLE);
            holder.iv_main.setImageBitmap(((RelStickerView) view).getMainImageBitmap());

            if (((RelStickerView) view).getVisibility() == View.VISIBLE) {
                holder.iv_hide.setImageResource(R.drawable.s_visible);
            } else {
                holder.iv_hide.setImageResource(R.drawable.s_hide);
            }

            if (((RelStickerView) view).isMultiTouchEnabled) {
                holder.iv_lock.setImageResource(R.drawable.s_unlock);
            } else {
                holder.iv_lock.setImageResource(R.drawable.s_lock);
            }
        }

        // Set up click listeners
        holder.iv_lock.setOnClickListener(v -> {
            if (view instanceof RelStickerView) {
                RelStickerView stickerView = (RelStickerView) view;
                boolean isLocked = !stickerView.isMultiTouchEnabled;
                stickerView.isMultiTouchEnabled = stickerView.setDefaultTouchListener(isLocked);
                holder.iv_lock.setImageResource(isLocked ? R.drawable.s_unlock : R.drawable.s_lock);
                if (!isLocked && stickerView.getBorderVisbilty()) {
                    stickerView.setBorderVisibility(false);
                }
            } else if (view instanceof AutofitTextRel) {
                AutofitTextRel textRel = (AutofitTextRel) view;
                boolean isLocked = !textRel.isMultiTouchEnabled;
                textRel.isMultiTouchEnabled = textRel.setDefaultTouchListener(isLocked);
                holder.iv_lock.setImageResource(isLocked ? R.drawable.s_unlock : R.drawable.s_lock);
                if (!isLocked && textRel.getBorderVisibility()) {
                    textRel.setBorderVisibility(false);
                }
            }
        });

        holder.iv_close.setOnClickListener(v -> startDragListener.onDelete(position));

        holder.iv_hide.setOnClickListener(v -> {
            if (view instanceof RelStickerView) {
                RelStickerView stickerView = (RelStickerView) view;
                boolean isVisible = stickerView.getVisibility() == View.VISIBLE;
                stickerView.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                holder.iv_hide.setImageResource(isVisible ? R.drawable.s_hide : R.drawable.s_visible);
                if (!isVisible && stickerView.getBorderVisbilty()) {
                    stickerView.setBorderVisibility(false);
                }
            } else if (view instanceof AutofitTextRel) {
                AutofitTextRel textRel = (AutofitTextRel) view;
                boolean isVisible = textRel.getVisibility() == View.VISIBLE;
                textRel.setVisibility(isVisible ? View.GONE : View.VISIBLE);
                holder.iv_hide.setImageResource(isVisible ? R.drawable.s_hide : R.drawable.s_visible);
                if (!isVisible && textRel.getBorderVisibility()) {
                    textRel.setBorderVisibility(false);
                }
            }
        });
    }

    public long getUniqueItemId(int i) {
        return ((Long) ((Pair) this.mItemList.get(i)).first).longValue();
    }
}