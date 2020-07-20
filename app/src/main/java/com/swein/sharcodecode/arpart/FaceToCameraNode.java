package com.swein.sharcodecode.arpart;

import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;

public class FaceToCameraNode extends Node {

    @Override
    public void onUpdate(FrameTime frameTime) {
        super.onUpdate(frameTime);

        if(getScene() != null) {

            Vector3 cameraPosition = getScene().getCamera().getWorldPosition();
            Vector3 nodePosition = getWorldPosition();
            Vector3 direction = Vector3.subtract(cameraPosition, nodePosition);

            setWorldRotation(Quaternion.lookRotation(direction, Vector3.up()));
        }
    }
}
