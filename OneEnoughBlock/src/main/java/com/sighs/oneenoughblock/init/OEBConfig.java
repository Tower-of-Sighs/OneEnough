package com.sighs.oneenoughblock.init;

import com.iafenvoy.jupiter.config.entry.BooleanEntry;
import com.iafenvoy.jupiter.interfaces.IConfigEntry;
import com.mafuyu404.oneenoughitem.init.config.DomainConfig;

import java.util.Optional;

public class OEBConfig extends DomainConfig {

    public static final IConfigEntry<Boolean> REPLACE_EXISTED_BLOCK =
            new BooleanEntry("config.oei.common.replace_existed_block", false).json("Replace_Existed_Block");

    private static final OEBConfig INSTANCE = new OEBConfig();

    @Override
    public void init() {
        var tab = this.createTab("common", "config.oeb.common.category.common");
        tab.add(REPLACE_EXISTED_BLOCK);
        tab.add(DEFAULT_RULES);
    }

    public OEBConfig() {
        super("oeb", "common.json", new DefaultRules(Optional.empty(), Optional.empty()));
    }

    public static OEBConfig getInstance() {
        return INSTANCE;
    }
}
