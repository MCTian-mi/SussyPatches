package dev.tianmi.sussypatches.integration.groovy;

import net.minecraftforge.fml.common.Optional;

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

@GregTechModule(moduleID = SusModules.BAUBLES_ID,
                containerID = Tags.MODID,
                modDependencies = Mods.Names.BAUBLES,
                name = SusModules.BAUBLES_NAME,
                description = SusModules.BAUBLES_DESC)
public class GroovyModule extends IntegrationSubmodule implements GroovyPlugin {

    @Override
    public @NotNull String getModId() {
        return Tags.MODID;
    }

    @Override
    public @NotNull String getContainerName() {
        return Tags.MODID;
    }

    @Optional.Method(modid = Mods.Names.GROOVY_SCRIPT)
    @Override
    public void onCompatLoaded(GroovyContainer<?> container) {
        if (SusConfig.API.recipeInfo) {
            ExpansionHelper.mixinMethod(RecipeBuilder.class, SusGroovyExpansions.class, "info");
        }
    }
}
