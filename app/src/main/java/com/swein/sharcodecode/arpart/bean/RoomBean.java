package com.swein.sharcodecode.arpart.bean;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.swein.sharcodecode.arpart.bean.basic.PlaneBean;
import com.swein.sharcodecode.arpart.builder.tool.MathTool;
import com.swein.sharcodecode.arpart.constants.ARConstants;

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
    public List<PlaneBean> wallList = new ArrayList<>();

    // object on the wall
    public List<PlaneBean> wallObjectList = new ArrayList<>();

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
        wallList.clear();
        wallObjectList.clear();

        height = 0;
        floorFixedY = 0;

        area = 0;
        circumference = 0;
        wallArea = 0;
        volume = 0;
    }

    public void calculate() {

        // 둘레
        circumference = 0;
        for(int i = 0; i < floor.segmentList.size(); i++) {
            circumference += floor.segmentList.get(i).length;
        }

        // 벽 면적
        wallArea = 0;
        for(int i = 0; i < floor.segmentList.size(); i++) {
            wallArea += MathTool.getLengthByUnit(ARConstants.arUnit, floor.segmentList.get(i).length) * MathTool.getLengthByUnit(ARConstants.arUnit, height);
        }

        // 면적
        List<Node> list = new ArrayList<>();
        for(int i = 0; i < floor.pointList.size(); i++) {
            list.add(floor.pointList.get(i).point);
        }
        area = MathTool.getAreaByUnit(ARConstants.arUnit, MathTool.calculateArea(list, normalVectorOfPlane));

        // 체적
        volume = MathTool.getLengthByUnit(ARConstants.arUnit, height) * area;
    }

    public void clear() {

        for(int i = 0; i < wallObjectList.size(); i++) {
            wallObjectList.get(i).clear();
        }
        wallObjectList.clear();


        for(int i = 0; i < wallList.size(); i++) {
            wallList.get(i).clear();
        }
        wallList.clear();


        if(ceiling != null) {
            ceiling.clear();
            ceiling = null;
        }

        if(floor != null) {
            floor.clear();
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
