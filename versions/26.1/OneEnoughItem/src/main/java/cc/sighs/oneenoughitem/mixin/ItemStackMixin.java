package cc.sighs.oneenoughitem.mixin;
import cc.sighs.oneenoughitem.client.ClientContext;
import cc.sighs.oneenoughitem.init.ItemReplacementCache;
import cc.sighs.oneenoughitem.init.config.OEIConfig;
import cc.sighs.oneenoughitem.util.ReplacementControl;
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
    private Holder<Item> item;
    @Mutable @Shadow @Final
    private PatchedDataComponentMap components;
    @Shadow
    public abstract Item getItem();
    @Shadow
    public abstract Holder<Item> typeHolder();
    @Unique
    private static final ThreadLocal<Boolean> oei$matching = ThreadLocal.withInitial(() -> false);

    @Inject(method = "<init>(Lnet/minecraft/core/Holder;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("TAIL"))
    private void oei$replace(Holder<Item> itemHolder, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        if (item == null || ClientContext.isBuilding() || ReplacementControl.shouldSkipReplacement()) return;
        Item target = ItemReplacementCache.replacementOf(item.value());
        if (target == null) return;
        ReplacementControl.withSkipReplacement(() -> {
            DataComponentPatch patch = this.components.asPatch();
            this.item = target.builtInRegistryHolder();
            this.components = PatchedDataComponentMap.fromPatch(target.components(), patch);
            ((ItemStack) (Object) this).applyComponentsAndValidate(patch);
        });
    }

    @Unique
    private boolean oei$canExtend(boolean original) {
        return !original && !ClientContext.isBuilding() && !ReplacementControl.shouldSkipReplacement()
                && !oei$matching.get() && ItemReplacementCache.hasAnyMappings() && OEIConfig.get().deeperReplace();
    }

    // TypedInstance's generic default method is erased to is(Object) in 26.1.
    public boolean is(Object expected) {
        return getItem() == expected || (expected instanceof Item source && oei$canExtend(false)
                && ItemReplacementCache.matches(getItem(), source));
    }

    public boolean is(Holder<Item> expected) {
        return typeHolder() == expected || (expected != null && oei$canExtend(false)
                && ItemReplacementCache.matches(getItem(), expected.value()));
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

}
