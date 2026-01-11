package com.flechazo.oneenoughfluid.init;

import cc.sighs.oelib.config.ConfigManager;
import cc.sighs.oelib.config.ConfigRecordCodecBuilder;
import cc.sighs.oelib.config.ConfigUnit;
import cc.sighs.oelib.config.field.ConfigField;
import cc.sighs.oelib.config.model.ConfigStorageFormat;
import cc.sighs.oelib.config.datafix.ConfigFixRegistry;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.Optional;
import java.util.HashMap;

public record OEFConfig(
        OEIConfig.DefaultRules defaultRules
) {
    private static final String FILE_NAME = "common";

    public static final ConfigUnit<OEFConfig> UNIT = ConfigRecordCodecBuilder.create(
            ResourceLocation.fromNamespaceAndPath("oef", "common_config"),
            instance -> instance.group(
                    ConfigField.map("Default_Rules_data", Codec.STRING, Replacements.ProcessingMode.CODEC)
                            .defaultValue(new HashMap<>())
                            .tooltip()
                            .comment("config.oef.default_rules.data")
                            .forGetter(cfg -> cfg.defaultRules().data().orElseGet(HashMap::new)),
                    ConfigField.map("Default_Rules_tag", Codec.STRING, Replacements.ProcessingMode.CODEC)
                            .defaultValue(new HashMap<>())
                            .tooltip()
                            .comment("config.oef.default_rules.tag")
                            .forGetter(cfg -> cfg.defaultRules().tag().orElseGet(HashMap::new))
            ).apply(instance, (data, tag) ->
                    new OEFConfig(
                            new OEIConfig.DefaultRules(
                                    data.isEmpty() ? Optional.empty() : Optional.of(data),
                                    tag.isEmpty() ? Optional.empty() : Optional.of(tag)
                            )
                    )),
            meta -> meta
                    .directory("oef")
                    .fileName(FILE_NAME)
                    .format(ConfigStorageFormat.JSON)
    );

    public static OEFConfig get() {
        return UNIT.get();
    }

    public static void register() {
        ConfigFixRegistry.register(ResourceLocation.fromNamespaceAndPath("oef", "common_config"), 1, b ->
                b.fix(0, 1, dyn -> {
                    var ctx = new ConfigFixRegistry.FixContext(dyn);
                    dyn = ctx.rename("common.Default_Rules.data", "Default_Rules_data");
                    dyn = new ConfigFixRegistry.FixContext(dyn).rename("common.Default_Rules.tag", "Default_Rules_tag");
                    dyn = new ConfigFixRegistry.FixContext(dyn).rename("Default_Rules.data", "Default_Rules_data");
                    dyn = new ConfigFixRegistry.FixContext(dyn).rename("Default_Rules.tag", "Default_Rules_tag");
                    return dyn;
                })
        );
        ConfigManager.registerServer(UNIT, player -> player.hasPermissions(4));
    }
}
