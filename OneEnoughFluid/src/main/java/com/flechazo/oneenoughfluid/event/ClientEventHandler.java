package com.flechazo.oneenoughfluid.event;

import com.flechazo.oneenoughfluid.Oneenoughfluid;
import com.flechazo.oneenoughfluid.client.gui.cache.GlobalFluidReplacementCache;
import com.flechazo.oneenoughfluid.init.FluidReplacementCache;
import com.mafuyu404.oelib.forge.data.DataManager;
import com.mafuyu404.oelib.forge.event.DataReloadEvent;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.event.base.AbstractReplacementEventHandler;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = Oneenoughfluid.MODID, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onDataReload(DataReloadEvent event) {
        if (event.isDataType(Replacements.class)) {
            Minecraft.getInstance().execute(() -> {
                HANDLER.rebuildReplacementCache("oef-client-data-reload", DataManager.get(Replacements.class));
                GlobalFluidReplacementCache.get().rebuild();
                FluidReplacementCache.endReloadOverride();
            });
        }
    }

    private static final Handler HANDLER = new Handler();

    private static class Handler extends AbstractReplacementEventHandler {
        @Override
        protected void clearModuleCache() {
            FluidReplacementCache.clearCache();
        }

        @Override
        protected void putToModuleCache(Replacements r) {
            FluidReplacementCache.putReplacement(buildReplacements(r));
        }

        @Override
        protected boolean tryResolveData(String id) {
            return ForgeRegistries.FLUIDS.getValue(new ResourceLocation(id)) != null;
        }

        @Override
        protected boolean tryResolveTag(String tagId) {
            TagKey<Fluid> tag = TagKey.create(Registries.FLUID, new ResourceLocation(tagId));
            for (Fluid f : ForgeRegistries.FLUIDS) {
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