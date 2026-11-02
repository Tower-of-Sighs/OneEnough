package cc.sighs.oneenoughblock;

import cc.sighs.oelib.data.DataRegistry;
import cc.sighs.oneenoughitem.api.DomainRegistry;
import cc.sighs.oneenoughitem.data.Replacements;
import cc.sighs.oneenoughblock.api.adapter.BlockDomainAdapter;
import cc.sighs.oneenoughblock.data.BlockReplacementValidator;
import cc.sighs.oneenoughblock.init.OEBConfig;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Oneenoughblock.MODID)
public class Oneenoughblock {

    public static final String MODID = "oneenoughblock";

    public static final Logger LOGGER = LogManager.getLogger();

    public Oneenoughblock(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        OEBConfig.register();
        DomainRegistry.register(new BlockDomainAdapter());
        DataRegistry.registerWithNamespaces(Replacements.class, Replacements.CODEC, "oeb");
        DataRegistry.registerNamespaceValidator(Replacements.class, "oeb", BlockReplacementValidator.class);
    }
}
