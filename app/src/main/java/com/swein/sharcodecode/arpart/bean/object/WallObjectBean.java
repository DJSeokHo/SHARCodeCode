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

}
