package dev.tianmi.sussypatches.core.mixin.tweak.tabnosearchbars;

import net.minecraft.creativetab.CreativeTabs;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;

import gregtech.api.util.BaseCreativeTab;

@Mixin(value = BaseCreativeTab.class, remap = false)
public abstract class BaseCreativeTabMixin extends CreativeTabs {

    @SuppressWarnings("DataFlowIssue")
    BaseCreativeTabMixin() {
        super(null);
    }

    @WrapWithCondition(method = "<init>",
                       at = @At(value = "INVOKE",
                                target = "Lgregtech/api/util/BaseCreativeTab;setBackgroundImageName(Ljava/lang/String;)Lnet/minecraft/creativetab/CreativeTabs;",
                                remap = true))
    private boolean noSearchBars(BaseCreativeTab instance, String s) {
        return false;
    }

    @ModifyReturnValue(method = "hasSearchBar", at = @At("RETURN"))
    private boolean noSearchBars(boolean original) {
        return false;
    }
}
