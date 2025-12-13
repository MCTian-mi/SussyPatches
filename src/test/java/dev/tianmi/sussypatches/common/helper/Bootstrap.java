package dev.tianmi.sussypatches.common.helper;

import dev.tianmi.sussypatches.api.unification.SusMaterialFlags;
import dev.tianmi.sussypatches.api.unification.material.properties.MolarProperty;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.fluids.GTFluidRegistration;
import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.api.unification.material.registry.MarkerMaterialRegistry;
import gregtech.common.items.MetaItems;
import gregtech.core.unification.material.internal.MaterialRegistryManager;
import gregtech.modules.ModuleManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.oredict.OreDictionary;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static gregtech.api.unification.material.Materials.BandedIron;
import static gregtech.api.unification.material.Materials.Ice;

public class Bootstrap {
    public static MaterialRegistryManager managerInternal;
    public static final String TEST_MOD = "sussypatches_test";
    public static final String TEST_MAP_NAME = "test_map";


    public static Material Wastewater;
    public static Material Brubium;
    public static Material Zalgonium;
    public static Material DilutedBrubium;

    public static RecipeMap<TestRecipeBuilder> TEST_MAP;

    public static void init() {
        try {
            Field deobfuscatedEnvironment = CoreModManager.class.getDeclaredField("deobfuscatedEnvironment");
            deobfuscatedEnvironment.setAccessible(true);
            deobfuscatedEnvironment.setBoolean(null, true);

            Method setLocale = I18n.class.getDeclaredMethod("setLocale", Locale.class); // No need to care about
            // obfuscation
            setLocale.setAccessible(true);
            setLocale.invoke(null, new Locale());

            // set FMLCommonHandler#sidedDelegate, since MaterialIconType and LocalizationUtils uses it
            Field sidedDelegate = FMLCommonHandler.class.getDeclaredField("sidedDelegate");
            sidedDelegate.setAccessible(true);
            sidedDelegate.set(FMLCommonHandler.instance(), new TestSidedHandler());
        } catch (NoSuchFieldException | InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Unexpected exception on test bootstrap", e);
        }
        managerInternal = MaterialRegistryManager.getInstance();
        GregTechAPI.materialManager = managerInternal;
        GregTechAPI.markerMaterialRegistry = MarkerMaterialRegistry.getInstance();

        net.minecraft.init.Bootstrap.register();
        ModMetadata meta = new ModMetadata();
        meta.modId = GTValues.MODID;
        Loader.instance().setupTestHarness(new DummyModContainer(meta));
        GregTechAPI.moduleManager = ModuleManager.getInstance();
        managerInternal.unfreezeRegistries();
        Materials.register();
        Wastewater = new Material.Builder(20000, new ResourceLocation(GTValues.MODID, "wastewater"))
                .fluid()
                .flags(SusMaterialFlags.NON_STOICHIOMETRIC)
                .build();
        Brubium = new Material.Builder(20001, new ResourceLocation(GTValues.MODID, "brubium"))
                .dust()
                .build();
        Zalgonium = new Material.Builder(20002, new ResourceLocation(GTValues.MODID, "zalgonium"))
                .dust()
                .fluid()
                .build();
        DilutedBrubium = new Material.Builder(20003, new ResourceLocation(GTValues.MODID, "diluted_brubium"))
                .fluid()
                .build();

        Ice.setProperty(MolarProperty.MOLAR, MolarProperty.fromFluidConversion(1000, 144));
        BandedIron.addFlags(SusMaterialFlags.SINGLE_ITEM_MOLE);

        managerInternal.closeRegistries();
        managerInternal.freezeRegistries();
        GTFluidRegistration.INSTANCE.register();
        OreDictUnifier.init();

        MetaItems.init();
        for (MetaItem<?> item : MetaItems.ITEMS) {
            if (item instanceof MetaPrefixItem) {
                item.registerSubItems();
                for (MetaItem.MetaValueItem i : item.getAllItems()) {
                    // The unlocalized name is specifically the ore prefix here
                    OreDictUnifier.onItemRegistration(new OreDictionary.OreRegisterEvent(
                            i.unlocalizedName, i.getStackForm()));
                }
            }
        }

        TEST_MAP =
                new RecipeMap<>(TEST_MAP_NAME, 4, 4, 4, 4, new TestRecipeBuilder(), false);
    }
}
