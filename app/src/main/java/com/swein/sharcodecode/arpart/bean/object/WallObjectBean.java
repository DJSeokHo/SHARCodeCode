package com.swein.sharcodecode.arpart.bean.object;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.ViewRenderable;

import java.util.ArrayList;
import java.util.List;

public class WallObjectBean {

    public List<Node> objectPointList = new ArrayList<>();
    public List<Node> objectLineList = new ArrayList<>();
    public List<Node> objectTextList = new ArrayList<>();
    public List<ViewRenderable> viewRenderableList = new ArrayList<>();

    public void clear() {

        for(int i = 0; i < objectPointList.size(); i++) {
            objectPointList.get(i).setParent(null);
        }
        objectPointList.clear();

        for(int i = 0; i < objectLineList.size(); i++) {
            objectLineList.get(i).setParent(null);
        }
        objectLineList.clear();

        for(int i = 0; i < objectTextList.size(); i++) {
            objectTextList.get(i).setParent(null);
        }
        objectTextList.clear();

        viewRenderableList.clear();
    }
}
