package com.swein.sharcodecode.arpart.bean.basic;

public class SegmentBean {

    public PointBean startPoint;
    public PointBean endPoint;

    public float length;
    
    public SegmentBean() {
        startPoint = new PointBean();
        endPoint = new PointBean();
        length = 0;
    }

    public void clear() {

        if(startPoint != null) {
            startPoint.clear();
            startPoint = null;
        }

        if(endPoint != null) {
            endPoint.clear();
            endPoint = null;
        }
    }
}
