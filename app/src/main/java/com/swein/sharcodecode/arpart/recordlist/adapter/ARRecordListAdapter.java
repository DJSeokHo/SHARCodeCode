package com.swein.sharcodecode.arpart.recordlist.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.bean.RoomBean;
import com.swein.sharcodecode.arpart.recordlist.adapter.item.ARRecordListItemViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ARRecordListAdapter extends RecyclerView.Adapter {

    public interface ARRecordListAdapterDelegate {
        void loadMore();
    }

    private final static String TAG = "ARRecordListAdapter";

    private List<RoomBean> roomBeanList = new ArrayList<>();

    private static final int TYPE_ITEM_NORMAL_LIST = 0;

    public ARRecordListAdapterDelegate arRecordListAdapterDelegate;

    public ARRecordListAdapter() {
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_holder_ar_record_list_item, viewGroup, false);
        return new ARRecordListItemViewHolder(view);
    }



    public void loadMoreList(List<RoomBean> roomBeanList) {
        this.roomBeanList.addAll(roomBeanList);
        notifyItemRangeChanged(this.roomBeanList.size() - roomBeanList.size() + 1, roomBeanList.size());

    }

    public void insert(List<RoomBean> roomBeanList) {
        this.roomBeanList.addAll(roomBeanList);
        notifyItemInserted(roomBeanList.size() - 1);
    }


    public void reloadList(List<RoomBean> roomBeanList) {

        this.roomBeanList.clear();
        this.roomBeanList.addAll(roomBeanList);

        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ARRecordListItemViewHolder) {
            ARRecordListItemViewHolder arRecordListItemViewHolder = (ARRecordListItemViewHolder) holder;
            arRecordListItemViewHolder.roomBean = roomBeanList.get(position);
            arRecordListItemViewHolder.updateView();
        }

        if(position == roomBeanList.size() - 1) {
            arRecordListAdapterDelegate.loadMore();
        }
    }


    @Override
    public int getItemViewType(int position) {
        // change item view holder ui here
        return TYPE_ITEM_NORMAL_LIST;
    }

    @Override
    public int getItemCount() {
        return roomBeanList.size();
    }
}
