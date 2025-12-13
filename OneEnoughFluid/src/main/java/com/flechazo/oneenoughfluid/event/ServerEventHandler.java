package com.flechazo.oneenoughfluid.event;

import com.flechazo.oneenoughfluid.Oneenoughfluid;
import com.flechazo.oneenoughfluid.init.FluidReplacementCache;
import com.mafuyu404.oelib.neoforge.data.DataManager;
import com.mafuyu404.oelib.neoforge.event.DataReloadEvent;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.event.base.AbstractReplacementEventHandler;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

import java.util.Optional;

@EventBusSubscriber(modid = Oneenoughfluid.MODID)
public class ServerEventHandler {

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        var server = event.getServer();
        var registryLookup = server.registryAccess().lookupOrThrow(Registries.FLUID);
        HANDLER.rebuildReplacementCache("oef-server-start", DataManager.get(Replacements.class), registryLookup);
    }

    @SubscribeEvent
    public static void onDataReload(DataReloadEvent event) {
        if (event.isDataType(Replacements.class)) {
            var server = DataManager.getCurrentServer();
            var registryLookup = server != null ? server.registryAccess().lookupOrThrow(Registries.FLUID) : null;
            HANDLER.rebuildReplacementCache("oef-server-data-reload", DataManager.get(Replacements.class), registryLookup);
            FluidReplacementCache.endReloadOverride();
        }
    }

    private static final Handler HANDLER = new Handler();

    private static class Handler extends AbstractReplacementEventHandler<Fluid> {
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
            BuiltInRegistries.FLUID.get(ResourceLocation.parse(id));
            return true;
        }

        @Override
        protected boolean tryResolveTag(String tagId, HolderLookup.RegistryLookup<Fluid> registryLookup) {
            TagKey<Fluid> tag = TagKey.create(Registries.FLUID, ResourceLocation.parse(tagId));
            for (Fluid f : BuiltInRegistries.FLUID) {
                if (f.builtInRegistryHolder().is(tag)) return true;
            }
            return false;
        }

        @Override
        protected Replacements buildReplacements(Replacements r) {
            var dr = OEIConfig.getDefaultRules("oef");
            if (r.rules().isEmpty() && dr != null) {
                return new Replacements(r.match(), r.result(), Optional.of(dr.toRules()));
            }
            return r;
        }
    }
}