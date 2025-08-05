package dev.tianmi.sussypatches.client.renderer.textures;

import static dev.tianmi.sussypatches.client.renderer.textures.cube.VisualStateRenderer.from;
import static gregtech.api.util.GTUtility.gregtechId;
import static gregtech.common.blocks.BlockCleanroomCasing.CasingType.PLASCRETE;
import static gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType.*;
import static gregtech.common.blocks.BlockMachineCasing.MachineCasingType.ULV;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.*;
import static gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING;
import static gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType.*;
import static gregtech.common.blocks.MetaBlocks.*;

import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.Nullable;

import dev.tianmi.sussypatches.api.util.SusMods;
import dev.tianmi.sussypatches.client.renderer.textures.cube.VisualStateRenderer;
import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.IObjectHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.BlockSteamCasing;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

/// Holds the default connected textures for GregTechCEu
/// See [GCyMConnectedTextures] for how to impl it for your own GT addon
@ParametersAreNonnullByDefault
public class ConnectedTextures {

    /// Fixing Z-fighting issues with overlays.
    /// This is an ugly workaround, but it works.
    ///
    /// @see #registerNonOverlays()
    public static final Set<ICubeRenderer> NONE_OVERLAYS = new ObjectOpenHashSet<>();

    public static final Map<ResourceLocation, Function<@Nullable IMultiblockPart, @Nullable ICubeRenderer>> REPLACEMENTS = new Object2ObjectArrayMap<>();

    // GTCEu
    public static final VisualStateRenderer PRIMITIVE_BRICKS_CTM;
    public static final VisualStateRenderer HEAT_PROOF_CASING_CTM;
    public static final VisualStateRenderer FROST_PROOF_CASING_CTM;
    public static final VisualStateRenderer SOLID_STEEL_CASING_CTM;
    public static final VisualStateRenderer VOLTAGE_CASING_ULV_CTM;
    public static final VisualStateRenderer CLEAN_STAINLESS_STEEL_CASING_CTM;
    public static final VisualStateRenderer STABLE_TITANIUM_CASING_CTM;
    public static final VisualStateRenderer ROBUST_TUNGSTENSTEEL_CASING_CTM;
    public static final VisualStateRenderer STEEL_TURBINE_CASING_CTM;
    public static final VisualStateRenderer STAINLESS_TURBINE_CASING_CTM;
    public static final VisualStateRenderer TITANIUM_TURBINE_CASING_CTM; // Unused
    public static final VisualStateRenderer TUNGSTENSTEEL_TURBINE_CASING_CTM;
    public static final VisualStateRenderer BRONZE_PLATED_BRICKS_CTM;
    public static final VisualStateRenderer BRONZE_FIREBOX_CTM;
    public static final VisualStateRenderer BRONZE_FIREBOX_ACTIVE_CTM;
    public static final VisualStateRenderer STEEL_FIREBOX_CTM;
    public static final VisualStateRenderer STEEL_FIREBOX_ACTIVE_CTM;
    public static final VisualStateRenderer TITANIUM_FIREBOX_CTM;
    public static final VisualStateRenderer TITANIUM_FIREBOX_ACTIVE_CTM;
    public static final VisualStateRenderer TUNGSTENSTEEL_FIREBOX_CTM;
    public static final VisualStateRenderer TUNGSTENSTEEL_FIREBOX_ACTIVE_CTM;
    public static final VisualStateRenderer COKE_BRICKS_CTM;
    public static final VisualStateRenderer GRATE_CASING_STEEL_FRONT_CTM;
    public static final VisualStateRenderer INERT_PTFE_CASING_CTM;
    public static final VisualStateRenderer STURDY_HSSE_CASING_CTM;
    public static final VisualStateRenderer PLASCRETE_CTM;
    public static final VisualStateRenderer COMPUTER_CASING_CTM;
    public static final VisualStateRenderer HIGH_POWER_CASING_CTM;
    public static final VisualStateRenderer ADVANCED_COMPUTER_CASING_CTM;
    public static final VisualStateRenderer PALLADIUM_SUBSTATION_CASING_CTM;
    public static final VisualStateRenderer WOOD_WALL_CTM;
    public static final VisualStateRenderer PRIMITIVE_PUMP_CTM;

