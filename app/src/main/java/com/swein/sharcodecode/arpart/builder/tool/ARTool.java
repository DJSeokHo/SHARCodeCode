package com.swein.sharcodecode.arpart.builder.tool;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.FaceToCameraNode;
import com.swein.sharcodecode.arpart.builder.ARBuilder;

import java.util.ArrayList;
import java.util.List;

public class ARTool {

    /**
     * create material
     */
    public interface MaterialCreatedDelegate {
        void onCreated(Material material);
    }
    public static void createColorMaterial(Context context, int color, MaterialCreatedDelegate materialCreatedDelegate) {
        MaterialFactory
                .makeOpaqueWithColor(context, new Color(color))
                .thenAccept(materialCreatedDelegate::onCreated);
    }

    /**
     * create view render able
     */
    public interface ViewRenderableCreatedDelegate {
        void onCreated(ViewRenderable viewRenderable);
    }
    public static void createViewRenderable(Context context, int layoutResource, ViewRenderableCreatedDelegate viewRenderableCreatedDelegate, boolean shadow) {
        ViewRenderable.builder()
                .setView(context, layoutResource)
                .build()
                .thenAccept(viewRenderable -> {
                    viewRenderable.setShadowCaster(shadow);
                    viewRenderable.setShadowReceiver(shadow);
                    viewRenderableCreatedDelegate.onCreated(viewRenderable);
                });
    }

    /**
     * update plan cloud point area
     */
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

    /**
     * check plan type
     */
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

    public static Node createWorldNode(float tx, float ty, float tz, Material material, boolean shadow) {
        ModelRenderable modelRenderable = ShapeFactory.makeSphere(0.01f, Vector3.zero(), material);
        modelRenderable.setShadowReceiver(shadow);
        modelRenderable.setShadowCaster(shadow);
        Node node = new Node();
        node.setRenderable(modelRenderable);
        node.setWorldPosition(new Vector3(tx, ty, tz));
        return node;
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

    public static Vector3 getNormalVectorOfThreeVectors(Vector3 original, Vector3 a, Vector3 b) {

        Vector3 side1 = new Vector3(a.x - original.x, a.y - original.y, a.z - original.z);
        Vector3 side2 = new Vector3(b.x - original.x, b.y - original.y, b.z - original.z);

        return Vector3.cross(side1, side2);
    }

    public static Vector3 transformWorldPositionToLocalPositionOfParent(Node parent, Vector3 worldPosition) {
        return parent.worldToLocalPoint(worldPosition);
    }

    public static AnchorNode createAnchorNode(Anchor anchor, Material material, boolean shadow) {

        ModelRenderable modelRenderable = ShapeFactory.makeSphere(0.01f, Vector3.zero(), material);
        modelRenderable.setShadowReceiver(shadow);
        modelRenderable.setShadowCaster(shadow);

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setRenderable(modelRenderable);

        return anchorNode;
    }

    public static float getLengthOfTwoNode(Node startNode, Node endNode) {
        Vector3 startVector3 = startNode.getWorldPosition();
        Vector3 endVector3 = endNode.getWorldPosition();

        Vector3 difference = Vector3.subtract(startVector3, endVector3);
        return difference.length();
    }

    public static Node drawSegment(Node startNode, Node endNode, Material lineMaterial, boolean shadow) {

        Vector3 startVector3 = startNode.getWorldPosition();
        Vector3 endVector3 = endNode.getWorldPosition();

        Vector3 difference = Vector3.subtract(startVector3, endVector3);
        Vector3 directionFromTopToBottom = difference.normalized();
        Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

        ModelRenderable lineModelRenderable = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), lineMaterial);
        lineModelRenderable.setShadowCaster(shadow);
        lineModelRenderable.setShadowReceiver(shadow);

        Node lineNode = new Node();

        lineNode.setParent(startNode);
        lineNode.setRenderable(lineModelRenderable);
        lineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
        lineNode.setWorldRotation(rotationFromAToB);

