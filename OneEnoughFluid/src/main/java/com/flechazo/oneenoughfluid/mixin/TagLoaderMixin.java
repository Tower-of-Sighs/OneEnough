package com.flechazo.oneenoughfluid.mixin;

import com.flechazo.oneenoughfluid.init.FluidReplacementCache;
import com.flechazo.oneenoughfluid.init.OEFConfig;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.mafuyu404.oneenoughitem.util.MixinUtils;
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

    private static final String FLUIDS_TAG_DIR = "tags/fluid";

    @Inject(method = "load(Lnet/minecraft/server/packs/resources/ResourceManager;)Ljava/util/Map;", at = @At("RETURN"))
    private void oneenoughfluid$processTags(ResourceManager resourceManager,
                                            CallbackInfoReturnable<Map<ResourceLocation, List<TagLoader.EntryWithSource>>> cir) {
        // 仅处理 fluids 标签域
        if (!FLUIDS_TAG_DIR.equals(this.directory)) return;

        var tags = cir.getReturnValue();
        if (tags == null || tags.isEmpty()) return;

        var snap = MixinUtils.ReplacementLoader.loadCurrentSnapshot(resourceManager);
        Map<String, String> currentMap = snap.dataMap();
        Map<String, Replacements.Rules> currentRules = snap.dataRules();
        final boolean fallbackEnabled = currentMap.isEmpty();

        Replacements.Rules defaultRules = null;
        try {
            var dr = OEFConfig.get();
            if (dr != null) defaultRules = dr.defaultRules().toRules();
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
                    mapped = FluidReplacementCache.matchFluid(fromStr);
                }
                if (mapped == null) continue;

                var rules = currentRules.getOrDefault(fromStr, defaultRules);
                boolean shouldReplace = rules != null && rules.tag()
                        .map(m -> m.get("fluids")).map(mode -> mode == Replacements.ProcessingMode.REPLACE)
                        .orElse(fallbackEnabled && FluidReplacementCache.shouldReplaceInTagType(fromStr, "fluid"));

                if (shouldReplace) {
                    it.remove();
                }
            }
        }
    }
}