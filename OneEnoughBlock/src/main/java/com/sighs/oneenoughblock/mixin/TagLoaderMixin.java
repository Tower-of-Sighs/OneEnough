package com.sighs.oneenoughblock.mixin;

import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.MixinUtils;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.sighs.oneenoughblock.init.BlockReplacementCache;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.tags.TagLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.Map;

@Mixin(TagLoader.class)
public abstract class TagLoaderMixin<T> {
    @Shadow
    @Final
    private String directory;

    private static final String BLOCKS_TAG_DIR = "tags/blocks";

    @Inject(method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;", at = @At("RETURN"))
    private void oneenoughblock$processTags(ResourceManager resourceManager,
                                            CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> cir) {
        // 仅处理 blocks 标签域
        if (!BLOCKS_TAG_DIR.equals(this.directory)) return;

        var tags = cir.getReturnValue();
        if (tags == null || tags.isEmpty()) return;

        var snap = MixinUtils.ReplacementLoader.loadCurrentSnapshot(resourceManager, "oeb");
        Map<String, String> currentMap = snap.itemMap();
        Map<String, Replacements.Rules> currentRules = snap.itemRules();
        final boolean fallbackEnabled = currentMap.isEmpty();

        Replacements.Rules defaultRules = null;
        try {
            var dr = OEIConfig.getDefaultRules("oeb");
            if (dr != null) defaultRules = dr.toRules();
        } catch (Exception ignored) {
        }

        for (Map.Entry<ResourceLocation, List<TagLoader.EntryWithSource>> tagEntry : tags.entrySet()) {
            var entries = tagEntry.getValue();
            if (entries == null || entries.isEmpty()) continue;
            var it = entries.iterator();
            while (it.hasNext()) {
                var tracked = it.next();
                var e = tracked.entry();
                if (e.isTag()) continue;
                var fromId = e.getId();
                var fromStr = fromId.toString();

                String mapped = currentMap.get(fromStr);
                if (mapped == null && fallbackEnabled) {
                    mapped = BlockReplacementCache.matchBlock(fromStr);
                }
                if (mapped == null) continue;

                var rules = currentRules.getOrDefault(fromStr, defaultRules);
                boolean shouldReplace = rules != null && rules.tag()
                        .map(m -> m.get("blocks")).map(mode -> mode == Replacements.ProcessingMode.REPLACE)
                        .orElse(fallbackEnabled && BlockReplacementCache.shouldReplaceInTagType(fromStr, "blocks"));

                if (shouldReplace) {
                    it.remove();
                }
            }
        }
    }
}