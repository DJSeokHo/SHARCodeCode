package com.swein.sharcodecode.arpart.builder.renderable;

import android.content.Context;

import com.google.ar.sceneform.rendering.ViewRenderable;
import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.builder.tool.ARTool;

public class ARRenderable {

    public static ARRenderable instance = new ARRenderable();
    private ARRenderable() {}

    public ViewRenderable guideSizeTextView;

    public void init(Context context) {
        ARTool.createViewRenderable(context, R.layout.view_renderable_text, viewRenderable -> {
            guideSizeTextView = viewRenderable;
        }, false);
    }

    public void destroy() {
        guideSizeTextView = null;
    }
}
