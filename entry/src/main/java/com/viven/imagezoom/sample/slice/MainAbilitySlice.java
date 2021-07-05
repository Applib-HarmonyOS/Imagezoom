package com.viven.imagezoom.sample.slice;

import com.viven.imagezoom.sample.ResourceTable;
import ohos.aafwk.ability.AbilitySlice;
import ohos.aafwk.content.Intent;

public class MainAbilitySlice extends AbilitySlice {
    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
        super.setUIContent(ResourceTable.Layout_ability_main);
        findComponentById(ResourceTable.Id_btnBasic).setClickedListener(component -> present(new BasicZoomAbilitySlice(), new Intent()));
        findComponentById(ResourceTable.Id_btnRecycler).setClickedListener(component -> present(new RecyclerViewAbilitySlice(), new Intent()));
    }

    @Override
    public void onActive() {
        super.onActive();
    }

    @Override
    public void onForeground(Intent intent) {
        super.onForeground(intent);
    }
}
