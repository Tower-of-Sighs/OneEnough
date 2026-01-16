package com.flechazo.oneenoughfluid;

import cc.sighs.oelib.config.ui.screen.ConfigScreen;
import cc.sighs.oelib.data.DataRegistry;
import com.flechazo.oneenoughfluid.api.adapter.FluidDomainAdapter;
import com.flechazo.oneenoughfluid.data.FluidReplacementValidator;
import com.flechazo.oneenoughfluid.init.OEFConfig;
import com.flechazo.oneenoughfluid.init.OEFReplacementStrategy;
import com.mafuyu404.oneenoughitem.api.DomainRegistry;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.util.MixinUtils;
import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
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
