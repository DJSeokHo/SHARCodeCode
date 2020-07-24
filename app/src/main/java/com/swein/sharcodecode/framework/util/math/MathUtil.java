package com.swein.sharcodecode.framework.util.math;

import com.google.ar.sceneform.math.Vector3;

import java.util.List;

public class MathUtil {

    public static Vector3 getNormalVectorOfThreeVectors(Vector3 original, Vector3 b, Vector3 c) {

        Vector3 side1 = Vector3.subtract(b, original);
        Vector3 side2 = Vector3.subtract(c, original);

        return Vector3.cross(side1, side2);
    }

    /**
     * computes the area of a 3D planar polygon
     *
     * @param n:                   the number of vertices in the polygon
     * @param vector3List:         an array of n+1 points in a 2D plane with V[n]=V[0]
     * @param normalVectorOfPlane: unit normal vector of the polygon's plane
     * @return the (float) area of the polygon
     */
    public static float area3DPolygon(int n, List<Vector3> vector3List, Vector3 normalVectorOfPlane) {
        float area = 0;
        float an, ax, ay, az; // abs value of normal and its coords
        int coord;           // coord to ignore: 1=x, 2=y, 3=z
        int i, j, k;         // loop indices

        if (n < 3) {
            return 0;  // a degenerate polygon
        }

        // select largest abs coordinate to ignore for projection
        ax = (normalVectorOfPlane.x > 0 ? normalVectorOfPlane.x : -normalVectorOfPlane.x);    // abs x-coord
        ay = (normalVectorOfPlane.y > 0 ? normalVectorOfPlane.y : -normalVectorOfPlane.y);    // abs y-coord
        az = (normalVectorOfPlane.z > 0 ? normalVectorOfPlane.z : -normalVectorOfPlane.z);    // abs z-coord

        coord = 3;                    // ignore z-coord
        if (ax > ay) {
            if (ax > az) {
                coord = 1;   // ignore x-coord
            }
        }
        else if (ay > az) {
            coord = 2;  // ignore y-coord
        }

        // compute area of the 2D projection
        switch (coord) {
            case 1:
                for (i = 1, j = 2, k = 0; i < n; i++, j++, k++) {
                    area += (vector3List.get(i).y * (vector3List.get(j).z - vector3List.get(k).z));
                }
                break;
            case 2:
                for (i = 1, j = 2, k = 0; i < n; i++, j++, k++) {
                    area += (vector3List.get(i).z * (vector3List.get(j).x - vector3List.get(k).x));
                }
                break;
            case 3:
                for (i = 1, j = 2, k = 0; i < n; i++, j++, k++) {
                    area += (vector3List.get(i).x * (vector3List.get(j).y - vector3List.get(k).y));
                }
                break;
        }

        switch (coord) {    // wrap-around term
            case 1:
                area += (vector3List.get(n).y * (vector3List.get(1).z - vector3List.get(n - 1).z));
                break;
            case 2:
                area += (vector3List.get(n).z * (vector3List.get(1).x - vector3List.get(n - 1).x));
                break;
            case 3:
                area += (vector3List.get(n).x * (vector3List.get(1).y - vector3List.get(n - 1).y));
                break;
        }

        // scale to get area before projection
        an = (float) Math.sqrt(ax * ax + ay * ay + az * az); // length of normal vector

        switch (coord) {
            case 1:
                area *= (an / (2 * normalVectorOfPlane.x));
                break;
            case 2:
                area *= (an / (2 * normalVectorOfPlane.y));
                break;
            case 3:
                area *= (an / (2 * normalVectorOfPlane.z));
                break;
        }

        return area;
    }

}
