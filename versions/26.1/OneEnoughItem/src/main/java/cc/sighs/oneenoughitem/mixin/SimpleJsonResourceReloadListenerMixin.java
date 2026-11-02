package cc.sighs.oneenoughitem.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.JsonOps;
import cc.sighs.oneenoughitem.event.RawJsonReloadEvent;
import net.neoforged.neoforge.common.NeoForge;
import cc.sighs.oneenoughitem.init.ItemReplacementCache;
import cc.sighs.oneenoughitem.init.OEIReplacementStrategy;
import cc.sighs.oneenoughitem.init.config.OEIConfig;
import cc.sighs.oneenoughitem.util.JsonReloadMixinHelper;
import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(SimpleJsonResourceReloadListener.class)
public abstract class SimpleJsonResourceReloadListenerMixin {

    @Shadow @Final private DynamicOps<JsonElement> ops;
    @Shadow @Final private Codec<?> codec;
    @Shadow @Final private FileToIdConverter lister;

    // Rewrite raw JSON before the listener decodes typed values.
    @Inject(
            method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/Map;",
            at = @At("HEAD"),
            cancellable = true
    )
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void oei$rewriteJsonBeforeDecoding(ResourceManager resourceManager,
                                                ProfilerFiller profiler,
                                                CallbackInfoReturnable<Map<Identifier, Object>> cir) {
        String directory = normalizeDirectory(lister.prefix());
        Map<Identifier, Object> results = new HashMap<>();
        Map<Identifier, JsonElement> jsonResults = new HashMap<>();
        var conditionalOps = ((ContextAwareReloadListenerAccessor) (Object) this).oei$makeConditionalOps(ops);
        Codec<JsonElement> jsonCodec = Codec.PASSTHROUGH.xmap(
                dynamic -> dynamic.convert(JsonOps.INSTANCE).getValue(), json -> new Dynamic<>(JsonOps.INSTANCE, json));
        SimpleJsonResourceReloadListener.scanDirectory(resourceManager, lister, conditionalOps, jsonCodec, jsonResults);
        JsonReloadMixinHelper.processJsonReload(directory, resourceManager, jsonResults, new OEIReplacementStrategy(),
                "recipe".equals(directory) ? ItemReplacementCache::beginReloadOverride : null,
                ItemReplacementCache::hasAnyMappings, modId -> OEIConfig.get().defaultRules().toRules(), "oei");
        NeoForge.EVENT_BUS.post(new RawJsonReloadEvent(directory, resourceManager, jsonResults));
        jsonResults.forEach((id, json) -> ((Codec<Object>) codec).parse(conditionalOps, json)
                .resultOrPartial(message -> cc.sighs.oneenoughitem.Oneenoughitem.LOGGER.error("Couldn't decode {}: {}", id, message))
                .ifPresent(value -> results.put(id, value)));
        cir.setReturnValue(results);
    }

    private static String normalizeDirectory(String prefix) {
        return switch (prefix) {
            case "recipes" -> "recipe";
            case "advancements" -> "advancement";
            case "loot_tables" -> "loot_table";
            default -> prefix;
        };
    }
}
