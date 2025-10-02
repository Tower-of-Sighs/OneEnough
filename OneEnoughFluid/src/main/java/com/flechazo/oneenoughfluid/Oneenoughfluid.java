package com.flechazo.oneenoughfluid;

import com.flechazo.oneenoughfluid.api.adapter.FluidDomainAdapter;
import com.flechazo.oneenoughfluid.data.FluidReplacementValidator;
import com.flechazo.oneenoughfluid.init.OEFConfig;
import com.mafuyu404.oelib.forge.data.DataRegistry;
import com.mafuyu404.oneenoughitem.api.DomainRegistry;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Oneenoughfluid.MODID)
public class Oneenoughfluid {
    public static final String MODID = "oneenoughfluid";
    private static final Logger LOGGER = LogUtils.getLogger();

    public Oneenoughfluid() {
        OEFConfig.getInstance();
        DomainRegistry.register(new FluidDomainAdapter());
        DataRegistry.registerWithNamespaces(Replacements.class, "oef");
        DataRegistry.registerNamespaceValidator(Replacements.class, "oef", FluidReplacementValidator.class);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
    }
}
