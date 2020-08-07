package com.swein.sharcodecode.arpart.recordlist;

import android.os.Bundle;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.recordlist.adapter.ARRecordListAdapter;
import com.swein.sharcodecode.framework.util.thread.ThreadUtil;

public class ARRecordListActivity extends FragmentActivity {

    private final static String TAG = "ARRecordListActivity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ARRecordListAdapter arRecordListAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r_record_list);

        findView();
        initList();
    }

    private void findView() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
    }


    private void initList() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(false);
            reload();
        });

        layoutManager = new LinearLayoutManager(this);

        arRecordListAdapter = new ARRecordListAdapter();
        arRecordListAdapter.arRecordListAdapterDelegate = this::loadMore;

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(arRecordListAdapter);
    }

    private void reload() {

        if(recyclerView == null) {
            return;
        }

        arRecordListAdapter.reloadList();
    }

    private void loadMore() {

        if(recyclerView == null) {
            return;
        }

        arRecordListAdapter.loadMoreList();

    }
}