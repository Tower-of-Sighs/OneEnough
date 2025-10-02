package com.flechazo.oneenoughfluid.init;

import com.mafuyu404.oneenoughitem.init.config.DomainConfig;

import java.util.Optional;

public class OEFConfig extends DomainConfig {
    protected OEFConfig() {
        super("oef", "common.json", new DefaultRules(Optional.empty(), Optional.empty()));
    }

    private static final OEFConfig INSTANCE = new OEFConfig();

    @Override
    public void init() {
        var tab = this.createTab("common", "config.oef.common.category.common");
        tab.add(DEFAULT_RULES);
    }

    public static OEFConfig getInstance() {
        return INSTANCE;
    }
}
