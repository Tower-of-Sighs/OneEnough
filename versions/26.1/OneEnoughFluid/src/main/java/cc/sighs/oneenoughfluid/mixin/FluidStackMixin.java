package cc.sighs.oneenoughfluid.mixin;
import cc.sighs.oneenoughfluid.init.FluidReplacementCache;
import cc.sighs.oneenoughitem.client.ClientContext;
import cc.sighs.oneenoughitem.util.ReplacementControl;
import net.minecraft.core.Holder;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidStack.class)
public abstract class FluidStackMixin {
    @Inject(method = "isSameFluidSameComponents(Lnet/neoforged/neoforge/fluids/FluidStack;Lnet/neoforged/neoforge/fluids/FluidStack;)Z",
            at = @At("RETURN"), cancellable = true)
    private static void oef$extendEquality(FluidStack actual, FluidStack expected, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ() && !ClientContext.isBuilding() && !ReplacementControl.shouldSkipReplacement()
                && FluidReplacementCache.matches(actual.getFluid(), expected.getFluid())
                && java.util.Objects.equals(actual.getComponents(), expected.getComponents())) cir.setReturnValue(true);
    }
    @ModifyVariable(method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V",
            at = @At("HEAD"), argsOnly = true)
    private static Holder<Fluid> oef$replaceConstructorFluid(Holder<Fluid> fluid) {
        if (fluid == null || ClientContext.isBuilding() || ReplacementControl.shouldSkipReplacement()) return fluid;
        Fluid target = FluidReplacementCache.replacementOf(fluid.value());
        return target != null && target != Fluids.EMPTY ? target.builtInRegistryHolder() : fluid;
    }
}
