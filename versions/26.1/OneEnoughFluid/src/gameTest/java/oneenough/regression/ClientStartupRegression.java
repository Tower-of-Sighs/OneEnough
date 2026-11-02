package oneenough.regression;

import cc.sighs.oelib.renderer.FluidRenderers;
import cc.sighs.oneenoughitem.api.DomainRegistry;
import cc.sighs.oneenoughitem.client.ModKeyMappings;
import cc.sighs.oneenoughitem.client.gui.ReplacementEditorScreen;
import cc.sighs.oneenoughfluid.client.gui.FluidSelectionScreen;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;

@EventBusSubscriber(modid = "oneenough_regression", value = Dist.CLIENT)
public final class ClientStartupRegression {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        if (!Boolean.getBoolean("oneenough.runClientRegressions")) return;
        event.enqueueWork(() -> {
            try {
                if (DomainRegistry.all().size() != 3) throw new AssertionError("Expected all three replacement domains");
                if (ModKeyMappings.OPEN_EDITOR == null) throw new AssertionError("Editor key was not initialized");
                if (FluidRenderers.of(Fluids.WATER) == null) throw new AssertionError("Fluid renderer reference was not created");
                new FluidSelectionScreen(new ReplacementEditorScreen(), true);
                System.out.println("PASS: 26.1 client startup, replacement domains and editor construction");
                System.exit(0);
            } catch (Throwable error) {
                error.printStackTrace();
                System.exit(1);
            }
        });
    }
}
