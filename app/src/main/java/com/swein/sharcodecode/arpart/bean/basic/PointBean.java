package com.swein.sharcodecode.arpart.bean.basic;

import com.google.ar.sceneform.Node;
import com.swein.sharcodecode.arpart.builder.material.ARMaterial;
import com.swein.sharcodecode.arpart.builder.tool.ARTool;

import org.json.JSONException;
import org.json.JSONObject;

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

    public JSONObject toJSONObject() throws JSONException {

        JSONObject jsonObject = new JSONObject();

        jsonObject.put("x", String.valueOf(point.getLocalPosition().x));
        jsonObject.put("y", String.valueOf(point.getLocalPosition().y));
        jsonObject.put("z", String.valueOf(point.getLocalPosition().z));

        return jsonObject;
    }

    public void init(JSONObject jsonObject) throws JSONException {
        point = ARTool.createLocalNode(
                Float.parseFloat(jsonObject.getString("x")),
                Float.parseFloat(jsonObject.getString("y")),
                Float.parseFloat(jsonObject.getString("z")),
                ARMaterial.instance.pointMaterial, ARMaterial.instance.nodeShadow);
    }
}
