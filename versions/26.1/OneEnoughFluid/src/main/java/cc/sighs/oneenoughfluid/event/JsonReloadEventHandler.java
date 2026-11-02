package cc.sighs.oneenoughfluid.event;
import cc.sighs.oneenoughfluid.Oneenoughfluid;
import cc.sighs.oneenoughfluid.init.FluidReplacementCache;
import cc.sighs.oneenoughfluid.init.OEFConfig;
import cc.sighs.oneenoughfluid.init.OEFReplacementStrategy;
import cc.sighs.oneenoughitem.event.RawJsonReloadEvent;
import cc.sighs.oneenoughitem.util.JsonReloadMixinHelper;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = Oneenoughfluid.MODID)
public final class JsonReloadEventHandler {
    @SubscribeEvent
    public static void rewriteFluidJson(RawJsonReloadEvent event) {
        JsonReloadMixinHelper.processJsonReload(event.directory, event.resourceManager, event.results,
                new OEFReplacementStrategy(), "recipe".equals(event.directory) ? FluidReplacementCache::beginReloadOverride : null,
                FluidReplacementCache::hasAnyMappings, modId -> OEFConfig.get().defaultRules().toRules(), "oef");
    }
}
