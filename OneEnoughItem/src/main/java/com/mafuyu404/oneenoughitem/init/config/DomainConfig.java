package com.mafuyu404.oneenoughitem.init.config;

import com.iafenvoy.jupiter.ConfigManager;
import com.iafenvoy.jupiter.ServerConfigManager;
import com.iafenvoy.jupiter.config.container.AutoInitConfigContainer;
import com.iafenvoy.jupiter.interfaces.IConfigEntry;
import com.mafuyu404.oneenoughitem.Oneenoughitem;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class DomainConfig extends AutoInitConfigContainer {
    public final String domainId;

    public final IConfigEntry<DefaultRules> DEFAULT_RULES;

    private static final Map<String, DomainConfig> DOMAIN_CONFIGS = new HashMap<>();

    protected DomainConfig(String domainId, String configFileName, DefaultRules defaultRules) {
        super(
                new ResourceLocation(Oneenoughitem.MODID, domainId + "_common_config"),
                "config." + domainId + ".common.title",
                "./config/" + domainId + "/" + configFileName
        );
        this.domainId = domainId;
        this.DEFAULT_RULES = new DefaultRulesEntry(
                "config." + domainId + ".default_rules",
                defaultRules,
                DefaultRules.CODEC
        ).json("Default_Rules");

        ConfigManager.getInstance().registerConfigHandler(this);
        ServerConfigManager.registerServerConfig(this, ServerConfigManager.PermissionChecker.IS_OPERATOR);

        if (DOMAIN_CONFIGS.containsKey(domainId)) {
            throw new IllegalStateException("Domain config for '" + domainId + "' already registered!");
        }
        DOMAIN_CONFIGS.put(domainId, this);
    }

    @Override
    public void init() {
    }

    public static DomainConfig get(String domainId) {
        return DOMAIN_CONFIGS.get(domainId);
    }

    public static DefaultRules getDefaultRules(String domainId) {
        DomainConfig cfg = DOMAIN_CONFIGS.get(domainId);
        return cfg != null ? cfg.DEFAULT_RULES.getValue() : null;
    }

    public record DefaultRules(
            Optional<Map<String, Replacements.ProcessingMode>> data,
            Optional<Map<String, Replacements.ProcessingMode>> tag
    ) {
        public static final Codec<DefaultRules> CODEC = RecordCodecBuilder.create(instance ->
                instance.group(
                        Codec.unboundedMap(Codec.STRING, Replacements.ProcessingMode.CODEC)
                                .optionalFieldOf("data")
                                .forGetter(DefaultRules::data),
                        Codec.unboundedMap(Codec.STRING, Replacements.ProcessingMode.CODEC)
                                .optionalFieldOf("tag")
                                .forGetter(DefaultRules::tag)
                ).apply(instance, DefaultRules::new)
        );

        public Replacements.Rules toRules() {
            return new Replacements.Rules(this.data, this.tag);
        }
    }
}