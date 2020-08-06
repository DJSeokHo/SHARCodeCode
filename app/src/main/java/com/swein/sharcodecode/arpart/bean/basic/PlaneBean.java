package com.swein.sharcodecode.arpart.bean.basic;

import com.swein.sharcodecode.arpart.builder.tool.MathTool;

import java.util.ArrayList;
import java.util.List;

public class PlaneBean {

    public List<PointBean> pointList = new ArrayList<>();
    public List<SegmentBean> segmentList = new ArrayList<>();

    public PlaneBean() {
        pointList.clear();
        segmentList.clear();
    }

    public void createSegment() {

        if(pointList.size() < 2) {
            return;
        }

        segmentList.clear();

        SegmentBean segmentBean;
        for(int i = 0; i < pointList.size() - 1; i++) {
            segmentBean = new SegmentBean();
            segmentBean.startPoint = pointList.get(i);
            segmentBean.endPoint = pointList.get(i + 1);
            segmentBean.length = MathTool.getLengthOfTwoNode(segmentBean.startPoint.point, segmentBean.endPoint.point);
            segmentList.add(segmentBean);
        }
        segmentBean = new SegmentBean();
        segmentBean.startPoint = pointList.get(pointList.size() - 1);
        segmentBean.endPoint = pointList.get(0);
        segmentBean.length = MathTool.getLengthOfTwoNode(segmentBean.startPoint.point, segmentBean.endPoint.point);
        segmentList.add(segmentBean);
    }

    public void clear() {

        for(int i = 0; i < pointList.size() - 1; i++) {
            pointList.get(i).clear();
        }
        pointList.clear();

        for(int i = 0; i < segmentList.size() - 1; i++) {
            segmentList.get(i).clear();
        }
        segmentList.clear();
    }
}
