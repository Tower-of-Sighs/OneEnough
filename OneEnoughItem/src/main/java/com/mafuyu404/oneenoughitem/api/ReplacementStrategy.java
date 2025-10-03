package com.mafuyu404.oneenoughitem.api;

import com.google.gson.JsonElement;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.util.MixinUtils;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface ReplacementStrategy {
    // ① 替换核心逻辑
    void replaceIdCommon(
            String id,
            MixinUtils.ReplaceContext ctx,
            BiConsumer<String, String> logReplaceAction,
            Consumer<JsonElement> setValueAction);

    // ② 当前快照加载
    MixinUtils.ReplacementLoader.CurrentSnapshot loadCurrentSnapshot(ResourceManager resourceManager);

    // ③ JSON 解析
    void parseReplacementJson(
            JsonElement root,
            Map<String, String> outMap,
            Map<String, Replacements.Rules> outDataRules,
            Map<String, Replacements.Rules> outTagRules);

    // ④ 根据目录名获取字段规则
    MixinUtils.FieldRule getDataDirFieldRule(String directory);

    // ⑤ 解析替换目标
    String resolveTarget(String id, MixinUtils.ReplaceContext ctx);

    // ⑥ 判断是否为源 id
    boolean isSourceIdWithCurrent(String id, MixinUtils.ReplaceContext ctx);
}
