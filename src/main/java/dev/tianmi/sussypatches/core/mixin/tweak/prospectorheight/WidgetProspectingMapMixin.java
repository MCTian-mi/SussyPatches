package dev.tianmi.sussypatches.core.mixin.tweak.prospectorheight;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.Optional;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import dev.tianmi.sussypatches.api.annotation.Implemented;
import dev.tianmi.sussypatches.api.core.mixin.extension.ProspectingMapExtension;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.util.Mods;
import gregtech.common.terminal.app.prospector.ProspectingTexture;
import gregtech.common.terminal.app.prospector.widget.WidgetProspectingMap;

@Implemented(in = {
        "https://github.com/GregTechCEu/GregTech/pull/2726",
        "https://github.com/GregTechCEu/GregTech/pull/2891"
})
@Mixin(value = WidgetProspectingMap.class, remap = false)
public abstract class WidgetProspectingMapMixin implements ProspectingMapExtension {

    @Shadow
    private ProspectingTexture texture;

    @Shadow
    @Final
    private List<String> hoveredNames;

    @Unique
    private int sus$hoveredOreHeight = 0;

    @Unique
    @Override
    public int sus$getHoveredHeight() {
        return sus$hoveredOreHeight;
    }

    @Unique
    @Override
    public void sus$setHoveredHeight(int hoveredHeight) {
        this.sus$hoveredOreHeight = hoveredHeight;
    }

    @Inject(method = "drawInForeground", at = @At(value = "INVOKE", target = "Ljava/util/List;clear()V"))
    private void clearHoveredHeight(int mouseX, int mouseY, CallbackInfo ci) {
        this.sus$hoveredOreHeight = 0;
    }

    @Inject(method = "drawInForeground", at = @At(value = "INVOKE", target = "Ljava/util/HashMap;<init>()V"))
    private void initOreHeight(int mouseX, int mouseY, CallbackInfo ci,
                               @Share("oreHeight") LocalRef<HashMap<String, Integer>> oreHeightRef) {
        oreHeightRef.set(new HashMap<>());
    }

    // Can't think of a better way
    @Redirect(method = "drawInForeground",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/HashMap;values()Ljava/util/Collection;"))
    private Collection<String> exposeMapLocal(HashMap<Byte, String> textureMap,
                                              @Share("textureMap") LocalRef<HashMap<Byte, String>> textureMapRef) {
        textureMapRef.set(textureMap);
        return null;
    }

    // Can't think of a better way
    @Redirect(method = "drawInForeground",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/Collection;forEach(Ljava/util/function/Consumer;)V"))
    private void redirectIterate(Collection<String> _null, Consumer<String> lambda,
                                 @Share("textureMap") LocalRef<HashMap<Byte, String>> textureMapRef,
                                 @Share("oreHeight") LocalRef<HashMap<String, Integer>> oreHeightRef) {
        var oreHeight = oreHeightRef.get();

        textureMapRef.get().forEach((height, dict) -> {
            // Call original lambda
            lambda.accept(dict);

            // Sadly these have to be copied over
            String name = OreDictUnifier.get(dict).getDisplayName();
            if (ProspectingTexture.SELECTED_ALL.equals(texture.getSelected()) ||
                    texture.getSelected().equals(dict)) {
                oreHeight.put(name, oreHeight.getOrDefault(name, 0) + Byte.toUnsignedInt(height));
            }
        });
    }

    @Inject(method = "drawInForeground",
            at = @At(value = "INVOKE", target = "Ljava/util/HashMap;forEach(Ljava/util/function/BiConsumer;)V"))
    private void calculateAverageHeight(int mouseX, int mouseY, CallbackInfo ci,
                                        @Local(name = "oreInfo") HashMap<String, Integer> oreInfo,
                                        @Share("oreHeight") LocalRef<HashMap<String, Integer>> oreHeightRef) {
        var oreHeight = oreHeightRef.get();

        oreHeight.forEach((name, height) -> {
            this.sus$hoveredOreHeight += height;
            int count = oreInfo.getOrDefault(name, 0);
            int avgHeight = count != 0 ? height / count : 0;
            oreHeight.put(name, avgHeight);
        });
        int totalCount = oreInfo.values().stream().reduce(0, Integer::sum);
        if (totalCount != 0) {
            this.sus$hoveredOreHeight /= totalCount;
        }
    }

    // Can't think of a better way
    // Can't capture @Share local in the lambda
    @Redirect(method = "drawInForeground",
              at = @At(value = "INVOKE",
                       target = "Ljava/util/HashMap;forEach(Ljava/util/function/BiConsumer;)V"))
    private void addOreHeightInfo(HashMap<String, Integer> oreInfo, BiConsumer<String, Integer> lambda,
                                  @Local(name = "tooltips") List<String> tooltips,
                                  @Share("oreHeight") LocalRef<HashMap<String, Integer>> oreHeightRef) {
        var oreHeight = oreHeightRef.get();
        oreInfo.forEach((name, count) -> {
            int height = oreHeight.getOrDefault(name, 0);
            tooltips.add(name + " --- " + TextFormatting.GOLD + count + TextFormatting.RESET + " @Y = " +
                    TextFormatting.GOLD + height);
            this.hoveredNames.add(name);
        });
    }

    @WrapOperation(method = "mouseClicked",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/client/multiplayer/WorldClient;getHeight(II)I",
                            remap = true))
    private int useAverageY(WorldClient worldClient, int x, int z, Operation<Integer> method) {
        return this.sus$hoveredOreHeight != 0 ? this.sus$hoveredOreHeight : method.call(worldClient, x, z);
    }

    @Optional.Method(modid = Mods.Names.VOXEL_MAP)
    @Redirect(method = "addVoxelMapWaypoint",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/multiplayer/WorldClient;getHeight(II)I",
                       remap = true))
    private static int redirectVoxelMapHeight(WorldClient _ignored, int _x, int _z, @Local(name = "b") BlockPos b) {
        return b.getY();
    }

    @Optional.Method(modid = Mods.Names.XAEROS_MINIMAP)
    @Redirect(method = "addXaeroMapWaypoint",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/client/multiplayer/WorldClient;getHeight(II)I",
                       remap = true))
    private static int redirectXaeroMapHeight(WorldClient _ignored, int _x, int _z, @Local(name = "b") BlockPos b) {
        return b.getY();
    }
}
