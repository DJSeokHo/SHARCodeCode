package com.swein.sharcodecode.arpart.bean.basic;

import org.json.JSONException;
import org.json.JSONObject;

public class SegmentBean {

    public PointBean startPoint;
    public PointBean endPoint;

    public float length;
    
    public SegmentBean() {
        startPoint = new PointBean();
        endPoint = new PointBean();
        length = 0;
    }

    public void clear() {

        if(startPoint != null) {
            startPoint.clear();
            startPoint = null;
        }

        if(endPoint != null) {
            endPoint.clear();
            endPoint = null;
        }
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("startPoint", startPoint.toJSONObject());
        jsonObject.put("endPoint", endPoint.toJSONObject());
        jsonObject.put("length", String.valueOf(length));

        return jsonObject;
    }

    public void init(JSONObject jsonObject) throws JSONException {

        startPoint.init(jsonObject.getJSONObject("startPoint"));
        endPoint.init(jsonObject.getJSONObject("endPoint"));
        length = Float.parseFloat(jsonObject.getString("length"));

    }
}
