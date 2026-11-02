package com.mafuyu404.oneenoughitem.client.gui;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ItemTagSelectionScreen extends BaseTagSelectionScreen {
    public ItemTagSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        super(parent, isForMatch, Component.translatable("gui.oneenoughitem.tag_selection.title"));
        this.allTags = BuiltInRegistries.ITEM.getTagNames()
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