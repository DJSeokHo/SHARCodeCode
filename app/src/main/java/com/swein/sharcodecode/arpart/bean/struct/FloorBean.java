package com.swein.sharcodecode.arpart.bean.struct;

import com.swein.sharcodecode.arpart.bean.basic.PlaneBean;

public class FloorBean {

    // check floor build
    public boolean isReadyToAutoClose;
    public boolean isAutoClosed;

    public PlaneBean planeBean;

    public FloorBean() {
        planeBean = new PlaneBean();
        isReadyToAutoClose = false;
        isAutoClosed = false;
    }

}
