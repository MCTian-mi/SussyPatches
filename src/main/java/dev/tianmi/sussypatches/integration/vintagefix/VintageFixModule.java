package dev.tianmi.sussypatches.integration.vintagefix;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.embeddedt.vintagefix.dynamicresources.model.DynamicBakedModelProvider;
import org.embeddedt.vintagefix.event.DynamicModelBakeEvent;
import org.jetbrains.annotations.NotNull;

import dev.tianmi.sussypatches.api.util.SusMods;
import dev.tianmi.sussypatches.common.SusConfig;
import dev.tianmi.sussypatches.core.mixin.compat.lampbakedmodel.LampBakedModelAccessor;
import dev.tianmi.sussypatches.core.mixin.compat.lampbakedmodel.LampBakedModelAccessor.EntryAccessor;
import dev.tianmi.sussypatches.core.mixin.compat.lampbakedmodel.LampBakedModelAccessor.KeyAccessor;
import dev.tianmi.sussypatches.modules.SusModules;
import gregtech.api.modules.GregTechModule;
import gregtech.integration.IntegrationSubmodule;

@GregTechModule(moduleID = SusModules.ViF_ID,
                containerID = SusModules.CORE_ID,
                modDependencies = SusMods.Names.VINTAGE_FIX,
                name = SusModules.ViF_NAME,
                description = SusModules.ViF_DESC)
public class VintageFixModule extends IntegrationSubmodule {

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return SusConfig.COMPAT.fixLampModel ? Collections.singletonList(VintageFixModule.class) :
                Collections.emptyList();
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onDynModelBake(DynamicModelBakeEvent event) {
        if (!(event.location instanceof ModelResourceLocation)) return;

        for (Map.Entry<KeyAccessor, EntryAccessor> e : LampBakedModelAccessor.getEntries().entrySet()) {
            var entry = e.getValue();
            if (entry.getOriginalModel() == event.location) {
                if (entry.getCustomItemModel() != null) {
                    IBakedModel model = event.bakedModel;
                    if (model != null) {
                        // Directly provide existing model to prevent using CTM models
                        IBakedModel customModel = e.getKey().getModelType().createModel(model);
                        DynamicBakedModelProvider.instance.putObject(entry.getCustomItemModel(), customModel);
                    }
                }
            }
        }
    }
}
