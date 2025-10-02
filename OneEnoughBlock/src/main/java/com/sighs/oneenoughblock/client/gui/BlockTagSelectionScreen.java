package com.sighs.oneenoughblock.client.gui;

import com.mafuyu404.oneenoughitem.client.gui.BaseTagSelectionScreen;
import com.mafuyu404.oneenoughitem.client.gui.ReplacementEditorScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BlockTagSelectionScreen extends BaseTagSelectionScreen {
    public BlockTagSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        super(parent, isForMatch, Component.literal("gui.oneenoughblock.add_block_tag"));
        this.allTags = ForgeRegistries.BLOCKS.tags().getTagNames()
                .map(TagKey::location)
                .sorted(Comparator.comparing(ResourceLocation::toString))
                .collect(Collectors.toList());
        this.filteredTags = new ArrayList<>(this.allTags);
    }

    @Override
    protected List<ResourceLocation> loadAllTags() {
        return this.allTags;
    }
}