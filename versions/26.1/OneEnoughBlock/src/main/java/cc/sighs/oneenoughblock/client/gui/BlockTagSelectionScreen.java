package cc.sighs.oneenoughblock.client.gui;

import cc.sighs.oneenoughitem.client.gui.BaseTagSelectionScreen;
import cc.sighs.oneenoughitem.client.gui.ReplacementEditorScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BlockTagSelectionScreen extends BaseTagSelectionScreen {
    public BlockTagSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        super(parent, isForMatch, Component.literal("gui.oneenoughblock.add_block_tag"));
        this.allTags = BuiltInRegistries.BLOCK.getTags()
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
