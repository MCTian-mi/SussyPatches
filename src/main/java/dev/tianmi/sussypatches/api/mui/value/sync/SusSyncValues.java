package dev.tianmi.sussypatches.api.mui.value.sync;

import java.util.function.Consumer;
import java.util.function.Supplier;

import com.cleanroommc.modularui.api.value.IValue;
import com.cleanroommc.modularui.network.NetworkUtils;
import com.cleanroommc.modularui.value.sync.GenericSyncValue;
import com.cleanroommc.modularui.value.sync.ValueSyncHandler;

import gregtech.api.recipes.RecipeMap;

public interface SusSyncValues {

    static ValueSyncHandler<RecipeMap<?>> ofMap(Supplier<RecipeMap<?>> getter, Consumer<RecipeMap<?>> setter) {
        return new GenericSyncValue<>(getter, setter,
                buf -> RecipeMap.getByName(NetworkUtils.readStringSafe(buf)),
                (buf, map) -> NetworkUtils.writeStringSafe(buf, map != null ? map.getUnlocalizedName() : ""));
    }

    static ValueSyncHandler<RecipeMap<?>> ofMap(IValue<RecipeMap<?>> value) {
        return ofMap(value::getValue, value::setValue);
    }
}
