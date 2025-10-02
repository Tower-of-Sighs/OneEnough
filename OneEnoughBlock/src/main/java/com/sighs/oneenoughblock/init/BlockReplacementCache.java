package com.sighs.oneenoughblock.init;

import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlockReplacementCache {
    private static final ConcurrentHashMap<String, String> BlockMapCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> TagMapCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Replacements.Rules> BlockRulesCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Replacements.Rules> TagRulesCache = new ConcurrentHashMap<>();
    private static volatile Map<String, String> ReloadOverrideBlockMap = null;

    public static String matchBlock(String id) {
        return Objects.requireNonNullElse(ReloadOverrideBlockMap, BlockMapCache).getOrDefault(id, null);
    }

    public static String matchTag(String tagId) {
        return TagMapCache.getOrDefault(tagId, null);
    }

    public static String matchTag(ResourceLocation tagId) {
        return tagId != null ? matchTag(tagId.toString()) : null;
    }

    public static boolean isTagReplaced(String tagId) {
        return tagId != null && TagMapCache.containsKey(tagId);
    }

    public static boolean isTagReplaced(ResourceLocation tagId) {
        return tagId != null && isTagReplaced(tagId.toString());
    }

    public static boolean isSourceBlockId(String id) {
        return id != null && Objects.requireNonNullElse(ReloadOverrideBlockMap, BlockMapCache).containsKey(id);
    }

    public static Optional<Block> resolveTarget(Block source) {
        var id = ForgeRegistries.BLOCKS.getKey(source);
        if (id == null) return Optional.empty();
        String target = matchBlock(id.toString());
        if (target != null) {
            Block b = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(target));
            return Optional.ofNullable(b);
        }
        return Optional.empty();
    }

    public static Optional<Block> resolveTargetByTags(Block source) {
        var tags = ForgeRegistries.BLOCKS.tags();
        for (var entry : TagMapCache.entrySet()) {
            String tagId = entry.getKey();
            String targetId = entry.getValue();
            var tagKey = tags.createTagKey(new ResourceLocation(tagId));
            if (source.builtInRegistryHolder().is(tagKey)) {
                Block target = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(targetId));
                if (target != null) {
                    return Optional.of(target);
                }
            }
        }
        return Optional.empty();
    }

    public static void putBlockMapping(String sourceId, String targetId) {
        if (sourceId != null && targetId != null) BlockMapCache.put(sourceId, targetId);
    }

    public static void putTagMapping(String sourceTag, String targetId) {
        if (sourceTag != null && targetId != null) TagMapCache.put(sourceTag, targetId);
    }

    public static void removeReplacements(Collection<String> blockIds, Collection<String> tagIds) {
        boolean changed = false;
        if (blockIds != null) {
            for (String id : blockIds) {
                if (id != null && BlockMapCache.remove(id) != null) changed = true;
            }
        }
        if (tagIds != null) {
            for (String tag : tagIds) {
                if (tag != null && TagMapCache.remove(tag) != null) changed = true;
            }
        }
    }

    public static Collection<String> trackSourceIdOf(String id) {
        Collection<String> result = new HashSet<>();
        BlockMapCache.forEach((match, target) -> {
            if (target.equals(id)) result.add(match);
        });
        return result;
    }

    public static void clearCache() {
        BlockMapCache.clear();
        TagMapCache.clear();
        BlockRulesCache.clear();
        TagRulesCache.clear();
    }

    public static void putReplacement(Replacements replacement) {
        String result = replacement.result();
        replacement.rules().ifPresentOrElse(rules -> {
            for (String m : replacement.match()) {
                if (m == null || m.isEmpty()) continue;
                if (m.startsWith("#")) {
                    String tagId = m.substring(1);
                    TagMapCache.put(tagId, result);
                    TagRulesCache.put(tagId, rules);
                } else {
                    BlockMapCache.put(m, result);
                    BlockRulesCache.put(m, rules);
                }
            }
        }, () -> {
            for (String m : replacement.match()) {
                if (m == null || m.isEmpty()) continue;
                if (m.startsWith("#")) {
                    TagMapCache.put(m.substring(1), result);
                } else {
                    BlockMapCache.put(m, result);
                }
            }
        });
    }

    public static Optional<Replacements.Rules> getGlobalDefaultRules() {
        try {
            var cfg = OEIConfig.getDefaultRules("oeb");
            if (cfg != null) {
                return Optional.of(cfg.toRules());
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    public static Optional<Replacements.Rules> getBlockRules(String blockId) {
        return Optional.ofNullable(BlockRulesCache.get(blockId));
    }

    public static Optional<Replacements.Rules> getTagRules(String tagId) {
        return Optional.ofNullable(TagRulesCache.get(tagId));
    }

    public static boolean shouldReplaceInDataDir(String blockId, String directory) {
        return getBlockRules(blockId)
                .or(BlockReplacementCache::getGlobalDefaultRules)
                .flatMap(Replacements.Rules::data)
                .map(dataRules -> dataRules.get(directory))
                .map(mode -> mode == Replacements.ProcessingMode.REPLACE)
                .orElse(false);
    }

    public static boolean shouldReplaceInTagType(String blockId, String tagType) {
        return getBlockRules(blockId)
                .or(BlockReplacementCache::getGlobalDefaultRules)
                .flatMap(Replacements.Rules::tag)
                .map(tagRules -> tagRules.get(tagType))
                .map(mode -> mode == Replacements.ProcessingMode.REPLACE)
                .orElse(false);
    }

    public static void beginReloadOverride(Map<String, String> currentBlockMap) {
        if (currentBlockMap == null || currentBlockMap.isEmpty()) {
            ReloadOverrideBlockMap = null;
            return;
        }
        ReloadOverrideBlockMap = new HashMap<>(currentBlockMap);
    }

    public static void endReloadOverride() {
        ReloadOverrideBlockMap = null;
    }

    public static boolean hasReloadOverride() {
        return ReloadOverrideBlockMap != null;
    }
}