package com.mafuyu404.oneenoughitem.api.adapter.ui;

import com.mafuyu404.oneenoughitem.api.ReplacementUiAdapter;
import com.mafuyu404.oneenoughitem.client.gui.util.ReplacementUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class ItemReplacementUiAdapter implements ReplacementUiAdapter {
    @Override
    public void addDataTooltip(List<Component> tooltip, String dataId) {
        ReplacementUtils.getReplacementInfo(dataId).addToTooltip(tooltip);
    }

    @Override
    public void addTagTooltip(List<Component> tooltip, ResourceLocation tagId) {
        ReplacementUtils.getTagReplacementInfo(tagId).addToTooltip(tooltip);
    }

    @Override
    public void renderDataIndicators(GuiGraphics g, String dataId, int x, int y) {
        var ri = ReplacementUtils.getReplacementInfo(dataId);
        if (ri.isReplaced()) {
            ReplacementUtils.ReplacementIndicator.renderItemReplaced(g, x, y);
        } else if (ri.isUsedAsResult()) {
            ReplacementUtils.ReplacementIndicator.renderItemUsedAsResult(g, x, y);
        }
    }

    @Override
    public void renderTagIndicators(GuiGraphics g, ResourceLocation tagId, int x, int y) {
        var ri = ReplacementUtils.getTagReplacementInfo(tagId);
        if (ri.isReplaced()) {
            ReplacementUtils.ReplacementIndicator.renderTagReplaced(g, x, y);
        }
    }
}