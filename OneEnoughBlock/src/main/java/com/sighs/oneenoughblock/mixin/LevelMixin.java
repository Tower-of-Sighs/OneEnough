package com.sighs.oneenoughblock.mixin;

import com.sighs.oneenoughblock.init.BlockReplacementCache;
import com.sighs.oneenoughblock.init.Utils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {

    @Shadow
    public abstract boolean setBlock(BlockPos pos, BlockState state, int flags, int recursion);

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onSetBlock(
            BlockPos pos,
            BlockState state,
            int flags,
            int recursion,
            CallbackInfoReturnable<Boolean> cir
    ) {
        Level level = (Level) (Object) this;

        var lookup = level.registryAccess()
                .lookupOrThrow(Registries.BLOCK);

        Block source = state.getBlock();

        BlockReplacementCache.resolveTarget(source, lookup)
                .filter(target -> !state.is(target))
                .ifPresent(target -> {
                    cir.setReturnValue(
                            setBlock(pos, Utils.saveState(state, target.defaultBlockState()), flags, recursion)
                    );
                });
    }
}
