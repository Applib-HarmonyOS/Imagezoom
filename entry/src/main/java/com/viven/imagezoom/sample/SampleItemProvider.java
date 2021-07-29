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

package com.viven.imagezoom.sample;

import com.viven.imagezoom.ImageZoomHelper;
import java.util.List;
import ohos.aafwk.ability.AbilitySlice;
import ohos.agp.components.BaseItemProvider;
import ohos.agp.components.Component;
import ohos.agp.components.ComponentContainer;
import ohos.agp.components.LayoutScatter;
import ohos.agp.components.element.FrameAnimationElement;
import ohos.multimodalinput.event.TouchEvent;

/**
 * Item Provider to put items into the list container.
 */
public class SampleItemProvider extends BaseItemProvider {
    private final List<Integer> list;
    private final AbilitySlice slice;
    private final ImageZoomHelper imageZoomHelper;

    /**
     * Item Provider constructor.
     *
     * @param list List of item indices.
     * @param slice Ability Slice object which contains the list container.
     * @param imageZoomHelper An ImageZoomHelper class object.
     */
    public SampleItemProvider(List<Integer> list, AbilitySlice slice, ImageZoomHelper imageZoomHelper) {
        this.list = list;
        this.slice = slice;
        this.imageZoomHelper = imageZoomHelper;
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        if (list != null && position >= 0 && position < list.size()) {
            return list.get(position);
        }
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public Component getComponent(int position, Component convertComponent, ComponentContainer componentContainer) {
        final Component cpt;
        if (convertComponent == null) {
            cpt = LayoutScatter.getInstance(slice).parse(ResourceTable.Layout_item_sample, null, false);
        } else {
            cpt = convertComponent;
        }

        Component animationComponent = cpt.findComponentById(ResourceTable.Id_display_animation);
        FrameAnimationElement frameAnimationElement = new FrameAnimationElement(cpt.getContext(),
                ResourceTable.Graphic_animation_element);
        animationComponent.setBackground(frameAnimationElement);
        frameAnimationElement.start();

        animationComponent.setTouchEventListener(new Component.TouchEventListener() {
            @Override
            public boolean onTouchEvent(Component component, TouchEvent touchEvent) {
                animationComponent.requestFocus();
                return imageZoomHelper.onDispatchTouchEvent((touchEvent)) || onTouchEvent(component, touchEvent);
            }
        });

        return cpt;
    }
}