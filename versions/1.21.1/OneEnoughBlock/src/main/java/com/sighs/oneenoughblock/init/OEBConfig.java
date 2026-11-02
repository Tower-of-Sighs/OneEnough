package com.sighs.oneenoughblock.init;

import cc.sighs.oelib.config.ConfigManager;
import cc.sighs.oelib.config.ConfigSchema;
import cc.sighs.oelib.config.ConfigUnit;
import cc.sighs.oelib.config.datafix.ConfigFixRegistry;
import cc.sighs.oelib.config.field.ConfigField;
import cc.sighs.oelib.config.model.ConfigStorageFormat;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.mojang.serialization.Codec;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Optional;
import java.util.Map;
import java.lang.invoke.MethodHandles;

public record OEBConfig(
        boolean replaceExistedBlock,
        boolean extendedBlockProperty,
        Map<String, Replacements.ProcessingMode> defaultRulesData,
        Map<String, Replacements.ProcessingMode> defaultRulesTag
) {
    private static final String FILE_NAME = "common";

    public static final ConfigUnit<OEBConfig> UNIT = ConfigSchema.defineServer(
            MethodHandles.lookup(),
            ResourceLocation.fromNamespaceAndPath("oeb", "common_config"),
            OEBConfig.class,
            meta -> meta
                    .directory("oeb")
                    .fileName(FILE_NAME)
                    .format(ConfigStorageFormat.JSON),
            instance -> instance.group(
                    ConfigField.bool("Replace_Existed_Block")
                            .defaultValue(false)
                            .tooltip()
                            .comment("config.oei.common.replace_existed_block")
                            .forGetter(OEBConfig::replaceExistedBlock),
                    ConfigField.bool("Extended_Block_Property")
                            .defaultValue(false)
                            .tooltip()
                            .forGetter(OEBConfig::extendedBlockProperty),
                    ConfigField.map("Default_Rules_data", Codec.STRING, Replacements.ProcessingMode.CODEC)
                            .defaultValue(new HashMap<>())
                            .tooltip()
                            .comment("config.oeb.default_rules.data")
                            .forGetter(OEBConfig::defaultRulesData),
                    ConfigField.map("Default_Rules_tag", Codec.STRING, Replacements.ProcessingMode.CODEC)
                            .defaultValue(new HashMap<>())
                            .tooltip()
                            .comment("config.oeb.default_rules.tag")
                            .forGetter(OEBConfig::defaultRulesTag)
            ).apply(instance, OEBConfig::new)
    );

    public OEBConfig(boolean replaceExistedBlock, boolean extendedBlockProperty, OEIConfig.DefaultRules defaultRules) {
        this(replaceExistedBlock, extendedBlockProperty,
                defaultRules.data().orElseGet(HashMap::new), defaultRules.tag().orElseGet(HashMap::new));
    }

    public OEIConfig.DefaultRules defaultRules() {
        return new OEIConfig.DefaultRules(
                defaultRulesData.isEmpty() ? Optional.empty() : Optional.of(defaultRulesData),
                defaultRulesTag.isEmpty() ? Optional.empty() : Optional.of(defaultRulesTag));
    }

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
