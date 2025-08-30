package dev.tianmi.sussypatches.api.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import dev.tianmi.sussypatches.api.unification.material.info.SusIconTypes;
import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.drawable.IKey;

import dev.tianmi.sussypatches.core.mixin.feature.grsrecipecreator.GTMaterialFluidAccessor;
import gregtech.api.GTValues;
import gregtech.api.fluids.GTFluid.GTMaterialFluid;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import gregtech.api.util.LocalizationUtils;
import mcp.MethodsReturnNonnullByDefault;

/// This also serves as a Lombok method extension holder.
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SusUtil {

    public static String getPrefix(Material material) {
        return material.getModid().equals(GTValues.MODID) ? "" : material.getModid() + ":";
    }

    // TODO: as a method extension
    public static TextureAtlasSprite getBlockSprite(MaterialIconType iconType, Material material) {
        return Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(iconType.getBlockTexturePath(material.getMaterialIconSet()).toString());
    }

    // TODO: as a method extension
    public static MaterialIconType getIconType(ItemPipeType itemPipeType) {
        return switch (itemPipeType) {
            case SMALL, RESTRICTIVE_SMALL -> SusIconTypes.pipeSmall;
            case LARGE, RESTRICTIVE_LARGE -> SusIconTypes.pipeLarge;
            case HUGE, RESTRICTIVE_HUGE -> SusIconTypes.pipeHuge;
            default -> SusIconTypes.pipeNormal;
        };
    }

    // TODO: as a method extension
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

    // TODO: as a method extension
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
}
