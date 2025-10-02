package com.sighs.oneenoughblock.api.adapter;

import com.mafuyu404.oneenoughitem.api.DomainAdapter;
import com.mafuyu404.oneenoughitem.api.DomainRuntimeCache;
import com.mafuyu404.oneenoughitem.api.ReplacementUiAdapter;
import com.mafuyu404.oneenoughitem.client.gui.ReplacementEditorScreen;
import com.mafuyu404.oneenoughitem.client.gui.cache.AbstractGlobalReplacementCache;
import com.mafuyu404.oneenoughitem.client.gui.util.GuiUtils;
import com.mafuyu404.oneenoughitem.init.ReplacementControl;
import com.sighs.oneenoughblock.Oneenoughblock;
import com.sighs.oneenoughblock.api.adapter.ui.BlockReplacementUiAdapter;
import com.sighs.oneenoughblock.client.gui.BlockSelectionScreen;
import com.sighs.oneenoughblock.client.gui.BlockTagSelectionScreen;
import com.sighs.oneenoughblock.client.gui.cache.GlobalBlockReplacementCache;
import com.sighs.oneenoughblock.init.BlockReplacementCache;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;

public class BlockDomainAdapter implements DomainAdapter {
    @Override
    public String modId() {
        return Oneenoughblock.MODID;
    }

    @Override
    public String id() {
        return "oeb";
    }

    @Override
    public String dataId() {
        return "Blocks";
    }

    @Override
    public Component selectObjectLabel() {
        return Component.translatable("gui.oneenoughblock.add_block");
    }

    @Override
    public Component selectTagLabel() {
        return Component.translatable("gui.oneenoughblock.add_block_tag");
    }

    @Override
    public Screen createObjectSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        return new BlockSelectionScreen(parent, isForMatch);
    }

    @Override
    public Screen createTagSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        return new BlockTagSelectionScreen(parent, isForMatch);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public AbstractGlobalReplacementCache globalCache() {
        return GlobalBlockReplacementCache.get();
    }

    @Override
    public DomainRuntimeCache runtimeCache() {
        return new DomainRuntimeCache() {
            @Override
            public String matchData(String id) {
                return BlockReplacementCache.matchBlock(id);
            }

            @Override
            public String matchTag(ResourceLocation tagId) {
                return BlockReplacementCache.matchTag(tagId);
            }

            @Override
            public void removeReplacements(Collection<String> dataIds, Collection<String> tagIds) {
                BlockReplacementCache.removeReplacements(dataIds, tagIds);
            }

            @Override
            public boolean isTagReplaced(String tagId) {
                return BlockReplacementCache.isTagReplaced(tagId);
            }

            @Override
            public boolean isTagReplaced(ResourceLocation tagId) {
                return BlockReplacementCache.isTagReplaced(tagId);
            }
        };
    }

    @Override
    public String dataIdFromItem(Item item) {
        if (item instanceof BlockItem bi) {
            var key = ForgeRegistries.BLOCKS.getKey(bi.getBlock());
            return key != null ? key.toString() : null;
        }
        return null;
    }

    @Override
    public ItemStack iconForDataId(String dataId) {
        var rl = ResourceLocation.tryParse(dataId);
        var block = rl != null ? ForgeRegistries.BLOCKS.getValue(rl) : null;
        if (block == null) return ItemStack.EMPTY;
        return ReplacementControl.withSkipReplacement(() -> new ItemStack(block.asItem()));
    }

    @Override
    public void renderDataId(GuiGraphics graphics, String dataId, int x, int y) {
        var stack = iconForDataId(dataId);
        GuiUtils.drawItemBox(graphics, x, y, 18, 18);
        graphics.renderItem(stack, x + 1, y + 1);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x + 1, y + 1);
    }

    @Override
    public Component displayName(String dataId) {
        var rl = ResourceLocation.tryParse(dataId);
        var block = rl != null ? ForgeRegistries.BLOCKS.getValue(rl) : null;
        if (block == null) return Component.literal(dataId);
        var stack = new ItemStack(block.asItem());
        return stack.getHoverName();
    }

    @Override
    public ReplacementUiAdapter uiAdapter() {
        return new BlockReplacementUiAdapter(runtimeCache(), globalCache());
    }
}