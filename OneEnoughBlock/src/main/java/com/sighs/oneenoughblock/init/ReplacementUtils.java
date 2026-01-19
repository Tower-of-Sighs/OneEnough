package com.sighs.oneenoughblock.init;

import com.sighs.oneenoughblock.Config;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.Map;
import java.util.Optional;

public class ReplacementUtils {
    public static Object2ObjectOpenHashMap<Block, Block> replacementsCache = new Object2ObjectOpenHashMap<>();
    public static Object2ObjectOpenHashMap<Block, BlockState> commonCache = new Object2ObjectOpenHashMap<>();
    public static Object2ObjectOpenHashMap<BlockState, BlockState> propertyCache = new Object2ObjectOpenHashMap<>();
    public static ObjectArrayList<Block> notReplacedBlocks = new ObjectArrayList<>();

    public static void onReload() {
        replacementsCache.clear();
        commonCache.clear();
        propertyCache.clear();
        notReplacedBlocks.clear();
    }

    public static BlockState getReplacement(BlockState state) {
        Block block = state.getBlock();
        if (notReplacedBlocks.contains(block)) {
            return null;
        }
        //是否开启原型继承
        if (Config.extendedBlockProperty.get()) {
            if (propertyCache.containsKey(state)) {
                return propertyCache.get(state);
            }
            var result = getReplacementBlockWithProperty(state);
            if (result != null) {
                propertyCache.put(state, result);
                return result;
            }

            return null;
        } else {
            if (commonCache.containsKey(block)) {
                return commonCache.get(block);
            }

            var result = getReplacementBlock(state);
            if (result != null) {
                commonCache.put(block, result);
                return result;
            }

            return null;
        }
    }

    public static Block getReplacementBlock(Block block) {
        if (replacementsCache.containsKey(block)) {
            return replacementsCache.get(block);
        }

        Optional<Block> target = BlockReplacementCache.resolveTarget(block)
                .or(() -> BlockReplacementCache.resolveTargetByTags(block))
                .filter(t -> t != block);

        if (target.isPresent()) {
            Block tb = target.get();
            replacementsCache.put(block, tb);
            return tb;
        } else {
            notReplacedBlocks.add(block);
            return null;
        }
    }

    public static BlockState getReplacementBlock(BlockState state) {
        var targetBlock = getReplacementBlock(state.getBlock());
        if (targetBlock == null) {
            return null;
        }
        return targetBlock.defaultBlockState();
    }

    public static BlockState getReplacementBlockWithProperty(BlockState state) {
        var targetBlock = getReplacementBlock(state.getBlock());
        if (targetBlock == null) {
            return null;
        }
        var targetState = targetBlock.defaultBlockState();
        return saveState(state, targetState);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> BlockState saveState(BlockState from, BlockState to) {
        for (Map.Entry<Property<?>, Comparable<?>> entry : from.getValues().entrySet()) {
            to = to.trySetValue((Property<T>) entry.getKey(), (T) entry.getValue());
        }
        return to;
    }
}
