package com.mafuyu404.oneenoughitem.mixin;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;
import com.mafuyu404.oneenoughitem.init.MixinUtils;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;

@Mixin(SimpleJsonResourceReloadListener.class)
public abstract class SimpleJsonResourceReloadListenerMixin {

    @Shadow
    @Final
    private String directory;

    @Inject(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/Map;", at = @At("RETURN"))
    private void oei$replaceItemIdsInJson(ResourceManager resourceManager,
                                          ProfilerFiller profiler,
                                          CallbackInfoReturnable<Map<ResourceLocation, JsonElement>> cir) {

        MixinUtils.FieldRule baseRule = MixinUtils.getDataDirFieldRule(this.directory);

        var oeiSnap = MixinUtils.ReplacementLoader.loadCurrentSnapshot(resourceManager, "oei");
        var oebSnap = MixinUtils.ReplacementLoader.loadCurrentSnapshot(resourceManager, "oeb");
        var oefSnap = MixinUtils.ReplacementLoader.loadCurrentSnapshot(resourceManager, "oef");

        Map<String, String> currentItemMap = new HashMap<>();
        Map<String, String> sourceDomain = new HashMap<>();

        oeiSnap.itemMap().forEach((k, v) -> {
            currentItemMap.put(k, v);
            sourceDomain.put(k, "oei");
        });
        Map<String, Replacements.Rules> currentItemRules = new HashMap<>(oeiSnap.itemRules());

        oebSnap.itemMap().forEach((k, v) -> {
            currentItemMap.put(k, v);
            sourceDomain.put(k, "oeb");
        });
        currentItemRules.putAll(oebSnap.itemRules());

        oefSnap.itemMap().forEach((k, v) -> {
            currentItemMap.put(k, v);
            sourceDomain.put(k, "oef");
        });
        currentItemRules.putAll(oefSnap.itemRules());

        if ("recipes".equals(this.directory)) {
            ItemReplacementCache.beginReloadOverride(oeiSnap.itemMap());
        }

        Set<String> currentSourceIds = new HashSet<>(currentItemMap.keySet());
        if (currentItemMap.isEmpty() && !ItemReplacementCache.hasAnyMappings()) return;

        MixinUtils.FieldRule effectiveRule = new MixinUtils.FieldRule(baseRule.keys(), baseRule.strict());
        Map<String, Replacements.Rules> effectiveItemRules = getStringRulesMapMultiDomain(currentItemRules, currentItemMap, sourceDomain);

        Map<ResourceLocation, JsonElement> results = cir.getReturnValue();
        if (results == null || results.isEmpty()) return;

        int replacedFiles = 0;
        int droppedFiles = 0;
        final boolean fallbackEnabled = currentItemMap.isEmpty(); // 扫不到映射时 fallback 到旧缓存

        for (Iterator<Map.Entry<ResourceLocation, JsonElement>> it = results.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<ResourceLocation, JsonElement> entry = it.next();
            JsonElement json = entry.getValue();
            if (json == null) continue;

            MixinUtils.ReplaceContext ctx = new MixinUtils.ReplaceContext(
                    this.directory, effectiveRule, currentItemMap, currentSourceIds, effectiveItemRules
            );
            // fallback 到旧缓存（同时影响规则与映射）
            ctx.allowCacheFallback = fallbackEnabled;

            try {
                JsonElement processed = replaceElement(json, ctx);
                if (ctx.shouldDrop) {
                    it.remove();
                    droppedFiles++;
                    MixinUtils.LogHelper.logFileOperation("dropped", entry.getKey(), this.directory, ctx.lastMappingOrigin);
                    continue;
                }
                if (ctx.mutated && processed != null) {
                    entry.setValue(processed);
                    replacedFiles++;
                    MixinUtils.LogHelper.logFileOperation("rewritten", entry.getKey(), this.directory,
                            "RULE-BASED", fallbackEnabled);
                }
            } catch (Exception e) {
                MixinUtils.LogHelper.logError("JSON rewrite", entry.getKey(), this.directory, e);
            }
        }

        if (replacedFiles > 0 || droppedFiles > 0) {
            MixinUtils.LogHelper.logSummary(this.directory, replacedFiles, droppedFiles,
                    "RULE-BASED", currentItemMap.isEmpty());
        }
    }

    private static Map<String, Replacements.Rules> getStringRulesMapMultiDomain(Map<String, Replacements.Rules> currentItemRules,
                                                                                Map<String, String> currentItemMap,
                                                                                Map<String, String> sourceDomain) {
        Map<String, Replacements.Rules> effective = new HashMap<>(currentItemRules);
        Map<String, Replacements.Rules> defaultByDomain = new HashMap<>();
        for (String domain : List.of("oei", "oeb", "oef")) {
            try {
                var dr = OEIConfig.getDefaultRules(domain);
                if (dr != null) defaultByDomain.put(domain, dr.toRules());
            } catch (Exception ignored) {
            }
        }
        for (String from : currentItemMap.keySet()) {
            if (!effective.containsKey(from)) {
                String d = sourceDomain.get(from);
                Replacements.Rules def = defaultByDomain.get(d);
                if (def != null) effective.put(from, def);
            }
        }
        return effective;
    }

    private JsonElement replaceElement(JsonElement element, MixinUtils.ReplaceContext ctx) {
        if (element == null || ctx.shouldDrop) return element;
        if (element.isJsonObject()) return replaceInObject(element.getAsJsonObject(), ctx);
        if (element.isJsonArray()) return replaceInArray(element.getAsJsonArray(), ctx);
        return element;
    }

    private JsonElement replaceInObject(JsonObject obj, MixinUtils.ReplaceContext ctx) {
        if (ctx.shouldDrop) return obj;

        for (String key : new HashSet<>(obj.keySet())) {
            JsonElement value = obj.get(key);
            if (value == null) continue;

            if (ctx.rule.keys().contains(key) && value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
                MixinUtils.IdReplacer.tryReplaceId(obj, key, value.getAsString(), ctx);
                if (ctx.shouldDrop) return obj;
                continue;
            }

            if (value.isJsonObject()) {
                obj.add(key, replaceInObject(value.getAsJsonObject(), ctx));
            } else if (value.isJsonArray()) {
                obj.add(key, replaceInArray(value.getAsJsonArray(), ctx));
            }
        }
        return obj;
    }

    private JsonElement replaceInArray(JsonArray array, MixinUtils.ReplaceContext ctx) {
        if (ctx.shouldDrop) return array;

        for (int i = 0; i < array.size(); i++) {
            JsonElement elt = array.get(i);
            if (elt == null) continue;

            if (elt.isJsonObject()) {
                array.set(i, replaceInObject(elt.getAsJsonObject(), ctx));
            } else if (elt.isJsonArray()) {
                array.set(i, replaceInArray(elt.getAsJsonArray(), ctx));
            } else if (elt.isJsonPrimitive() && elt.getAsJsonPrimitive().isString()) {
                MixinUtils.IdReplacer.tryReplaceId(array, i, elt.getAsString(), ctx);
                if (ctx.shouldDrop) return array;
            }
        }
        return array;
    }
}