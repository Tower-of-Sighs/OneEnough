package com.mafuyu404.oneenoughitem.mixin;

import com.mafuyu404.oneenoughitem.Oneenoughitem;
import com.mafuyu404.oneenoughitem.client.ClientContext;
import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;
import com.mafuyu404.oneenoughitem.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Predicate;

@Mixin(value = ItemStack.class)
public abstract class ItemStackMixin {
    @Mutable
    @Shadow
    @Final
    @Nullable
    private Item item;

    @Mutable
    @Shadow(remap = false)
    @Final
    PatchedDataComponentMap components;

    @Shadow
    public abstract Item getItem();

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("TAIL"))
    private void replaceWithComponents(ItemLike itemLike, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        performReplacement();
    }

    private void performReplacement() {
        if (this.item == null) {
            return;
        }

        if (isInCreativeModeTabBuilding()) {
            return;
        }

        // 检查是否应该跳过替换
        if (ReplacementControl.shouldSkipReplacement()) {
            return;
        }

        String originItemId = Utils.getItemRegistryName(this.item);
        String targetItemId = ItemReplacementCache.matchItem(originItemId);

        if (targetItemId != null) {
            Item newItem = Utils.getItemById(targetItemId);
            if (newItem != null) {
                DataComponentPatch currentPatch = this.components.asPatch();

                this.item = newItem;

                this.components = PatchedDataComponentMap.fromPatch(newItem.components(), currentPatch);

                newItem.verifyComponentsAfterLoad((ItemStack) (Object) this);

                Oneenoughitem.LOGGER.debug("Successfully replaced item {} with {}", originItemId, targetItemId);
            } else {
                Oneenoughitem.LOGGER.warn("Target item not found: {}", targetItemId);
            }
        }
    }

    @Inject(method = "is(Ljava/util/function/Predicate;)Z", at = @At("HEAD"), cancellable = true)
    private void extend(Predicate<Holder<Item>> predicate, CallbackInfoReturnable<Boolean> cir) {
        if (!OEIConfig.get().deeperReplace()) return;
        if (!predicate.test(getItem().builtInRegistryHolder())) {
            String itemId = Utils.getItemRegistryName(item);

            boolean matched = false;

            for (Item matchItem : ItemReplacementCache.trackSourceOf(itemId)) {
                if (predicate.test(matchItem.builtInRegistryHolder())) matched = true;
            }
            cir.setReturnValue(matched);
        }
    }

    @Inject(method = "is(Lnet/minecraft/core/Holder;)Z", at = @At("HEAD"), cancellable = true)
    private void extend(Holder<Item> itemHolder, CallbackInfoReturnable<Boolean> cir) {
        if (!OEIConfig.get().deeperReplace()) return;
        if (getItem().builtInRegistryHolder() != itemHolder) {
            String itemId = Utils.getItemRegistryName(item);

            boolean matched = false;

            for (Item matchItem : ItemReplacementCache.trackSourceOf(itemId)) {
                if (matchItem.builtInRegistryHolder() == itemHolder) {
                    matched = true;
                    break;
                }
            }
            cir.setReturnValue(matched);
        }
    }

    @Inject(method = "is(Lnet/minecraft/world/item/Item;)Z", at = @At("HEAD"), cancellable = true)
    private void extend(Item inputItem, CallbackInfoReturnable<Boolean> cir) {
        if (!OEIConfig.get().deeperReplace()) return;
        if (item != inputItem) {
            String inputItemId = Utils.getItemRegistryName(inputItem);
            String ItemId = Utils.getItemRegistryName(item);

            if (Utils.isItemIdEmpty(inputItemId) || Utils.isItemIdEmpty(ItemId)) return;

            boolean matched = false;

            for (String matchId : ItemReplacementCache.trackSourceIdOf(ItemId)) {
                if (matchId.equals(inputItemId)) {
                    matched = true;
                    break;
                }
            }

            cir.setReturnValue(matched);
        }
    }

    private boolean isInCreativeModeTabBuilding() {
        return ClientContext.isBuilding();
    }
}