package dev.tianmi.sussypatches.core.mixin.compat.vintagefix.lampbakedmodel;

import java.util.Map;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import gregtech.client.model.lamp.LampBakedModel;
import gregtech.client.model.lamp.LampModelType;

@Mixin(value = LampBakedModel.class, remap = false)
public interface LampBakedModelAccessor {

    @Accessor("ENTRIES")
    static Map<KeyAccessor, EntryAccessor> getEntries() {
        throw new AssertionError("LampBakedModelAccessor is not applied!");
    }

    @Mixin(value = LampBakedModel.Entry.class, remap = false)
    interface EntryAccessor {

        @Accessor("customItemModel")
        ModelResourceLocation getCustomItemModel();

        @Invoker("getOriginalModelLocation")
        ModelResourceLocation getOriginalModel();
    }

    // WTF why is Entry class public while Key is private??????
    // I love you CEu
    @Mixin(targets = "gregtech.client.model.lamp.LampBakedModel$Key", remap = false)
    interface KeyAccessor {

        @Accessor("modelType")
        LampModelType getModelType();
    }
}
