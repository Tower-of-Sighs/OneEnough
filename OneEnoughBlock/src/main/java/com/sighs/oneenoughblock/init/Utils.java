package com.sighs.oneenoughblock.init;

import com.sighs.oneenoughblock.Oneenoughblock;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;

import java.util.*;

public class Utils {

    @SuppressWarnings("unchecked")
    public static <T extends Comparable<T>> BlockState saveState(BlockState from, BlockState to) {
        if (OEBConfig.get().extendedBlockProperty()) return to;
        for (Map.Entry<Property<?>, Comparable<?>> entry :  from.getValues().entrySet()) {
            to = to.trySetValue((Property<T>) entry.getKey(), (T) entry.getValue());
        }
        return to;
    }

    public static String getBlockRegistryName(Block block) {
        if (block == null) return null;
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return id.toString();
    }

    public static Block getBlockById(String id) {
        if (id == null || id.isEmpty()) return null;
        try {
            return BuiltInRegistries.BLOCK.get(ResourceLocation.parse(id));
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isTagExists(ResourceLocation tagId, HolderLookup.RegistryLookup<Block> registryLookup) {
        TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagId);
        return registryLookup.get(tagKey).isPresent();
    }

    public static Collection<Block> getBlocksOfTag(ResourceLocation tagId, HolderLookup.RegistryLookup<Block> registryLookup) {
        TagKey<Block> tagKey = TagKey.create(Registries.BLOCK, tagId);
        Collection<Block> result = new HashSet<>();

        Oneenoughblock.LOGGER.debug("Attempting to resolve tag: {}", tagId);

        var tagOptional = registryLookup.get(tagKey);
        if (tagOptional.isPresent()) {
            var holderSet = tagOptional.get();
            for (var holder : holderSet) {
                result.add(holder.value());
            }
            Oneenoughblock.LOGGER.debug("Tag {} resolved to {} blocks: {}",
                    tagId, result.size(),
                    result.stream().map(Utils::getBlockRegistryName).toList());
        } else {
            Oneenoughblock.LOGGER.warn("Tag {} not found in registry lookup", tagId);
        }

        return result;
    }

    public static List<Block> resolveBlockList(List<String> identifiers, HolderLookup.RegistryLookup<Block> registryLookup) {
        List<Block> result = new ArrayList<>();

        for (String id : identifiers) {
            if (id == null || id.isEmpty()) continue;

            if (id.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(id.substring(1));
                if (tagId == null) {
                    Oneenoughblock.LOGGER.warn("Invalid tag ID format: {}", id);
                    continue;
                }

                Collection<Block> tagBlocks = getBlocksOfTag(tagId, registryLookup);
                if (tagBlocks.isEmpty()) {
                    Oneenoughblock.LOGGER.warn("Tag {} is empty or not found", tagId);
                } else {
                    result.addAll(tagBlocks);
                }
            } else {
                Block block = getBlockById(id);
                if (block != null) {
                    result.add(block);
                } else {
                    Oneenoughblock.LOGGER.warn("Block ID not found: {}", id);
                }
            }
        }

        return result;
    }
}
