/*
 * Copyright (C) 2020-21 Application Library Engineering Group
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
