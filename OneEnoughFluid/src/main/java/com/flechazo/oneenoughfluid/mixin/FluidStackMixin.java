package com.flechazo.oneenoughfluid.mixin;

import com.flechazo.oneenoughfluid.init.FluidReplacementCache;
import com.mafuyu404.oneenoughitem.client.ClientContext;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidStack.class)
public abstract class FluidStackMixin {
    @Shadow(remap = false) @Mutable
    private Holder.Reference<Fluid> fluidDelegate;

    @Shadow(remap = false)
    public abstract Fluid getFluid();

    @Inject(method = "<init>(Lnet/minecraft/world/level/material/Fluid;I)V", at = @At("RETURN"), remap = false)
    private void oef$replaceOnInit(Fluid fluid, int amount, CallbackInfo ci) {
        if (fluid == null || fluid == Fluids.EMPTY || ClientContext.isBuilding()
                || ReplacementControl.shouldSkipReplacement()) return;
        Fluid target = FluidReplacementCache.replacementOf(fluid);
        if (target != null && target != Fluids.EMPTY) {
            fluidDelegate = ForgeRegistries.FLUIDS.getDelegateOrThrow(target);
        }
    }

    @Inject(method = "isFluidEqual(Lnet/minecraftforge/fluids/FluidStack;)Z", at = @At("RETURN"), cancellable = true, remap = false)
    private void oef$extendEquality(FluidStack other, CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValueZ() || other == null || ClientContext.isBuilding()
                || ReplacementControl.shouldSkipReplacement()) return;
        // Forge fluid equality includes NBT. A replacement must preserve that requirement.
        if (FluidReplacementCache.matches(getFluid(), other.getFluid())
                && FluidStack.areFluidStackTagsEqual((FluidStack) (Object) this, other)) {
            cir.setReturnValue(true);
        }
    }
}
