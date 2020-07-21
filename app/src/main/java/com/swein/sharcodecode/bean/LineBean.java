package com.swein.sharcodecode.bean;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;

public class LineBean {

    public final static int STATE_NOT_READY = 0;
    public final static int STATE_READY = 1;
    public final static int STATE_FINISHED = 2;

    public Node startNode = null;
    public Node endNode = null;
    public float length = 0;
    public int state = STATE_NOT_READY;

    public void calculateLength() {

        Vector3 startVector3 = startNode.getWorldPosition();
        Vector3 endVector3 = endNode.getWorldPosition();
        Vector3 difference = Vector3.subtract(startVector3, endVector3);
        length = difference.length();
    }
}
