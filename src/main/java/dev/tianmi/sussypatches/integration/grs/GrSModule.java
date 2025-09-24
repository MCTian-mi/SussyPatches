package dev.tianmi.sussypatches.integration.grs;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.groovyscript.api.GroovyPlugin;
import com.cleanroommc.groovyscript.compat.mods.GroovyContainer;
import com.cleanroommc.groovyscript.sandbox.expand.ExpansionHelper;

import dev.tianmi.sussypatches.Tags;
import dev.tianmi.sussypatches.common.SusConfig;
import dev.tianmi.sussypatches.modules.SusModules;
import gregtech.api.modules.GregTechModule;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;

@GregTechModule(moduleID = SusModules.GrS_ID,
                containerID = Tags.MODID,
                modDependencies = Mods.Names.GROOVY_SCRIPT,
                name = SusModules.GrS_NAME,
                description = SusModules.GrS_DESC)
public class GrSModule extends IntegrationSubmodule implements GroovyPlugin {

    @NotNull
    @Override
    public String getModId() {
        return Tags.MODID;
    }

    @NotNull
    @Override
    public String getContainerName() {
        return Tags.MODNAME;
    }

    @Override
    public void onCompatLoaded(GroovyContainer<?> container) {
        if (SusConfig.API.recipeInfo) {
            ExpansionHelper.mixinMethod(RecipeBuilder.class, GroovyExpansions.class, "info");
        }
    }
}
