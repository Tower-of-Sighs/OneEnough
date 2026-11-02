package cc.sighs.oneenoughfluid.event;

import cc.sighs.oelib.data.neoforge.DataManager;
import cc.sighs.oelib.event.neoforge.DataReloadEvent;
import cc.sighs.oneenoughfluid.Oneenoughfluid;
import cc.sighs.oneenoughfluid.init.FluidReplacementCache;
import cc.sighs.oneenoughfluid.init.OEFConfig;
import cc.sighs.oneenoughitem.data.Replacements;
import cc.sighs.oneenoughitem.event.base.AbstractReplacementEventHandler;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.Optional;
import net.neoforged.neoforge.event.TagsUpdatedEvent;

@EventBusSubscriber(modid = Oneenoughfluid.MODID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        var registryLookup = server.registryAccess().lookupOrThrow(Registries.FLUID);
        HANDLER.rebuildServerReplacementCache("oef-server-start", DataManager.get(Replacements.class), registryLookup);
    }


    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        if (event.getUpdateCause() != TagsUpdatedEvent.UpdateCause.SERVER_DATA_LOAD) return;
        HANDLER.rebuildServerReplacementCache("oef-tags-ready", DataManager.get(Replacements.class), event.getLookupProvider().lookupOrThrow(Registries.FLUID));
        FluidReplacementCache.endReloadOverride();
    }

    @SubscribeEvent
    public static void onDataReload(DataReloadEvent event) {
        if (event.isDataType(Replacements.class)) {
            var server = net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
            var registryLookup = server != null ? server.registryAccess().lookupOrThrow(Registries.FLUID) : null;
            HANDLER.rebuildReplacementCache("oef-server-data-reload", DataManager.get(Replacements.class), registryLookup);
            FluidReplacementCache.endReloadOverride();
        }
    }

    private static final Handler HANDLER = new Handler();

    private static class Handler extends AbstractReplacementEventHandler<Fluid> {
        @Override
        protected boolean acceptLocation(Identifier location) { return "oef".equals(location.getNamespace()); }

        @Override
        protected void clearModuleCache() {
            FluidReplacementCache.clearCache();
        }

        @Override
        protected void putToModuleCache(Replacements r, HolderLookup.RegistryLookup<Fluid> registryLookup) {
            FluidReplacementCache.putReplacement(buildReplacements(r));
        }

        @Override
        protected boolean tryResolveData(String id, HolderLookup.RegistryLookup<Fluid> registryLookup) {
            BuiltInRegistries.FLUID.getValue(Identifier.parse(id));
            return true;
        }

        @Override
        protected boolean tryResolveTag(String tagId, HolderLookup.RegistryLookup<Fluid> registryLookup) {
            TagKey<Fluid> tag = TagKey.create(Registries.FLUID, Identifier.parse(tagId));
            for (Fluid f : BuiltInRegistries.FLUID) {
                if (f.builtInRegistryHolder().is(tag)) return true;
            }
            return false;
        }

        @Override
        protected Replacements buildReplacements(Replacements r) {
            var dr = OEFConfig.get();
            if (r.rules().isEmpty() && dr != null) {
                return new Replacements(r.match(), r.result(), Optional.of(dr.defaultRules().toRules()));
            }
            return r;
        }
    }
}