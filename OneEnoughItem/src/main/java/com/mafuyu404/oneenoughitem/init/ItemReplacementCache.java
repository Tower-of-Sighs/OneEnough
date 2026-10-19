package com.mafuyu404.oneenoughitem.init;

import com.mafuyu404.oneenoughitem.Oneenoughitem;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.mafuyu404.oneenoughitem.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ItemReplacementCache {
    private static final ConcurrentHashMap<String, String> ItemMapCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> TagMapCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Replacements.Rules> ItemRulesCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Replacements.Rules> TagRulesCache = new ConcurrentHashMap<>();
    // 反向索引：替换结果 id -> 所有源 id，避免热路径上遍历 ItemMapCache
    private static final ConcurrentHashMap<String, Set<String>> ReverseItemCache = new ConcurrentHashMap<>();
    // 反向索引对应的 Item 集合（惰性解析，映射变化时清空）
    private static final ConcurrentHashMap<String, Collection<Item>> ReverseItemObjectCache = new ConcurrentHashMap<>();
    // Item 对象级反向索引（按引用比较），供 ItemStack.is(Item) 热路径使用，惰性构建
    private static volatile Map<Item, Set<Item>> ItemIdentityIndex = null;
    private static volatile int indexGeneration = 0;
    private static volatile Map<String, String> ReloadOverrideItemMap = null;

    private static void invalidateIndexes() {
        indexGeneration++;
        ItemIdentityIndex = null;
        ReverseItemObjectCache.clear();
    }

    private static Map<Item, Set<Item>> identityIndex() {
        Map<Item, Set<Item>> index = ItemIdentityIndex;
        if (index != null) return index;
        int gen = indexGeneration;
        Map<Item, Set<Item>> built = new IdentityHashMap<>();
        ReverseItemCache.forEach((resultId, sourceIds) -> {
            Item result = Utils.getItemById(resultId);
            if (result == null || result == Items.AIR) return;
            Set<Item> sources = Collections.newSetFromMap(new IdentityHashMap<>());
            for (String sourceId : sourceIds) {
                Item source = Utils.getItemById(sourceId);
                if (source != null && source != Items.AIR) sources.add(source);
            }
            if (!sources.isEmpty()) built.put(result, sources);
        });
        if (gen == indexGeneration) ItemIdentityIndex = built;
        return built;
    }

    /**
     * ItemStack.is(Item) 热路径：source 是否被替换成了 result。仅按引用查表，不涉及字符串。
     */
    public static Set<Item> sourceItemsOf(Item result) {
        Set<Item> sources = identityIndex().get(result);
        return sources == null ? Collections.emptySet() : sources;
    }

    public static boolean isReplacedBy(Item source, Item result) {
        Set<Item> sources = identityIndex().get(result);
        return sources != null && sources.contains(source);
    }

    private static void indexItem(String sourceId, String resultId) {
        String old = ItemMapCache.put(sourceId, resultId);
        if (old != null && !old.equals(resultId)) {
            unindexItem(sourceId, old);
        }
        ReverseItemCache.computeIfAbsent(resultId, k -> ConcurrentHashMap.newKeySet()).add(sourceId);
        invalidateIndexes();
    }

    private static void unindexItem(String sourceId, String resultId) {
        ReverseItemCache.computeIfPresent(resultId, (k, set) -> {
            set.remove(sourceId);
            return set.isEmpty() ? null : set;
        });
        invalidateIndexes();
    }

    public static String matchItem(String id) {
        return Objects.requireNonNullElse(ReloadOverrideItemMap, ItemMapCache).getOrDefault(id, null);
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

    public static Optional<Replacements.Rules> getItemRules(String itemId) {
        return Optional.ofNullable(ItemRulesCache.get(itemId));
    }

    public static Optional<Replacements.Rules> getTagRules(String tagId) {
        return Optional.ofNullable(TagRulesCache.get(tagId));
    }

    private static Optional<Replacements.Rules> getGlobalDefaultRules() {
        try {
            var cfg = OEIConfig.get();
            if (cfg != null) {
                return Optional.of(cfg.defaultRules().toRules());
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    public static boolean shouldReplaceInDataDir(String itemId, String directory) {
        return getItemRules(itemId)
                .or(ItemReplacementCache::getGlobalDefaultRules)
                .flatMap(Replacements.Rules::data)
                .map(dataRules -> dataRules.get(directory))
                .map(mode -> mode == Replacements.ProcessingMode.REPLACE)
                .orElse(false);
    }

    public static boolean shouldReplaceInTagType(String itemId, String tagType) {
        return getItemRules(itemId)
                .or(ItemReplacementCache::getGlobalDefaultRules)
                .flatMap(Replacements.Rules::tag)
                .map(tagRules -> tagRules.get(tagType))
                .map(mode -> mode == Replacements.ProcessingMode.REPLACE)
                .orElse(false);
    }

    public static void putReplacement(Replacements replacement) {
        List<Item> resolvedItems = Utils.resolveItemList(replacement.match());

        for (Item item : resolvedItems) {
            String id = Utils.getItemRegistryName(item);
            if (id != null) {
                indexItem(id, replacement.result());
                replacement.rules().ifPresent(rules -> ItemRulesCache.put(id, rules));
                Oneenoughitem.LOGGER.debug("Added item replacement mapping: {} -> {}", id, replacement.result());
            }
        }

        for (String matchItem : replacement.match()) {
            if (matchItem.startsWith("#")) {
                String tagId = matchItem.substring(1);
                TagMapCache.put(tagId, replacement.result());
                replacement.rules().ifPresent(rules -> TagRulesCache.put(tagId, rules));
                Oneenoughitem.LOGGER.debug("Added tag replacement mapping: {} -> {}", tagId, replacement.result());
            }
        }

        if (resolvedItems.isEmpty() && replacement.match().stream().noneMatch(s -> s.startsWith("#"))) {
            Oneenoughitem.LOGGER.warn("No valid items or tags resolved from match: {}", replacement.match());
        } else {
            int tagCount = (int) replacement.match().stream().filter(s -> s.startsWith("#")).count();
            Oneenoughitem.LOGGER.info("Added replacement rule for {} items and {} tags: {} -> {}",
                    resolvedItems.size(), tagCount, replacement.match(), replacement.result());
        }
    }

    // 给 JSON 拦截层做快速判断
    public static boolean hasAnyMappings() {
        return !ItemMapCache.isEmpty() || !TagMapCache.isEmpty();
    }

    public static boolean isSourceItemId(String id) {
        if (id == null) return false;
        return Objects.requireNonNullElse(ReloadOverrideItemMap, ItemMapCache).containsKey(id);
    }

    public static boolean isSourceTagId(String id) {
        return id != null && TagMapCache.containsKey(id);
    }

    public static void clearCache() {
        int previousItemSize = ItemMapCache.size();
        int previousTagSize = TagMapCache.size();
        ItemMapCache.clear();
        ReverseItemCache.clear();
        invalidateIndexes();
        TagMapCache.clear();
        ItemRulesCache.clear();
        TagRulesCache.clear();
        if (previousItemSize > 0 || previousTagSize > 0) {
            Oneenoughitem.LOGGER.info("Cleared {} cached item mappings and {} cached tag mappings",
                    previousItemSize, previousTagSize);
        }
    }

    /**
     * 从缓存中移除指定的物品替换
     */
    public static boolean removeItemReplacement(String itemId) {
        if (itemId != null && ItemMapCache.containsKey(itemId)) {
            String removed = ItemMapCache.remove(itemId);
            if (removed != null) unindexItem(itemId, removed);
            ItemRulesCache.remove(itemId);
            Oneenoughitem.LOGGER.debug("Removed item replacement from runtime cache: {} -> {}", itemId, removed);
            return true;
        }
        return false;
    }

    /**
     * 从缓存中移除指定的标签替换
     */
    public static boolean removeTagReplacement(String tagId) {
        if (tagId != null && TagMapCache.containsKey(tagId)) {
            String removed = TagMapCache.remove(tagId);
            TagRulesCache.remove(tagId);
            Oneenoughitem.LOGGER.debug("Removed tag replacement from runtime cache: {} -> {}", tagId, removed);
            return true;
        }
        return false;
    }

    /**
     * 批量移除物品和标签替换
     */
    public static void removeReplacements(Collection<String> itemIds, Collection<String> tagIds) {
        boolean changed = false;

        if (itemIds != null) {
            for (String itemId : itemIds) {
                if (removeItemReplacement(itemId)) {
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
            Oneenoughitem.LOGGER.info("Removed {} item replacements and {} tag replacements from runtime cache",
                    itemIds != null ? itemIds.size() : 0, tagIds != null ? tagIds.size() : 0);
        }
    }

    private static final Set<String> NO_SOURCES = Collections.emptySet();

    /**
     * 返回映射到 id 的所有源物品 id。O(1) 查反向索引，返回的集合只读且为实时视图。
     */
    public static Collection<String> trackSourceIdOf(String id) {
        if (id == null) return NO_SOURCES;
        Set<String> set = ReverseItemCache.get(id);
        return set == null ? NO_SOURCES : Collections.unmodifiableSet(set);
    }

    public static Collection<Item> trackSourceOf(String id) {
        if (id == null) return Collections.emptySet();
        if (!ReverseItemCache.containsKey(id)) return Collections.emptySet();
        return ReverseItemObjectCache.computeIfAbsent(id, k -> {
            Collection<Item> result = new HashSet<>();
            for (String sourceId : ReverseItemCache.getOrDefault(k, NO_SOURCES)) {
                Item item = Utils.getItemById(sourceId);
                if (item != null) result.add(item);
            }
            return result;
        });
    }

    // 开始/结束 "重载期覆盖映射"
    public static void beginReloadOverride(Map<String, String> currentItemMap) {
        if (currentItemMap == null || currentItemMap.isEmpty()) {
            ReloadOverrideItemMap = null;
            return;
        }
        ReloadOverrideItemMap = new HashMap<>(currentItemMap);
        Oneenoughitem.LOGGER.info("Enabled reload-override mapping for this resource reload: {} items",
                ReloadOverrideItemMap.size());
    }

    public static void endReloadOverride() {
        if (ReloadOverrideItemMap != null) {
            Oneenoughitem.LOGGER.info("Disabled reload-override mapping: {} entries", ReloadOverrideItemMap.size());
        }
        ReloadOverrideItemMap = null;
    }

    public static boolean hasReloadOverride() {
        return ReloadOverrideItemMap != null;
    }
}