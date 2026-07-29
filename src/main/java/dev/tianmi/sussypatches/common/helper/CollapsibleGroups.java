package dev.tianmi.sussypatches.common.helper;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Multimaps;
import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.api.core.mixin.extension.GTToolExtension;
import dev.tianmi.sussypatches.common.SusConfig;
import gregtech.api.GregTechAPI;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.MetaItem.MetaValueItem;
import gregtech.api.metatileentity.ITieredMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.blocks.BlockCompressed;
import gregtech.common.blocks.BlockFrame;
import gregtech.common.blocks.BlockLamp;
import gregtech.common.blocks.BlockOre;
import gregtech.common.items.MetaItems;
import gregtech.common.items.ToolItems;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.cable.Insulation;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.FluidPipeType;
import gregtech.common.pipelike.itempipe.BlockItemPipe;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.val;
import mezz.jei.api.ICollapsibleGroupRegistry;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import org.jspecify.annotations.NullMarked;

import java.util.*;

import static gregtech.common.blocks.MetaBlocks.*;

@NullMarked
public final class CollapsibleGroups {

    /// Registers all GTCEu-related collapsible groups when enabled in config.
    public static void registerGroups(ICollapsibleGroupRegistry registry) {
        if (!SusConfig.TWEAK.collapseGTItems) return;

        buildPrefixGroups(registry);
        buildCableGroup(registry);
        buildItemPipeGroup(registry);
        buildFluidPipeGroup(registry);
        buildOreGroup(registry);
        buildFrameGroup(registry);
        buildLampGroup(registry);
        buildStorageBlockGroup(registry);
        buildFluidCellGroup(registry);
        buildMoldGroup(registry);
        buildExtruderShapeGroup(registry);
        buildTieredMTEGroups(registry);
        buildGlassLensGroup(registry);
        buildChemicalDyesGroup(registry);

        if (SusConfig.TWEAK.showAllToolItems) {
            buildToolGroups(registry);
        }
    }

    /// One group per [OrePrefix] (dusts, plates, ingots, etc.).
    private static void buildPrefixGroups(ICollapsibleGroupRegistry registry) {
        Map<OrePrefix, List<ItemStack>> buckets = new Object2ObjectOpenHashMap<>();
        for (MetaItem<?> metaItem : MetaItems.ITEMS) {
            if (!(metaItem instanceof MetaPrefixItem prefixItem)) continue;
            OrePrefix prefix = prefixItem.getOrePrefix();
            for (MetaValueItem valueItem : metaItem.getAllItems()) {
                buckets.computeIfAbsent(prefix, k -> new ArrayList<>()).add(valueItem.getStackForm());
            }
        }
        for (Map.Entry<OrePrefix, List<ItemStack>> entry : buckets.entrySet()) {
            String name = entry.getKey().name();
            addGroup(registry, "oreprefix." + name, entry.getValue());
        }
    }

    /// All GT cables and wires (every [Insulation] × material).
    private static void buildCableGroup(ICollapsibleGroupRegistry registry) {
        List<ItemStack> stacks = new ArrayList<>();
        for (BlockCable[] blocks : CABLES.values()) {
            for (BlockCable block : blocks) {
                addSubBlocks(stacks, block);
            }
        }
        addGroup(registry, "cables", stacks);
    }

    /// All GT item pipes (every [ItemPipeType] × material).
    private static void buildItemPipeGroup(ICollapsibleGroupRegistry registry) {
        List<ItemStack> stacks = new ArrayList<>();
        for (BlockItemPipe[] blocks : ITEM_PIPES.values()) {
            for (BlockItemPipe block : blocks) {
                addSubBlocks(stacks, block);
            }
        }
        addGroup(registry, "item_pipes", stacks);
    }

    /// All GT fluid pipes (every [FluidPipeType] × material).
    private static void buildFluidPipeGroup(ICollapsibleGroupRegistry registry) {
        List<ItemStack> stacks = new ArrayList<>();
        for (BlockFluidPipe[] blocks : FLUID_PIPES.values()) {
            for (BlockFluidPipe block : blocks) {
                addSubBlocks(stacks, block);
            }
        }
        addGroup(registry, "fluid_pipes", stacks);
    }

    /// All GT ore variants (material × stone type).
    private static void buildOreGroup(ICollapsibleGroupRegistry registry) {
        List<ItemStack> stacks = new ArrayList<>();
        for (BlockOre block : ORES) {
            addSubBlocks(stacks, block);
        }
        addGroup(registry, "ores", stacks);
    }

    /// All GT frame boxes.
    private static void buildFrameGroup(ICollapsibleGroupRegistry registry) {
        List<ItemStack> stacks = new ArrayList<>();
        for (BlockFrame block : FRAME_BLOCKS) {
            for (Material material : block.getVariantProperty().getAllowedValues()) {
                if (material == Materials.NULL) continue;
                stacks.add(block.getItem(material));
            }
        }
        addGroup(registry, "frames", stacks);
    }

