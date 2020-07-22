package com.swein.sharcodecode.framework.util.ar;

import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;

public class ARUtil {

    public static double getAnchorNodesDistanceMeters(AnchorNode startAnchorNode, AnchorNode endAnchorNode) {

        float dx = startAnchorNode.getAnchor().getPose().tx() - endAnchorNode.getAnchor().getPose().tx();
        float dy = startAnchorNode.getAnchor().getPose().ty() - endAnchorNode.getAnchor().getPose().ty();
        float dz = startAnchorNode.getAnchor().getPose().tz() - endAnchorNode.getAnchor().getPose().tz();

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static double getNodesDistanceMeters(Node startNode, Node endNode) {
        float dx = startNode.getWorldPosition().x - endNode.getWorldPosition().x;
        float dy = startNode.getWorldPosition().y - endNode.getWorldPosition().y;
        float dz = startNode.getWorldPosition().z - endNode.getWorldPosition().z;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static double getNodesDistanceMetersWithoutHeight(Node startNode, Node endNode) {
        float dx = startNode.getWorldPosition().x - endNode.getWorldPosition().x;
        float dz = startNode.getWorldPosition().z - endNode.getWorldPosition().z;

        return Math.sqrt(dx * dx + dz * dz);
    }
}
