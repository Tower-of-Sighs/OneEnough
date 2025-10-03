package com.mafuyu404.oneenoughitem.init;

import com.google.gson.JsonElement;
import com.mafuyu404.oneenoughitem.api.ReplacementStrategy;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.util.MixinUtils;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class AbstractReplacementStrategy implements ReplacementStrategy {
    public static final List<String> REPLACEMENT_DIR_CANDIDATES = List.of("replacements");

    @Override
    public void replaceIdCommon(
            String id,
            MixinUtils.ReplaceContext ctx,
            BiConsumer<String, String> logReplaceAction,
            Consumer<JsonElement> setValueAction) {
        MixinUtils.IdReplacer.replaceIdCommon(id, ctx, logReplaceAction, setValueAction);
    }

    @Override
    public MixinUtils.ReplacementLoader.CurrentSnapshot loadCurrentSnapshot(ResourceManager resourceManager) {
        return MixinUtils.ReplacementLoader.loadCurrentSnapshot(resourceManager);
    }

    @Override
    public void parseReplacementJson(
            JsonElement root,
            Map<String, String> outMap,
            Map<String, Replacements.Rules> outDataRules,
            Map<String, Replacements.Rules> outTagRules) {
        MixinUtils.ReplacementLoader.parseReplacementJson(root, outMap, outDataRules, outTagRules);
    }

    @Override
    public MixinUtils.FieldRule getDataDirFieldRule(String directory) {
        return MixinUtils.getDataDirFieldRule(directory);
    }

    @Override
    public String resolveTarget(String id, MixinUtils.ReplaceContext ctx) {
        return MixinUtils.TargetResolver.resolveTarget(id, ctx);
    }

    @Override
    public boolean isSourceIdWithCurrent(String id, MixinUtils.ReplaceContext ctx) {
        return MixinUtils.TargetResolver.isSourceIdWithCurrent(id, ctx);
    }
}
