package dev.tianmi.sussypatches.core.mixin.api.itemoverlayevent;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import dev.tianmi.sussypatches.api.event.RenderItemOverlayEvent;

@Mixin(RenderItem.class)
public class RenderItemMixin {

    @Inject(method = "renderItemOverlayIntoGUI", at = @At("HEAD"))
    private void postRenderItemOverlayEvent(FontRenderer fr, ItemStack stack,
                                            int x, int y, @Nullable String text,
                                            CallbackInfo ci) {
        var event = new RenderItemOverlayEvent();
        if (!MinecraftForge.EVENT_BUS.post(event)) {
            var operation = event.getOperation();
            if (!operation.isEmpty()) {
                operation.render(stack, x, y, text);
            }
        }
    }
}
