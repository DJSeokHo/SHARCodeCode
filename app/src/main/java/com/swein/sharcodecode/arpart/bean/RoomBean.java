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
        for(int i = 0; i < floor.pointList.size() - 1; i++) {
            circumference += Vector3.subtract(
                    floor.pointList.get(i + 1).point.getWorldPosition(),
                    floor.pointList.get(i).point.getWorldPosition()).length();
        }
        circumference += Vector3.subtract(
                floor.pointList.get(floor.pointList.size() - 1).point.getWorldPosition(),
                floor.pointList.get(0).point.getWorldPosition()
        ).length();

        // 벽 면적
        wallArea = ARTool.getLengthByUnit(arUnit, circumference) * ARTool.getLengthByUnit(arUnit, height);

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

        for(int i = 0; i < ceiling.pointList.size(); i++) {
            ceiling.pointList.get(i).point.setParent(null);
        }
        ceiling.pointList.clear();
        ceiling = null;

        for(int i = 0; i < floor.pointList.size(); i++) {
            floor.pointList.get(i).point.setParent(null);
        }
        floor.pointList.clear();
        floor = null;

        normalVectorOfPlane = null;

        height = 0;
        floorFixedY = 0;

        area = 0;
        circumference = 0;
        wallArea = 0;
        volume = 0;
    }
}
