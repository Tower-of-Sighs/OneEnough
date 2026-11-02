package cc.sighs.oneenoughitem.event;

import cc.sighs.oneenoughitem.data.Replacements;
import cc.sighs.oelib.data.neoforge.DataManager;
import cc.sighs.oelib.event.neoforge.DataReloadEvent;
import cc.sighs.oneenoughitem.Oneenoughitem;
import cc.sighs.oneenoughitem.client.ModKeyMappings;
import cc.sighs.oneenoughitem.client.gui.ReplacementEditorScreen;
import cc.sighs.oneenoughitem.client.gui.cache.ItemGlobalReplacementCache;
import cc.sighs.oneenoughitem.event.base.AbstractReplacementEventHandler;
import cc.sighs.oneenoughitem.init.ItemReplacementCache;
import cc.sighs.oneenoughitem.init.access.CreativeModeTabIconRefresher;
import cc.sighs.oneenoughitem.init.config.OEIConfig;
import cc.sighs.oneenoughitem.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.lwjgl.glfw.GLFW;

import java.util.Optional;

@EventBusSubscriber(modid = Oneenoughitem.MODID, value = Dist.CLIENT)
public class ClientEventHandler {

    private static final Handler HANDLER = new Handler();

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (ModKeyMappings.OPEN_EDITOR.consumeClick()) {
            if (mc.screen == null && hasCtrlDown()) {
                mc.setScreen(new ReplacementEditorScreen());
            }
        }
    }

    @SubscribeEvent
    public static void onDataReload(DataReloadEvent event) {
        if (event.isDataType(Replacements.class)) {
            Minecraft.getInstance().execute(() -> {
                HolderLookup.RegistryLookup<Item> registryLookup = Minecraft.getInstance().level != null
                        ? Minecraft.getInstance().level.registryAccess().lookupOrThrow(Registries.ITEM)
                        : null;

                HANDLER.rebuildReplacementCache("client-data-reload", DataManager.get(Replacements.class), registryLookup);
                ItemGlobalReplacementCache.get().rebuild();

                refreshAllCreativeModeTabIcons();
                Oneenoughitem.LOGGER.info("Replacement cache rebuilt due to data reload: {} entries loaded, {} invalid",
                        event.getLoadedCount(), event.getInvalidCount());

                ItemReplacementCache.endReloadOverride();
            });
        }
    }

    private static boolean hasCtrlDown() {
        long window = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    private static void refreshAllCreativeModeTabIcons() {
        try {
            for (var tab : CreativeModeTabs.tabs()) {
                if (tab instanceof CreativeModeTabIconRefresher refresher) {
                    refresher.oei$refreshIconCache();
                }
            }
            Oneenoughitem.LOGGER.info("Refreshed creative mode tab icons after replacement reload");
        } catch (Exception e) {
            Oneenoughitem.LOGGER.warn("Failed to refresh creative mode tab icons", e);
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
            return Utils.isTagExists(Identifier.parse(tagId), registryLookup);
        }

        @Override
        protected Replacements buildReplacements(Replacements r) {
            var dr = OEIConfig.get();
            if (r.rules().isEmpty() && dr != null) {
                return new Replacements(r.match(), r.result(), Optional.of(dr.defaultRules().toRules()));
            }
            return r;
        }

        @Override
        protected boolean acceptLocation(Identifier location) {
            return "oei".equals(location.getNamespace());
        }
    }
}
