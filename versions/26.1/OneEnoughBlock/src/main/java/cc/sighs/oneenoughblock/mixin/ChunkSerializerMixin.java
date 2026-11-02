package cc.sighs.oneenoughblock.mixin;

import cc.sighs.oneenoughblock.api.IPalettedContainer;
import cc.sighs.oneenoughblock.init.OEBConfig;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.storage.SerializableChunkData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(value = SerializableChunkData.class)
public class ChunkSerializerMixin {
    @ModifyArg(
            method = "parse",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/chunk/LevelChunkSection;<init>(Lnet/minecraft/world/level/chunk/PalettedContainer;Lnet/minecraft/world/level/chunk/PalettedContainerRO;)V",
                    ordinal = 0
            )
    )
    private static PalettedContainer<BlockState>
    applyReplacementOnRead(PalettedContainer<BlockState> container) {
        if (OEBConfig.get().replaceExistedBlock()) {
            ((IPalettedContainer) container).handleReplace();
        }
        return container;
    }
}
