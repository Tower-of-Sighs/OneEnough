package com.flechazo.oneenoughfluid;

import cc.sighs.oelib.config.ui.screen.ConfigScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = Oneenoughfluid.MODID, dist = Dist.CLIENT)
public class OEFClient {
    public OEFClient(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (minecraft, parent) -> new ConfigScreen(parent, "oef"));
    }
}