    /// All GT lamps, both normal and borderless.
    private static void buildLampGroup(ICollapsibleGroupRegistry registry) {
        List<ItemStack> stacks = new ArrayList<>();
        for (BlockLamp block : LAMPS.values()) {
            addSubBlocks(stacks, block);
        }
        for (BlockLamp block : BORDERLESS_LAMPS.values()) {
            addSubBlocks(stacks, block);
        }
        addGroup(registry, "lamps", stacks);
    }

    /// All GT compressed / material storage blocks.
    private static void buildStorageBlockGroup(ICollapsibleGroupRegistry registry) {
        List<ItemStack> stacks = new ArrayList<>();
        for (BlockCompressed block : COMPRESSED_BLOCKS) {
            for (Material material : block.getVariantProperty().getAllowedValues()) {
                if (material == Materials.NULL) continue;
                stacks.add(block.getItem(material));
            }
        }
        addGroup(registry, "storage_blocks", stacks);
    }

    /// Empty GT fluid cells
    private static void buildFluidCellGroup(ICollapsibleGroupRegistry registry) {
        var stacks = new MetaValueItem[]{
                MetaItems.FLUID_CELL,
                MetaItems.FLUID_CELL_UNIVERSAL,
                MetaItems.FLUID_CELL_LARGE_STEEL,
                MetaItems.FLUID_CELL_LARGE_ALUMINIUM,
                MetaItems.FLUID_CELL_LARGE_STAINLESS_STEEL,
                MetaItems.FLUID_CELL_LARGE_TITANIUM,
                MetaItems.FLUID_CELL_LARGE_TUNGSTEN_STEEL,
                MetaItems.FLUID_CELL_GLASS_VIAL,
        };
        addGroup(registry, "fluid_cells", stacks);
    }

    /// GT fluid-solidifier molds.
    private static void buildMoldGroup(ICollapsibleGroupRegistry registry) {
        addGroup(registry, "molds", MetaItems.SHAPE_MOLDS);
    }

    /// GT extruder shapes.
    private static void buildExtruderShapeGroup(ICollapsibleGroupRegistry registry) {
        addGroup(registry, "extruder_shapes", MetaItems.SHAPE_EXTRUDERS);
    }

    /// GT glass lens.
    private static void buildGlassLensGroup(ICollapsibleGroupRegistry registry) {
        addGroup(registry, "glass_lens", MetaItems.GLASS_LENSES.values().toArray(new MetaValueItem[0]));
    }

    /// GT chemical dyes.
    private static void buildChemicalDyesGroup(ICollapsibleGroupRegistry registry) {
        addGroup(registry, "chemical_dyes", MetaItems.DYE_ONLY_ITEMS);
    }

    /// One group per loaded tiered singleblock family.
    private static void buildTieredMTEGroups(ICollapsibleGroupRegistry registry) {
        ListMultimap<String, ITieredMetaTileEntity> groups = Multimaps.newListMultimap(new HashMap<>(), ArrayList::new);

        for (val mte : GregTechAPI.MTE_REGISTRY) {
            if (mte instanceof ITieredMetaTileEntity tiered) {
                groups.put(tiered.getTierlessTooltipKey(), tiered);
            }
        }

        for (val key : groups.keySet()) {
            val group = groups.get(key);
            group.sort(Comparator.comparingInt(ITieredMetaTileEntity::getTier));
            registry.newGroup(Tags.MOD_ID + ":" + key, key)
                    .add(group.stream().map(it -> ((MetaTileEntity) it).getStackForm()).toArray())
                    .build();
        }
    }

    /// One group per materialized GT tool family when tool material sub-items are enabled.
    private static void buildToolGroups(ICollapsibleGroupRegistry registry) {
        for (var tool : ToolItems.getAllTools()) {
            List<ItemStack> stacks = new ArrayList<>();
            for (var material : GTToolExtension.getMaterials(tool)) {
                stacks.add(tool.get(material));
            }
            if (stacks.size() < 2) continue;
            String id = "tool." + tool.getToolId();
            addGroup(registry, id, stacks);
        }
    }

    private static void addSubBlocks(List<ItemStack> out, Block block) {
        NonNullList<ItemStack> sub = NonNullList.create();
        block.getSubBlocks(CreativeTabs.SEARCH, sub);
        out.addAll(sub);
    }

    private static void addGroup(ICollapsibleGroupRegistry registry,
                                 String id,
                                 MetaValueItem[] items) {
        List<ItemStack> stacks = new ArrayList<>(items.length);
        for (var item : items) {
            // CEu nonsense...
            //noinspection ConstantValue
            if (item == null) continue;
            stacks.add(item.getStackForm());
        }
        addGroup(registry, id, stacks);
    }

    private static void addGroup(ICollapsibleGroupRegistry registry,
                                 String id,
                                 Collection<ItemStack> stacks) {
        registry.newGroup(Tags.MOD_ID + ":" + id, Tags.MOD_ID + ".jei.group." + id).add(stacks.toArray()).build();
    }
}
