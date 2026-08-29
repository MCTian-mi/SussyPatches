package dev.tianmi.sussypatches.api.util;

import dev.tianmi.sussypatches.api.unification.material.info.SusIconTypes;
import dev.tianmi.sussypatches.integration.baubles.BaublesModule;
import gregtech.api.GTValues;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.PropertyFluidFilter;
import gregtech.api.items.metaitem.FilteredFluidStats;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.unification.material.properties.FluidPipeProperties;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.Mods;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import lombok.SneakyThrows;
import lombok.val;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.Range;
import sun.misc.Unsafe;

import java.util.Arrays;

public class SusUtil {

    public static final Unsafe UNSAFE = initUnsafe();

    @SneakyThrows
    private static Unsafe initUnsafe() {
        val field = Unsafe.class.getDeclaredField("theUnsafe");
        field.setAccessible(true);
        return (Unsafe) field.get(null);
    }

    public static FluidPipeProperties GLASS = new FluidPipeProperties(1000, 1200, false, true, false, false);

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

    private static PropertyFluidFilter withMaxFluidTemperature(Material material, int maxFluidTemperature) {
        val from = material.getProperty(PropertyKey.FLUID_PIPE);
        val into = new PropertyFluidFilter(maxFluidTemperature, from.isGasProof(), false, from.isCryoProof(), from.isPlasmaProof());
        for (val attribute : from.getContainedAttributes()) {
            into.setCanContain(attribute, from.canContain(attribute));
        }
        return into;
    }

    public static FilteredFluidStats modifyFiltersByOrdinal(int capacity, int maxFluidTemperature, boolean allowPartialFill, @Range(from = 0, to = 7) int ordinal) {
        return new FilteredFluidStats(capacity, allowPartialFill, switch (ordinal) {
            case 0, 1 ->
                    withMaxFluidTemperature(Materials.Iron.hasProperty(PropertyKey.FLUID_PIPE) ? Materials.Iron : Materials.Steel, maxFluidTemperature);
            case 2 -> withMaxFluidTemperature(Materials.Steel, maxFluidTemperature);
            case 3 -> withMaxFluidTemperature(Materials.Aluminium, maxFluidTemperature);
            case 4 -> withMaxFluidTemperature(Materials.StainlessSteel, maxFluidTemperature);
            case 5 -> withMaxFluidTemperature(Materials.Titanium, maxFluidTemperature);
            case 6 -> withMaxFluidTemperature(Materials.TungstenSteel, maxFluidTemperature);
            case 7 -> GLASS;
            default ->
                    throw new RuntimeException("Detected more than 8 fluid cells in MetaItem1, this is insane..."); // Should never throw
        });
    }

    public static boolean isWorldClient(World world) {
        return world.isRemote || world.getMinecraftServer() == null;
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
}
