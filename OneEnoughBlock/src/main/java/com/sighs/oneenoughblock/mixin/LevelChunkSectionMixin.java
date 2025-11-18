package com.sighs.oneenoughblock.mixin;

import com.sighs.oneenoughblock.init.BlockReplacementCache;
import com.sighs.oneenoughblock.init.Utils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin {
    @Shadow
    @Final
    private PalettedContainer<BlockState> states;

    @Shadow
    public abstract BlockState setBlockState(int x, int y, int z, BlockState state, boolean useLocks);

    @Inject(method = "setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;", at = @At("HEAD"), cancellable = true)
    public void setBlockState(int x, int y, int z, BlockState state, boolean useLocks, CallbackInfoReturnable<BlockState> cir) {
        BlockReplacementCache.resolveTarget(state.getBlock()).ifPresent(target -> {
            if (!state.is(target)) {
                cir.setReturnValue(setBlockState(x, y, z, Utils.saveState(state, target.defaultBlockState()), useLocks));
            }
        });
        // 标签替换
        BlockReplacementCache.resolveTargetByTags(state.getBlock()).ifPresent(target -> {
            if (!state.is(target)) {
                cir.setReturnValue(setBlockState(x, y, z, Utils.saveState(state, target.defaultBlockState()), useLocks));
            }
        });
    }

//    @Inject(method = "getBlockState", at = @At("HEAD"))
//    public void getBlockState(int x, int y, int z, CallbackInfoReturnable<BlockState> cir) {
//        BlockState current = this.states.get(x, y, z);
//        BlockReplacementCache.resolveTarget(current.getBlock()).ifPresent(target -> {
//            setBlockState(x, y, z, target.defaultBlockState(), false);
//        });
//        // 标签替换
//        BlockReplacementCache.resolveTargetByTags(current.getBlock()).ifPresent(target -> {
//            setBlockState(x, y, z, target.defaultBlockState(), false);
//        });
//    }
}
