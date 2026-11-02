package com.mafuyu404.oneenoughitem.mixin;

import com.google.gson.JsonElement;
import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;
import com.mafuyu404.oneenoughitem.init.OEIReplacementStrategy;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.mafuyu404.oneenoughitem.util.JsonReloadMixinHelper;
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

import java.util.Map;

@Mixin(SimpleJsonResourceReloadListener.class)
public abstract class SimpleJsonResourceReloadListenerMixin {

    @Shadow
    @Final
    private String directory;

    @Inject(method = "prepare(Lnet/minecraft/server/packs/resources/ResourceManager;Lnet/minecraft/util/profiling/ProfilerFiller;)Ljava/util/Map;", at = @At("RETURN"))
    private void oei$replaceItemIdsInJson(ResourceManager resourceManager,
                                          ProfilerFiller profiler,
                                          CallbackInfoReturnable<Map<ResourceLocation, JsonElement>> cir) {
        JsonReloadMixinHelper.processJsonReload(
                this.directory,
                resourceManager,
                cir.getReturnValue(),
                new OEIReplacementStrategy(),
                "recipe".equals(this.directory) ? ItemReplacementCache::beginReloadOverride : null,
                ItemReplacementCache::hasAnyMappings,
                modId -> {
                    var cfg = OEIConfig.get();
                    return cfg.defaultRules().toRules();
                },
                "oei"
        );
    }
}