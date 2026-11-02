package cc.sighs.oneenoughblock.event;

import cc.sighs.oelib.data.neoforge.DataManager;
import cc.sighs.oelib.event.neoforge.DataReloadEvent;
import cc.sighs.oneenoughitem.data.Replacements;
import cc.sighs.oneenoughitem.event.base.AbstractReplacementEventHandler;
import cc.sighs.oneenoughblock.client.gui.cache.GlobalBlockReplacementCache;
import cc.sighs.oneenoughblock.init.BlockReplacementCache;
import cc.sighs.oneenoughblock.init.OEBConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Optional;

@EventBusSubscriber(modid = "oneenoughblock", value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void onDataReload(DataReloadEvent event) {
        if (event.isDataType(Replacements.class)) {
            HolderLookup.RegistryLookup<Block> registryLookup = Minecraft.getInstance().level != null
                    ? Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.BLOCK)
                    : null;
            Minecraft.getInstance().execute(() -> {
            HANDLER.rebuildReplacementCache("oeb-client-data-reload", DataManager.get(Replacements.class), registryLookup);
            GlobalBlockReplacementCache.get().rebuild();
            BlockReplacementCache.endReloadOverride();
            });
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