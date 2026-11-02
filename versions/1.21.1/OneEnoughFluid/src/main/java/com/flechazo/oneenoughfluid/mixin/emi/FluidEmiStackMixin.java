package com.flechazo.oneenoughfluid.mixin.emi;

import com.flechazo.oneenoughfluid.init.FluidReplacementCache;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;
import net.minecraft.world.level.material.Fluid;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.emi.emi.api.stack.FluidEmiStack", remap = false)
public abstract class FluidEmiStackMixin {
    @Shadow @Final @Mutable
    private Fluid fluid;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void oef$unifyIndexKey(CallbackInfo ci) {
        if (ReplacementControl.shouldSkipReplacement()) return;
        Fluid target = FluidReplacementCache.replacementOf(fluid);
        if (target != null) fluid = target;
    }
}
