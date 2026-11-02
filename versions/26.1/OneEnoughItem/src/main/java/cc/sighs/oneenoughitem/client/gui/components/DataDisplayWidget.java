package cc.sighs.oneenoughitem.client.gui.components;

import cc.sighs.oneenoughitem.Oneenoughitem;
import cc.sighs.oneenoughitem.api.DomainRegistry;
import cc.sighs.oneenoughitem.client.gui.util.GuiUtils;
import cc.sighs.oneenoughitem.util.ReplacementControl;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.fml.ModList;

import java.util.ArrayList;
import java.util.List;

public class DataDisplayWidget extends AbstractWidget {
    private final String dataId;
    private final Button.OnPress removeAction;
    private final boolean skipReplacement;
    private static final Identifier CROSS_TEX =
            Identifier.fromNamespaceAndPath(Oneenoughitem.MODID, "textures/gui/cross.png");

    public DataDisplayWidget(int x, int y, String dataId, Button.OnPress removeAction, boolean skipReplacement) {
        super(x, y, 18, 18, Component.empty());
        this.dataId = dataId;
        this.removeAction = removeAction;
        this.skipReplacement = skipReplacement;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        GuiUtils.drawItemBox(graphics, this.getX(), this.getY(), this.width, this.height);

        if (this.dataId != null && !this.dataId.isEmpty()) {
            if (skipReplacement) {
                ReplacementControl.withSkipReplacement(() -> {
                    DomainRegistry.current().renderDataId(graphics, dataId, this.getX(), this.getY());
                });
            } else {
                DomainRegistry.current().renderDataId(graphics, dataId, this.getX(), this.getY());
            }
        }

        if (this.isHovered() && this.removeAction != null) {
            int crossX = this.getX() + this.width - 9;
            int crossY = this.getY() + 1;
            GuiUtils.blit(graphics, CROSS_TEX, crossX, crossY, 0, 0, 8, 8, 8, 8);
        }

        if (this.isHovered() && !this.dataId.isEmpty()) {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }

    public void renderTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!this.isHovered() || this.dataId.isEmpty()) return;

        renderToolTip(graphics, mouseX, mouseY);
    }

    public void renderToolTip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (!this.isHovered() || this.dataId.isEmpty()) return;

        List<Component> lines = new ArrayList<>();

        Component name;
        if (skipReplacement) {
            name = ReplacementControl.withSkipReplacement(
                    () -> DomainRegistry.current().displayName(this.dataId)
            );
        } else {
            name = DomainRegistry.current().displayName(this.dataId);
        }

        if (name == null || name.getString().isBlank()) {
            name = Component.literal(this.dataId);
        }
        lines.add(name);

        var rl = Identifier.tryParse(this.dataId);
        if (rl != null) {
            String modId = rl.getNamespace();
            String modName = ModList.get().getModContainerById(modId)
                    .map(c -> c.getModInfo().getDisplayName())
                    .orElse(modId);
            lines.add(Component.literal(modName).withStyle(ChatFormatting.BLUE));
        }

        graphics.setComponentTooltipForNextFrame(Minecraft.getInstance().font, lines, mouseX, mouseY);
    }

    public String getDataId() {
        return this.dataId;
    }

    public void setPosition(int x, int y) {
        this.setX(x);
        this.setY(y);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        narrationElementOutput.add(NarratedElementType.TITLE,
                DomainRegistry.current().displayName(this.dataId));
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (event.button() == 0 && this.isHovered() && this.removeAction != null) {
            int crossX = this.getX() + this.width - 9;
            int crossY = this.getY() + 1;
            if (mouseX >= crossX && mouseX < crossX + 8 && mouseY >= crossY && mouseY < crossY + 8) {
                this.removeAction.onPress(null);
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
