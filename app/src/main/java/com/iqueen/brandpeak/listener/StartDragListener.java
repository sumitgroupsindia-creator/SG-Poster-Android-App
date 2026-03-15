package com.sgdigitalposter.app.listener;

import androidx.recyclerview.widget.RecyclerView;

public interface StartDragListener {
    void requestDrag(RecyclerView.ViewHolder viewHolder);
    void onDragEnd(RecyclerView.ViewHolder viewHolder);
    void onDelete(int position);
}
