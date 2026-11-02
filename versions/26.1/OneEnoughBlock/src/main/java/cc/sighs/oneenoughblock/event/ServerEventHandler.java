package cc.sighs.oneenoughblock.event;

import cc.sighs.oelib.data.neoforge.DataManager;
import cc.sighs.oelib.event.neoforge.DataReloadEvent;
import cc.sighs.oneenoughitem.data.Replacements;
import cc.sighs.oneenoughitem.event.base.AbstractReplacementEventHandler;
import cc.sighs.oneenoughblock.init.BlockReplacementCache;
import cc.sighs.oneenoughblock.init.OEBConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.Optional;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@EventBusSubscriber(modid = "oneenoughblock")
public class ServerEventHandler {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        var registryLookup = server.registryAccess().lookupOrThrow(Registries.BLOCK);
        HANDLER.rebuildServerReplacementCache("oeb-server-start", DataManager.get(Replacements.class), registryLookup);
    }


    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) return;
        HANDLER.rebuildServerReplacementCache("oeb-tags-ready", DataManager.get(Replacements.class), event.getLookupProvider().lookupOrThrow(Registries.BLOCK));
        BlockReplacementCache.endReloadOverride();
    }

    @SubscribeEvent
    public static void onDataReload(DataReloadEvent event) {
        if (event.isDataType(Replacements.class)) {
            var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            var registryLookup = server != null ? server.registryAccess().lookupOrThrow(Registries.BLOCK) : null;
            HANDLER.rebuildReplacementCache("oeb-server-data-reload", DataManager.get(Replacements.class), registryLookup);
        }
    }

    private static final Handler HANDLER = new Handler();

    private static class Handler extends AbstractReplacementEventHandler<Block> {
        @Override
        protected boolean acceptLocation(Identifier location) {
            return "oeb".equals(location.getNamespace());
        }

        @Override
        protected void clearModuleCache() {
            BlockReplacementCache.clearCache();
        }

        @Override
        protected void putToModuleCache(Replacements r, HolderLookup.RegistryLookup<Block> registryLookup) {
            BlockReplacementCache.putReplacement(buildReplacements(r));
        }

        @Override
        protected boolean tryResolveData(String id, HolderLookup.RegistryLookup<Block> registryLookup) {
            return BuiltInRegistries.BLOCK.containsKey(Identifier.parse(id));
        }

        @Override
        protected boolean tryResolveTag(String tagId, HolderLookup.RegistryLookup<Block> registryLookup) {
            TagKey<Block> tag = TagKey.create(Registries.BLOCK, Identifier.parse(tagId));
            for (Block b : BuiltInRegistries.BLOCK) {
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