package dev.tianmi.sussypatches.core.mixin.bugfix.unbindframebuffer;

import java.util.function.Consumer;

import net.minecraft.client.shader.Framebuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import codechicken.lib.render.shader.ShaderObject;
import codechicken.lib.render.shader.ShaderProgram.UniformCache;
import dev.tianmi.sussypatches.api.annotation.Implemented;
import gregtech.client.shader.Shaders;

@Implemented(in = "https://github.com/GregTechCEu/GregTech/pull/2614")
@Mixin(value = Shaders.class, remap = false)
public abstract class ShadersMixin {

    @Inject(method = "renderFullImageInFBO", at = @At("TAIL"))
    private static void unbindFramebuffer(Framebuffer fbo,
                                          ShaderObject frag,
                                          Consumer<UniformCache> uniformCache,
                                          CallbackInfoReturnable<Framebuffer> cir) {
        fbo.bindFramebuffer(false);
    }
}
