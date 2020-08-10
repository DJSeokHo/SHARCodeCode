package com.swein.sharcodecode.arpart.recordlist;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.ARActivity;
import com.swein.sharcodecode.arpart.bean.RoomBean;
import com.swein.sharcodecode.arpart.constants.ARESSArrows;
import com.swein.sharcodecode.arpart.recordlist.adapter.ARRecordListAdapter;
import com.swein.sharcodecode.framework.util.activity.ActivityUtil;
import com.swein.sharcodecode.framework.util.debug.ILog;
import com.swein.sharcodecode.framework.util.eventsplitshot.eventcenter.EventCenter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ARRecordListActivity extends FragmentActivity {

    private final static String TAG = "ARRecordListActivity";

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private RecyclerView.LayoutManager layoutManager;
    private ARRecordListAdapter arRecordListAdapter;

    private ImageView imageViewAR;

    private List<RoomBean> roomBeanList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r_record_list);

        initESS();

        findView();
        setListener();
        initList();
    }

    private void initESS() {
        EventCenter.instance.addEventObserver(ARESSArrows.SAVE_ROOM_INFO, this, (arrow, poster, data) -> {
            JSONObject roomBeanJSONObject = (JSONObject) data.get("roomBeanJSONObject");
            ILog.iLogDebug(TAG, roomBeanJSONObject.toString());
            RoomBean roomBean = new RoomBean();
            try {
                roomBean.init(roomBeanJSONObject);
                roomBeanList.add(roomBean);

                insert(roomBean);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
        });
    }

    private void findView() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView = findViewById(R.id.recyclerView);
        imageViewAR = findViewById(R.id.imageViewAR);
    }

    private void setListener() {
        imageViewAR.setOnClickListener(view -> {
            ActivityUtil.startNewActivityWithoutFinish(this, ARActivity.class);
        });
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

    private void insert(RoomBean roomBean) {
        if(recyclerView == null) {
            return;
        }

        arRecordListAdapter.insert(roomBean);
    }

    private void reload() {

        if(recyclerView == null) {
            return;
        }

        arRecordListAdapter.reloadList(roomBeanList);
    }

    private void loadMore() {

//        if(recyclerView == null) {
//            return;
//        }
//
//        arRecordListAdapter.loadMoreList();

    }

    private void removeESS() {
        EventCenter.instance.removeAllObserver(this);
    }

    @Override
    protected void onDestroy() {
        removeESS();
        super.onDestroy();
    }
}