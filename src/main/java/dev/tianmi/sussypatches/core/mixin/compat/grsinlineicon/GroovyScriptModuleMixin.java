package dev.tianmi.sussypatches.core.mixin.compat.grsinlineicon;

import net.minecraft.item.ItemStack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.mapper.ObjectMapper;
import com.cleanroommc.groovyscript.mapper.TextureBinder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import gregtech.integration.groovy.GroovyScriptModule;

@Mixin(value = GroovyScriptModule.class, remap = false)
public class GroovyScriptModuleMixin {

    // TODO: more impl
    @SuppressWarnings({ "UnstableApiUsage", "DuplicateBranchesInSwitch" })
    @WrapOperation(method = "onCompatLoaded",
                   at = @At(value = "INVOKE",
                            target = "Lcom/cleanroommc/groovyscript/compat/mods/GroovyContainer;objectMapperBuilder(Ljava/lang/String;Ljava/lang/Class;)Lcom/cleanroommc/groovyscript/mapper/ObjectMapper$Builder;"))
    private <V> ObjectMapper.Builder<V> addTextureBinder(GroovyContainer<?> container, String name, Class<V> returnType,
                                                         Operation<ObjectMapper.Builder<V>> method) {
        var builder = method.call(container, name, returnType);
        return switch (name) {
            case "recipemap" -> builder;
            case "material" -> builder;
            case "oreprefix" -> builder;
            case "metaitem" -> builder.textureBinder(TextureBinder.of(v -> (ItemStack) v, TextureBinder.ofItem()));
            case "element" -> builder;
            default -> builder;
        };
    }
}
