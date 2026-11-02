package com.sighs.oneenoughblock.event;

import cc.sighs.oelib.data.forge.DataManager;
import cc.sighs.oelib.event.forge.DataReloadEvent;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.event.base.AbstractReplacementEventHandler;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.sighs.oneenoughblock.init.BlockReplacementCache;
import com.sighs.oneenoughblock.init.OEBConfig;
import com.sighs.oneenoughblock.init.ReplacementUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import net.minecraftforge.event.TagsUpdatedEvent;

@Mod.EventBusSubscriber(modid = "oneenoughblock")
public class ServerEventHandler {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        HANDLER.rebuildServerReplacementCache("oeb-server-start", DataManager.get(Replacements.class));
    }


    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) return;
        HANDLER.rebuildServerReplacementCache("oeb-tags-ready", DataManager.get(Replacements.class));
        BlockReplacementCache.endReloadOverride();
    }

    @SubscribeEvent
    public static void onDataReload(DataReloadEvent event) {
        if (event.isDataType(Replacements.class)) {
            HANDLER.rebuildReplacementCache("oeb-server-data-reload", DataManager.get(Replacements.class));
            ReplacementUtils.onReload();
        }
    }

    private static final Handler HANDLER = new Handler();

    private static class Handler extends AbstractReplacementEventHandler {
        @Override
        protected boolean acceptLocation(ResourceLocation location) {
            return "oeb".equals(location.getNamespace());
        }

        @Override
        protected void clearModuleCache() {
            BlockReplacementCache.clearCache();
        }

        @Override
        protected void putToModuleCache(Replacements r) {
            BlockReplacementCache.putReplacement(buildReplacements(r));
        }

        @Override
        protected boolean tryResolveData(String id) {
            return ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(id));
        }

        @Override
        protected boolean tryResolveTag(String tagId) {
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, new ResourceLocation(tagId));
            for (Block b : ForgeRegistries.BLOCKS) {
                if (b.builtInRegistryHolder().is(tag)) return true;
            }
            return false;
        }

        @Override
        protected Replacements buildReplacements(Replacements r) {
            var dr = OEBConfig.get();
            if (r.rules().isEmpty() && dr != null) {
                return new Replacements(r.match(), r.result(), Optional.of(dr.defaultRules().toRules()));
            }
            return r;
        }
    }
}
