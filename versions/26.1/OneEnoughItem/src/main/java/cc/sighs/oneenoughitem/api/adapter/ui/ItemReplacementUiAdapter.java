package cc.sighs.oneenoughitem.api.adapter.ui;

import cc.sighs.oneenoughitem.api.ReplacementUiAdapter;
import cc.sighs.oneenoughitem.client.gui.util.ReplacementUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public class ItemReplacementUiAdapter implements ReplacementUiAdapter {
    @Override
    public void addDataTooltip(List<Component> tooltip, String dataId) {
        ReplacementUtils.getReplacementInfo(dataId).addToTooltip(tooltip);
    }

    @Override
    public void addTagTooltip(List<Component> tooltip, Identifier tagId) {
        ReplacementUtils.getTagReplacementInfo(tagId).addToTooltip(tooltip);
    }

    @Override
    public void renderDataIndicators(GuiGraphicsExtractor g, String dataId, int x, int y) {
        var ri = ReplacementUtils.getReplacementInfo(dataId);
        if (ri.isReplaced()) {
            ReplacementUtils.ReplacementIndicator.renderItemReplaced(g, x, y);
        } else if (ri.isUsedAsResult()) {
            ReplacementUtils.ReplacementIndicator.renderItemUsedAsResult(g, x, y);
        }
    }

    @Override
    public void renderTagIndicators(GuiGraphicsExtractor g, Identifier tagId, int x, int y) {
        var ri = ReplacementUtils.getTagReplacementInfo(tagId);
        if (ri.isReplaced()) {
            ReplacementUtils.ReplacementIndicator.renderTagReplaced(g, x, y);
        }
    }
}