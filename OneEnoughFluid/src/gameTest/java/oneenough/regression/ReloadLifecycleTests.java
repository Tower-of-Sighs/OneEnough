package oneenough.regression;

import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import com.sighs.oneenoughblock.init.BlockReplacementCache;
import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;

@GameTestHolder("oneenough_regression")
@PrefixGameTestTemplate(false)
public class ReloadLifecycleTests {
 @GameTest(template="empty", timeoutTicks=1800)
 public static void reloadAndDelayedDrops(GameTestHelper h) {
  var manager=cc.sighs.oelib.data.forge.DataManager.get(com.mafuyu404.oneenoughitem.data.Replacements.class);
  System.out.println("LIFECYCLE startup loaded="+manager.getAllData().size()+" includingDeferred="+manager.getDataList().size());
  // Let the other synchronous cache tests finish before exercising server reloads.
  h.runAfterDelay(40, () -> reload(h, 0));
 }
 private static void reload(GameTestHelper h,int round) {
  var server=h.getLevel().getServer();
  server.reloadResources(server.getPackRepository().getSelectedIds()).whenComplete((ignored,error)->server.execute(()->{
   if(error!=null){h.fail("Resource reload failed: "+error);return;}
   try {
    verify(h);
    System.out.println("LIFECYCLE tag mapping after reload="+ItemReplacementCache.matchItem("minecraft:clay_ball"));
    h.runAfterDelay(200,()->{
     verify(h);
     System.out.println("LIFECYCLE round="+round+" drops and recipe remain present after 200 ticks");
     if(round<2) reload(h,round+1); else h.succeed();
    });
   } catch(Throwable e){h.fail("Post-reload check failed: "+e);}
  }));
 }
 private static void verify(GameTestHelper h) {
  var server=h.getLevel().getServer();
  var id=new ResourceLocation("oneenough_regression","reload");
  var params=new LootParams.Builder(h.getLevel()).withParameter(LootContextParams.ORIGIN,Vec3.ZERO)
   .withParameter(LootContextParams.BLOCK_STATE,Blocks.OAK_LOG.defaultBlockState())
   .withParameter(LootContextParams.TOOL,ItemStack.EMPTY).create(LootContextParamSets.BLOCK);
  var drops=server.getLootData().getLootTable(id).getRandomItems(params);
  h.assertTrue(drops.size()==1 && drops.get(0).is(Items.NETHERITE_BLOCK),"Reload lost the rewritten loot table");
  h.assertTrue(server.getRecipeManager().byKey(id).isPresent(),"Reload lost the rewritten recipe");
  h.assertTrue("minecraft:gold_block".equals(BlockReplacementCache.matchBlock("minecraft:oak_planks")),"OEB mapping lost");
  h.assertTrue(BlockReplacementCache.matchBlock("minecraft:oak_log")==null,"OEI leaked into OEB after real reload");
  h.assertTrue(ItemReplacementCache.replacementOf(Items.OAK_LOG)==Items.NETHERITE_BLOCK,"Item mapping lost after reload");
 }
}
