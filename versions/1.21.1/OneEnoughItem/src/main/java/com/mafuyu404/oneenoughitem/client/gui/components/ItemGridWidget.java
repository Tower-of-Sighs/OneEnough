package com.mafuyu404.oneenoughitem.client.gui.components;

import com.mafuyu404.oneenoughitem.api.DomainRegistry;
import com.mafuyu404.oneenoughitem.client.gui.util.GuiUtils;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ItemGridWidget extends AbstractWidget {
    private final int gridWidth;
    private final int gridHeight;
    private final Consumer<ItemStack> onItemClick;
    private List<ItemStack> items = new ArrayList<>();
    private int hoveredIndex = -1;

    private Set<String> selectedItemIds = Collections.emptySet();

    public ItemGridWidget(int x, int y, int gridWidth, int gridHeight, Consumer<ItemStack> onItemClick) {
        super(x, y, gridWidth * 18, gridHeight * 18, Component.empty());
        this.gridWidth = gridWidth;
        this.gridHeight = gridHeight;
        this.onItemClick = onItemClick;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.hoveredIndex = -1;

        for (int i = 0; i < this.items.size() && i < this.gridWidth * 18 / 18 * this.gridHeight; i++) {
            int row = i / this.gridWidth;
            int col = i % this.gridWidth;
            int itemX = this.getX() + col * 18;
            int itemY = this.getY() + row * 18;

            GuiUtils.drawItemBox(graphics, itemX, itemY, 18, 18);

            ItemStack itemStack = this.items.get(i);
            String dataId = DomainRegistry.current().dataIdFromItem(itemStack.getItem());
            if (dataId != null && !dataId.isEmpty()) {
                DomainRegistry.current().renderDataId(graphics, dataId, itemX, itemY);
                DomainRegistry.current().uiAdapter().renderDataIndicators(graphics, dataId, itemX, itemY);
            } else {
                ReplacementControl.withSkipReplacement(() -> {
                    graphics.renderItem(itemStack, itemX + 1, itemY + 1);
                    graphics.renderItemDecorations(Minecraft.getInstance().font, itemStack, itemX + 1, itemY + 1);
                });
            }
            if (dataId != null && this.selectedItemIds.contains(dataId)) {
                graphics.fill(itemX + 1, itemY + 1, itemX + 17, itemY + 17, 0x8033AAFF);
            }

            if (mouseX >= itemX && mouseX < itemX + 18 && mouseY >= itemY && mouseY < itemY + 18) {
                graphics.fill(itemX + 1, itemY + 1, itemX + 17, itemY + 17, 0x80FFFFFF);
                this.hoveredIndex = i;
            }
        }

        if (this.hoveredIndex >= 0 && this.hoveredIndex < this.items.size()) {
            ItemStack hoveredStack = this.items.get(this.hoveredIndex);
            String dataId = DomainRegistry.current().dataIdFromItem(hoveredStack.getItem());
            if (dataId != null && !dataId.isEmpty()) {
                List<Component> tooltip = new ArrayList<>();
                Component name = DomainRegistry.current().displayName(dataId);
                tooltip.add(name);
                ReplacementControl.withSkipReplacement(() -> {
                    DomainRegistry.current().uiAdapter().addDataTooltip(tooltip, dataId);
                });
                var rl = ResourceLocation.tryParse(dataId);
                if (rl != null) {
                    String modId = rl.getNamespace();
                    String modName = ModList.get().getModContainerById(modId)
                            .map(c -> c.getModInfo().getDisplayName())
                            .orElse(modId);
                    tooltip.add(Component.literal(modName).withStyle(ChatFormatting.BLUE));
                }
                graphics.renderComponentTooltip(Minecraft.getInstance().font, tooltip, mouseX, mouseY);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.isHovered()) {
            int relativeX = (int) (mouseX - this.getX());
            int relativeY = (int) (mouseY - this.getY());
            int col = relativeX / 18;
            int row = relativeY / 18;
            int index = row * this.gridWidth + col;

            if (index >= 0 && index < this.items.size()) {
                this.onItemClick.accept(this.items.get(index));
                return true;
            }
        }
        return false;
    }

    public void setItems(List<ItemStack> items) {
        this.items = new ArrayList<>(items);
    }

    public void setSelectedItemIds(Set<String> selectedItemIds) {
        this.selectedItemIds = selectedItemIds != null ? selectedItemIds : Collections.emptySet();
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE, Component.literal("Item Grid"));
    }
}