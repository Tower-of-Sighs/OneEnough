package cc.sighs.oneenoughitem.mixin.emi;

import cc.sighs.oneenoughitem.init.ItemReplacementCache;
import cc.sighs.oneenoughitem.util.ReplacementControl;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "dev.emi.emi.api.stack.ItemEmiStack", remap = false)
public abstract class ItemEmiStackMixin {
    @Shadow @Final @Mutable
    private Item item;

    @Inject(method = "<init>*", at = @At("RETURN"))
    private void oei$unifyIndexKey(CallbackInfo ci) {
        if (ReplacementControl.shouldSkipReplacement()) return;
        Item target = ItemReplacementCache.replacementOf(item);
        if (target != null) item = target;
    }
}
