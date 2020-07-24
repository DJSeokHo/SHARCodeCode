package com.swein.sharcodecode.framework.util.ar;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.rendering.ShapeFactory;

import java.util.List;

public class ARUtil {

    public static double getNodesDistanceMeters(Node startNode, Node endNode) {
        float dx = startNode.getWorldPosition().x - endNode.getWorldPosition().x;
        float dy = startNode.getWorldPosition().y - endNode.getWorldPosition().y;
        float dz = startNode.getWorldPosition().z - endNode.getWorldPosition().z;

        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    public static double getNodesDistanceMetersWithoutHeight(Node startNode, Node endNode) {
        float dx = startNode.getWorldPosition().x - endNode.getWorldPosition().x;
        float dz = startNode.getWorldPosition().z - endNode.getWorldPosition().z;

        return Math.sqrt(dx * dx + dz * dz);
    }

    public static void updatePlanRenderer(PlaneRenderer planeRenderer) {

        planeRenderer.getMaterial().thenAccept(material -> {
            material.setFloat3(PlaneRenderer.MATERIAL_SPOTLIGHT_RADIUS, 1000f, 1000f, 1000f);
            material.setFloat3(PlaneRenderer.MATERIAL_COLOR, new Color(1f, 1f, 1f, 1f));
        });

//        // Build texture sampler
//        Texture.Sampler sampler = Texture.Sampler.builder()
//                .setMinFilter(Texture.Sampler.MinFilter.LINEAR)
//                .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
//                .setWrapMode(Texture.Sampler.WrapMode.REPEAT).build();
//
//        // Build texture with sampler
//        CompletableFuture<Texture> trigrid = Texture.builder()
//                .setSource(this, R.drawable.grid_blue)
//                .setSampler(sampler).build();
//
//        planeRenderer.getMaterial().thenAcceptBoth(trigrid, (material, texture) -> {
//            material.setTexture(PlaneRenderer.MATERIAL_TEXTURE, texture);
//            material.setFloat(PlaneRenderer.MATERIAL_SPOTLIGHT_RADIUS, 1000f);
//        });
    }

    public static AnchorNode createAnchorNode(Anchor anchor, Material material, boolean shadow) {

        ModelRenderable modelRenderable = ShapeFactory.makeSphere(0.01f, Vector3.zero(), material);
        modelRenderable.setShadowReceiver(shadow);
        modelRenderable.setShadowCaster(shadow);

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setRenderable(modelRenderable);

        return anchorNode;
    }

    public static AnchorNode createAnchorNode(Anchor anchor) {
        return new AnchorNode(anchor);
    }

    public static Node createLocalNode(float tx, float ty, float tz, Material material, boolean shadow) {
        ModelRenderable modelRenderable = ShapeFactory.makeSphere(0.01f, Vector3.zero(), material);
        modelRenderable.setShadowReceiver(shadow);
        modelRenderable.setShadowCaster(shadow);
        Node node = new Node();
        node.setRenderable(modelRenderable);
        node.setLocalPosition(new Vector3(tx, ty, tz));
        return node;
    }

    public static Node createWorldNode(float tx, float ty, float tz, Material material, boolean shadow) {
        ModelRenderable modelRenderable = ShapeFactory.makeSphere(0.01f, Vector3.zero(), material);
        modelRenderable.setShadowReceiver(shadow);
        modelRenderable.setShadowCaster(shadow);
        Node node = new Node();
        node.setRenderable(modelRenderable);
        node.setWorldPosition(new Vector3(tx, ty, tz));
        return node;
    }

    public static String checkPlanType(List<HitResult> hitTestResultList, String none, String wall, String ceiling, String floor) {

        for (HitResult hitResult : hitTestResultList) {

            Trackable trackable = hitResult.getTrackable();

            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                if(((Plane) trackable).getType() == Plane.Type.VERTICAL) {
                    return wall; // wall
                }
                else if(((Plane) trackable).getType() == Plane.Type.HORIZONTAL_DOWNWARD_FACING) {
                    return ceiling; // ceiling
                }
                else if(((Plane) trackable).getType() == Plane.Type.HORIZONTAL_UPWARD_FACING) {
                    return floor; // floor
                }
            }
        }

        return none;
    }

    public static void removeChildFormNode(Node node) {
        List<Node> childList = node.getChildren();
        if(!childList.isEmpty()) {
            for (int i = childList.size() - 1; i >= 0; i--) {
                childList.get(i).setParent(null);
            }
        }
    }

    public static Vector3 getNormalVectorOfThreeVectors(Vector3 original, Vector3 a, Vector3 b) {

        Vector3 side1 = new Vector3(a.x - original.x, a.y - original.y, a.z - original.z);
        Vector3 side2 = new Vector3(b.x - original.x, b.y - original.y, b.z - original.z);

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

    public static float getLengthBetweenPointToPlane(Vector3 pointInAir, Vector3 pointAtPlane, Vector3 normalVectorOfPlane) {

        Vector3 ab = new Vector3(pointInAir.x - pointAtPlane.x, pointInAir.y - pointAtPlane.y, pointInAir.z - pointAtPlane.z);
        return Math.abs(Vector3.dot(ab, normalVectorOfPlane) / normalVectorOfPlane.length());
    }
}
