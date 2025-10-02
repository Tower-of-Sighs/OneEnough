package com.mafuyu404.oneenoughitem.api;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public interface ReplacementUiAdapter {
    void addDataTooltip(List<Component> tooltip, String dataId);

    void addTagTooltip(List<Component> tooltip, ResourceLocation tagId);

    void renderDataIndicators(GuiGraphics graphics, String dataId, int x, int y);

    void renderTagIndicators(GuiGraphics graphics, ResourceLocation tagId, int x, int y);
}