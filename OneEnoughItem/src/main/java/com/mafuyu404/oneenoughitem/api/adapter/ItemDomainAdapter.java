package com.mafuyu404.oneenoughitem.api.adapter;

import com.mafuyu404.oneenoughitem.Oneenoughitem;
import com.mafuyu404.oneenoughitem.api.DomainAdapter;
import com.mafuyu404.oneenoughitem.api.DomainRuntimeCache;
import com.mafuyu404.oneenoughitem.api.ReplacementUiAdapter;
import com.mafuyu404.oneenoughitem.api.adapter.ui.ItemReplacementUiAdapter;
import com.mafuyu404.oneenoughitem.client.gui.ItemSelectionScreen;
import com.mafuyu404.oneenoughitem.client.gui.ItemTagSelectionScreen;
import com.mafuyu404.oneenoughitem.client.gui.ReplacementEditorScreen;
import com.mafuyu404.oneenoughitem.client.gui.cache.AbstractGlobalReplacementCache;
import com.mafuyu404.oneenoughitem.client.gui.cache.ItemGlobalReplacementCache;
import com.mafuyu404.oneenoughitem.client.gui.util.GuiUtils;
import com.mafuyu404.oneenoughitem.init.ItemReplacementCache;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;
import com.mafuyu404.oneenoughitem.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;

public class ItemDomainAdapter implements DomainAdapter {
    @Override
    public String modId() {
        return Oneenoughitem.MODID;
    }

    @Override
    public String id() {
        return "oei";
    }

    @Override
    public String dataId() {
        return "Items";
    }

    @Override
    public Component selectObjectLabel() {
        return Component.translatable("gui.oneenoughitem.add_item");
    }

    @Override
    public Component selectTagLabel() {
        return Component.translatable("gui.oneenoughitem.add_item_tag");
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Screen createObjectSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        return new ItemSelectionScreen(parent, isForMatch);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Screen createTagSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        return new ItemTagSelectionScreen(parent, isForMatch);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public AbstractGlobalReplacementCache globalCache() {
        return ItemGlobalReplacementCache.get();
    }

    @Override
    public DomainRuntimeCache runtimeCache() {
        return new DomainRuntimeCache() {
            @Override
            public String matchData(String id) {
                return ItemReplacementCache.matchItem(id);
            }

            @Override
            public String matchTag(ResourceLocation tagId) {
                return ItemReplacementCache.matchTag(tagId);
            }

            @Override
            public void removeReplacements(Collection<String> dataIds, Collection<String> tagIds) {
                ItemReplacementCache.removeReplacements(dataIds, tagIds);
            }

            @Override
            public boolean isTagReplaced(String tagId) {
                return ItemReplacementCache.isTagReplaced(tagId);
            }

            @Override
            public boolean isTagReplaced(ResourceLocation tagId) {
                return ItemReplacementCache.isTagReplaced(tagId);
            }
        };
    }

    @Override
    public String dataIdFromItem(Item item) {
        return Utils.getItemRegistryName(item);
    }

    @Override
    public ItemStack iconForDataId(String dataId) {
        var rl = ResourceLocation.tryParse(dataId);
        var item = rl != null ? ForgeRegistries.ITEMS.getValue(rl) : null;
        return item != null ? ReplacementControl.withSkipReplacement(() -> new ItemStack(item)) : ItemStack.EMPTY;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void renderDataId(GuiGraphics graphics, String dataId, int x, int y) {
        var stack = iconForDataId(dataId);
        GuiUtils.drawItemBox(graphics, x, y, 18, 18);
        graphics.renderItem(stack, x + 1, y + 1);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x + 1, y + 1);
    }

    @Override
    public Component displayName(String dataId) {
        if (dataId == null || dataId.isEmpty()) {
            return Component.literal("");
        }
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(dataId));
        if (item == null) {
            return Component.literal(dataId);
        }
        ItemStack stack = new ItemStack(item);
        return stack.getHoverName();
    }

    @Override
    public ReplacementUiAdapter uiAdapter() {
        return new ItemReplacementUiAdapter();
    }
}