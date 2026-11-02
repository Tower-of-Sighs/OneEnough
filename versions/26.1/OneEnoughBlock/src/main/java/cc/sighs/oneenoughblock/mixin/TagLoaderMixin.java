package cc.sighs.oneenoughblock.mixin;

import cc.sighs.oneenoughitem.data.Replacements;
import cc.sighs.oneenoughitem.util.MixinUtils;
import cc.sighs.oneenoughblock.init.BlockReplacementCache;
import cc.sighs.oneenoughblock.init.OEBConfig;
import net.minecraft.resources.Identifier;
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
                                            CallbackInfoReturnable<Map<Identifier, List<TagLoader.EntryWithSource>>> cir) {
        // Only process the block tag directory.
        if (!BLOCKS_TAG_DIR.equals(this.directory)) return;

        var tags = cir.getReturnValue();
        if (tags == null || tags.isEmpty()) return;

        var snap = MixinUtils.ReplacementLoader.loadCurrentSnapshot(resourceManager);
        Map<String, String> currentMap = snap.dataMap();
        Map<String, Replacements.Rules> currentRules = snap.dataRules();
        final boolean fallbackEnabled = false;

        Replacements.Rules defaultRules = null;
        try {
            var dr = OEBConfig.get();
            defaultRules = dr.defaultRules().toRules();
        } catch (Exception ignored) {
        }

        for (Map.Entry<Identifier, List<TagLoader.EntryWithSource>> tagEntry : tags.entrySet()) {
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
