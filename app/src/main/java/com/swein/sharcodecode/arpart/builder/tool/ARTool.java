package com.swein.sharcodecode.arpart.builder.tool;

import android.content.Context;
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
import com.swein.sharcodecode.arpart.constants.ARConstants;

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

        planeRenderer.setShadowReceiver(false);

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

    public static AnchorNode createAnchorNode(Anchor anchor, Material material, boolean shadow) {

        ModelRenderable modelRenderable = ShapeFactory.makeSphere(0.01f, Vector3.zero(), material);
        modelRenderable.setShadowReceiver(shadow);
        modelRenderable.setShadowCaster(shadow);

        AnchorNode anchorNode = new AnchorNode(anchor);
        anchorNode.setRenderable(modelRenderable);

        return anchorNode;
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

    public static void removeChildFormNode(Node node) {
        List<Node> childList = node.getChildren();
        if(!childList.isEmpty()) {
            for (int i = childList.size() - 1; i >= 0; i--) {
                childList.get(i).setParent(null);
            }
        }
    }

    public interface SetSegmentSizeTextViewDelegate {
        void onFinish(ViewRenderable viewRenderable, FaceToCameraNode faceToCameraNode);
    }
    public static void setSegmentSizeTextView(Context context, float originalLength, ARConstants.ARUnit arUnit, Node parentNode, @Nullable SetSegmentSizeTextViewDelegate setSegmentSizeTextViewDelegate) {
        float length = MathTool.getLengthByUnit(arUnit, originalLength);

        ViewRenderable.builder()
                .setView(context, R.layout.view_renderable_text)
                .build()
                .thenAccept(viewRenderable -> {

                    TextView textView = ((TextView)viewRenderable.getView());
                    textView.setText(String.format("%.2f", length) + " " + MathTool.getLengthUnitString(arUnit));
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

    public static void setSegmentSizeTextView(Context context, float originalLength, ARConstants.ARUnit arUnit, Node parentNode, float textHeight, @Nullable SetSegmentSizeTextViewDelegate setSegmentSizeTextViewDelegate) {
        float length = MathTool.getLengthByUnit(arUnit, originalLength);

        ViewRenderable.builder()
                .setView(context, R.layout.view_renderable_text)
                .build()
                .thenAccept(viewRenderable -> {

                    TextView textView = ((TextView)viewRenderable.getView());
                    textView.setText(String.format("%.2f", length) + " " + MathTool.getLengthUnitString(arUnit));
                    viewRenderable.setShadowCaster(false);
                    viewRenderable.setShadowReceiver(false);

                    FaceToCameraNode faceToCameraNode = new FaceToCameraNode();
                    faceToCameraNode.setParent(parentNode);

                    faceToCameraNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
                    faceToCameraNode.setLocalPosition(new Vector3(0f, textHeight, 0f));
                    faceToCameraNode.setRenderable(viewRenderable);

                    if(setSegmentSizeTextViewDelegate != null) {
                        setSegmentSizeTextViewDelegate.onFinish(viewRenderable, faceToCameraNode);
                    }
                });
    }

    public static AnchorNode createAnchorNode(Anchor anchor) {
        return new AnchorNode(anchor);
    }


}
