package com.sighs.oneenoughblock.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Utils {
    public static String getBlockRegistryName(Block block) {
        if (block == null) return null;
        ResourceLocation id = ForgeRegistries.BLOCKS.getKey(block);
        return id != null ? id.toString() : null;
    }

    public static Block getBlockById(String id) {
        if (id == null || id.isEmpty()) return null;
        try {
            return ForgeRegistries.BLOCKS.getValue(new ResourceLocation(id));
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isTagExists(ResourceLocation tagId) {
        ITagManager<Block> mgr = ForgeRegistries.BLOCKS.tags();
        TagKey<Block> key = ForgeRegistries.BLOCKS.tags().createTagKey(tagId);
        return mgr != null && mgr.isKnownTagName(key);
    }

    public static Collection<Block> getBlocksOfTag(ResourceLocation tagId) {
        ITagManager<Block> mgr = ForgeRegistries.BLOCKS.tags();
        if (mgr == null) return Collections.emptyList();
        TagKey<Block> key = ForgeRegistries.BLOCKS.tags().createTagKey(tagId);

        ITag<Block> tag = mgr.getTag(key);
        return tag.stream().toList();
    }

    public static List<Block> resolveBlockList(List<String> identifiers) {
        List<Block> result = new ArrayList<>();
        for (String id : identifiers) {
            if (id == null || id.isEmpty()) continue;
            if (id.startsWith("#")) {
                ResourceLocation tag = new ResourceLocation(id.substring(1));
                result.addAll(getBlocksOfTag(tag));
            } else {
                Block f = getBlockById(id);
                if (f != null) result.add(f);
            }
        }
        return result;
    }
}
