package oneenough.regression;

import com.flechazo.oneenoughfluid.init.FluidReplacementCache;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.List;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = "oneenough_regression", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class EmiClientRegression {
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        if (!Boolean.getBoolean("oneenough.runClientRegressions")) return;
        event.enqueueWork(() -> {
            try {
                ItemReplacementCache.putReplacement(new Replacements(List.of("minecraft:iron_ingot"), "minecraft:gold_ingot", Optional.empty()));
                FluidReplacementCache.putReplacement(new Replacements(List.of("minecraft:water"), "minecraft:lava", Optional.empty()));
                CompoundTag tag = new CompoundTag();
                tag.putString("variant", "keep");
                verify(!ReplacementControl.shouldSkipReplacement(), "Unexpected replacement guard before EMI test");
                verify(ItemReplacementCache.replacementOf(Items.IRON_INGOT) == Items.GOLD_INGOT, "Item mapping was not ready before EMI test");
                Class<?> items = Class.forName("dev.emi.emi.api.stack.ItemEmiStack");
                Object item = items.getConstructor(Item.class, CompoundTag.class, long.class).newInstance(Items.IRON_INGOT, tag, 42L);
                verify(items.getMethod("getKey").invoke(item) == Items.GOLD_INGOT, "EMI item index key stayed on the source item");
                verify(items.getMethod("getAmount").invoke(item).equals(42L), "EMI item amount changed");
                verify(tag.equals(items.getMethod("getNbt").invoke(item)), "EMI item NBT changed");
                Class<?> fluids = Class.forName("dev.emi.emi.api.stack.FluidEmiStack");
                Object fluid = fluids.getConstructor(Fluid.class, CompoundTag.class, long.class).newInstance(Fluids.WATER, tag, 250L);
                verify(fluids.getMethod("getKey").invoke(fluid) == Fluids.LAVA, "EMI fluid index key stayed on the source fluid");
                verify(fluids.getMethod("getAmount").invoke(fluid).equals(250L), "EMI fluid amount changed");
                verify(tag.equals(fluids.getMethod("getNbt").invoke(fluid)), "EMI fluid NBT changed");
                System.out.println("PASS: EMI client item/fluid index keys, amounts and NBT");
                System.exit(0);
            } catch (Throwable error) {
                error.printStackTrace();
                System.exit(1);
            }
        });
    }

    private static void verify(boolean value, String message) {
        if (!value) throw new AssertionError(message);
    }
}
