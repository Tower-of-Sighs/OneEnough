package cc.sighs.oneenoughitem;

import cc.sighs.oelib.data.DataRegistry;
import cc.sighs.oneenoughitem.api.DomainRegistry;
import cc.sighs.oneenoughitem.api.adapter.ItemDomainAdapter;
import cc.sighs.oneenoughitem.data.ItemReplacementValidator;
import cc.sighs.oneenoughitem.data.Replacements;
import cc.sighs.oneenoughitem.init.OEIReplacementStrategy;
import cc.sighs.oneenoughitem.init.config.OEIConfig;
import cc.sighs.oneenoughitem.util.MixinUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Oneenoughitem.MODID)
public class Oneenoughitem {
    public static final String MODID = "oneenoughitem";
    public static final Logger LOGGER = LogManager.getLogger();

    public Oneenoughitem(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        MixinUtils.setStrategy(new OEIReplacementStrategy());
        OEIConfig.register();
        DomainRegistry.register(new ItemDomainAdapter());
        DataRegistry.registerWithNamespaces(Replacements.class, Replacements.CODEC, "oei");
        DataRegistry.registerNamespaceValidator(Replacements.class, "oei", ItemReplacementValidator.class);
    }
}
