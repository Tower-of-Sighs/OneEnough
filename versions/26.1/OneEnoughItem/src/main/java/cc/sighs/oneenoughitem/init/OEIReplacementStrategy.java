package cc.sighs.oneenoughitem.init;

import com.google.gson.JsonElement;
import cc.sighs.oneenoughitem.data.Replacements;
import cc.sighs.oneenoughitem.util.MixinUtils;
import net.minecraft.server.packs.resources.ResourceManager;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class OEIReplacementStrategy extends AbstractReplacementStrategy {
    @Override
    public void replaceIdCommon(String id,
                                MixinUtils.ReplaceContext ctx,
                                BiConsumer<String, String> logReplaceAction,
                                Consumer<JsonElement> setValueAction) {
        super.replaceIdCommon(id, ctx, logReplaceAction, setValueAction);
    }

    @Override
    public MixinUtils.ReplacementLoader.CurrentSnapshot loadCurrentSnapshot(ResourceManager resourceManager) {
        return super.loadCurrentSnapshot(resourceManager);
    }

    @Override
    public void parseReplacementJson(JsonElement root,
                                     Map<String, String> outMap,
                                     Map<String, Replacements.Rules> outItemRules,
                                     Map<String, Replacements.Rules> outTagRules) {
        super.parseReplacementJson(root, outMap, outItemRules, outTagRules);
    }

    @Override
    public String resolveTarget(String id, MixinUtils.ReplaceContext ctx) {
        return super.resolveTarget(id, ctx);
    }

    @Override
    public boolean isSourceIdWithCurrent(String id, MixinUtils.ReplaceContext ctx) {
        return super.isSourceIdWithCurrent(id, ctx);
    }

    @Override
    public MixinUtils.FieldRule getDataDirFieldRule(String directory) {
        return super.getDataDirFieldRule(directory);
    }
}