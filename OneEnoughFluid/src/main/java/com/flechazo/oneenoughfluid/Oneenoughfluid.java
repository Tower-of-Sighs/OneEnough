package com.flechazo.oneenoughfluid;

import com.flechazo.oneenoughfluid.api.adapter.FluidDomainAdapter;
import com.flechazo.oneenoughfluid.data.FluidReplacementValidator;
import com.flechazo.oneenoughfluid.init.OEFReplacementStrategy;
import cc.sighs.oelib.data.DataRegistry;
import com.mafuyu404.oneenoughitem.api.DomainRegistry;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.util.MixinUtils;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Oneenoughfluid.MODID)
public class Oneenoughfluid {
    public static final String MODID = "oneenoughfluid";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Oneenoughfluid() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        var context = ModLoadingContext.get();
        modEventBus.register(this);
        MixinUtils.setStrategy(new OEFReplacementStrategy());

        DomainRegistry.register(new FluidDomainAdapter());
        DataRegistry.registerWithNamespaces(Replacements.class, Replacements.CODEC, "oef");
        DataRegistry.registerNamespaceValidator(Replacements.class, "oef", FluidReplacementValidator.class);
    }
}
