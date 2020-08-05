package com.swein.sharcodecode.arpart.recordlist;

import android.os.Bundle;

import androidx.fragment.app.FragmentActivity;

import com.swein.sharcodecode.R;

public class ARRecordListActivity extends FragmentActivity {

    private final static String TAG = "ARRecordListActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r_record_list);
    }
}