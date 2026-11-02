package cc.sighs.oneenoughitem.mixin;

import com.google.gson.JsonElement;
import com.mojang.serialization.DynamicOps;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.resource.ContextAwareReloadListener;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/** Provides access to the inherited conditional serialization context. */
@Mixin(ContextAwareReloadListener.class)
public interface ContextAwareReloadListenerAccessor {

    @Invoker("makeConditionalOps")
    ConditionalOps<JsonElement> oei$makeConditionalOps(DynamicOps<JsonElement> ops);
}
