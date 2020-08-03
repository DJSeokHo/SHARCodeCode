package com.swein.sharcodecode.arpart.builder.tool;

import android.content.Context;

import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;

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
}
