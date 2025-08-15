package dev.tianmi.sussypatches.integration.jei;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;

import gregtech.api.items.toolitem.IGTTool;
import mezz.jei.api.ISubtypeRegistry.ISubtypeInterpreter;

public final class GTToolSubtypeHandler implements ISubtypeInterpreter {

    @NotNull
    @Override
    public String apply(@NotNull ItemStack itemStack) {
        var tool = (IGTTool) itemStack.getItem();
        var material = tool.getToolMaterial(itemStack);
        String additionalData = material == null ? NONE : material.getRegistryName();
        return String.format("%s;%s", tool.getToolId(), additionalData);
    }
}
