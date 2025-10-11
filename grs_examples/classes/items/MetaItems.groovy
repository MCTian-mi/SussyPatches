package classes.items

import gregtech.api.items.metaitem.MetaItem
import gregtech.api.items.metaitem.StandardMetaItem
import net.minecraft.util.ResourceLocation

class MetaItems {

    static def item

    static def OVERWORLD_DISPLAY_ITEM
    static def NETHER_DISPLAY_ITEM
    static def END_DISPLAY_ITEM
    static def END_MAIN_ISLAND_DISPLAY_ITEM
    static def END_OUTER_ISLANDS_DISPLAY_ITEM
    static def MOON_DISPLAY_ITEM
    static def MERCURY_DISPLAY_ITEM
    static def MARS_DISPLAY_ITEM
    static def DEEP_DARK_DISPLAY_ITEM
    static def DEIMOS_DISPLAY_ITEM
    static def PHOBOS_DISPLAY_ITEM
    static def PLUTO_DISPLAY_ITEM
    static def TITAN_DISPLAY_ITEM

    static void registerItems() {
        item = new StandardMetaItem() {

            @Override
            ResourceLocation createItemModelPath(MetaItem.MetaValueItem metaValueItem, String postfix) {
                new ResourceLocation("supersymmetry", formatModelPath(metaValueItem) + postfix)
            }

        }.tap { setRegistryName 'sus_meta_item' }

        /// Dimension Display items 1000 - 1199
        OVERWORLD_DISPLAY_ITEM = item.addItem(1000, 'display.overworld')
        NETHER_DISPLAY_ITEM = item.addItem(1001, 'display.nether')
        END_DISPLAY_ITEM = item.addItem(1002, 'display.end')
        END_MAIN_ISLAND_DISPLAY_ITEM = item.addItem(1003, 'display.end_main_island')
        END_OUTER_ISLANDS_DISPLAY_ITEM = item.addItem(1004, 'display.end_outer_islands')
        MOON_DISPLAY_ITEM = item.addItem(1005, 'display.moon')
        MERCURY_DISPLAY_ITEM = item.addItem(1006, 'display.mercury')
        MARS_DISPLAY_ITEM = item.addItem(1007, 'display.mars')
        DEIMOS_DISPLAY_ITEM = item.addItem(1008, 'display.deimos')
        PHOBOS_DISPLAY_ITEM = item.addItem(1009, 'display.phobos')
        PLUTO_DISPLAY_ITEM = item.addItem(1010, 'display.pluto')
        TITAN_DISPLAY_ITEM = item.addItem(1011, 'display.titan')
        DEEP_DARK_DISPLAY_ITEM = item.addItem(1012, 'display.deep_dark')
    }

}