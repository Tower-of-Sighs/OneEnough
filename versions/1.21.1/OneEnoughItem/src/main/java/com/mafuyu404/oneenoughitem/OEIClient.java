package com.mafuyu404.oneenoughitem;

import cc.sighs.oelib.config.ui.screen.ConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Oneenoughitem.MODID, dist = Dist.CLIENT)
public class OEIClient {
    public OEIClient(IEventBus modEventBus, ModContainer modContainer) {
        ModLoadingContext.get().registerExtensionPoint(IConfigScreenFactory.class, () -> (minecraft, parent) -> new ConfigScreen(parent, "oei"));
    }
}
