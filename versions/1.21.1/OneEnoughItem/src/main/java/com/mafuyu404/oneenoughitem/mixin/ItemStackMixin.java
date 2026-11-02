package com.mafuyu404.oneenoughitem.mixin;
import com.mafuyu404.oneenoughitem.client.ClientContext;
import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;
import java.util.function.Predicate;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
    @Mutable @Shadow @Final
    private Item item;
    @Mutable @Shadow @Final
    private PatchedDataComponentMap components;
    @Shadow
    public abstract Item getItem();
    @Unique
    private static final ThreadLocal<Boolean> oei$matching = ThreadLocal.withInitial(() -> false);

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("TAIL"))
    private void oei$replace(ItemLike itemLike, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        if (item == null || ClientContext.isBuilding() || ReplacementControl.shouldSkipReplacement()) return;
        Item target = ItemReplacementCache.replacementOf(item);
        if (target == null) return;
        ReplacementControl.withSkipReplacement(() -> {
            DataComponentPatch patch = this.components.asPatch();
            this.item = target;
            this.components = PatchedDataComponentMap.fromPatch(target.components(), patch);
            target.verifyComponentsAfterLoad((ItemStack) (Object) this);
        });
    }

    @Unique
    private boolean oei$canExtend(boolean original) {
        return !original && !ClientContext.isBuilding() && !ReplacementControl.shouldSkipReplacement()
                && !oei$matching.get() && ItemReplacementCache.hasAnyMappings() && OEIConfig.get().deeperReplace();
    }

    // Preserve vanilla and other mods' successful matches; only extend a failed match.
    @Inject(method = "is(Ljava/util/function/Predicate;)Z", at = @At("RETURN"), cancellable = true)
    private void extend(Predicate<Holder<Item>> predicate, CallbackInfoReturnable<Boolean> cir) {
        if (!oei$canExtend(cir.getReturnValueZ())) return;
        oei$matching.set(true);
        try {
            for (Item source : ItemReplacementCache.sourcesOf(getItem())) {
                if (predicate.test(source.builtInRegistryHolder())) {
                    cir.setReturnValue(true);
                    return;
                }
            }
        } finally {
            oei$matching.remove();
        }
    }

    @Inject(method = "is(Lnet/minecraft/core/Holder;)Z", at = @At("RETURN"), cancellable = true)
    private void extend(Holder<Item> expected, CallbackInfoReturnable<Boolean> cir) {
        if (expected != null && oei$canExtend(cir.getReturnValueZ()) && ItemReplacementCache.matches(getItem(), expected.value())) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "is(Lnet/minecraft/world/item/Item;)Z", at = @At("RETURN"), cancellable = true)
    private void extend(Item expected, CallbackInfoReturnable<Boolean> cir) {
        if (oei$canExtend(cir.getReturnValueZ()) && ItemReplacementCache.matches(getItem(), expected)) {
            cir.setReturnValue(true);
        }
    }
}
