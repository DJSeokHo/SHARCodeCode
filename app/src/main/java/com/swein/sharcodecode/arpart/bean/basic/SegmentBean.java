package com.swein.sharcodecode.arpart.bean.basic;

public class SegmentBean {

    public PointBean startPoint;
    public PointBean endPoint;

    public float length = 0;
    
    public SegmentBean() {
        startPoint = new PointBean();
        endPoint = new PointBean();
    }
}
