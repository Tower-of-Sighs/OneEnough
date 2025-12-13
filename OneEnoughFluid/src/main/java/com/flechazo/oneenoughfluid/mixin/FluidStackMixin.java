package com.flechazo.oneenoughfluid.mixin;

import com.flechazo.oneenoughfluid.init.FluidReplacementCache;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidStack.class)
public abstract class FluidStackMixin {
    @Shadow
    @Final
    @Mutable
    private Fluid fluid;

    @Shadow
    public abstract Fluid getFluid();

    @Inject(method = "<init>(Lnet/minecraft/world/level/material/Fluid;I)V", at = @At("RETURN"))
    private void oef$replaceOnInit(Fluid fluid, int amount, CallbackInfo ci) {
        if (fluid == null || fluid == Fluids.EMPTY) return;
        if (ReplacementControl.shouldSkipReplacement()) return;
        try {
            var id = BuiltInRegistries.FLUID.getKey(fluid);
            String targetId = FluidReplacementCache.matchFluid(id.toString());
            if (targetId == null) return;
            Fluid target = BuiltInRegistries.FLUID.get(ResourceLocation.parse(targetId));
            if (target != Fluids.EMPTY) {
                this.fluid = target;
            }
        } catch (Exception ignored) {
        }
    }

    @Inject(method = "isFluidEqual", at = @At("HEAD"), cancellable = true)
    private void oef$extendEquality(FluidStack other, CallbackInfoReturnable<Boolean> cir) {
        Fluid self = getFluid();
        if (self == null) return;
        if (self == other.getFluid()) return;
        var id = BuiltInRegistries.FLUID.getKey(self);
        boolean matched = false;
        for (String matchId : FluidReplacementCache.trackSourceIdOf(id.toString())) {
            Fluid match = BuiltInRegistries.FLUID.get(ResourceLocation.parse(matchId));
            if (match == other.getFluid()) {
                matched = true;
                break;
            }
        }
        if (matched) cir.setReturnValue(true);
    }
}
