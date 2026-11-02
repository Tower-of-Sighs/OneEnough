package cc.sighs.oneenoughfluid;

import cc.sighs.oelib.data.DataRegistry;
import cc.sighs.oneenoughfluid.api.adapter.FluidDomainAdapter;
import cc.sighs.oneenoughfluid.data.FluidReplacementValidator;
import cc.sighs.oneenoughfluid.init.OEFConfig;
import cc.sighs.oneenoughfluid.init.OEFReplacementStrategy;
import cc.sighs.oneenoughitem.api.DomainRegistry;
import cc.sighs.oneenoughitem.data.Replacements;
import cc.sighs.oneenoughitem.util.MixinUtils;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(Oneenoughfluid.MODID)
public class Oneenoughfluid {
    public static final String MODID = "oneenoughfluid";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Oneenoughfluid(IEventBus modEventBus, ModContainer modContainer, Dist dist) {
        MixinUtils.setStrategy(new OEFReplacementStrategy());
        OEFConfig.register();
        DomainRegistry.register(new FluidDomainAdapter());
        DataRegistry.registerWithNamespaces(Replacements.class, Replacements.CODEC, "oef");
        DataRegistry.registerNamespaceValidator(Replacements.class, "oef", FluidReplacementValidator.class);
    }
}
