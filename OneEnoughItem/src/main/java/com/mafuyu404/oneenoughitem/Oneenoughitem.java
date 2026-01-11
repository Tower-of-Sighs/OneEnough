package com.mafuyu404.oneenoughitem;

import cc.sighs.oelib.config.ui.screen.ConfigScreen;
import cc.sighs.oelib.data.DataRegistry;
import com.mafuyu404.oneenoughitem.api.DomainRegistry;
import com.mafuyu404.oneenoughitem.api.adapter.ItemDomainAdapter;
import com.mafuyu404.oneenoughitem.data.ItemReplacementValidator;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.OEIReplacementStrategy;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.mafuyu404.oneenoughitem.util.MixinUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
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
        modEventBus.addListener(this::setup);
        if (dist == Dist.CLIENT) {
            modContainer.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, parent) -> new ConfigScreen(parent, "oei"));
        }
    }

    private void setup(final FMLCommonSetupEvent event) {
        DataRegistry.registerWithNamespaces(Replacements.class, Replacements.CODEC, "oei");
        DataRegistry.registerNamespaceValidator(Replacements.class, "oei", ItemReplacementValidator.class);
        LOGGER.info("OneEnoughItem initialized with OELib data-driven framework");
    }
}
