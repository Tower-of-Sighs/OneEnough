package oneenough.regression;

import cc.sighs.oelib.data.neoforge.DataManager;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.google.gson.JsonElement;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import java.util.HashMap;
import java.util.Map;

final class DataReloadTestSupport {
    private DataReloadTestSupport() {}

    static void load(DataManager<Replacements> manager, Map<ResourceLocation, Replacements> entries) {
        Map<ResourceLocation, JsonElement> resources = new HashMap<>();
        entries.forEach((source, value) -> resources.put(source,
                Replacements.CODEC.encodeStart(JsonOps.INSTANCE, value).result().orElseThrow()));
        var previousBatch = manager.getDeferredBatch();
        try {
            var apply = DataManager.class.getDeclaredMethod("apply", Map.class, ResourceManager.class, ProfilerFiller.class);
            apply.setAccessible(true);
            apply.invoke(manager, resources, null, null);
        } catch (ReflectiveOperationException error) {
            throw new AssertionError("Failed to apply test resources", error);
        }
        if (previousBatch.isCurrent()) throw new AssertionError("Reload retained an old batch handle");
    }
}
