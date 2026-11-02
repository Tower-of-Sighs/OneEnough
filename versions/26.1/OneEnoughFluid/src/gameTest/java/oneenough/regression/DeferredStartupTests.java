package oneenough.regression;

import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import cc.sighs.oneenoughitem.init.ItemReplacementCache;
import cc.sighs.oneenoughblock.init.BlockReplacementCache;
import cc.sighs.oneenoughblock.init.ReplacementUtils;
import cc.sighs.oneenoughfluid.init.FluidReplacementCache;
import net.minecraft.world.item.Items;
import cc.sighs.oelib.data.DataManager;
import cc.sighs.oneenoughitem.data.Replacements;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;

@EventBusSubscriber(modid="oneenough_regression")
public final class DeferredStartupTests {
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void afterStartup(ServerStartedEvent event) {
        check(ItemReplacementCache.replacementOf(Items.CLAY_BALL)==Items.GOLD_INGOT,"Deferred item tag was not active at server startup");
        check(ReplacementUtils.getReplacementBlock(Blocks.CRYING_OBSIDIAN)==Blocks.DIAMOND_BLOCK,"Deferred block tag was not active at server startup");
        check(FluidReplacementCache.replacementOf(Fluids.LAVA)==Fluids.WATER,"Deferred fluid tag was not active at server startup");
        check(BlockReplacementCache.matchBlock("minecraft:clay_ball")==null,"Deferred item rule leaked into blocks");
        check(DataManager.getDeferredBatch(Replacements.class).entries().keySet().stream()
                .noneMatch(source -> source.getNamespace().equals("oei") || source.getNamespace().equals("oeb")
                        || source.getNamespace().equals("oef")), "OEL still has deferred OE entries after startup");
        check(!DataManager.getAllData(Replacements.class).isEmpty(), "OEL effective snapshot is empty");
        System.out.println("PASS: item/block/fluid deferred tags active before any player joins or reload runs");
    }
    private static void check(boolean passed,String message) { if(!passed) throw new AssertionError(message); }
}
