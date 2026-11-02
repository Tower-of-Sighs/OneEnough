package oneenough.regression;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;
import java.util.List;

@Mod.EventBusSubscriber(modid="oneenough_regression")
public final class SyncServerProbe {
    private static MinecraftServer server;
    private static int ticks;
    private static boolean reloading;
    private static boolean complete;
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void started(ServerStartedEvent event) {
        if (!SyncProbe.ENABLED) return;
        server = event.getServer();
        try {
            verifyGameplay();
            SyncProbe.write("expected.txt", SyncProbe.snapshot());
            SyncProbe.write("phase.txt", "1");
            SyncProbe.write("ready.txt", "ready");
            System.out.println("SYNC_PROBE SERVER_READY entries=" + cc.sighs.oelib.data.DataManager.getAllData(com.mafuyu404.oneenoughitem.data.Replacements.class).size());
        } catch (Throwable error) { SyncProbe.fail("server", error); }
    }
    @SubscribeEvent
    public static void tick(TickEvent.ServerTickEvent event) {
        if (!SyncProbe.ENABLED || server == null || event.phase != TickEvent.Phase.END || complete) return;
        if (++ticks % 20 != 0) return;
        try {
            if (!reloading && allClients(1)) {
                SyncProbe.check(server.getPlayerCount() == 3, "Expected three simultaneous players, got " + server.getPlayerCount());
                verifyGameplay(); reloading = true;
                server.reloadResources(server.getPackRepository().getSelectedIds()).whenComplete((ignored, error) -> server.execute(() -> {
                    if (error != null) { SyncProbe.fail("server", error); return; }
                    try {
                        verifyGameplay();
                        SyncProbe.write("expected.txt", SyncProbe.snapshot());
                        SyncProbe.write("phase.txt", "2");
                        System.out.println("SYNC_PROBE RELOAD_COMPLETE players=" + server.getPlayerCount());
                    } catch (Throwable failure) { SyncProbe.fail("server", failure); }
                }));
            }
            if (reloading && allClients(2)) {
                verifyGameplay(); complete = true;
                SyncProbe.write("SUCCESS.txt", "3 clients joined sequentially; all snapshots and caches match; reload sync, recipe and loot passed");
                System.out.println("SYNC_PROBE ALL_PASS");
                SyncProbe.write("stop.txt", "stop");
                server.halt(false);
            }
        } catch (Throwable error) { complete=true; SyncProbe.fail("server", error); }
    }
    private static boolean allClients(int phase) {
        for (int client=1; client<=3; client++) if (!SyncProbe.exists("client"+client+"-phase"+phase+".txt")) return false;
        return true;
    }
    private static void verifyGameplay() {
        SyncProbe.verifyCaches();
        var id = new ResourceLocation("oneenough_regression", "reload");
        var recipe = server.getRecipeManager().byKey(id).orElseThrow();
        ReplacementControl.withSkipReplacement(() -> {
            SyncProbe.check(recipe.getIngredients().get(0).test(new ItemStack(Items.NETHERITE_BLOCK)), "Recipe does not accept rewritten input");
            SyncProbe.check(!recipe.getIngredients().get(0).test(new ItemStack(Items.OAK_LOG)), "Recipe still accepts old input");
        });
        SyncProbe.check(recipe.getResultItem(server.registryAccess()).is(Items.STICK), "Recipe result changed");
        var params = new LootParams.Builder(server.overworld()).withParameter(LootContextParams.ORIGIN, Vec3.ZERO)
                .withParameter(LootContextParams.BLOCK_STATE, Blocks.OAK_LOG.defaultBlockState())
                .withParameter(LootContextParams.TOOL, ItemStack.EMPTY).create(LootContextParamSets.BLOCK);
        var drops = server.getLootData().getLootTable(id).getRandomItems(params);
        SyncProbe.check(drops.size()==1 && drops.get(0).is(Items.NETHERITE_BLOCK), "Loot replacement mismatch");
        System.out.println("SYNC_PROBE SERVER_GAMEPLAY_PASS recipe+loot+threeDomains players="+server.getPlayerCount());
    }
}
