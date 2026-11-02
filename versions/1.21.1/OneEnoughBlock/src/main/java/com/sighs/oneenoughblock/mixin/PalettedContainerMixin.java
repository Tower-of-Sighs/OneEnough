package com.sighs.oneenoughblock.mixin;

import com.sighs.oneenoughblock.api.IPalettedContainer;
import com.sighs.oneenoughblock.init.ReplacementUtils;
import net.minecraft.util.BitStorage;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.Palette;
import net.minecraft.world.level.chunk.PalettedContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashMap;
import java.util.Map;

@Mixin(PalettedContainer.class)
public class PalettedContainerMixin implements IPalettedContainer {
    @Shadow
    private volatile PalettedContainer.Data<?> data;

    @Override
    @SuppressWarnings("unchecked")
    public void handleReplace() {
        BitStorage storage = data.storage();
        Palette<BlockState> palette = (Palette<BlockState>) data.palette();
        Map<Integer, BlockState> replacements = new HashMap<>();
        int paletteSize = palette.getSize();
        for (int i = 0; i < paletteSize; i++) {
            BlockState replacement = ReplacementUtils.getReplacement(palette.valueFor(i));
            if (replacement != null) replacements.put(i, replacement);
        }
        if (replacements.isEmpty()) return;

        // Adding a state can resize both palette and storage, including a single-value palette.
        // Keep the original ids and let the container perform the resize through its public API.
        int[] originalIds = new int[storage.getSize()];
        for (int i = 0; i < originalIds.length; i++) originalIds[i] = storage.get(i);
        PalettedContainer<BlockState> container = (PalettedContainer<BlockState>) (Object) this;
        for (int i = 0; i < originalIds.length; i++) {
            BlockState replacement = replacements.get(originalIds[i]);
            if (replacement != null) container.set(i & 15, i >> 8, (i >> 4) & 15, replacement);
        }
    }
}
