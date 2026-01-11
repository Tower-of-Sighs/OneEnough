package com.sighs.oneenoughblock.init;


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

public record OEBConfig(
        boolean replaceExistedBlock,
        OEIConfig.DefaultRules defaultRules
) {
    private static final String FILE_NAME = "common";

    public static final ConfigUnit<OEBConfig> UNIT = ConfigRecordCodecBuilder.create(
            ResourceLocation.fromNamespaceAndPath("oeb", "common_config"),
            instance -> instance.group(
                    ConfigField.bool("Replace_Existed_Block")
                            .defaultValue(false)
                            .tooltip()
                            .comment("config.oei.common.replace_existed_block")
                            .forGetter(OEBConfig::replaceExistedBlock),
                    ConfigField.map("Default_Rules_data", Codec.STRING, Replacements.ProcessingMode.CODEC)
                            .defaultValue(new HashMap<>())
                            .tooltip()
                            .comment("config.oeb.default_rules.data")
                            .forGetter(cfg -> cfg.defaultRules().data().orElseGet(HashMap::new)),
                    ConfigField.map("Default_Rules_tag", Codec.STRING, Replacements.ProcessingMode.CODEC)
                            .defaultValue(new HashMap<>())
                            .tooltip()
                            .comment("config.oeb.default_rules.tag")
                            .forGetter(cfg -> cfg.defaultRules().tag().orElseGet(HashMap::new))
            ).apply(instance, (replaceExistedBlock, data, tag) ->
                    new OEBConfig(
                            replaceExistedBlock,
                            new OEIConfig.DefaultRules(
                                    data.isEmpty() ? Optional.empty() : Optional.of(data),
                                    tag.isEmpty() ? Optional.empty() : Optional.of(tag)
                            )
                    )),
            meta -> meta
                    .directory("oeb")
                    .fileName(FILE_NAME)
                    .format(ConfigStorageFormat.JSON)
    );

    public static OEBConfig get() {
        return UNIT.get();
    }

    public static void register() {
        ConfigFixRegistry.register(ResourceLocation.fromNamespaceAndPath("oeb", "common_config"), 1, b ->
                b.fix(0, 1, dyn -> {
                    var ctx = new ConfigFixRegistry.FixContext(dyn);
                    dyn = ctx.rename("common.Replace_Existed_Block", "Replace_Existed_Block");
                    dyn = new ConfigFixRegistry.FixContext(dyn).rename("common.Default_Rules.data", "Default_Rules_data");
                    dyn = new ConfigFixRegistry.FixContext(dyn).rename("common.Default_Rules.tag", "Default_Rules_tag");
                    dyn = new ConfigFixRegistry.FixContext(dyn).rename("Default_Rules.data", "Default_Rules_data");
                    dyn = new ConfigFixRegistry.FixContext(dyn).rename("Default_Rules.tag", "Default_Rules_tag");
                    return dyn;
                })
        );
        ConfigManager.registerServer(UNIT, player -> player.hasPermissions(4));
    }
}
