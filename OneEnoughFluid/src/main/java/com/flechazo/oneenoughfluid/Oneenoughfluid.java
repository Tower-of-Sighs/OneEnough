package com.flechazo.oneenoughfluid;

import com.flechazo.oneenoughfluid.api.adapter.FluidDomainAdapter;
import com.flechazo.oneenoughfluid.data.FluidReplacementValidator;
import com.flechazo.oneenoughfluid.init.OEFConfig;
import com.flechazo.oneenoughfluid.init.OEFReplacementStrategy;
import com.mafuyu404.oelib.forge.data.DataRegistry;
import com.mafuyu404.oneenoughitem.api.DomainRegistry;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.util.MixinUtils;
import com.mojang.logging.LogUtils;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Oneenoughfluid.MODID)
public class Oneenoughfluid {
    public static final String MODID = "oneenoughfluid";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Oneenoughfluid() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
        MixinUtils.setStrategy(new OEFReplacementStrategy());
        OEFConfig.getInstance();

        DomainRegistry.register(new FluidDomainAdapter());

        DataRegistry.registerWithNamespaces(Replacements.class, "oef");
        DataRegistry.registerNamespaceValidator(Replacements.class, "oef", FluidReplacementValidator.class);
    }
}
