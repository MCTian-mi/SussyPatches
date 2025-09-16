package dev.tianmi.sussypatches.api.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import java.util.Arrays;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.drawable.UITexture;

import dev.tianmi.sussypatches.api.unification.material.info.SusIconTypes;
import dev.tianmi.sussypatches.integration.baubles.BaublesModule;
import dev.tianmi.sussypatches.core.mixin.feature.grsrecipecreator.GTMaterialFluidAccessor;
import dev.tianmi.sussypatches.core.mixin.feature.grsrecipecreator.RecipeMapAccessor;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.fluids.GTFluid.GTMaterialFluid;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.Mods;
import gregtech.api.util.LocalizationUtils;
import gregtech.common.items.MetaItems;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import gregtech.integration.jei.JustEnoughItemsModule;
import gregtech.integration.jei.recipe.RecipeMapCategory;

/// This also serves as a Lombok [ExtensionMethod] holder.
public class SusUtil {

    public static <T> T tap(T instance, Consumer<T> lambda) {
        lambda.accept(instance);
        return instance;
    }

    public static <T> void let(T instance, Consumer<T> lambda) {
        lambda.accept(instance);
    }

    public static <V, T> V with(T instance, Function<T, V> lambda) {
        return lambda.apply(instance);
    }

    public static <T> T orElse(@Nullable T instance, T fallback) {
        return instance != null ? instance : fallback;
    }

    public static <T> T orElse(@Nullable T instance, Supplier<@NotNull T> fallback) {
        return instance != null ? instance : fallback.get();
    }

    public static String getPrefix(Material material) {
        return material.getModid().equals(GTValues.MODID) ? "" : material.getModid() + ":";
    }

    public static NonNullList<ItemStack> addAll(NonNullList<ItemStack> items, IItemHandler handler, boolean recursive) {
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            var stack = handler.getStackInSlot(slot);
            if (stack.isEmpty()) continue;
            items.add(stack);

            if (!recursive || stack.getCount() > 1) continue;
            var stackHandler = stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
            if (stackHandler != null) {
                addAll(items, stackHandler, true);
            }
        }
        return items;
    }

    public static NonNullList<ItemStack> gatherAllItems(EntityPlayer player) {
        IItemHandler playerInv = player.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        if (Mods.Baubles.isModLoaded()) {
            playerInv = new ItemHandlerList(Arrays.asList(playerInv, BaublesModule.getBaublesInvWrapper(player)));
        }
        assert playerInv != null;
        return addAll(NonNullList.create(), playerInv, true);
    }

    // TODO: as a method extension
    public static TextureAtlasSprite getBlockSprite(MaterialIconType iconType, Material material) {
        return Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(iconType.getBlockTexturePath(material.getMaterialIconSet()).toString());
    }

    public static MaterialIconType getIconType(ItemPipeType itemPipeType) {
        return switch (itemPipeType) {
            case SMALL, RESTRICTIVE_SMALL -> SusIconTypes.pipeSmall;
            case LARGE, RESTRICTIVE_LARGE -> SusIconTypes.pipeLarge;
            case HUGE, RESTRICTIVE_HUGE -> SusIconTypes.pipeHuge;
            default -> SusIconTypes.pipeNormal;
        };
    }

    public static MaterialIconType getIconType(FluidPipeType fluidPipeType) {
        return switch (fluidPipeType) {
            case TINY -> SusIconTypes.pipeTiny;
            case SMALL -> SusIconTypes.pipeSmall;
            case LARGE -> SusIconTypes.pipeLarge;
            case HUGE -> SusIconTypes.pipeHuge;
            case QUADRUPLE -> SusIconTypes.pipeQuadruple;
            case NONUPLE -> SusIconTypes.pipeNonuple;
            default -> SusIconTypes.pipeNormal;
        };
    }

    public static MaterialIconType getIconType(Insulation insulation) {
        return switch (insulation) {
            case CABLE_SINGLE -> SusIconTypes.insulationSingle;
            case CABLE_DOUBLE -> SusIconTypes.insulationDouble;
            case CABLE_QUADRUPLE -> SusIconTypes.insulationQuadruple;
            case CABLE_OCTAL -> SusIconTypes.insulationOctal;
            case CABLE_HEX -> SusIconTypes.insulationHex;
            default -> SusIconTypes.insulationSide;
        };
    }

    public static boolean isEmpty(@Nullable FluidStack stack) {
        return stack == null || stack.getFluid() == null || stack.amount <= 0;
    }

    public static IKey asKey(@Nullable FluidStack stack) {
        if (isEmpty(stack)) return IKey.EMPTY;
        var fluid = stack.getFluid();
        if (fluid instanceof GTMaterialFluid matFluid) {
            String translationKey = ((GTMaterialFluidAccessor) matFluid).getTranslationKey();
            String override = "fluid." + matFluid.getMaterial().getUnlocalizedName();
            IKey localizedName = LocalizationUtils.hasKey(override) ? IKey.lang(override) :
                    IKey.lang(matFluid.getMaterial().getUnlocalizedName());
            return translationKey == null ? localizedName : IKey.lang(translationKey, localizedName);
        }
        return IKey.lang(fluid.getUnlocalizedName(stack));
    }

    public static ItemDrawable asDrawable(ItemStack stack) {
        return new ItemDrawable(stack);
    }

    public static ItemStack getCatalyst(RecipeMap<?> recipeMap) {
        for (var category : recipeMap.getRecipesByCategory().keySet()) {

            var jeiCategory = RecipeMapCategory.getCategoryFor(category);
            if (jeiCategory == null) break;

            for (var catalyst : JustEnoughItemsModule.jeiRuntime.getRecipeRegistry().getRecipeCatalysts(jeiCategory)) {
                if (catalyst instanceof ItemStack stack) {
                    return stack;
                }
            }
        }

        return MetaItems.LOGO.getStackForm();
    }

    public static UITexture toMuiTexture(TextureArea t) {
        return UITexture.builder()
                .location(t.imageLocation)
                .uv((float) t.offsetX, (float) t.offsetY, (float) t.imageWidth, (float) t.imageHeight)
                .build();
    }

    public static UITexture getProgressBar(RecipeMap<?> recipeMap) {
        return toMuiTexture(((RecipeMapAccessor) recipeMap).getProgressBarTexture());
    }

    public static FluidTankList createTankList(int size) {
        var tanks = new FluidTank[size];
        for (int i = 0; i < size; i++) {
            tanks[i] = new FluidTank(Integer.MAX_VALUE);
        }
        return new FluidTankList(false, tanks);
    }

    public static void clear(IItemHandlerModifiable itemHandler) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            itemHandler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public static void clear(IMultipleTankHandler tankHandler) {
        for (int i = 0; i < tankHandler.getTanks(); i++) {
            tankHandler.getTankAt(i).drain(Integer.MAX_VALUE, true);
        }
    }
}
