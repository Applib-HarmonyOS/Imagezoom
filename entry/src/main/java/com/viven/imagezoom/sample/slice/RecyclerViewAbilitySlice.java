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

import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ListContainer;
import com.viven.imagezoom.ImageZoomHelper;
import com.viven.imagezoom.sample.ResourceTable;
import com.viven.imagezoom.sample.SampleItemProvider;
import java.util.ArrayList;
import java.util.List;

/**
 * Ability Slice to test zooming feature in a list container.
 */
public class RecyclerViewAbilitySlice extends AbilitySlice {
    ImageZoomHelper imageZoomHelper;
    ImageZoomHelper.OnZoomListener onZoomListener;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_recycler_view);

        imageZoomHelper = new ImageZoomHelper(this.getAbility());
        onZoomListener = new ImageZoomHelper.OnZoomListener() {
            @Override
            public void onImageZoomStarted(Component view) {
                // Do Something
            }

            @Override
            public void onImageZoomEnded(Component view) {
                // Do Something
            }
        };
        initListContainer();
    }

    @Override
    public void onActive() {
        super.onActive();
        imageZoomHelper.addOnZoomListener(onZoomListener);
    }

    @Override
    protected void onInactive() {
        super.onInactive();
        imageZoomHelper.removeOnZoomListener(onZoomListener);
    }

    private void initListContainer() {
        ListContainer listContainer = (ListContainer) findComponentById(ResourceTable.Id_list);
        List<Integer> list = getData();
        SampleItemProvider sampleItemProvider = new SampleItemProvider(list, this, imageZoomHelper);
        listContainer.setItemProvider(sampleItemProvider);
    }

    private ArrayList<Integer> getData() {
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(i);
        }
        return list;
    }
}
