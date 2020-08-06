package com.swein.sharcodecode.arpart.bean;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.swein.sharcodecode.arpart.bean.basic.PlaneBean;
import com.swein.sharcodecode.arpart.builder.ARBuilder;
import com.swein.sharcodecode.arpart.builder.tool.ARTool;

import java.util.ArrayList;
import java.util.List;

public class RoomBean {

    private final static String TAG = "RoomBean";

    // normal vector of floor
    public Vector3 normalVectorOfPlane;

    // floor
    public PlaneBean floor;

    // ceiling
    public PlaneBean ceiling;

    // room wall
    public List<PlaneBean> wallList;

    // room height
    public float height;

    // room floor fixed y
    public float floorFixedY;

    public float area; // 면적
    public float circumference; // 둘레
    public float wallArea; // 벽면적
    public float volume; // 체적

    public RoomBean() {

        normalVectorOfPlane = new Vector3();

        floor = new PlaneBean();
        ceiling = new PlaneBean();
        wallList = new ArrayList<>();

        height = 0;
        floorFixedY = 0;
    }

    public void calculate(ARBuilder.ARUnit arUnit) {

        // 둘레
        circumference = 0;
        for(int i = 0; i < floor.segmentList.size(); i++) {
            circumference += floor.segmentList.get(i).length;
        }

        // 벽 면적
        wallArea = 0;
        for(int i = 0; i < floor.segmentList.size(); i++) {
            wallArea += ARTool.getLengthByUnit(arUnit, floor.segmentList.get(i).length) * ARTool.getLengthByUnit(arUnit, height);
        }

        // 면적
        List<Node> list = new ArrayList<>();
        for(int i = 0; i < floor.pointList.size(); i++) {
            list.add(floor.pointList.get(i).point);
        }
        area = ARTool.getAreaByUnit(arUnit, ARTool.calculateArea(list, normalVectorOfPlane));

        // 체적
        volume = ARTool.getLengthByUnit(arUnit, height) * area;
    }

    public void clear() {

        if(wallList != null) {
            for(int i = 0; i < wallList.size(); i++) {
                for(int j = 0; j < wallList.get(i).pointList.size(); j++) {
                    wallList.get(i).pointList.get(j).point.setParent(null);
                }
                wallList.get(i).pointList.clear();
            }
            wallList.clear();
            wallList = null;
        }


        if(ceiling != null) {
            for(int i = 0; i < ceiling.pointList.size(); i++) {
                ceiling.pointList.get(i).point.setParent(null);
            }
            ceiling.pointList.clear();
            ceiling = null;
        }


        if(floor != null) {
            for(int i = 0; i < floor.pointList.size(); i++) {
                floor.pointList.get(i).point.setParent(null);
            }
            floor.pointList.clear();
            floor = null;
        }


        normalVectorOfPlane = null;

        height = 0;
        floorFixedY = 0;

        area = 0;
        circumference = 0;
        wallArea = 0;
        volume = 0;
    }
}
