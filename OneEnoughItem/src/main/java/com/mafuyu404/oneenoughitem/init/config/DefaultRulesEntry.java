package com.mafuyu404.oneenoughitem.init.config;

import com.iafenvoy.jupiter.config.entry.BaseEntry;
import com.iafenvoy.jupiter.config.type.ConfigType;
import com.iafenvoy.jupiter.config.type.SingleConfigType;
import com.iafenvoy.jupiter.interfaces.IConfigEntry;
import com.mojang.serialization.Codec;

public class DefaultRulesEntry extends BaseEntry<DomainConfig.DefaultRules> {
    private final Codec<DomainConfig.DefaultRules> codec;

    public DefaultRulesEntry(String nameKey, DomainConfig.DefaultRules defaultValue, Codec<DomainConfig.DefaultRules> codec) {
        super(nameKey, defaultValue);
        this.codec = codec;
    }

    @Override
    public ConfigType<DomainConfig.DefaultRules> getType() {
        return new SingleConfigType<>();
    }

    @Override
    public IConfigEntry<DomainConfig.DefaultRules> newInstance() {
        return new DefaultRulesEntry(this.nameKey, this.defaultValue, this.codec)
                .visible(this.visible)
                .json(this.jsonKey);
    }

    public Codec<DomainConfig.DefaultRules> getCodec() {
        return codec;
    }
}