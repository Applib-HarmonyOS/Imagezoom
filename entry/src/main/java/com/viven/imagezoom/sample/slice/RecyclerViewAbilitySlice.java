package com.viven.imagezoom.sample.slice;

import com.viven.imagezoom.ImageZoomHelper;
import com.viven.imagezoom.sample.ResourceTable;
import com.viven.imagezoom.sample.SampleItemProvider;
import java.util.ArrayList;
import java.util.List;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;
import ohos.agp.components.Component;
import ohos.agp.components.ListContainer;

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
