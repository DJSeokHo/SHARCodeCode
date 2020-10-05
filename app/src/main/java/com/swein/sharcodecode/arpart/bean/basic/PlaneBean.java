package com.swein.sharcodecode.arpart.bean.basic;

import com.swein.sharcodecode.arpart.builder.tool.MathTool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlaneBean {

    public List<PointBean> pointList = new ArrayList<>();
    public List<SegmentBean> segmentList = new ArrayList<>();

    public String type;

    public int objectOnIndex = -1;

    public PlaneBean() {
        pointList.clear();
        segmentList.clear();
        type = "";
    }

    public void createSegment() {

        if(pointList.size() < 2) {
            return;
        }

        segmentList.clear();

        SegmentBean segmentBean;
        for(int i = 0; i < pointList.size() - 1; i++) {
            segmentBean = new SegmentBean();
            segmentBean.startPoint = pointList.get(i);
            segmentBean.endPoint = pointList.get(i + 1);
            segmentBean.length = MathTool.getLengthOfTwoNode(segmentBean.startPoint.point, segmentBean.endPoint.point);
            segmentList.add(segmentBean);
        }
        segmentBean = new SegmentBean();
        segmentBean.startPoint = pointList.get(pointList.size() - 1);
        segmentBean.endPoint = pointList.get(0);
        segmentBean.length = MathTool.getLengthOfTwoNode(segmentBean.startPoint.point, segmentBean.endPoint.point);
        segmentList.add(segmentBean);
    }

    public void clear() {

        for(int i = 0; i < pointList.size() - 1; i++) {
            pointList.get(i).clear();
        }
        pointList.clear();

        for(int i = 0; i < segmentList.size() - 1; i++) {
            segmentList.get(i).clear();
        }
        segmentList.clear();

        type = "";
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();

        JSONArray pointArray = new JSONArray();
        for(int i = 0; i < pointList.size(); i++) {
            pointArray.put(pointList.get(i).toJSONObject());
        }
        jsonObject.put("pointArray", pointArray);

        JSONArray segmentArray = new JSONArray();
        for(int i = 0; i < segmentList.size(); i++) {
            segmentArray.put(segmentList.get(i).toJSONObject());
        }
        jsonObject.put("segmentArray", segmentArray);

        jsonObject.put("type", type);

        jsonObject.put("objectOnIndex", objectOnIndex);
        return jsonObject;
    }

    public void init(JSONObject jsonObject) throws JSONException {

        JSONArray pointArray = jsonObject.getJSONArray("pointArray");
        JSONArray segmentArray = jsonObject.getJSONArray("segmentArray");

        PointBean pointBean;
        for(int i = 0; i < pointArray.length(); i++) {
            pointBean = new PointBean();
            pointBean.init(pointArray.getJSONObject(i));
            pointList.add(pointBean);
        }

        SegmentBean segmentBean;
        for(int i = 0; i < segmentArray.length(); i++) {
            segmentBean = new SegmentBean();
            segmentBean.init(segmentArray.getJSONObject(i));
            segmentList.add(segmentBean);
        }

        type = jsonObject.getString("type");
        objectOnIndex = jsonObject.getInt("objectOnIndex");
    }
}
