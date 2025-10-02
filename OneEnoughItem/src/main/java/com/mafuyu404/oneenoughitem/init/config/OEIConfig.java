package com.mafuyu404.oneenoughitem.init.config;

import com.iafenvoy.jupiter.config.entry.BooleanEntry;
import com.iafenvoy.jupiter.interfaces.IConfigEntry;

import java.util.Optional;

public class OEIConfig extends DomainConfig {
    public static final IConfigEntry<Boolean> DEEPER_REPLACE =
            new BooleanEntry("config.oei.common.deeper_replace", false).json("Deeper_Replace");
    public static final IConfigEntry<Boolean> ENABLE_LITE =
            new BooleanEntry("config.oei.common.enable_lite", false).json("Enable_Lite");
    public static final IConfigEntry<Boolean> CLEAR_FOOD_PROPERTIES =
            new BooleanEntry("config.oei.common.clear_food_properties", false).json("Clear_Food_Properties");

    private static final OEIConfig INSTANCE = new OEIConfig();

    private OEIConfig() {
        super("oei", "common.json", new DefaultRules(Optional.empty(), Optional.empty()));
    }

    @Override
    public void init() {
        var tab = this.createTab("common", "config.oei.common.category.common");
        tab.add(DEEPER_REPLACE);
        tab.add(ENABLE_LITE);
        tab.add(CLEAR_FOOD_PROPERTIES);
        tab.add(DEFAULT_RULES);
    }

    public static OEIConfig getInstance() {
        return INSTANCE;
    }
}