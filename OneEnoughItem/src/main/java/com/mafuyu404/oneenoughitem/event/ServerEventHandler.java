package com.mafuyu404.oneenoughitem.event;

import com.mafuyu404.oelib.neoforge.data.DataManager;
import com.mafuyu404.oelib.neoforge.event.DataReloadEvent;
import com.mafuyu404.oneenoughitem.Oneenoughitem;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.event.base.AbstractReplacementEventHandler;
import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.mafuyu404.oneenoughitem.util.Utils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.Optional;

@EventBusSubscriber(modid = Oneenoughitem.MODID)
public class ServerEventHandler {

    private static final Handler HANDLER = new Handler();

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        var registryLookup = server.registryAccess().lookupOrThrow(Registries.ITEM);
        HANDLER.rebuildReplacementCache("oei-server-start", DataManager.get(Replacements.class), registryLookup);
    }

    @SubscribeEvent
    public static void onDataReload(DataReloadEvent event) {
        if (event.isDataType(Replacements.class)) {
            var server = DataManager.getCurrentServer();
            var registryLookup = server != null ? server.registryAccess().lookupOrThrow(Registries.ITEM) : null;

            HANDLER.rebuildReplacementCache("server-data-reload", DataManager.get(Replacements.class), registryLookup);

            Oneenoughitem.LOGGER.info("Server replacement cache rebuilt due to data reload: {} entries loaded, {} invalid",
                    event.getLoadedCount(), event.getInvalidCount());
            ItemReplacementCache.endReloadOverride();
        }
    }

    private static class Handler extends AbstractReplacementEventHandler<Item> {
        @Override
        protected void clearModuleCache() {
            ItemReplacementCache.clearCache();
        }

        @Override
        protected void putToModuleCache(Replacements r, HolderLookup.RegistryLookup<Item> registryLookup) {
            ItemReplacementCache.putReplacement(buildReplacements(r), registryLookup);
        }

        @Override
        protected boolean tryResolveData(String id, HolderLookup.RegistryLookup<Item> registryLookup) {
            return Utils.getItemById(id) != null;
        }

        @Override
        protected boolean tryResolveTag(String tagId, HolderLookup.RegistryLookup<Item> registryLookup) {
            return Utils.isTagExists(ResourceLocation.parse(tagId), registryLookup);
        }

        @Override
        protected Replacements buildReplacements(Replacements r) {
            var dr = OEIConfig.getDefaultRules("oei");
            if (r.rules().isEmpty() && dr != null) {
                return new Replacements(r.match(), r.result(), Optional.of(dr.toRules()));
            }
            return r;
        }

        @Override
        protected boolean acceptLocation(ResourceLocation location) {
            return "oei".equals(location.getNamespace());
        }
    }
}
