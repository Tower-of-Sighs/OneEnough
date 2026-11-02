package oneenough.regression;

import cc.sighs.oelib.data.neoforge.DataManager;
import cc.sighs.oelib.event.neoforge.DataReloadEvent;
import com.google.gson.*;
import cc.sighs.oneenoughitem.data.Replacements;
import cc.sighs.oneenoughitem.init.OEIReplacementStrategy;
import cc.sighs.oneenoughitem.init.ItemReplacementCache;
import cc.sighs.oneenoughitem.util.JsonReloadMixinHelper;
import cc.sighs.oneenoughitem.util.MixinUtils;
import cc.sighs.oneenoughblock.init.BlockReplacementCache;

import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;


import java.util.*;



public class ReloadRegressionTests {

 public static void diagnose(GameTestHelper h) {
  h.assertTrue(!cc.sighs.oneenoughitem.util.Utils.isTagExists(Identifier.parse("oneenough_regression:deferred_item"), null), "Early tag validation must wait for a registry lookup");
  ItemReplacementCache.clearCache();
  ItemReplacementCache.endReloadOverride();
  var manager=DataManager.get(Replacements.class);
  var saved=manager.getAllData();
  try {
   var modes=new Replacements.Rules(Optional.of(Map.of("loot_table",Replacements.ProcessingMode.REPLACE,"recipe",Replacements.ProcessingMode.REPLACE)),Optional.empty());
   for(String directory:List.of("loot_table","recipe")) {
    var strategy=new OEIReplacementStrategy(){
     @Override public MixinUtils.ReplacementLoader.CurrentSnapshot loadCurrentSnapshot(ResourceManager ignored) {
      return new MixinUtils.ReplacementLoader.CurrentSnapshot(Map.of("minecraft:oak_log","minecraft:stone"),Map.of("minecraft:oak_log",modes),Map.of());
     }
    };
    Map<Identifier,JsonElement> json=new HashMap<>();
    json.put(Identifier.parse("test:sample"),JsonParser.parseString(directory.equals("loot_table") ? "{\"pools\":[{\"entries\":[{\"type\":\"minecraft:item\",\"name\":\"minecraft:oak_log\"}]}]}" : "{\"ingredient\":{\"item\":\"minecraft:oak_log\"},\"result\":\"minecraft:stone\"}"));
    JsonReloadMixinHelper.processJsonReload(directory,h.getLevel().getServer().getResourceManager(),json,strategy,null,()->false,id->modes,"oei");
    System.out.println("REGRESSION "+directory+" remaining files="+json.size());
    h.assertTrue(json.size() == 1 && json.values().iterator().next().toString().contains("minecraft:stone"),"Valid replacement removed its resource");
   }
   var rule=new Replacements(List.of("minecraft:oak_log"),"minecraft:stone",Optional.empty());
   DataReloadTestSupport.load(manager, Map.of(Identifier.parse("oei:item_only"),rule));
   cc.sighs.oneenoughblock.event.ServerEventHandler.onDataReload(new DataReloadEvent(Replacements.class,1,0));
   System.out.println("REGRESSION OEI-only rule leaked into OEB="+BlockReplacementCache.matchBlock("minecraft:oak_log"));
   h.assertTrue(BlockReplacementCache.matchBlock("minecraft:oak_log") == null,"OEI rules leaked into OEB");
   ItemReplacementCache.putReplacement(rule, h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ITEM));
   var emptySnapshot=new OEIReplacementStrategy(){
    @Override public MixinUtils.ReplacementLoader.CurrentSnapshot loadCurrentSnapshot(ResourceManager ignored){return new MixinUtils.ReplacementLoader.CurrentSnapshot(Map.of(),Map.of(),Map.of());}
   };
   Map<Identifier,JsonElement> json=new HashMap<>();
   json.put(Identifier.parse("test:old"),JsonParser.parseString("{\"name\":\"minecraft:oak_log\"}"));
   JsonReloadMixinHelper.processJsonReload("loot_table",h.getLevel().getServer().getResourceManager(),json,emptySnapshot,null,ItemReplacementCache::hasAnyMappings,id->modes,"oei");
   System.out.println("REGRESSION removed rules + stale cache remaining loot files="+json.size());
   // Explicit rule data from the cache may suppress the directory, so repeat with an explicit mode.
   ItemReplacementCache.clearCache();
   ItemReplacementCache.putReplacement(new Replacements(rule.match(),rule.result(),Optional.of(modes)), h.getLevel().registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.ITEM));
   json.put(Identifier.parse("test:old"),JsonParser.parseString("{\"name\":\"minecraft:oak_log\"}"));
   JsonReloadMixinHelper.processJsonReload("loot_table",h.getLevel().getServer().getResourceManager(),json,emptySnapshot,null,ItemReplacementCache::hasAnyMappings,id->modes,"oei");
   h.assertTrue(json.size() == 1 && json.values().iterator().next().toString().contains("minecraft:oak_log"),"Removed rules were reapplied from stale cache");
   System.out.println("REGRESSION empty snapshot preserves loot despite stale runtime cache: confirmed");
   ItemReplacementCache.beginReloadOverride(Map.of());
   h.assertTrue(ItemReplacementCache.replacementOf(net.minecraft.world.item.Items.OAK_LOG) == null, "Empty override exposed old mappings");
   ItemReplacementCache.endReloadOverride();
   DataReloadTestSupport.load(manager, Map.of(Identifier.parse("oeb:block_only"),rule));
   h.assertTrue("minecraft:stone".equals(BlockReplacementCache.matchBlock("minecraft:oak_log")), "OEB rule no longer loads");
   // Repeated resource loads must not bring removed rules back.
   for (int i=0;i<20;i++) {
    DataReloadTestSupport.load(manager, Map.of());
    h.assertTrue(BlockReplacementCache.matchBlock("minecraft:oak_log") == null, "Removed block mapping returned");
    h.assertTrue(ItemReplacementCache.replacementOf(net.minecraft.world.item.Items.OAK_LOG) == null, "Removed item mapping returned");
   }
   System.out.println("REGRESSION repeated empty resource loads remain empty");
  } finally {
   DataReloadTestSupport.load(manager, saved);
   ItemReplacementCache.clearCache();
   ItemReplacementCache.endReloadOverride();
   BlockReplacementCache.clearCache();
  }
 }
}
