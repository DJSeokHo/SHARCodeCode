package com.swein.sharcodecode.arpart.bean;

import com.google.ar.sceneform.math.Vector3;
import com.swein.sharcodecode.arpart.bean.struct.CeilingBean;
import com.swein.sharcodecode.arpart.bean.struct.FloorBean;
import com.swein.sharcodecode.arpart.bean.struct.WallBean;

public class RoomBean {

    // normal vector of floor
    public Vector3 normalVectorOfPlane;

    // floor
    public FloorBean floorBean;

    // ceiling
    public CeilingBean ceilingBean;

    // room wall
    public WallBean wallBean;

    // room height
    public float height;

    // room floor fixed y
    public float floorFixedY;

    public RoomBean() {
        normalVectorOfPlane = new Vector3();
        floorBean = new FloorBean();
        ceilingBean = new CeilingBean();
        wallBean = new WallBean();
        height = 0;
        floorFixedY = 0;
    }
}
