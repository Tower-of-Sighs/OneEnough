package com.sighs.oneenoughblock;

import cc.sighs.oelib.config.ui.screen.ConfigScreen;
import cc.sighs.oelib.data.DataRegistry;
import com.mafuyu404.oneenoughitem.api.DomainRegistry;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.sighs.oneenoughblock.api.adapter.BlockDomainAdapter;
import com.sighs.oneenoughblock.data.BlockReplacementValidator;
import com.sighs.oneenoughblock.init.OEBConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Oneenoughblock.MODID)
public class Oneenoughblock {

    public static final String MODID = "oneenoughblock";

    public static final Logger LOGGER = LogManager.getLogger();

    public Oneenoughblock() {
        OEBConfig.register();
        DomainRegistry.register(new BlockDomainAdapter());
        var context = ModLoadingContext.get();
        if (FMLEnvironment.dist.equals(Dist.CLIENT)) {
            context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(((minecraft, screen) -> new ConfigScreen(screen, "oeb"))));
        }
        DataRegistry.registerWithNamespaces(Replacements.class, Replacements.CODEC, "oeb");
        DataRegistry.registerNamespaceValidator(Replacements.class, "oeb", BlockReplacementValidator.class);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
    }
}
