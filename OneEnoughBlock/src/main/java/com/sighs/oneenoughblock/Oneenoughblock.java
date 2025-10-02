package com.sighs.oneenoughblock;

import com.mafuyu404.oelib.forge.data.DataRegistry;
import com.mafuyu404.oneenoughitem.api.DomainRegistry;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.sighs.oneenoughblock.api.adapter.BlockDomainAdapter;
import com.sighs.oneenoughblock.data.BlockReplacementValidator;
import com.sighs.oneenoughblock.init.OEBConfig;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(Oneenoughblock.MODID)
public class Oneenoughblock {

    public static final String MODID = "oneenoughblock";

    public Oneenoughblock() {
        OEBConfig.getInstance();
        DomainRegistry.register(new BlockDomainAdapter());
        DataRegistry.registerWithNamespaces(Replacements.class, "oeb");
        DataRegistry.registerNamespaceValidator(Replacements.class, "oeb", BlockReplacementValidator.class);
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.register(this);
    }
}
