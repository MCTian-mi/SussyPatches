package dev.tianmi.sussypatches.api.mui;

import java.util.Map;

import com.cleanroommc.modularui.drawable.UITexture;

import dev.tianmi.sussypatches.Tags;
import gregtech.api.recipes.RecipeMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

public interface SusGuiTextures {

    Map<RecipeMap<?>, UITexture> PROGRESS_BARS = new Object2ObjectArrayMap<>(RecipeMap.getRecipeMaps().size());

    UITexture VANILLA_SCROLL_BAR = UITexture.builder()
            .location(Tags.MODID, "gui/widgets/mc_scroll_bar")
            .imageSize(28, 17)
            .uv(0F, 0F, .5F, 1F)
            .adaptable(3)
            .tiled()
            .name("mc_scroll_bar")
            .build();

    UITexture VANILLA_SCROLL_BAR_DISABLED = UITexture.builder()
            .location(Tags.MODID, "gui/widgets/mc_scroll_bar")
            .imageSize(28, 17)
            .uv(.5F, 0F, 1F, 1F)
            .adaptable(3)
            .tiled()
            .name("mc_scroll_bar_disabled")
            .build();
}
