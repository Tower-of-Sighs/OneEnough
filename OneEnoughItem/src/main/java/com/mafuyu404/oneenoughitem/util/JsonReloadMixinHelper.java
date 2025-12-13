package com.mafuyu404.oneenoughitem.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mafuyu404.oneenoughitem.api.ReplacementStrategy;
import com.mafuyu404.oneenoughitem.data.Replacements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class JsonReloadMixinHelper {

    private JsonReloadMixinHelper() {
    }

    public static void processJsonReload(
            String directory,
            ResourceManager resourceManager,
            Map<ResourceLocation, JsonElement> results,
            ReplacementStrategy strategy,
            Consumer<Map<String, String>> cacheBeginReloadOverride,
            Supplier<Boolean> hasAnyMappings,
            Function<String, Replacements.Rules> getDefaultRules,
            String modId
    ) {
        MixinUtils.FieldRule baseRule = strategy.getDataDirFieldRule(directory);
        var snapshot = strategy.loadCurrentSnapshot(resourceManager);
        Map<String, String> currentDataMap = snapshot.dataMap();
        Map<String, Replacements.Rules> currentDataRules = snapshot.dataRules();

        if ("recipe".equals(directory) && cacheBeginReloadOverride != null) {
            cacheBeginReloadOverride.accept(currentDataMap);
        }

        Set<String> currentSourceIds = new HashSet<>(currentDataMap.keySet());

        if (currentDataMap.isEmpty() && Boolean.FALSE.equals(hasAnyMappings.get())) {
            return;
        }

        MixinUtils.FieldRule effectiveRule = new MixinUtils.FieldRule(baseRule.keys(), baseRule.strict());
        Map<String, Replacements.Rules> effectiveDataRules = getStringRulesMap(currentDataRules, currentDataMap, getDefaultRules, modId);

        if (results == null || results.isEmpty()) return;

        int replacedFiles = 0;
        int droppedFiles = 0;
        final boolean fallbackEnabled = currentDataMap.isEmpty();

        for (var it = results.entrySet().iterator(); it.hasNext(); ) {
            var entry = it.next();
            JsonElement json = entry.getValue();
            if (json == null) continue;

            MixinUtils.ReplaceContext ctx = new MixinUtils.ReplaceContext(
                    directory, effectiveRule, currentDataMap, currentSourceIds, effectiveDataRules
            );
            ctx.allowCacheFallback = fallbackEnabled;

            try {
                JsonElement processed = replaceElementWithStrategy(json, ctx, strategy);

                if (ctx.shouldDrop) {
                    it.remove();
                    droppedFiles++;
                    MixinUtils.LogHelper.logFileOperation("dropped", entry.getKey(), directory, ctx.lastMappingOrigin);
                    continue;
                }

                if (ctx.mutated && processed != null) {
                    entry.setValue(processed);
                    replacedFiles++;
                    MixinUtils.LogHelper.logFileOperation("rewritten", entry.getKey(), directory,
                            "RULE-BASED", fallbackEnabled);
                }

            } catch (Exception e) {
                MixinUtils.LogHelper.logError("JSON rewrite", entry.getKey(), directory, e);
            }
        }

        if (replacedFiles > 0 || droppedFiles > 0) {
            MixinUtils.LogHelper.logSummary(directory, replacedFiles, droppedFiles,
                    "RULE-BASED", currentDataMap.isEmpty());
        }
    }

    private static @NotNull Map<String, Replacements.Rules> getStringRulesMap(
            Map<String, Replacements.Rules> currentDataRules,
            Map<String, String> currentDataMap,
            Function<String, Replacements.Rules> getDefaultRules,
            String modId
    ) {
        Replacements.Rules defaultRules = null;
        try {
            defaultRules = getDefaultRules.apply(modId);
        } catch (Exception ignored) {
        }

        Map<String, Replacements.Rules> effectiveDataRules = new HashMap<>(currentDataRules);

        if (defaultRules != null) {
            for (String from : currentDataMap.keySet()) {
                effectiveDataRules.putIfAbsent(from, defaultRules);
            }
        }
        return effectiveDataRules;
    }


    private static JsonElement replaceElementWithStrategy(JsonElement element, MixinUtils.ReplaceContext ctx, ReplacementStrategy strategy) {
        if (element == null || ctx.shouldDrop) return element;
        if (element.isJsonObject()) return replaceInObjectWithStrategy(element.getAsJsonObject(), ctx, strategy);
        if (element.isJsonArray()) return replaceInArrayWithStrategy(element.getAsJsonArray(), ctx, strategy);
        return element;
    }

    private static JsonElement replaceInObjectWithStrategy(JsonObject obj, MixinUtils.ReplaceContext ctx, ReplacementStrategy strategy) {
        if (ctx.shouldDrop) return obj;
        for (String key : new HashSet<>(obj.keySet())) {
            JsonElement value = obj.get(key);
            if (value == null) continue;

            if (ctx.rule.keys().contains(key) && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                strategy.replaceIdCommon(
                        value.getAsString(),
                        ctx,
                        (oldId, newId) -> MixinUtils.LogHelper.logReplace(ctx.dataType, oldId, newId, ctx.lastMappingOrigin, false),
                        v -> obj.add(key, v)
                );
                if (ctx.shouldDrop) return obj;
                continue;
            }

            if (value.isJsonObject()) obj.add(key, replaceInObjectWithStrategy(value.getAsJsonObject(), ctx, strategy));
            else if (value.isJsonArray())
                obj.add(key, replaceInArrayWithStrategy(value.getAsJsonArray(), ctx, strategy));
        }
        return obj;
    }

    private static JsonElement replaceInArrayWithStrategy(JsonArray array, MixinUtils.ReplaceContext ctx, ReplacementStrategy strategy) {
        if (ctx.shouldDrop) return array;
        for (int i = 0; i < array.size(); i++) {
            final int index = i;
            JsonElement elt = array.get(i);
            if (elt == null) continue;

            if (elt.isJsonObject()) {
                array.set(index, replaceInObjectWithStrategy(elt.getAsJsonObject(), ctx, strategy));
            } else if (elt.isJsonArray()) {
                array.set(index, replaceInArrayWithStrategy(elt.getAsJsonArray(), ctx, strategy));
            } else if (elt.isJsonPrimitive() && elt.getAsJsonPrimitive().isString()) {
                strategy.replaceIdCommon(
                        elt.getAsString(),
                        ctx,
                        (oldId, newId) -> MixinUtils.LogHelper.logReplace(ctx.dataType, oldId, newId, ctx.lastMappingOrigin, true),
                        v -> array.set(index, v)
                );
                if (ctx.shouldDrop) return array;
            }
        }
        return array;
    }
}