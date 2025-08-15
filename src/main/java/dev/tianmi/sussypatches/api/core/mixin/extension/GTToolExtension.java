package dev.tianmi.sussypatches.api.core.mixin.extension;

import java.util.*;

import javax.annotation.ParametersAreNonnullByDefault;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.unification.material.Material;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import mcp.MethodsReturnNonnullByDefault;

@MixinExtension(IGTTool.class)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface GTToolExtension {

    Map<String, Collection<gregtech.api.unification.material.Material>> TOOL_MATERIALS = new Object2ObjectArrayMap<>();

    static GTToolExtension cast(IGTTool tool) {
        return (GTToolExtension) tool;
    }

    static Collection<Material> getMaterials(IGTTool tool) {
        return cast(tool).sus$getMaterials();
    }

    static void addMaterial(IGTTool tool, Material material) {
        cast(tool).sus$addMaterial(material);
    }

    default Collection<Material> sus$getMaterials() {
        return TOOL_MATERIALS.getOrDefault(((IGTTool) this).getToolId(), Collections.emptyList());
    }

    default void sus$addMaterial(Material material) {
        TOOL_MATERIALS.computeIfAbsent(((IGTTool) this).getToolId(), s -> new HashSet<>()).add(material);
    }
}
