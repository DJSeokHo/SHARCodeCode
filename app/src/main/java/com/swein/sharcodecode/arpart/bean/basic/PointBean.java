package com.swein.sharcodecode.arpart.bean.basic;

import com.google.ar.sceneform.Node;

public class PointBean {

    public Node point;

    public PointBean() {
        point = new Node();
    }

    public void clear() {
        if(point != null) {
            point.setParent(null);
            point = null;
        }
    }
}
