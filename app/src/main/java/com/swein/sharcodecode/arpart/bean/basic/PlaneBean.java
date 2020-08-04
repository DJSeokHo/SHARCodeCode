package com.swein.sharcodecode.arpart.bean.basic;

import com.swein.sharcodecode.arpart.builder.tool.ARTool;

import java.util.ArrayList;
import java.util.List;

public class PlaneBean {

    public List<PointBean> pointList;
    public List<SegmentBean> segmentList;

    public PlaneBean() {
        pointList = new ArrayList<>();

    }

    public void createSegment() {

        if(pointList.size() < 2) {
            return;
        }

        segmentList = new ArrayList<>();

        SegmentBean segmentBean;
        for(int i = 0; i < pointList.size() - 1; i++) {
            segmentBean = new SegmentBean();
            segmentBean.startPoint = pointList.get(i);
            segmentBean.endPoint = pointList.get(i + 1);
            segmentBean.length = ARTool.getLengthOfTwoNode(segmentBean.startPoint.point, segmentBean.endPoint.point);
            segmentList.add(segmentBean);
        }
        segmentBean = new SegmentBean();
        segmentBean.startPoint = pointList.get(pointList.size() - 1);
        segmentBean.endPoint = pointList.get(0);
        segmentBean.length = ARTool.getLengthOfTwoNode(segmentBean.startPoint.point, segmentBean.endPoint.point);
        segmentList.add(segmentBean);
    }
}
