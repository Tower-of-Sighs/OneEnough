package com.flechazo.oneenoughfluid;

import cc.sighs.oelib.config.ui.screen.ConfigScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = Oneenoughfluid.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class OneenoughfluidClient {
    @SubscribeEvent
    public static void registerConfigScreen(FMLClientSetupEvent event) {
        var context = ModLoadingContext.get();
        context.registerExtensionPoint(ConfigScreenHandler.ConfigScreenFactory.class, () -> new ConfigScreenHandler.ConfigScreenFactory(((minecraft, screen) -> new ConfigScreen(screen, "oef"))));
    }
}