        return lineNode;
    }

    public interface SetSegmentSizeTextViewDelegate {
        void onFinish(ViewRenderable viewRenderable, FaceToCameraNode faceToCameraNode);
    }
    public static void setSegmentSizeTextView(Context context, float originalLength, ARBuilder.ARUnit arUnit, Node parentNode, @Nullable SetSegmentSizeTextViewDelegate setSegmentSizeTextViewDelegate) {
        float length = getLengthByUnit(arUnit, originalLength);

        ViewRenderable.builder()
                .setView(context, R.layout.view_renderable_text)
                .build()
                .thenAccept(viewRenderable -> {

                    TextView textView = ((TextView)viewRenderable.getView());
                    textView.setText(String.format("%.2f", length) + " " + ARTool.getLengthUnitString(arUnit));
                    viewRenderable.setShadowCaster(false);
                    viewRenderable.setShadowReceiver(false);

                    FaceToCameraNode faceToCameraNode = new FaceToCameraNode();
                    faceToCameraNode.setParent(parentNode);

                    faceToCameraNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
                    faceToCameraNode.setLocalPosition(new Vector3(0f, 0.05f, 0f));
                    faceToCameraNode.setRenderable(viewRenderable);

                    if(setSegmentSizeTextViewDelegate != null) {
                        setSegmentSizeTextViewDelegate.onFinish(viewRenderable, faceToCameraNode);
                    }
                });
    }

    public static AnchorNode createAnchorNode(Anchor anchor) {
        return new AnchorNode(anchor);
    }

    public static double getNodesDistanceMetersWithoutHeight(Node startNode, Node endNode) {
        float dx = startNode.getWorldPosition().x - endNode.getWorldPosition().x;
        float dz = startNode.getWorldPosition().z - endNode.getWorldPosition().z;

        return Math.sqrt(dx * dx + dz * dz);
    }

    public static void removeChildFormNode(Node node) {
        List<Node> childList = node.getChildren();
        if(!childList.isEmpty()) {
            for (int i = childList.size() - 1; i >= 0; i--) {
                childList.get(i).setParent(null);
            }
        }
    }

    /**
     * calculate polygon plane area in 3D
     */
    public static float calculateArea(List<Node> list, Vector3 normalVectorOfPlane) {

        List<Vector3> vector3List = new ArrayList<>();
        for(int i = 0; i < list.size(); i++) {
            vector3List.add(list.get(i).getWorldPosition());
        }
        vector3List.add(list.get(0).getWorldPosition());

        return Math.abs(area3DPolygon(list.size(), vector3List, normalVectorOfPlane));
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

    public static String getLengthUnitString(ARBuilder.ARUnit ARUnit) {
        switch (ARUnit) {
            case M:
                return "m";

            case CM:
                return "cm";

            default:
                return "";
        }
    }

    public static SpannableString getAreaUnitString(ARBuilder.ARUnit ARUnit) {
        switch (ARUnit) {
            case M:
                return getM2();

            case CM:
                return getCM2();

            default:
                return null;
        }
    }

    public static SpannableString getVolumeUnitString(ARBuilder.ARUnit ARUnit) {
        switch (ARUnit) {
            case M:
                return getM3();

            case CM:
                return getCM3();

            default:
                return null;
        }
    }

    public static float getLengthByUnit(ARBuilder.ARUnit ARUnit, float length) {
        switch (ARUnit) {
            case CM:
                return length * 100;

            case M:
                return length;

            default:
                return 0;
        }
    }

    public static float getAreaByUnit(ARBuilder.ARUnit ARUnit, float area) {
        switch (ARUnit) {
            case CM:
                return area * 10000;

            case M:
                return area;

            default:
                return 0;
        }
    }

    public static SpannableString getM2() {
        SpannableString m2 = new SpannableString("m2");
        m2.setSpan(new RelativeSizeSpan(0.5f), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        m2.setSpan(new SuperscriptSpan(), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return m2;
    }

    public static SpannableString getCM2() {
        SpannableString cm2 = new SpannableString("cm2");
        cm2.setSpan(new RelativeSizeSpan(0.5f), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cm2.setSpan(new SuperscriptSpan(), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return cm2;
    }

    public static SpannableString getM3() {
        SpannableString m2 = new SpannableString("m3");
        m2.setSpan(new RelativeSizeSpan(0.5f), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        m2.setSpan(new SuperscriptSpan(), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return m2;
    }

    public static SpannableString getCM3() {
        SpannableString cm2 = new SpannableString("cm3");
        cm2.setSpan(new RelativeSizeSpan(0.5f), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cm2.setSpan(new SuperscriptSpan(), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return cm2;
    }
}
