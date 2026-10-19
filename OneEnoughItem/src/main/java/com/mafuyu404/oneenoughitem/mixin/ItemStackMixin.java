package com.mafuyu404.oneenoughitem.mixin;

import com.mafuyu404.oneenoughitem.Oneenoughitem;
import com.mafuyu404.oneenoughitem.client.ClientContext;
import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;
import com.mafuyu404.oneenoughitem.init.config.OEIConfig;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;
import com.mafuyu404.oneenoughitem.util.Utils;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.function.Predicate;

@Mixin(value = ItemStack.class)
public abstract class ItemStackMixin {
    @Mutable
    @Shadow
    @Final
    @Deprecated
    @Nullable
    private Item item;

    @Mutable
    @Shadow(remap = false)
    @Final
    @org.jetbrains.annotations.Nullable
    private Holder.Reference<Item> delegate;

    @Shadow
    public abstract Item getItem();

    @Inject(method = "forgeInit", at = @At("HEAD"), remap = false)
    private void replace(CallbackInfo ci) {
        if (item == null) {
            return;
        }

        // 检查是否应该跳过替换
        if (ReplacementControl.shouldSkipReplacement()) {
            return;
        }

        if (!ItemReplacementCache.hasReloadOverride()) {
            // 常规路径：按引用查表，不涉及注册名与字符串
            ItemReplacementCache.Replacement replacement = ItemReplacementCache.replacementOf(item);
            if (replacement == null) {
                return;
            }
            if (replacement.item() == Items.AIR && isInCreativeModeTabBuilding()) return;
            item = replacement.item();
            delegate = replacement.delegate();
            return;
        }

        try {
            String originItemId = Utils.getItemRegistryName(item);
            if (originItemId == null) {
                return;
            }

            String targetItemId = ItemReplacementCache.matchItem(originItemId);
            if (targetItemId != null) {
                if (targetItemId.equals("minecraft:air") && isInCreativeModeTabBuilding()) return;
                Item replacementItem = Utils.getItemById(targetItemId);
                if (replacementItem != null) {
                    item = replacementItem;
                    delegate = ForgeRegistries.ITEMS.getDelegateOrThrow(replacementItem);
                } else {
                    Oneenoughitem.LOGGER.warn("ItemStackMixin: Replacement item is null for targetItemId: {}, original item: {}",
                            targetItemId, originItemId);
                }
            }
        } catch (Exception e) {
            String itemInfo = item != null ? Utils.getItemRegistryName(item) : "null";
            Oneenoughitem.LOGGER.error("ItemStackMixin: Failed to replace item: {}", itemInfo, e);
        }
    }

    @Inject(method = "is(Ljava/util/function/Predicate;)Z", at = @At("HEAD"), cancellable = true)
    private void extend(Predicate<Holder<Item>> predicate, CallbackInfoReturnable<Boolean> cir) {
        if (!OEIConfig.get().deeperReplace()) return;
        if (predicate.test(getItem().builtInRegistryHolder())) return;
        boolean matched = false;
        for (Item matchItem : ItemReplacementCache.sourceItemsOf(item)) {
            if (predicate.test(matchItem.builtInRegistryHolder())) {
                matched = true;
                break;
            }
        }
        cir.setReturnValue(matched);
    }

    @Inject(method = "is(Lnet/minecraft/core/Holder;)Z", at = @At("HEAD"), cancellable = true)
    private void extend(Holder<Item> itemHolder, CallbackInfoReturnable<Boolean> cir) {
        if (!OEIConfig.get().deeperReplace()) return;
        if (getItem().builtInRegistryHolder() == itemHolder) return;
        var sources = ItemReplacementCache.sourceItemsOf(item);
        if (sources.isEmpty()) return;
        for (Item matchItem : sources) {
            if (matchItem.builtInRegistryHolder() == itemHolder) {
                cir.setReturnValue(true);
                return;
            }
        }
        cir.setReturnValue(false);
    }

    @Inject(method = "is(Lnet/minecraft/world/item/Item;)Z", at = @At("HEAD"), cancellable = true)
    private void extend(Item inputItem, CallbackInfoReturnable<Boolean> cir) {
        if (!OEIConfig.get().deeperReplace()) return;
        if (item != inputItem && ItemReplacementCache.isReplacedBy(inputItem, item)) {
            cir.setReturnValue(true);
        }
    }

    private boolean isInCreativeModeTabBuilding() {
        return ClientContext.isBuilding();
    }
}