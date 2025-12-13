package com.flechazo.oneenoughfluid.event;

import com.flechazo.oneenoughfluid.Oneenoughfluid;
import com.flechazo.oneenoughfluid.client.gui.cache.GlobalFluidReplacementCache;
import com.flechazo.oneenoughfluid.init.FluidReplacementCache;
import com.mafuyu404.oelib.neoforge.data.DataManager;
import com.mafuyu404.oelib.neoforge.event.DataReloadEvent;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.event.base.AbstractReplacementEventHandler;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Optional;

@EventBusSubscriber(modid = Oneenoughfluid.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onDataReload(DataReloadEvent event) {
        if (event.isDataType(Replacements.class)) {
            HolderLookup.RegistryLookup<Fluid> registryLookup = Minecraft.getInstance().level != null
                    ? Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.FLUID)
                    : null;
            Minecraft.getInstance().execute(() -> {
                HANDLER.rebuildReplacementCache("oef-client-data-reload", DataManager.get(Replacements.class), registryLookup);
                GlobalFluidReplacementCache.get().rebuild();
                FluidReplacementCache.endReloadOverride();
            });
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