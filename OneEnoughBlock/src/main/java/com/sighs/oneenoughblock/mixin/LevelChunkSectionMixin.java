package com.sighs.oneenoughblock.mixin;

import com.sighs.oneenoughblock.init.ReplacementUtils;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(LevelChunkSection.class)
public abstract class LevelChunkSectionMixin {
    @ModifyVariable(method = "setBlockState(IIILnet/minecraft/world/level/block/state/BlockState;Z)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"), argsOnly = true)
    private BlockState oeb$replaceState(BlockState state) {
        BlockState target = ReplacementUtils.getReplacement(state);
        return target != null ? target : state;
    }
}
