package com.sighs.oneenoughblock;

import net.minecraftforge.common.ForgeConfigSpec;

public class Config {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec.ConfigValue<Boolean> extendedBlockProperty;
    static final ForgeConfigSpec SPEC ;

    static {
        extendedBlockProperty = BUILDER.comment("是否在替换方块的时候保留方块状态")
                .define("extendedBlockProperty", false);
        SPEC = BUILDER.build();
    }
}
