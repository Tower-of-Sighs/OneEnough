package cc.sighs.oneenoughitem.mixin;

import cc.sighs.oneenoughitem.client.ClientContext;
import cc.sighs.oneenoughitem.init.ItemReplacementCache;
import cc.sighs.oneenoughitem.init.access.CreativeModeTabIconRefresher;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackLinkedSet;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mixin(CreativeModeTab.class)
public abstract class CreativeModeTabMixin implements CreativeModeTabIconRefresher {
    @Mutable @Shadow @Nullable
    private ItemStack iconItemStack;

    @Redirect(method = "buildContents", at = @At(value = "INVOKE", target =
            "Lnet/neoforged/neoforge/event/EventHooks;onCreativeModeTabBuildContents(Lnet/minecraft/world/item/CreativeModeTab;Lnet/minecraft/world/item/CreativeModeTab$DisplayItemsGenerator;Lnet/minecraft/world/item/CreativeModeTab$ItemDisplayParameters;Lnet/minecraft/world/item/CreativeModeTab$Output;)V", remap = false))
    private void oei$buildUnmodifiedContents(CreativeModeTab tab,
            CreativeModeTab.DisplayItemsGenerator generator, CreativeModeTab.ItemDisplayParameters parameters,
            CreativeModeTab.Output output) {
        List<ItemStack> stacks = new ArrayList<>();
        List<CreativeModeTab.TabVisibility> visibility = new ArrayList<>();
        ClientContext.beginBuilding();
        try {
            // Mods must be able to build and position their original entries before unification.
            EventHooks.onCreativeModeTabBuildContents(tab, generator, parameters, (stack, mode) -> {
                stacks.add(stack);
                visibility.add(mode);
            });
        } finally {
            ClientContext.endBuilding();
        }
        Set<ItemStack> parent = ItemStackLinkedSet.createTypeAndComponentsSet();
        Set<ItemStack> search = ItemStackLinkedSet.createTypeAndComponentsSet();
        for (int i = 0; i < stacks.size(); i++) {
            ItemStack original = stacks.get(i);
            ItemStack stack = ItemReplacementCache.replacementOf(original.getItem()) != null ? original.copy() : original;
            if (stack.isEmpty()) continue;
            if (visibility.get(i) != CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY) parent.add(stack);
            if (visibility.get(i) != CreativeModeTab.TabVisibility.PARENT_TAB_ONLY) search.add(stack);
        }
        for (ItemStack stack : parent) {
            output.accept(stack, search.contains(stack) ? CreativeModeTab.TabVisibility.PARENT_AND_SEARCH_TABS
                    : CreativeModeTab.TabVisibility.PARENT_TAB_ONLY);
        }
        for (ItemStack stack : search) {
            if (!parent.contains(stack)) output.accept(stack, CreativeModeTab.TabVisibility.SEARCH_TAB_ONLY);
        }
    }

    @Override
    public void oei$refreshIconCache() {
        iconItemStack = null;
    }
}