    @Nullable
    public static ICubeRenderer get(ResourceLocation id, @Nullable IMultiblockPart part) {
        var function = REPLACEMENTS.get(id);
        if (function == null) return null;
        return function.apply(part);
    }

    public static void registerCustomOverride(ResourceLocation id,
                                              Function<@Nullable IMultiblockPart, @Nullable ICubeRenderer> function) {
        REPLACEMENTS.put(id, function);
    }

    public static boolean shouldOffset(ICubeRenderer overlay) {
        return !NONE_OVERLAYS.contains(overlay);
    }

    private static ICubeRenderer fromBoilerPart(@Nullable IMultiblockPart part,
                                                ICubeRenderer casing,
                                                ICubeRenderer firebox,
                                                ICubeRenderer fireboxActive) {
        if (part instanceof IMultiblockAbilityPart<?>ability &&
                ability.getAbility() == MultiblockAbility.EXPORT_FLUIDS) {
            return casing;
        } else if (part instanceof MetaTileEntityMultiblockPart actualPart) {
            return actualPart.getController().isActive() ? fireboxActive : firebox;
        }
        return casing;
    }

    public static void init() {
        registerNonOverlays();
        registerCTMOverrides();

        if (SusMods.GCyM.isLoaded()) {
            GCyMConnectedTextures.init();
        }
    }

    static {
        PRIMITIVE_BRICKS_CTM = from(METAL_CASING.getState(PRIMITIVE_BRICKS));
        HEAT_PROOF_CASING_CTM = from(METAL_CASING.getState(INVAR_HEATPROOF));
        FROST_PROOF_CASING_CTM = from(METAL_CASING.getState(ALUMINIUM_FROSTPROOF));
        SOLID_STEEL_CASING_CTM = from(METAL_CASING.getState(STEEL_SOLID));
        VOLTAGE_CASING_ULV_CTM = from(MACHINE_CASING.getState(ULV));
        CLEAN_STAINLESS_STEEL_CASING_CTM = from(METAL_CASING.getState(STAINLESS_CLEAN));
        STABLE_TITANIUM_CASING_CTM = from(METAL_CASING.getState(TITANIUM_STABLE));
        ROBUST_TUNGSTENSTEEL_CASING_CTM = from(METAL_CASING.getState(TUNGSTENSTEEL_ROBUST));
        STEEL_TURBINE_CASING_CTM = from(TURBINE_CASING.getState(STEEL_TURBINE_CASING));
        STAINLESS_TURBINE_CASING_CTM = from(TURBINE_CASING.getState(STAINLESS_TURBINE_CASING));
        TITANIUM_TURBINE_CASING_CTM = from(TURBINE_CASING.getState(TITANIUM_TURBINE_CASING));
        TUNGSTENSTEEL_TURBINE_CASING_CTM = from(TURBINE_CASING.getState(TUNGSTENSTEEL_TURBINE_CASING));
        BRONZE_PLATED_BRICKS_CTM = from(METAL_CASING.getState(BRONZE_BRICKS));
        BRONZE_FIREBOX_CTM = from(BOILER_FIREBOX_CASING.getState(BRONZE_FIREBOX));
        BRONZE_FIREBOX_ACTIVE_CTM = from(BOILER_FIREBOX_CASING.getState(BRONZE_FIREBOX));
        STEEL_FIREBOX_CTM = from(BOILER_FIREBOX_CASING.getState(STEEL_FIREBOX));
        STEEL_FIREBOX_ACTIVE_CTM = from(BOILER_FIREBOX_CASING.getState(STEEL_FIREBOX));
        TITANIUM_FIREBOX_CTM = from(BOILER_FIREBOX_CASING.getState(TITANIUM_FIREBOX));
        TITANIUM_FIREBOX_ACTIVE_CTM = from(BOILER_FIREBOX_CASING.getState(TITANIUM_FIREBOX));
        TUNGSTENSTEEL_FIREBOX_CTM = from(BOILER_FIREBOX_CASING.getState(TUNGSTENSTEEL_FIREBOX));
        TUNGSTENSTEEL_FIREBOX_ACTIVE_CTM = from(BOILER_FIREBOX_CASING.getState(TUNGSTENSTEEL_FIREBOX));
        COKE_BRICKS_CTM = from(METAL_CASING.getState(COKE_BRICKS));
        GRATE_CASING_STEEL_FRONT_CTM = from(MULTIBLOCK_CASING.getState(GRATE_CASING));
        INERT_PTFE_CASING_CTM = from(METAL_CASING.getState(PTFE_INERT_CASING));
        STURDY_HSSE_CASING_CTM = from(METAL_CASING.getState(HSSE_STURDY));
        PLASCRETE_CTM = from(CLEANROOM_CASING.getState(PLASCRETE));
        COMPUTER_CASING_CTM = from(COMPUTER_CASING.getState(BlockComputerCasing.CasingType.COMPUTER_CASING));
        HIGH_POWER_CASING_CTM = from(COMPUTER_CASING.getState(BlockComputerCasing.CasingType.HIGH_POWER_CASING));
        ADVANCED_COMPUTER_CASING_CTM = from(
                COMPUTER_CASING.getState(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING));
        PALLADIUM_SUBSTATION_CASING_CTM = from(METAL_CASING.getState(PALLADIUM_SUBSTATION));
        WOOD_WALL_CTM = from(STEAM_CASING.getState(BlockSteamCasing.SteamCasingType.WOOD_WALL));
        PRIMITIVE_PUMP_CTM = from(STEAM_CASING.getState(BlockSteamCasing.SteamCasingType.PUMP_DECK));
    }

