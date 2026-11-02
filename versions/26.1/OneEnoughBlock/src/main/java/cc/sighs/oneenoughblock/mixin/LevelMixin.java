package cc.sighs.oneenoughblock.mixin;

import cc.sighs.oneenoughblock.init.ReplacementUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Level.class)
public abstract class LevelMixin {
    @ModifyVariable(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"), argsOnly = true)
    private BlockState oeb$replaceState(BlockState state) {
        BlockState target = ReplacementUtils.getReplacement(state);
        return target != null ? target : state;
    }
}
