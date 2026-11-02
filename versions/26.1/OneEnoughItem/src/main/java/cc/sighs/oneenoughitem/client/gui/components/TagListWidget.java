package cc.sighs.oneenoughitem.client.gui.components;

import cc.sighs.oneenoughitem.api.DomainRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class TagListWidget extends ObjectSelectionList<TagListWidget.TagEntry> {
    private final Consumer<Identifier> onTagSelect;
    private Set<Identifier> selectedTags = Collections.emptySet();

    public TagListWidget(Minecraft minecraft, int width, int height, int y, int itemHeight, Consumer<Identifier> onTagSelect) {
        super(minecraft, width, height, y, itemHeight);
        this.onTagSelect = onTagSelect;
    }


    public void setTags(List<Identifier> tags) {
        this.clearEntries();
        for (Identifier tag : tags) {
            this.addEntry(new TagEntry(tag));
        }
    }

    public void setSelectedTags(java.util.Set<Identifier> selectedTags) {
        this.selectedTags = selectedTags != null ? selectedTags : Collections.emptySet();
    }

    public TagEntry getEntryAtMouse(double mouseX, double mouseY) {
        return this.getEntryAtPosition(mouseX, mouseY);
    }

    public class TagEntry extends ObjectSelectionList.Entry<TagEntry> {
        private final Identifier tagId;

        public TagEntry(Identifier tagId) {
            this.tagId = tagId;
        }

        @Override
        public void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            int x = this.getContentX();
            int y = this.getContentY();
            int entryWidth = this.getContentWidth();
            int entryHeight = this.getContentHeight();
            if (TagListWidget.this.selectedTags.contains(this.tagId)) {
                graphics.fill(x, y, x + entryWidth, y + entryHeight, 0x8033AAFF);
            }
            String tagText = "#" + this.tagId.toString();
            boolean replaced = DomainRegistry.current().runtimeCache().isTagReplaced(this.tagId)
                    || DomainRegistry.current().globalCache().getTagReplacement(this.tagId.toString()) != null;
            int textColor = replaced ? 0xFFFF5555 : 0xFFFFFFFF;
            if (isMouseOver && !replaced) {
                textColor = 0xFFFFFF88;
            }
            graphics.text(TagListWidget.this.minecraft.font, tagText, x + 5, y + 5, textColor);
            if (replaced) {
                DomainRegistry.current().uiAdapter().renderTagIndicators(graphics, this.tagId, x + entryWidth - 20, y + 2);
            }
            if (isMouseOver) {
                graphics.setComponentTooltipForNextFrame(TagListWidget.this.minecraft.font, this.getTooltip(), mouseX, mouseY);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            TagListWidget.this.onTagSelect.accept(this.tagId);
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal("#" + this.tagId.toString());
        }

        public List<Component> getTooltip() {
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal("#" + this.tagId.toString()));
            DomainRegistry.current().uiAdapter().addTagTooltip(tooltip, this.tagId);
            tooltip.add(Component.literal(this.tagId.getNamespace()).withStyle(ChatFormatting.BLUE));
            return tooltip;
        }
    }
}
