package dev.tianmi.sussypatches.core.mixin.feature.visiblefluidcell;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import dev.tianmi.sussypatches.api.core.mixin.extension.SpecialModelExtension;
import dev.tianmi.sussypatches.api.item.IModelDispatcher;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.items.metaitem.stats.IItemComponent;

@Mixin(value = MetaItem.class, remap = false)
public abstract class MetaItemMixin<T extends MetaItem<?>.MetaValueItem & SpecialModelExtension> {

    @SuppressWarnings("InvokeAssignCanReplacedWithExpression")
    @Inject(method = "getModelIndex",
            at = @At(value = "INVOKE_ASSIGN",
                     target = "Lgregtech/api/items/metaitem/MetaItem;getItem(Lnet/minecraft/item/ItemStack;)Lgregtech/api/items/metaitem/MetaItem$MetaValueItem;"),
            cancellable = true)
    private void fromDispatcher(ItemStack itemStack, CallbackInfoReturnable<Integer> cir,
                                @Local(name = "metaValueItem") T metaValueItem) {
        var dispatcher = metaValueItem.getModelDispatcher();
        if (dispatcher != null) cir.setReturnValue(dispatcher.getModelIndex(itemStack));
    }

    @Mixin(value = MetaValueItem.class, remap = false)
    private static abstract class MetaValueItemMixin implements SpecialModelExtension {

        @Unique
        @Nullable
        private IModelDispatcher sus$modelDispatcher;

        @Unique
        @Nullable
        @Override
        @SuppressWarnings("AddedMixinMembersNamePattern")
        public IModelDispatcher getModelDispatcher() {
            return sus$modelDispatcher;
        }

        @Unique
        @Override
        @SuppressWarnings("AddedMixinMembersNamePattern")
        public MetaValueItem setModelDispatcher(IModelDispatcher modelDispatcher) {
            this.sus$modelDispatcher = modelDispatcher;
            return (MetaValueItem) (Object) this;
        }

        @Inject(method = "addItemComponentsInternal",
                at = @At(value = "FIELD",
                         target = "Lgregtech/api/items/metaitem/MetaItem$MetaValueItem;allStats:Ljava/util/List;",
                         opcode = Opcodes.GETFIELD))
        private void checkModelDispatcher(IItemComponent[] ignored, CallbackInfo ci,
                                          @Local(name = "itemComponent") IItemComponent itemComponent) {
            if (itemComponent instanceof IModelDispatcher modelDispatcher) {
                this.sus$modelDispatcher = modelDispatcher;
            }
        }
    }
}
