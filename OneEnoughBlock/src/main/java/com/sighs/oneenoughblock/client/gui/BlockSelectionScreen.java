package com.sighs.oneenoughblock.client.gui;

import com.mafuyu404.oneenoughitem.client.gui.BaseObjectSelectionScreen;
import com.mafuyu404.oneenoughitem.client.gui.ReplacementEditorScreen;
import com.mafuyu404.oneenoughitem.client.gui.util.GuiUtils;
import com.mafuyu404.oneenoughitem.init.ReplacementControl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class BlockSelectionScreen extends BaseObjectSelectionScreen<Block> {

    public BlockSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        super(parent, isForMatch, Component.literal("gui.oneenoughblock.add_block"));
        this.allObjects = loadAllObjects();
        this.filteredObjects = new ArrayList<>(this.allObjects);
        applySort();
    }

    @Override
    protected Component sortLabel() {
        return Component.translatable("gui.oneenoughitem.sort.name");
    }

    @Override
    protected List<Block> loadAllObjects() {
        return ForgeRegistries.BLOCKS.getValues().stream()
                .filter(b -> b.asItem() != Items.AIR)
                .collect(Collectors.toList());
    }

    @Override
    protected String getId(Block obj) {
        var key = ForgeRegistries.BLOCKS.getKey(obj);
        return key != null ? key.toString() : null;
    }

    @Override
    protected void renderObject(Block obj, GuiGraphics graphics, int x, int y) {
        ItemStack stack = ReplacementControl.withSkipReplacement(() -> new ItemStack(obj.asItem()));
        GuiUtils.drawItemBox(graphics, x, y, 18, 18);
        graphics.renderItem(stack, x + 1, y + 1);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x + 1, y + 1);
    }

    @Override
    protected void onSelectSingle(String id) {
        var key = ResourceLocation.tryParse(id);
        if (key == null) return;
        Block block = ForgeRegistries.BLOCKS.getValue(key);
        if (block == null || block.asItem() == Items.AIR) return;
        if (this.isForMatch) this.parent.addMatchItem(block.asItem());
        else this.parent.setResultItem(block.asItem());
    }

    @Override
    protected boolean isSelectable(String id, boolean forMatch) {
        var key = ResourceLocation.tryParse(id);
        if (key == null) return false;
        Block block = ForgeRegistries.BLOCKS.getValue(key);
        return block != null && block.asItem() != Items.AIR;
    }

    @Override
    protected Comparator<Block> getComparator(SortMode mode) {
        return switch (mode) {
            case NAME -> Comparator.comparing(b -> new ItemStack(b.asItem()).getHoverName().getString());
            case MOD ->
                    Comparator.comparing(b -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(b)).getNamespace());
            case ID -> Comparator.comparing(b -> Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(b)).toString());
        };
    }

    @Override
    protected void updateGrid() {

        if (this.itemGrid == null) return;

        List<ItemStack> pageItems = new ArrayList<>();
        int startIndex = this.currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, this.filteredObjects.size());
        for (int i = startIndex; i < endIndex; i++) {
            Block b = this.filteredObjects.get(i);
            ItemStack stack = ReplacementControl.withSkipReplacement(() -> new ItemStack(b.asItem()));
            pageItems.add(stack);
        }
        this.itemGrid.setItems(pageItems);
        this.itemGrid.setSelectedItemIds(this.selectedIds);
    }
}