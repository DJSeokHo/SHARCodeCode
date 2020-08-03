package com.swein.sharcodecode.arpart.bean.basic;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.swein.sharcodecode.arpart.FaceToCameraNode;

public class SegmentBean {

    public Node lineNode;
    public FaceToCameraNode sizeMarkNode;
    public ViewRenderable sizeMarkTextView;
    public float length = 0;

    public PointBean startPoint;
    public PointBean endPoint;

    public SegmentBean(ViewRenderable sizeMarkTextView) {
        lineNode = new Node();
        sizeMarkNode = new FaceToCameraNode();
        this.sizeMarkTextView = sizeMarkTextView;

        startPoint = new PointBean();
        endPoint = new PointBean();
    }
}
