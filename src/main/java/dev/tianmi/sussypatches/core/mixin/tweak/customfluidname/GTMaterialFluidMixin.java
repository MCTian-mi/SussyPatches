package dev.tianmi.sussypatches.core.mixin.tweak.customfluidname;

import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.GTFluid.GTMaterialFluid;
import gregtech.api.unification.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GTMaterialFluid.class, remap = false)
public abstract class GTMaterialFluidMixin {

    @Unique
    private String sus$customFluidLocalizationKey = null;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(@NotNull String fluidName, ResourceLocation still, ResourceLocation flowing,
                        @NotNull FluidState state, @Nullable String translationKey, @NotNull Material material,
                        CallbackInfo ci) {
        if (translationKey != null) {
            this.sus$customFluidLocalizationKey = "fluid." + material.getUnlocalizedName() + '.' + translationKey.substring(translationKey.lastIndexOf('.') + 1);
        }
    }

    @SuppressWarnings("deprecation")
    @Inject(method = "toTextComponentTranslation",
            at = @At(value = "NEW",
                     target = "net/minecraft/util/text/TextComponentTranslation"),
            slice = @Slice(from = @At(value = "FIELD",
                                      target = "Lgregtech/api/fluids/GTFluid$GTMaterialFluid;translationKey:Ljava/lang/String;",
                                      opcode = Opcodes.GETFIELD),
                           to = @At(value = "TAIL")),
            cancellable = true)
    private void checkCustomKey(CallbackInfoReturnable<TextComponentTranslation> cir) {
        if (sus$customFluidLocalizationKey != null && net.minecraft.util.text.translation.I18n.canTranslate(sus$customFluidLocalizationKey)) {
            cir.setReturnValue(new TextComponentTranslation(sus$customFluidLocalizationKey));
        }
    }

    @SideOnly(Side.CLIENT)
    @Inject(method = "getLocalizedName",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/client/resources/I18n;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"),
            slice = @Slice(from = @At(value = "FIELD",
                                      target = "Lgregtech/api/fluids/GTFluid$GTMaterialFluid;translationKey:Ljava/lang/String;",
                                      opcode = Opcodes.GETFIELD),
                           to = @At(value = "TAIL")),
            cancellable = true)
    private void checkCustomKeyToo(FluidStack stack, CallbackInfoReturnable<String> cir) {
        if (sus$customFluidLocalizationKey != null && I18n.hasKey(sus$customFluidLocalizationKey)) {
            cir.setReturnValue(I18n.format(sus$customFluidLocalizationKey));
        }
    }
}
