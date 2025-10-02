package com.flechazo.oneenoughfluid.mixin;

import com.flechazo.oneenoughfluid.init.FluidReplacementCache;
import com.mafuyu404.oneenoughitem.init.ReplacementControl;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
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
    @Shadow
    @Mutable
    private Holder.Reference<Fluid> fluidDelegate;

    @Shadow
    public abstract Fluid getFluid();

    @Inject(method = "<init>(Lnet/minecraft/world/level/material/Fluid;I)V", at = @At("RETURN"))
    private void oef$replaceOnInit(Fluid fluid, int amount, CallbackInfo ci) {
        if (fluid == null || fluid == Fluids.EMPTY) return;
        if (ReplacementControl.shouldSkipReplacement()) return;
        try {
            var id = ForgeRegistries.FLUIDS.getKey(fluid);
            if (id == null) return;
            String targetId = FluidReplacementCache.matchFluid(id.toString());
            if (targetId == null) return;
            Fluid target = ForgeRegistries.FLUIDS
                    .getValue(new ResourceLocation(targetId));
            if (target != null && target != Fluids.EMPTY) {
                this.fluidDelegate = ForgeRegistries.FLUIDS.getDelegateOrThrow(target);
            }
        } catch (Exception ignored) {
        }
    }

    @Inject(method = "isFluidEqual(Lnet/minecraftforge/fluids/FluidStack;)Z", at = @At("HEAD"), cancellable = true, remap = false)
    private void oef$extendEquality(FluidStack other, CallbackInfoReturnable<Boolean> cir) {
        Fluid self = getFluid();
        if (self == null) return;
        if (self == other.getFluid()) return;
        var id = ForgeRegistries.FLUIDS.getKey(self);
        if (id == null) return;
        boolean matched = false;
        for (String matchId : FluidReplacementCache.trackSourceIdOf(id.toString())) {
            Fluid match = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(matchId));
            if (match != null && match == other.getFluid()) {
                matched = true;
                break;
            }
        }
        if (matched) cir.setReturnValue(true);
    }

    private static Item findBucketForFluid(Fluid f) {
        for (Item item : ForgeRegistries.ITEMS) {
            if (item instanceof BucketItem bucket) {
                if (bucket.getFluid() == f) return item;
            }
        }
        return null;
    }
}