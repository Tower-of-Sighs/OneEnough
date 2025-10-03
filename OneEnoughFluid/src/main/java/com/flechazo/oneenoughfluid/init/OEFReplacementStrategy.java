package com.flechazo.oneenoughfluid.init;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.AbstractReplacementStrategy;
import com.mafuyu404.oneenoughitem.util.MixinUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class OEFReplacementStrategy extends AbstractReplacementStrategy {
    @Override
    public void replaceIdCommon(String id, MixinUtils.ReplaceContext ctx, BiConsumer<String, String> logReplaceAction, Consumer<JsonElement> setValueAction) {
        String target = resolveTarget(id, ctx);
        if (target == null) return;

        boolean shouldProcess = false;
        if (ctx.dataRules != null) {
            Replacements.Rules rules = ctx.dataRules.get(id);
            if (rules != null) {
                shouldProcess = rules.data()
                        .map(m -> m.get(ctx.dataType))
                        .map(mode -> mode == Replacements.ProcessingMode.REPLACE)
                        .orElse(false);
            } else if (ctx.allowCacheFallback) {
                shouldProcess = FluidReplacementCache.shouldReplaceInDataDir(id, ctx.dataType);
            }
        } else if (ctx.allowCacheFallback) {
            shouldProcess = FluidReplacementCache.shouldReplaceInDataDir(id, ctx.dataType);
        }
        if (!shouldProcess) {
            return;
        }

        if ("minecraft:empty".equals(target)) {
            ctx.shouldDrop = true;
            MixinUtils.LogHelper.logDrop(ctx.dataType, id, target, ctx.lastMappingOrigin, "mapping");
            return;
        }

        if (!target.equals(id)) {
            setValueAction.accept(new JsonPrimitive(target));
            ctx.mutated = true;
            logReplaceAction.accept(id, target);
        }

        if (ctx.rule.strict() && isSourceIdWithCurrent(id, ctx)) {
            ctx.shouldDrop = true;
            MixinUtils.LogHelper.logDrop(ctx.dataType, id, ctx.lastMappingOrigin, ctx.lastMappingOrigin, "strict residual source-id");
        }
    }

    @Override
    public MixinUtils.ReplacementLoader.CurrentSnapshot loadCurrentSnapshot(ResourceManager resourceManager) {
        Map<String, String> map = new HashMap<>();
        Map<String, Replacements.Rules> fluidRules = new HashMap<>();
        Map<String, Replacements.Rules> tagRules = new HashMap<>();
        Predicate<ResourceLocation> jsonPredicate = rl -> rl.getPath().endsWith(".json") && "oef".equals(rl.getNamespace());

        for (String baseDir : REPLACEMENT_DIR_CANDIDATES) {
            try {
                Map<ResourceLocation, Resource> res = resourceManager.listResources(baseDir, jsonPredicate);
                if (res.isEmpty()) continue;

                for (Map.Entry<ResourceLocation, Resource> e : res.entrySet()) {
                    try (Reader reader = e.getValue().openAsReader()) {
                        JsonElement root = JsonParser.parseReader(reader);
                        parseReplacementJson(root, map, fluidRules, tagRules);
                    } catch (Exception ex) {
                        MixinUtils.LogHelper.logParseError(e.getKey(), ex);
                    }
                }
            } catch (Exception ignore) {
            }
        }
        return new MixinUtils.ReplacementLoader.CurrentSnapshot(map, fluidRules, tagRules);
    }

    @Override
    public String resolveTarget(String id, MixinUtils.ReplaceContext ctx) {
        if (ctx.dataMap != null) {
            String v = ctx.dataMap.get(id);
            if (v != null) {
                ctx.lastMappingOrigin = "CURRENT";
                return v;
            }
        }
        if (ctx.allowCacheFallback) {
            String v = FluidReplacementCache.matchFluid(id);
            if (v != null) {
                ctx.lastMappingOrigin = "CACHE";
                return v;
            }
        }
        ctx.lastMappingOrigin = "N/A";
        return null;
    }

    @Override
    public boolean isSourceIdWithCurrent(String id, MixinUtils.ReplaceContext ctx) {
        if (ctx.sourceDataIds != null && ctx.sourceDataIds.contains(id))
            return true;
        return ctx.allowCacheFallback && FluidReplacementCache.isSourceFluidId(id);
    }

    @Override
    public void parseReplacementJson(
            JsonElement root,
            Map<String, String> outMap,
            Map<String, Replacements.Rules> outDataRules,
            Map<String, Replacements.Rules> outTagRules) {
        if (root == null) return;
        if (root.isJsonObject()) {
            var obj = root.getAsJsonObject();
            if (obj.has("matchFluid") && obj.get("matchFluid").isJsonArray() && obj.has("resultFluid")) {
                String result = obj.get("resultFluid").getAsString();
                obj.getAsJsonArray("matchFluid").forEach(el -> {
                    if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
                        outMap.put(el.getAsString(), result);
                    }
                });
            }
        }
    }

    @Override
    public MixinUtils.FieldRule getDataDirFieldRule(String directory) {
        return switch (directory) {
            case "recipes" -> new MixinUtils.FieldRule(Set.of("fluid", "id", "result", "result_fluid"), true);
            case "advancements" -> new MixinUtils.FieldRule(Set.of("fluid", "result_fluid"), true);
            case "loot_tables" -> new MixinUtils.FieldRule(Set.of("name", "result_fluid", "fluid"), true);
            default -> new MixinUtils.FieldRule(Set.of("fluid", "id", "result", "result_fluid"), false);
        };
    }
}
