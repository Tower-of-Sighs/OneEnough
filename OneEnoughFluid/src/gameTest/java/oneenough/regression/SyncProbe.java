package oneenough.regression;

import cc.sighs.oelib.data.DataManager;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;
import com.sighs.oneenoughblock.init.ReplacementUtils;
import com.flechazo.oneenoughfluid.init.FluidReplacementCache;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

final class SyncProbe {
    static final boolean ENABLED = Boolean.getBoolean("oneenough.syncProbe");
    static Path root() { return Path.of(System.getProperty("oneenough.syncProbeRoot")); }
    static void write(String name, String value) {
        try { Files.createDirectories(root()); Files.writeString(root().resolve(name), value); }
        catch (Exception error) { throw new IllegalStateException(error); }
    }
    static String read(String name) {
        try { return Files.readString(root().resolve(name)); }
        catch (Exception error) { throw new IllegalStateException(error); }
    }
    static boolean exists(String name) { return Files.exists(root().resolve(name)); }
    static String snapshot() {
        return DataManager.getAllData(Replacements.class).entrySet().stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .map(e -> e.getKey() + "=" + Replacements.CODEC.encodeStart(JsonOps.INSTANCE, e.getValue()).result().orElseThrow())
                .collect(Collectors.joining("\n"));
    }
    static void verifyCaches() {
        check(ItemReplacementCache.replacementOf(Items.CLAY_BALL) == Items.GOLD_INGOT, "Deferred item tag cache mismatch");
        check(ItemReplacementCache.replacementOf(Items.OAK_LOG) == Items.NETHERITE_BLOCK, "Direct item cache mismatch");
        check(ReplacementUtils.getReplacementBlock(Blocks.CRYING_OBSIDIAN) == Blocks.DIAMOND_BLOCK, "Deferred block cache mismatch");
        check(FluidReplacementCache.replacementOf(Fluids.LAVA) == Fluids.WATER, "Deferred fluid cache mismatch");
    }
    static void check(boolean ok, String message) { if (!ok) throw new AssertionError(message); }
    static void fail(String role, Throwable error) {
        error.printStackTrace(); write(role + "-FAIL.txt", error.toString());
        System.err.println("SYNC_PROBE FAIL " + role + ": " + error);
    }
}
