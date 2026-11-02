package com.flechazo.oneenoughfluid.init;

import com.flechazo.oneenoughfluid.Oneenoughfluid;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.mafuyu404.oneenoughitem.util.ReplacementIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public final class FluidReplacementCache {
    private static final ConcurrentHashMap<String, String> FluidMapCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, String> TagMapCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Replacements.Rules> FluidRulesCache = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Replacements.Rules> TagRulesCache = new ConcurrentHashMap<>();
    private static volatile Map<String, String> ReloadOverrideFluidMap = null;
    private static volatile ReplacementIndex<Fluid> registryIndex;

    private static synchronized void invalidateIndex() {
        registryIndex = null;
    }

    private static synchronized ReplacementIndex<Fluid> index() {
        if (registryIndex == null) {
            Map<Fluid, Fluid> mappings = new IdentityHashMap<>();
            Objects.requireNonNullElse(ReloadOverrideFluidMap, FluidMapCache).forEach((sourceId, targetId) -> {
                Fluid source = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(sourceId));
                Fluid target = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(targetId));
                if (source != null && target != null) mappings.put(source, target);
            });
            registryIndex = new ReplacementIndex<>(mappings);
        }
        return registryIndex;
    }

    public static Fluid replacementOf(Fluid source) {
        if (!hasAnyMappings()) return null;
        ReplacementIndex<Fluid> current = registryIndex;
        return (current != null ? current : index()).replacementOf(source);
    }

    public static boolean matches(Fluid actual, Fluid expected) {
        if (actual == expected) return true;
        if (!hasAnyMappings() || actual == null || expected == null) return false;
        ReplacementIndex<Fluid> current = registryIndex;
        return (current != null ? current : index()).matches(actual, expected);
    }

    public static String matchFluid(String id) {
        return Objects.requireNonNullElse(ReloadOverrideFluidMap, FluidMapCache).getOrDefault(id, null);
    }

    public static String matchTag(String tagId) {
        return TagMapCache.getOrDefault(tagId, null);
    }

    public static String matchTag(ResourceLocation tagId) {
        return tagId != null ? matchTag(tagId.toString()) : null;
    }

    public static boolean isSourceFluidId(String id) {
        return id != null && Objects.requireNonNullElse(ReloadOverrideFluidMap, FluidMapCache).containsKey(id);
    }

    public static boolean isSourceTagId(String id) {
        return id != null && TagMapCache.containsKey(id);
    }

    public static boolean isTagReplaced(String tagId) {
        return tagId != null && TagMapCache.containsKey(tagId);
    }

    public static boolean isTagReplaced(ResourceLocation tagId) {
        return tagId != null && isTagReplaced(tagId.toString());
    }

    public static Optional<Replacements.Rules> getFluidRules(String itemId) {
        return Optional.ofNullable(FluidRulesCache.get(itemId));
    }

    public static Optional<Replacements.Rules> getTagRules(String tagId) {
        return Optional.ofNullable(TagRulesCache.get(tagId));
    }

    public static void clearCache() {
        FluidMapCache.clear();
        TagMapCache.clear();
        FluidRulesCache.clear();
        TagRulesCache.clear();
        invalidateIndex();
    }

    public static boolean hasAnyMappings() {
        return !Objects.requireNonNullElse(ReloadOverrideFluidMap, FluidMapCache).isEmpty() || !TagMapCache.isEmpty();
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
                    FluidMapCache.put(m, result);
                    FluidRulesCache.put(m, rules);
                }
            }
        }, () -> {
            for (String m : replacement.match()) {
                if (m == null || m.isEmpty()) continue;
                if (m.startsWith("#")) {
                    TagMapCache.put(m.substring(1), result);
                } else {
                    FluidMapCache.put(m, result);
                }
            }
        });
        // Expand tag rules for runtime FluidStack and EMI lookups as well as JSON replacement.
        for (Fluid source : resolve(replacement.match())) {
            ResourceLocation id = ForgeRegistries.FLUIDS.getKey(source);
            if (id == null) continue;
            FluidMapCache.put(id.toString(), result);
            replacement.rules().ifPresent(rules -> FluidRulesCache.put(id.toString(), rules));
        }
        invalidateIndex();
    }


    public static boolean removeFluidReplacement(String fluidId) {
        if (fluidId != null && FluidMapCache.containsKey(fluidId)) {
            String removed = FluidMapCache.remove(fluidId);
            FluidRulesCache.remove(fluidId);
            invalidateIndex();
            Oneenoughfluid.LOGGER.debug("Removed fluid replacement from runtime cache: {} -> {}", fluidId, removed);
            return true;
        }
        return false;
    }

    public static boolean removeTagReplacement(String tagId) {
        if (tagId != null && TagMapCache.containsKey(tagId)) {
            String removed = TagMapCache.remove(tagId);
            TagRulesCache.remove(tagId);
            Oneenoughfluid.LOGGER.debug("Removed tag replacement from runtime cache: {} -> {}", tagId, removed);
            return true;
        }
        return false;
    }

    public static void removeReplacements(Collection<String> fluidIds, Collection<String> tagIds) {
        boolean changed = false;

        if (fluidIds != null) {
            for (String fluidId : fluidIds) {
                if (removeFluidReplacement(fluidId)) {
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
            Oneenoughfluid.LOGGER.info("Removed {} fluid replacements and {} tag replacements from runtime cache",
                    fluidIds != null ? fluidIds.size() : 0, tagIds != null ? tagIds.size() : 0);
        }
    }

    public static Collection<String> trackSourceIdOf(String id) {
        Collection<String> result = new HashSet<>();
        FluidMapCache.forEach((matchFluid, resultFluid) -> {
            if (resultFluid.equals(id)) result.add(matchFluid);
        });
        return result;
    }

    private static List<Fluid> resolve(List<String> ids) {
        List<Fluid> out = new ArrayList<>();
        for (String id : ids) {
            if (id == null || id.isEmpty()) continue;

            if (id.startsWith("#")) {
                ResourceLocation tagId = new ResourceLocation(id.substring(1));
                ITag<Fluid> tag = ForgeRegistries.FLUIDS.tags().getTag(TagKey.create(ForgeRegistries.FLUIDS.getRegistryKey(), tagId));
                out.addAll(tag.stream().toList());
            } else {
                Fluid fluid = ForgeRegistries.FLUIDS.getValue(new ResourceLocation(id));
                if (fluid != null) out.add(fluid);
            }
        }
        return out;
    }

    private static Optional<Replacements.Rules> getGlobalDefaultRules() {
        try {
            var cfg = OEFConfig.get();
            if (cfg != null) {
                return Optional.of(cfg.defaultRules().toRules());
            }
        } catch (Exception ignored) {
        }
        return Optional.empty();
    }

    public static boolean shouldReplaceInDataDir(String itemId, String directory) {
        return getFluidRules(itemId)
                .or(FluidReplacementCache::getGlobalDefaultRules)
                .flatMap(Replacements.Rules::data)
                .map(dataRules -> dataRules.get(directory))
                .map(mode -> mode == Replacements.ProcessingMode.REPLACE)
                .orElse(false);
    }

    public static boolean shouldReplaceInTagType(String itemId, String tagType) {
        return getFluidRules(itemId)
                .or(FluidReplacementCache::getGlobalDefaultRules)
                .flatMap(Replacements.Rules::tag)
                .map(tagRules -> tagRules.get(tagType))
                .map(mode -> mode == Replacements.ProcessingMode.REPLACE)
                .orElse(false);
    }

    public static void beginReloadOverride(Map<String, String> currentFluidMap) {
        if (currentFluidMap == null || currentFluidMap.isEmpty()) {
            ReloadOverrideFluidMap = Map.of();
            invalidateIndex();
            return;
        }
        ReloadOverrideFluidMap = new HashMap<>(currentFluidMap);
        invalidateIndex();
        Oneenoughfluid.LOGGER.info("Enabled reload-override mapping for this resource reload: {} fluid",
                ReloadOverrideFluidMap.size());
    }

    public static void endReloadOverride() {
        if (ReloadOverrideFluidMap != null) {
            Oneenoughfluid.LOGGER.info("Disabled reload-override mapping: {} entries", ReloadOverrideFluidMap.size());
        }
        ReloadOverrideFluidMap = null;
        invalidateIndex();
    }

    public static boolean hasReloadOverride() {
        return ReloadOverrideFluidMap != null;
    }
}
