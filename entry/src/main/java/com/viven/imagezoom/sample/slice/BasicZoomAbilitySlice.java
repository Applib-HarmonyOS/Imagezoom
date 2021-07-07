package com.viven.imagezoom.sample.slice;

import com.viven.imagezoom.ImageZoomHelper;
import com.viven.imagezoom.sample.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.Image;
import ohos.multimodalinput.event.TouchEvent;

/**
 * Ability Slice to test basic zooming feature.
 */
public class BasicZoomAbilitySlice extends AbilitySlice {
    Image img;
    ImageZoomHelper imageZoomHelper;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_basic_zoom);

        imageZoomHelper = new ImageZoomHelper(this.getAbility());
        img = (Image) findComponentById(ResourceTable.Id_img);
        imageZoomHelper = new ImageZoomHelper(this.getAbility());

        img.setTouchEventListener(new Component.TouchEventListener() {
            @Override
            public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
                img.requestFocus();
                return imageZoomHelper.onDispatchTouchEvent((touchEvent)) || onTouchEvent(component, touchEvent);
            }
        });

        imageZoomHelper.addOnZoomListener(new ImageZoomHelper.OnZoomListener() {
            @Override
            public void onImageZoomStarted(Component view) {
                // Do Something
            }

            @Override
            public void onImageZoomEnded(Component view) {
                // Do Something
            }
        });
    }
}
