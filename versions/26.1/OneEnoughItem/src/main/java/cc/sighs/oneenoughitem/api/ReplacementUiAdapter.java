package cc.sighs.oneenoughitem.api;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public interface ReplacementUiAdapter {
    void addDataTooltip(List<Component> tooltip, String dataId);

    void addTagTooltip(List<Component> tooltip, Identifier tagId);

    void renderDataIndicators(GuiGraphicsExtractor graphics, String dataId, int x, int y);

    void renderTagIndicators(GuiGraphicsExtractor graphics, Identifier tagId, int x, int y);
}