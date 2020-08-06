package com.swein.sharcodecode.arpart.constants;

public class ARConstants {

    public final static double MIN_OPEN_GL_VERSION = 3.0;

    public enum MeasureHeightWay {
        NONE, AUTO, DRAW
    }
    public enum ARUnit {
        M, CM
    }
    public enum ARProcess {
        DETECT_PLANE, MEASURE_HEIGHT_HINT, MEASURE_HEIGHT, MEASURE_ROOM, DRAW_WALL_OBJECT
    }

    // measure height way
    public static ARConstants.MeasureHeightWay measureHeightWay = ARConstants.MeasureHeightWay.NONE;

    // current unit
    public static ARConstants.ARUnit arUnit = ARConstants.ARUnit.M;

    // build process state
    public static ARConstants.ARProcess arProcess = ARConstants.ARProcess.DETECT_PLANE;
}