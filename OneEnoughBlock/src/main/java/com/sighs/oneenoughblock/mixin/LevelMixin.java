package com.sighs.oneenoughblock.mixin;

import com.sighs.oneenoughblock.init.BlockReplacementCache;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelMixin {
    @Shadow
    public abstract boolean setBlock(BlockPos pos, BlockState state, int p_46607_, int p_46608_);

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At("HEAD"), cancellable = true)
    private void onSetBlock(BlockPos pos, BlockState state, int p_46607_, int p_46608_, CallbackInfoReturnable<Boolean> cir) {
        BlockReplacementCache.resolveTarget(state.getBlock()).ifPresent(target -> {
            if (!state.is(target)) {
                cir.setReturnValue(setBlock(pos, target.defaultBlockState(), p_46607_, p_46608_));
            }
        });
        // 标签替换
        BlockReplacementCache.resolveTargetByTags(state.getBlock()).ifPresent(target -> {
            if (!state.is(target)) {
                cir.setReturnValue(setBlock(pos, target.defaultBlockState(), p_46607_, p_46608_));
            }
        });
    }
}
