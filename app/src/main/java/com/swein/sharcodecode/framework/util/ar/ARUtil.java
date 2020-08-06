package com.swein.sharcodecode.framework.util.ar;

import com.google.ar.sceneform.math.Vector3;

import java.util.List;

public class ARUtil {


    /**
     *
     * @param rayVector ray vector
     * @param rayPoint ray start point
     * @param planeNormal normal vector of plane
     * @param planePoint a point at the plane
     * @return
     */
//    public static Vector3 calculateIntersectionPointOfLineAndPlane(Vector3 rayVector, Vector3 rayPoint, Vector3 planeNormal, Vector3 planePoint) {
//        if (Vector3.dot(planeNormal, rayVector.normalized()) == 0) {
//            return null;
//        }
//
//        double t = (Vector3.dot(planeNormal, planePoint) - Vector3.dot(planeNormal, rayPoint)) / Vector3.dot(planeNormal, rayVector.normalized());
//
//        Vector3 result = new Vector3();
//        result.x = (float) (rayPoint.x + rayVector.normalized().x * t);
//        result.y = (float) (rayPoint.y + rayVector.normalized().y * t);
//        result.z = (float) (rayPoint.z + rayVector.normalized().z * t);
//        return result;
//    }

    /**
     *
     * @param rayVector ray vector
     * @param rayPoint ray start point
     * @param planeNormal normal vector of plane
     * @param planePoint a point at the plane
     * @return
     */
    public static Vector3 calculateIntersectionPointOfLineAndPlane(Vector3 rayVector, Vector3 rayPoint, Vector3 planeNormal, Vector3 planePoint) {

        Vector3 diff = new Vector3();
        diff.x = rayPoint.x - planePoint.x;
        diff.y = rayPoint.y - planePoint.y;
        diff.z = rayPoint.z - planePoint.z;

        float prod1 = Vector3.dot(diff, planeNormal);
        float prod2 = Vector3.dot(rayVector, planeNormal);
        float prod3 = prod1 / prod2;

        Vector3 result = new Vector3();
        result.x = rayPoint.x - rayVector.x * prod3;
        result.y = rayPoint.y - rayVector.y * prod3;
        result.z = rayPoint.z - rayVector.z * prod3;

        return result;
    }

    /**
     * find the 3D intersection of a segment and a plane
     * return 0 = disjoint (no intersection)
     * 1 =  intersection in the unique point intersectVector
     * 2 = the  segment lies in the plane
     */
    public static int calculateIntersectionOfLineAndPlane(Vector3 rayVector, Vector3 rayPoint, Vector3 planeNormal, Vector3 planePoint, Vector3 intersectVector)
    {
        Vector3 u = new Vector3();
        u.x = rayVector.x - rayPoint.x;
        u.y = rayVector.y - rayPoint.y;
        u.z = rayVector.z - rayPoint.z;

        Vector3 w = new Vector3();
        w.x = rayPoint.x - planePoint.x;
        w.y = rayPoint.y - planePoint.y;
        w.z = rayPoint.z - planePoint.z;

        float D = Vector3.dot(planeNormal, u);
        float N = -Vector3.dot(planeNormal, w);

        if (Math.abs(D) < 0.00000001) {
            // segment is parallel to plane
            if (N == 0) {
                // segment lies in plane
                return 2;
            }
            else {
                // no intersection
                return 0;
            }
        }

        // they are not parallel
        // compute intersect param
        float sI = N / D;
        if (sI < 0 || sI > 1) {
            // no intersection
            return 0;
        }

        intersectVector.x = rayPoint.x + sI * u.x;
        intersectVector.y = rayPoint.y + sI * u.y;
        intersectVector.z = rayPoint.z + sI * u.z;

        // compute segment intersect point
        return 1;
    }

    public static boolean checkIsVectorInPolygon(Vector3 p, List<Vector3> poly) {
        float px = p.x;
        float py = p.z;
        boolean flag = false;

        for(int i = 0, l = poly.size(), j = l - 1; i < l; j = i, i++) {
            float sx = poly.get(i).x;
            float sy = poly.get(i).z;
            float tx = poly.get(j).x;
            float ty = poly.get(j).z;

            // vector on polygon's side
            if((sx == px && sy == py) || (tx == px && ty == py)) {
                return false;
            }


            if((sy < py && ty >= py) || (sy >= py && ty < py)) {

                float x = sx + (py - sy) * (tx - sx) / (ty - sy);

                // vector on polygon's side
                if(x == px) {
                    return false;
                }

                if(x > px) {
                    flag = !flag;
                }
            }
        }

        // vector in polygon
        return flag;
    }

    public static boolean checkIsVectorInPolygon(Vector3 p, Vector3 leftBottom, Vector3 rightTop) {

        boolean isIn = false;

        boolean hr = false;
        if(leftBottom.x <= p.x && p.x <= rightTop.x) {
            hr = true;
        }

        boolean vr = false;
        if(leftBottom.y <= p.y && p.y <= rightTop.y) {
            vr = true;
        }

        if(hr && vr) {
            isIn = true;
        }


        boolean hl = false;
        if(leftBottom.x >= p.x && p.x >= rightTop.x) {
            hl = true;
        }

        boolean vl = false;
        if(leftBottom.y <= p.y && p.y <= rightTop.y) {
            vl = true;
        }

        if(hl && vl) {
            isIn = true;
        }


        return isIn;
    }

}
