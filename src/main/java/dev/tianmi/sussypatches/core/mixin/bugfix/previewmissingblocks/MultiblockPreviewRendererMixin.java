package dev.tianmi.sussypatches.core.mixin.bugfix.previewmissingblocks;

import org.spongepowered.asm.mixin.Mixin;

import dev.tianmi.sussypatches.api.annotation.Transformer;
import dev.tianmi.sussypatches.core.asm.transformer.MBPRTransformer;
import gregtech.client.renderer.handler.MultiblockPreviewRenderer;

@Transformer(clazz = MBPRTransformer.class)
@Mixin(value = MultiblockPreviewRenderer.class, remap = false)
public abstract class MultiblockPreviewRendererMixin {}
