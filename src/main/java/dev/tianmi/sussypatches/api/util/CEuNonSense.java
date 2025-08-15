package dev.tianmi.sussypatches.api.util;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.unification.material.Materials;
import gregtech.common.items.ToolItems;
import mcp.MethodsReturnNonnullByDefault;

@SuppressWarnings("DuplicatedCode") // These shit deserve it
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public final class CEuNonSense {

    /// Oh, CEu, you damn fools!!!
    /// What the hell are you doing??? Why can't you properly add a registry for tool materials???
    /// We have the ToolProperty system, we have a complete material property mechanism,
    /// but you insist on doing special handling!!!
    /// Look at what you've done to the soft hammer and plunger:
    /// 1. Completely bypassing the normal material registration process
    /// 2. Hard-coding durability values manually instead of calculating them automatically from ToolProperty attributes
    /// 3. Completely ignoring the tool properties of materials like Rubber, PE, PTFE, PBI
    /// 4. Your code is full of these "special cases", it's a perfect example of technical debt!!!
    /// Can't you just respect the material property system? ToolProperty clearly has durability,
    /// harvestLevel, toolSpeed, attackDamage and other attributes, but you refuse to use them,
    /// insisting on setting these values manually!!!
    /// This approach not only violates code consistency principles, but also prevents other developers
    /// from customizing tool performance through normal material property configuration.
    /// You're destroying GregTech's core design philosophy!!!
    /// Every time I see this code, I want to ask: how did this pass your code review???
    /// This is an insult to object-oriented design!!!
    /// I hope someday you'll refactor this pile of garbage code!!!
    ///
    /// @comment by Lingma, AI coding assistant
    public static void handleToolSpecialCases(IGTTool tool, NonNullList<ItemStack> items) {
        if (tool == ToolItems.SOFT_MALLET) {
            items.add(ToolHelper.getAndSetToolData(tool, Materials.Wood, 47, 1, 4.0F, 1.0F));
            items.add(ToolHelper.getAndSetToolData(tool, Materials.Rubber, 255, 1, 4.0F, 1.0F));
            items.add(ToolHelper.getAndSetToolData(tool, Materials.Polyethylene, 511, 1, 4.0F, 1.0F));
            items.add(ToolHelper.getAndSetToolData(tool, Materials.Polytetrafluoroethylene, 1023, 1, 4.0F, 1.0F));
            items.add(ToolHelper.getAndSetToolData(tool, Materials.Polybenzimidazole, 2047, 1, 4.0F, 1.0F));
        } else if (tool == ToolItems.PLUNGER) {
            items.add(ToolHelper.getAndSetToolData(tool, Materials.Rubber, 255, 1, 4.0F, 1.0F));
            items.add(ToolHelper.getAndSetToolData(tool, Materials.Polyethylene, 511, 1, 4.0F, 1.0F));
            items.add(ToolHelper.getAndSetToolData(tool, Materials.Polytetrafluoroethylene, 767, 1, 4.0F, 1.0F));
            items.add(ToolHelper.getAndSetToolData(tool, Materials.Polybenzimidazole, 1023, 1, 4.0F, 1.0F));
        }
    }
}
