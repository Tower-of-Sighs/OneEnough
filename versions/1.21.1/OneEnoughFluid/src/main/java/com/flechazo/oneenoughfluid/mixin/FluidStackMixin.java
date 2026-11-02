package com.flechazo.oneenoughfluid.mixin;
import com.flechazo.oneenoughfluid.init.FluidReplacementCache;
import com.mafuyu404.oneenoughitem.client.ClientContext;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(FluidStack.class)
public abstract class FluidStackMixin {
    @Shadow
    public abstract Fluid getFluid();

    @Inject(method = "is(Lnet/minecraft/world/level/material/Fluid;)Z", at = @At("RETURN"), cancellable = true)
    private void oef$extendEquality(Fluid expected, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && !ClientContext.isBuilding() && !ReplacementControl.shouldSkipReplacement()
                && FluidReplacementCache.matches(getFluid(), expected)) cir.setReturnValue(true);
    }
    @ModifyVariable(method = "<init>(Lnet/minecraft/world/level/material/Fluid;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",
            at = @At("HEAD"), argsOnly = true)
    private static Fluid oef$replaceConstructorFluid(Fluid fluid) {
        if (fluid == null || ClientContext.isBuilding() || ReplacementControl.shouldSkipReplacement()) return fluid;
        Fluid target = FluidReplacementCache.replacementOf(fluid);
        return target != null && target != Fluids.EMPTY ? target : fluid;
    }
}
