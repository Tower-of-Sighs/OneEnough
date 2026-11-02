package oneenough.regression;

import com.flechazo.oneenoughfluid.init.FluidReplacementCache;
import com.mafuyu404.oneenoughitem.client.ClientContext;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;
import com.mafuyu404.oneenoughitem.util.ReplacementIndex;
import com.sighs.oneenoughblock.init.BlockReplacementCache;
import com.sighs.oneenoughblock.init.ReplacementUtils;
import com.sighs.oneenoughblock.api.IPalettedContainer;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Predicate;

@Mod("oneenough_regression")
@GameTestHolder("oneenough_regression")
@PrefixGameTestTemplate(false)
public final class ReplacementRegressionTests {
    private static boolean failCreativeGenerator;
    private static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, "oneenough_regression");
    private static final RegistryObject<CreativeModeTab> DUPLICATES = TABS.register("duplicates", () -> CreativeModeTab.builder()
            .title(Component.literal("Replacement regression"))
            .icon(() -> new ItemStack(Items.IRON_INGOT))
            .displayItems((parameters, output) -> {
                if (failCreativeGenerator) throw new IllegalStateException("expected generator failure");
                output.accept(Items.IRON_INGOT, CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
                output.accept(Items.GOLD_INGOT, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
                ItemStack variant = new ItemStack(Items.IRON_INGOT);
                CompoundTag tag = new CompoundTag();
                tag.putString("variant", "keep");
                variant.setTag(tag);
                output.accept(variant);
            }).build());

    public ReplacementRegressionTests() {
        TABS.register(FMLJavaModLoadingContext.get().getModEventBus());
    }

    @GameTest(template = "empty")
    public static void replacementRegressions(GameTestHelper helper) {
        boolean previousDeep = OEIConfig.get().deeperReplace();
        try {
            OEIConfig.UNIT.setNoSave(OEIConfig::deeperReplace, true);
            reset();
            vanillaPredicateIsPreserved(helper);
            itemMappingsAndReloads(helper);
            creativeContentsAreDeduplicated(helper);
            fluidMappingsAndNbt(helper);
            nestedReplacementControl(helper);
            cyclicAndChainedRules(helper);
            blockCacheInvalidation(helper);
            ReloadRegressionTests.diagnose(helper);
            helper.succeed();
        } finally {
            reset();
            OEIConfig.UNIT.setNoSave(OEIConfig::deeperReplace, previousDeep);
        }
    }

    private static void reset() {
        ItemReplacementCache.endReloadOverride();
        ItemReplacementCache.clearCache();
        FluidReplacementCache.endReloadOverride();
        FluidReplacementCache.clearCache();
        BlockReplacementCache.endReloadOverride();
        BlockReplacementCache.clearCache();
        ReplacementControl.clearSkipReplacement();
    }

    private static Replacements rule(String source, String target) {
        return new Replacements(List.of(source), target, Optional.empty());
    }

    private static void vanillaPredicateIsPreserved(GameTestHelper h) {
        ItemStack stack = new ItemStack(Items.ENDER_EYE);
        AtomicInteger calls = new AtomicInteger();
        Predicate<Holder<Item>> predicate = holder -> {
            calls.incrementAndGet();
            return holder.value() == Items.ENDER_EYE;
        };
        h.assertTrue(stack.is(predicate), "Deep replacement erased a successful vanilla predicate");
        h.assertTrue(calls.get() == 1, "A vanilla predicate was evaluated more than once without rules");
        h.assertTrue(!stack.is(Items.DIAMOND), "Unrelated item matched without rules");
    }

    private static void itemMappingsAndReloads(GameTestHelper h) {
        ItemReplacementCache.putReplacement(rule("minecraft:iron_ingot", "minecraft:gold_ingot"));
        ItemStack replaced = new ItemStack(Items.IRON_INGOT, 7);
        h.assertTrue(replaced.getItem() == Items.GOLD_INGOT && replaced.getCount() == 7, "Item constructor replacement failed");
        h.assertTrue(replaced.is(Items.IRON_INGOT), "Item overload did not match the source");
        h.assertTrue(replaced.is(Items.IRON_INGOT.builtInRegistryHolder()), "Holder overload did not match the source");
        h.assertTrue(replaced.is(holder -> holder.value() == Items.IRON_INGOT), "Predicate overload did not match the source");
        AtomicInteger nested = new AtomicInteger();
        AtomicReference<Predicate<Holder<Item>>> recursive = new AtomicReference<>();
        Predicate<Holder<Item>> reentrant = holder -> {
            nested.incrementAndGet();
            return holder.value() == Items.IRON_INGOT && replaced.is(recursive.get());
        };
        recursive.set(reentrant);
        replaced.is(reentrant);
        h.assertTrue(nested.get() <= 3, "Predicate extension recursively entered itself");
        ClientContext.beginBuilding();
        try {
            h.assertTrue(new ItemStack(Items.IRON_INGOT).getItem() == Items.IRON_INGOT, "Creative generation changed an original entry");
        } finally {
            ClientContext.endBuilding();
        }
        ItemReplacementCache.beginReloadOverride(Map.of("minecraft:iron_ingot", "minecraft:diamond"));
        h.assertTrue(new ItemStack(Items.IRON_INGOT).getItem() == Items.DIAMOND, "Reload override used a stale item index");
        ItemReplacementCache.endReloadOverride();
        h.assertTrue(new ItemStack(Items.IRON_INGOT).getItem() == Items.GOLD_INGOT, "Ending reload override did not restore mappings");
        ItemReplacementCache.removeItemReplacement("minecraft:iron_ingot");
        h.assertTrue(new ItemStack(Items.IRON_INGOT).getItem() == Items.IRON_INGOT, "Removed item rule remained cached");
    }

    private static void fluidMappingsAndNbt(GameTestHelper h) {
        FluidReplacementCache.putReplacement(rule("minecraft:water", "minecraft:lava"));
        FluidStack replaced = new FluidStack(Fluids.WATER, 250);
        FluidStack source = ReplacementControl.withSkipReplacement(() -> new FluidStack(Fluids.WATER, 250));
        h.assertTrue(replaced.getFluid() == Fluids.LAVA && replaced.getAmount() == 250, "Fluid constructor replacement failed");
        h.assertTrue(replaced.isFluidEqual(source), "Fluid equality did not match the source");
        CompoundTag tag = new CompoundTag();
        tag.putString("variant", "different");
        source.setTag(tag);
        h.assertTrue(!replaced.isFluidEqual(source), "Fluid replacement ignored differing NBT");
        ClientContext.beginBuilding();
        try {
            h.assertTrue(new FluidStack(Fluids.WATER, 1000).getFluid() == Fluids.WATER, "Creative generation changed a bucket variant");
        } finally {
            ClientContext.endBuilding();
        }
        FluidReplacementCache.beginReloadOverride(Map.of("minecraft:lava", "minecraft:water"));
        h.assertTrue(new FluidStack(Fluids.LAVA, 1).getFluid() == Fluids.WATER, "Fluid reload override failed");
        FluidReplacementCache.endReloadOverride();
        FluidReplacementCache.removeFluidReplacement("minecraft:water");
        h.assertTrue(new FluidStack(Fluids.WATER, 1).getFluid() == Fluids.WATER, "Removed fluid rule remained cached");
    }

    private static void creativeContentsAreDeduplicated(GameTestHelper h) {
        ItemReplacementCache.putReplacement(rule("minecraft:iron_ingot", "minecraft:gold_ingot"));
        CreativeModeTab tab = DUPLICATES.get();
        CreativeModeTab.ItemDisplayParameters parameters = new CreativeModeTab.ItemDisplayParameters(
                h.getLevel().enabledFeatures(), false, h.getLevel().registryAccess());
        failCreativeGenerator = true;
        try {
            tab.buildContents(parameters);
            h.fail("Expected the test generator to throw");
        } catch (IllegalStateException expected) {
            h.assertTrue(!ClientContext.isBuilding(), "Creative generation leaked its guard after an exception");
        } finally {
            failCreativeGenerator = false;
        }
        tab.buildContents(parameters);
        h.assertTrue(tab.getDisplayItems().size() == 2, "Creative replacement did not deduplicate entries while retaining NBT variants");
        h.assertTrue(tab.getSearchTabDisplayItems().size() == 2, "Creative replacement did not merge parent and search visibility");
        h.assertTrue(tab.getDisplayItems().stream().allMatch(stack -> stack.getItem() == Items.GOLD_INGOT), "Creative entries were not unified");
        ItemReplacementCache.clearCache();
    }

    private static void nestedReplacementControl(GameTestHelper h) {
        ReplacementControl.withSkipReplacement(() -> {
            ReplacementControl.withSkipReplacement(() -> {});
            h.assertTrue(ReplacementControl.shouldSkipReplacement(), "An inner scope cleared the outer replacement guard");
            try {
                ReplacementControl.withSkipReplacement((Runnable) () -> { throw new IllegalStateException("expected"); });
            } catch (IllegalStateException expected) {
                h.assertTrue(ReplacementControl.shouldSkipReplacement(), "An exception cleared the outer replacement guard");
            }
        });
        h.assertTrue(!ReplacementControl.shouldSkipReplacement(), "Replacement guard leaked out of its scope");
        ClientContext.beginBuilding();
        ClientContext.beginBuilding();
        ClientContext.endBuilding();
        h.assertTrue(ClientContext.isBuilding(), "Nested creative generation cleared the outer guard");
        ClientContext.endBuilding();
        h.assertTrue(!ClientContext.isBuilding(), "Creative guard leaked out of its scope");
    }

    private static void cyclicAndChainedRules(GameTestHelper h) {
        Object a = new Object(), b = new Object(), c = new Object();
        Map<Object, Object> rules = new IdentityHashMap<>();
        rules.put(a, b);
        rules.put(b, c);
        ReplacementIndex<Object> chain = new ReplacementIndex<>(rules);
        h.assertTrue(chain.replacementOf(a) == c && chain.sourcesOf(c).size() == 2, "Replacement chain did not resolve to its terminal target");
        rules.put(c, a);
        ReplacementIndex<Object> cycle = new ReplacementIndex<>(rules);
        h.assertTrue(cycle.replacementOf(a) == null && cycle.replacementOf(b) == null, "Cyclic rules changed the original object");
    }

    private static void blockCacheInvalidation(GameTestHelper h) {
        BlockReplacementCache.putBlockMapping("minecraft:stone", "minecraft:gold_block");
        h.assertTrue(ReplacementUtils.getReplacement(Blocks.STONE.defaultBlockState()).is(Blocks.GOLD_BLOCK), "Block mapping was not applied");
        PalettedContainer<BlockState> palette = new PalettedContainer<>(Block.BLOCK_STATE_REGISTRY,
                Blocks.STONE.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);
        ((IPalettedContainer) palette).handleReplace();
        h.assertTrue(palette.get(0, 0, 0).is(Blocks.GOLD_BLOCK) && palette.get(15, 15, 15).is(Blocks.GOLD_BLOCK),
                "Existing-chunk replacement failed when the palette resized");
        BlockReplacementCache.removeBlockReplacement("minecraft:stone");
        h.assertTrue(ReplacementUtils.getReplacement(Blocks.STONE.defaultBlockState()) == null, "Removed block mapping remained cached");
        BlockReplacementCache.putBlockMapping("minecraft:stone", "minecraft:gold_block");
        BlockReplacementCache.putBlockMapping("minecraft:gold_block", "minecraft:stone");
        h.assertTrue(ReplacementUtils.getReplacement(Blocks.STONE.defaultBlockState()) == null, "Cyclic block rules should keep the original state");
        LevelChunkSection section = new LevelChunkSection(h.getLevel().registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.BIOME));
        section.setBlockState(0, 0, 0, Blocks.STONE.defaultBlockState(), false);
        h.assertTrue(section.getBlockState(0, 0, 0).is(Blocks.STONE), "Cyclic block placement did not keep the original state");
    }
}
