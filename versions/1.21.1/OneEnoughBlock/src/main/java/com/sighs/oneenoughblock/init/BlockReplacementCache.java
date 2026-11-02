package com.sighs.oneenoughblock.init;

import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.sighs.oneenoughblock.Oneenoughblock;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.tags.TagKey;

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
        var id = BuiltInRegistries.BLOCK.getKey(source);
        if (id == null) return Optional.empty();
        String target = matchBlock(id.toString());
        if (target != null) {
            Block b = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(target));
            return Optional.ofNullable(b);
        }
        return Optional.empty();
    }

    public static Optional<Block> resolveTargetByTags(Block source) {
        for (var entry : TagMapCache.entrySet()) {
            String tagId = entry.getKey();
            String targetId = entry.getValue();
            var tagKey = TagKey.create(Registries.BLOCK, ResourceLocation.parse(tagId));
            if (source.builtInRegistryHolder().is(tagKey)) {
                Block target = BuiltInRegistries.BLOCK.get(ResourceLocation.parse(targetId));
                if (target != null) {
                    return Optional.of(target);
                }
            }
        }
        return Optional.empty();
    }

    public static Optional<Block> resolveTarget(Block source, HolderLookup.RegistryLookup<Block> lookup) {
        return resolveTarget(source).or(() -> resolveTargetByTags(source));
    }

    public static Optional<Block> resolveTargetByTags(Block source, HolderLookup.RegistryLookup<Block> lookup) {
        return resolveTargetByTags(source);
    }
    public static void putBlockMapping(String sourceId, String targetId) {
        if (sourceId != null && targetId != null) {
            BlockMapCache.put(sourceId, targetId);
            ReplacementUtils.onReload();
        }
    }

    public static void putTagMapping(String sourceTag, String targetId) {
        if (sourceTag != null && targetId != null) {
            TagMapCache.put(sourceTag, targetId);
            ReplacementUtils.onReload();
        }
    }

    public static boolean removeBlockReplacement(String blockId) {
        if (blockId != null && BlockMapCache.containsKey(blockId)) {
            String removed = BlockMapCache.remove(blockId);
            BlockRulesCache.remove(blockId);
            ReplacementUtils.onReload();
            Oneenoughblock.LOGGER.debug("Removed fluid replacement from runtime cache: {} -> {}", blockId, removed);
            return true;
        }
        return false;
    }

    public static boolean removeTagReplacement(String tagId) {
        if (tagId != null && TagMapCache.containsKey(tagId)) {
            String removed = TagMapCache.remove(tagId);
            TagRulesCache.remove(tagId);
            ReplacementUtils.onReload();
            Oneenoughblock.LOGGER.debug("Removed tag replacement from runtime cache: {} -> {}", tagId, removed);
            return true;
        }
        return false;
    }

    public static void removeReplacements(Collection<String> blockIds, Collection<String> tagIds) {
        boolean changed = false;

        if (blockIds != null) {
            for (String blockId : blockIds) {
                if (removeBlockReplacement(blockId)) {
                    changed = true;
                }
            }
        }

        if (tagIds != null) {
            for (String tagId : tagIds) {
                if (removeTagReplacement(tagId)) {
                    changed = true;
                }
            }
        }

        if (changed) {
            Oneenoughblock.LOGGER.info("Removed {} block replacements and {} tag replacements from runtime cache",
                    blockIds != null ? blockIds.size() : 0, tagIds != null ? tagIds.size() : 0);
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
        ReplacementUtils.onReload();
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
        ReplacementUtils.onReload();
    }

    public static Optional<Replacements.Rules> getGlobalDefaultRules() {
        try {
            var cfg = OEBConfig.get();
            if (cfg != null) {
                return Optional.of(cfg.defaultRules().toRules());
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
            ReloadOverrideBlockMap = Map.of();
            ReplacementUtils.onReload();
            return;
        }
        ReloadOverrideBlockMap = new HashMap<>(currentBlockMap);
        ReplacementUtils.onReload();
    }

    public static void endReloadOverride() {
        if (ReloadOverrideBlockMap != null) {
            Oneenoughblock.LOGGER.info("Disabled reload-override mapping: {} entries", ReloadOverrideBlockMap.size());
        }
        ReloadOverrideBlockMap = null;
        ReplacementUtils.onReload();
    }

    public static boolean hasReloadOverride() {
        return ReloadOverrideBlockMap != null;
    }
}
