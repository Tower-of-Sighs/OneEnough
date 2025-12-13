package com.sighs.oneenoughblock;

import com.mafuyu404.oelib.data.DataRegistry;
import com.mafuyu404.oneenoughitem.api.DomainRegistry;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.sighs.oneenoughblock.api.adapter.BlockDomainAdapter;
import com.sighs.oneenoughblock.data.BlockReplacementValidator;
import com.sighs.oneenoughblock.init.OEBConfig;
import net.neoforged.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(Oneenoughblock.MODID)
public class Oneenoughblock {

    public static final String MODID = "oneenoughblock";

    public static final Logger LOGGER = LogManager.getLogger();

    public Oneenoughblock() {
        OEBConfig.getInstance();
        DomainRegistry.register(new BlockDomainAdapter());
        DataRegistry.registerWithNamespaces(Replacements.class, Replacements.CODEC, "oeb");
        DataRegistry.registerNamespaceValidator(Replacements.class, "oeb", BlockReplacementValidator.class);
    }
}
