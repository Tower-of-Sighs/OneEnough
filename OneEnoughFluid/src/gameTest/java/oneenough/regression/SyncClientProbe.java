package oneenough.regression;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;

@Mod.EventBusSubscriber(modid="oneenough_regression", value=Dist.CLIENT)
public final class SyncClientProbe {
    private static int connectedTicks;
    private static int completedPhase;
    private static int phaseTicks;
    private static int currentPhase;
    private static boolean failed;
    @SubscribeEvent
    public static void tick(TickEvent.ClientTickEvent event) {
        if (!SyncProbe.ENABLED || event.phase != TickEvent.Phase.END) return;
        var mc = Minecraft.getInstance();
        int client = Integer.getInteger("oneenough.syncProbeClient", 0);
        if (SyncProbe.exists("stop.txt")) { mc.stop(); return; }
        if (failed || mc.level == null || mc.player == null || mc.getConnection() == null) return;
        connectedTicks++;
        if (connectedTicks % 20 != 0 || !SyncProbe.exists("phase.txt")) return;
        int phase = Integer.parseInt(SyncProbe.read("phase.txt").trim());
        if (phase == completedPhase) return;
        if (phase != currentPhase) { currentPhase=phase; phaseTicks=0; }
        phaseTicks += 20;
        if (phaseTicks < 200) return;
        try {
            SyncProbe.check(SyncProbe.snapshot().equals(SyncProbe.read("expected.txt")), "OEL snapshot differs from server: " + SyncProbe.snapshot());
            SyncProbe.verifyCaches();
            var recipe = mc.getConnection().getRecipeManager().byKey(new ResourceLocation("oneenough_regression", "reload")).orElseThrow();
            ReplacementControl.withSkipReplacement(() -> {
                SyncProbe.check(recipe.getIngredients().get(0).test(new ItemStack(Items.NETHERITE_BLOCK)), "Client recipe has old input");
                SyncProbe.check(!recipe.getIngredients().get(0).test(new ItemStack(Items.OAK_LOG)), "Client recipe still accepts old input");
            });
            SyncProbe.check(recipe.getResultItem(mc.level.registryAccess()).is(Items.STICK), "Client recipe result changed");
            completedPhase=phase;
            SyncProbe.write("client"+client+"-phase"+phase+".txt", SyncProbe.snapshot());
            System.out.println("SYNC_PROBE CLIENT_PASS client="+client+" phase="+phase+" player="+mc.player.getGameProfile().getName());
        } catch (Throwable error) { failed=true; SyncProbe.fail("client"+client,error); }
    }
}
