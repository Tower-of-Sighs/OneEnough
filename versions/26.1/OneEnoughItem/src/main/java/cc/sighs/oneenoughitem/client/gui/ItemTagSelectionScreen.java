package cc.sighs.oneenoughitem.client.gui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ItemTagSelectionScreen extends BaseTagSelectionScreen {
    public ItemTagSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        super(parent, isForMatch, Component.translatable("gui.oneenoughitem.tag_selection.title"));
        this.allTags = BuiltInRegistries.ITEM.getTags()
                .map(tag -> tag.key().location())
                .sorted(Comparator.comparing(Identifier::toString))
                .collect(Collectors.toList());
        this.filteredTags = new ArrayList<>(this.allTags);
    }

    @Override
    protected List<Identifier> loadAllTags() {
        return this.allTags;
    }
}
