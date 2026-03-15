package com.sgdigitalposter.app.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sgdigitalposter.app.databinding.ItemSubsPlanBinding;
import com.sgdigitalposter.app.MyApplication;
import com.sgdigitalposter.app.items.SubsPlanItem;
import com.sgdigitalposter.app.listener.ClickListener;
import com.sgdigitalposter.app.utils.Constant;

import java.util.List;

public class SubsPlanAdapter extends RecyclerView.Adapter<SubsPlanAdapter.MyViewHolder> {

    Context context;
    ClickListener<SubsPlanItem> listener;
    List<SubsPlanItem> subsPlanItemList;

    public SubsPlanAdapter(Context context, ClickListener<SubsPlanItem> listener) {
        this.context = context;
        this.listener = listener;
    }

    public void subsPlanItemList(List<SubsPlanItem> subsPlanItemList) {
        this.subsPlanItemList = subsPlanItemList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemSubsPlanBinding binding = ItemSubsPlanBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MyViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.binding.setSubdata(subsPlanItemList.get(position));
        SubPointAdapter adapter = new SubPointAdapter(subsPlanItemList.get(position).pointItemList);
        holder.binding.lvPoints.setAdapter(adapter);
        holder.binding.lvPoints.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView rv, MotionEvent e) {
            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {
            }
        });
        holder.binding.lvPoints.setNestedScrollingEnabled(false);

        if(MyApplication.prefManager().getString(Constant.CURRENCY).equals("INR")){
            holder.binding.tvCurrency.setText("₹");
            holder.binding.tvCurrency2.setText("₹");
        }else if(MyApplication.prefManager().getString(Constant.CURRENCY).equals("USD")){
            holder.binding.tvCurrency.setText("$");
            holder.binding.tvCurrency2.setText("$");
        }

        holder.binding.cvSubs.setOnClickListener(v->{
            listener.onClick(subsPlanItemList.get(position));
        });
  }

    @Override
    public int getItemCount() {
        if (subsPlanItemList != null && subsPlanItemList.size() > 0) {
            return subsPlanItemList.size();
        } else {
            return 0;
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        ItemSubsPlanBinding binding;

        public MyViewHolder(ItemSubsPlanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
