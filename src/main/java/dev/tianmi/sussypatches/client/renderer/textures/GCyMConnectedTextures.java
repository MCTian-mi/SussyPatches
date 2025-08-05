package dev.tianmi.sussypatches.client.renderer.textures;

import static dev.tianmi.sussypatches.client.renderer.textures.ConnectedTextures.*;
import static dev.tianmi.sussypatches.client.renderer.textures.cube.VisualStateRenderer.from;
import static gregicality.multiblocks.api.utils.GCYMUtil.gcymId;
import static gregicality.multiblocks.common.block.GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING;
import static gregicality.multiblocks.common.block.blocks.BlockLargeMultiblockCasing.CasingType.*;

import java.util.Arrays;

import javax.annotation.ParametersAreNonnullByDefault;

import dev.tianmi.sussypatches.client.renderer.textures.cube.VisualStateRenderer;
import gregicality.multiblocks.api.render.GCYMTextures;
import gregtech.api.capability.IMufflerHatch;

@ParametersAreNonnullByDefault
public class GCyMConnectedTextures {

    // GCyM
    public static final VisualStateRenderer MACERATOR_CASING_CTM;
    public static final VisualStateRenderer BLAST_CASING_CTM;
    public static final VisualStateRenderer ASSEMBLING_CASING_CTM;
    public static final VisualStateRenderer STRESS_PROOF_CTM;
    public static final VisualStateRenderer CORROSION_PROOF_CASING_CTM;
    public static final VisualStateRenderer VIBRATION_SAFE_CASING_CTM;
    public static final VisualStateRenderer WATERTIGHT_CASING_CTM;
    public static final VisualStateRenderer CUTTER_CASING_CTM;
    public static final VisualStateRenderer NONCONDUCTING_CASING_CTM;
    public static final VisualStateRenderer MIXER_CASING_CTM;
    public static final VisualStateRenderer ENGRAVER_CASING_CTM;
    public static final VisualStateRenderer ATOMIC_CASING_CTM; // Unused
    public static final VisualStateRenderer STEAM_CASING_CTM;

    static {
        MACERATOR_CASING_CTM = from(LARGE_MULTIBLOCK_CASING.getState(MACERATOR_CASING));
        BLAST_CASING_CTM = from(LARGE_MULTIBLOCK_CASING.getState(HIGH_TEMPERATURE_CASING));
        ASSEMBLING_CASING_CTM = from(LARGE_MULTIBLOCK_CASING.getState(ASSEMBLING_CASING));
        STRESS_PROOF_CTM = from(LARGE_MULTIBLOCK_CASING.getState(STRESS_PROOF_CASING));
        CORROSION_PROOF_CASING_CTM = from(
                LARGE_MULTIBLOCK_CASING.getState(CORROSION_PROOF_CASING));
        VIBRATION_SAFE_CASING_CTM = from(
                LARGE_MULTIBLOCK_CASING.getState(VIBRATION_SAFE_CASING));
        WATERTIGHT_CASING_CTM = from(LARGE_MULTIBLOCK_CASING.getState(WATERTIGHT_CASING));
        CUTTER_CASING_CTM = from(LARGE_MULTIBLOCK_CASING.getState(CUTTER_CASING));
        NONCONDUCTING_CASING_CTM = from(
                LARGE_MULTIBLOCK_CASING.getState(NONCONDUCTING_CASING));
        MIXER_CASING_CTM = from(LARGE_MULTIBLOCK_CASING.getState(MIXER_CASING));
        ENGRAVER_CASING_CTM = from(LARGE_MULTIBLOCK_CASING.getState(ENGRAVER_CASING));
        ATOMIC_CASING_CTM = from(LARGE_MULTIBLOCK_CASING.getState(ATOMIC_CASING)); // Unused
        STEAM_CASING_CTM = from(LARGE_MULTIBLOCK_CASING.getState(STEAM_CASING));
    }

    public static void init() {
        registerNonOverlays();
        registerCTMOverrides();
    }

    public static void registerNonOverlays() {
        ConnectedTextures.NONE_OVERLAYS.addAll(Arrays.asList(
                GCYMTextures.MACERATOR_CASING,
                GCYMTextures.BLAST_CASING,
                GCYMTextures.ASSEMBLING_CASING,
                GCYMTextures.STRESS_PROOF_CASING,
                GCYMTextures.CORROSION_PROOF_CASING,
                GCYMTextures.VIBRATION_SAFE_CASING,
                GCYMTextures.WATERTIGHT_CASING,
                GCYMTextures.CUTTER_CASING,
                GCYMTextures.NONCONDUCTING_CASING,
                GCYMTextures.MIXER_CASING,
                GCYMTextures.ENGRAVER_CASING,
                GCYMTextures.ATOMIC_CASING,
                GCYMTextures.STEAM_CASING));
    }

    public static void registerCTMOverrides() {
        MACERATOR_CASING_CTM.override(gcymId("large_macerator"));
        BLAST_CASING_CTM.override(gcymId("alloy_blast_smelter"),
                gcymId("large_arc_furnace"));
        ASSEMBLING_CASING_CTM.override(gcymId("large_assembler"),
                gcymId("large_circuit_assembler"));
        WATERTIGHT_CASING_CTM.override(gcymId("large_autoclave"),
                gcymId("large_chemical_bath"),
                gcymId("large_extractor"),
                gcymId("large_distillery"),
                gcymId("large_solidifier"));
        STRESS_PROOF_CTM.override(gcymId("large_bender"),
                gcymId("large_extruder"),
                gcymId("large_wiremill"));
        CORROSION_PROOF_CASING_CTM.override(gcymId("large_brewer"));
        VIBRATION_SAFE_CASING_CTM.override(gcymId("large_centrifuge"),
                gcymId("large_sifter"));
        CUTTER_CASING_CTM.override(gcymId("large_cutter"));
        NONCONDUCTING_CASING_CTM.override(gcymId("large_electrolyzer"),
                gcymId("large_polarizer"));
        MIXER_CASING_CTM.override(gcymId("large_mixer"));
        SOLID_STEEL_CASING_CTM.override(gcymId("large_packager"));
        ENGRAVER_CASING_CTM.override(gcymId("large_engraver"));
        ROBUST_TUNGSTENSTEEL_CASING_CTM.override(gcymId("electric_implosion_compressor"));

        registerCustomOverride(gcymId("mega_blast_furnace"),
                part -> part instanceof IMufflerHatch ? ROBUST_TUNGSTENSTEEL_CASING_CTM : BLAST_CASING_CTM);

        FROST_PROOF_CASING_CTM.override(gcymId("mega_vacuum_freezer"));
        STEAM_CASING_CTM.override(gcymId("steam_engine"));
    }
}
