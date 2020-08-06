package com.swein.sharcodecode.arpart.builder.material;

import android.content.Context;
import android.graphics.Color;

import com.google.ar.sceneform.rendering.Material;
import com.swein.sharcodecode.arpart.builder.tool.ARTool;

public class ARMaterial {

    public static ARMaterial instance = new ARMaterial();
    private ARMaterial() {}

    // material
    public Material pointMaterial;
    public Material segmentMaterial;

    public Material objectPointMaterial;
    public Material objectSegmentMaterial;
    public Material objectGuideNodeMaterial;

    public Material guideNodeMaterial;
    public Material guideSegmentMaterial;


    public void init(Context context) {

        // create node material
        ARTool.createColorMaterial(context, Color.GREEN, material -> {
            pointMaterial = material;
            segmentMaterial = material;

            guideNodeMaterial = material;
            guideSegmentMaterial = material;
        });

        // create object node material
        ARTool.createColorMaterial(context, Color.BLUE, material -> {
            objectPointMaterial = material;
            objectSegmentMaterial = material;
            objectGuideNodeMaterial = material;
        });

    }

    public void destroy() {

        pointMaterial = null;
        segmentMaterial = null;

        objectPointMaterial = null;
        objectSegmentMaterial = null;
        objectGuideNodeMaterial = null;

        guideNodeMaterial = null;
        guideSegmentMaterial = null;
    }
}
