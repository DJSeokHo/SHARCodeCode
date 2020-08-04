package com.swein.sharcodecode.arpart.bean.basic;

import java.util.ArrayList;
import java.util.List;

public class PlaneBean {

    public List<PointBean> pointList;
    public List<SegmentBean> segmentList;

    public PlaneBean() {
        pointList = new ArrayList<>();
        segmentList = new ArrayList<>();
    }

    public void createSegment() {

    }
}
