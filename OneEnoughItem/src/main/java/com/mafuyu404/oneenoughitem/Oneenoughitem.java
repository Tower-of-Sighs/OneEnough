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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Oneenoughitem.MODID)
public class Oneenoughitem {
    public static final String MODID = "oneenoughitem";
    public static final Logger LOGGER = LogManager.getLogger();
    public Oneenoughitem() {
        MixinUtils.setStrategy(new OEIReplacementStrategy());
        OEIConfig.register();
        DomainRegistry.register(new ItemDomainAdapter());
        DataRegistry.registerWithNamespaces(Replacements.class, Replacements.CODEC, "oei");
        DataRegistry.registerNamespaceValidator(Replacements.class, "oei", ItemReplacementValidator.class);
    }
}