    /// Register instances of [SimpleOverlayRenderer] which isn't technically an overlay.
    /// A dirty workaround, yeah.
    /// Honestly, use [SimpleCubeRenderer] instead plz.
    ///
    /// @see SimpleOverlayRendererMixin
    public static void registerNonOverlays() {
        NONE_OVERLAYS.addAll(Arrays.asList(
                Textures.BRONZE_PLATED_BRICKS,
                Textures.PRIMITIVE_BRICKS,
                Textures.COKE_BRICKS,
                Textures.HEAT_PROOF_CASING,
                Textures.FROST_PROOF_CASING,
                Textures.SOLID_STEEL_CASING,
                Textures.CLEAN_STAINLESS_STEEL_CASING,
                Textures.STABLE_TITANIUM_CASING,
                Textures.ROBUST_TUNGSTENSTEEL_CASING,
                Textures.STURDY_HSSE_CASING,
                Textures.PALLADIUM_SUBSTATION_CASING,
                Textures.INERT_PTFE_CASING,
                Textures.PLASCRETE,
                Textures.FUSION_TEXTURE,
                Textures.ACTIVE_FUSION_TEXTURE,
                Textures.GRATE_CASING,
                Textures.HIGH_POWER_CASING));
    }

    /// Override original [ICubeRenderer]s for CEu/GCyM multiblocks.
    ///
    /// @see MultiblockControllerBaseMixin
    public static void registerCTMOverrides() {
        PRIMITIVE_BRICKS_CTM.override(gregtechId("primitive_blast_furnace.bronze"));
        HEAT_PROOF_CASING_CTM.override(gregtechId("electric_blast_furnace"),
                gregtechId("multi_furnace"));
        FROST_PROOF_CASING_CTM.override(gregtechId("vacuum_freezer"));
        SOLID_STEEL_CASING_CTM.override(gregtechId("implosion_compressor"),
                gregtechId("large_miner.ev"),
                gregtechId("central_monitor"),
                gregtechId("fluid_drilling_rig.mv"),
                gregtechId("tank.steel"));
        VOLTAGE_CASING_ULV_CTM.override(gregtechId("pyrolyse_oven")); // Who would give connected textures for this
        // :clueless:
        CLEAN_STAINLESS_STEEL_CASING_CTM.override(gregtechId("distillation_tower"),
                gregtechId("cracker"));
        STABLE_TITANIUM_CASING_CTM.override(gregtechId("large_combustion_engine"),
                gregtechId("large_miner.iv"),
                gregtechId("fluid_drilling_rig.hv"));
        ROBUST_TUNGSTENSTEEL_CASING_CTM.override(gregtechId("extreme_combustion_engine"),
                gregtechId("large_miner.luv"),
                gregtechId("processing_array"),
                gregtechId("fluid_drilling_rig.ev"));
        STEEL_TURBINE_CASING_CTM.override(gregtechId("large_turbine.steam"));
        STAINLESS_TURBINE_CASING_CTM.override(gregtechId("large_turbine.gas"));
        TUNGSTENSTEEL_TURBINE_CASING_CTM.override(gregtechId("large_turbine.plasma"));

        registerCustomOverride(gregtechId("large_boiler.bronze"), part -> fromBoilerPart(
                part, BRONZE_PLATED_BRICKS_CTM, BRONZE_FIREBOX_CTM, BRONZE_FIREBOX_ACTIVE_CTM));

        registerCustomOverride(gregtechId("large_boiler.steel"), part -> fromBoilerPart(
                part, SOLID_STEEL_CASING_CTM, STEEL_FIREBOX_CTM, STEEL_FIREBOX_ACTIVE_CTM));

        registerCustomOverride(gregtechId("large_boiler.titanium"), part -> fromBoilerPart(
                part, STABLE_TITANIUM_CASING_CTM, TITANIUM_FIREBOX_CTM, TITANIUM_FIREBOX_ACTIVE_CTM));

        registerCustomOverride(gregtechId("large_boiler.tungstensteel"), part -> fromBoilerPart(
                part, ROBUST_TUNGSTENSTEEL_CASING_CTM, TUNGSTENSTEEL_FIREBOX_CTM, TUNGSTENSTEEL_FIREBOX_ACTIVE_CTM));

        COKE_BRICKS_CTM.override(gregtechId("coke_oven"));

        registerCustomOverride(gregtechId("assembly_line"), part -> part == null || part instanceof IDataAccessHatch ?
                GRATE_CASING_STEEL_FRONT_CTM : SOLID_STEEL_CASING_CTM);

        // Skipping Fusion Reactors, it doesn't look like they should have connected textures

        INERT_PTFE_CASING_CTM.override(gregtechId("large_chemical_reactor"));

        final boolean useSteel = ConfigHolder.machines.steelSteamMultiblocks;

        registerCustomOverride(gregtechId("steam_oven"), part -> {
            if (part instanceof IMultiblockAbilityPart<?>abilityPart &&
                    abilityPart.getAbility() == MultiblockAbility.STEAM) {
                if (part instanceof MetaTileEntityMultiblockPart actualPart) {
                    boolean active = actualPart.getController().isActive();
                    if (useSteel) {
                        return active ? STEEL_FIREBOX_ACTIVE_CTM : STEEL_FIREBOX_CTM;
                    } else {
                        return active ? BRONZE_FIREBOX_ACTIVE_CTM : BRONZE_FIREBOX_CTM;
                    }
                }
            }
            return useSteel ? SOLID_STEEL_CASING_CTM : BRONZE_PLATED_BRICKS_CTM;
        });

        registerCustomOverride(gregtechId("steam_grinder"),
                part -> useSteel ? SOLID_STEEL_CASING_CTM : BRONZE_PLATED_BRICKS_CTM);

        STURDY_HSSE_CASING_CTM.override(gregtechId("advanced_processing_array"));
        PLASCRETE_CTM.override(gregtechId("cleanroom"));
        BRONZE_PLATED_BRICKS_CTM.override(gregtechId("charcoal_pile"));  // Why would one use this...?

        registerCustomOverride(gregtechId("data_bank"),
                part -> part instanceof IDataAccessHatch ? COMPUTER_CASING_CTM : HIGH_POWER_CASING_CTM);

        registerCustomOverride(gregtechId("research_station"), part -> part == null || part instanceof IObjectHolder ?
                ADVANCED_COMPUTER_CASING_CTM : COMPUTER_CASING_CTM);

        registerCustomOverride(gregtechId("high_performance_computing_array"),
                part -> part == null ? ADVANCED_COMPUTER_CASING_CTM : COMPUTER_CASING_CTM);

        COMPUTER_CASING_CTM.override(gregtechId("network_switch"));
        PALLADIUM_SUBSTATION_CASING_CTM.override(gregtechId("power_substation"));
        HIGH_POWER_CASING_CTM.override(gregtechId("active_transformer"));
        WOOD_WALL_CTM.override(gregtechId("tank.wood"));
        PRIMITIVE_PUMP_CTM.override(gregtechId("primitive_water_pump"));
    }
}
