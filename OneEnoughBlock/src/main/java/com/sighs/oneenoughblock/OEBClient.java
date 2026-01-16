package com.sighs.oneenoughblock;

import cc.sighs.oelib.config.ui.screen.ConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Oneenoughblock.MODID, dist = Dist.CLIENT)
public class OEBClient {
    public OEBClient(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, parent) -> new ConfigScreen(parent, "oeb"));
    }
}
