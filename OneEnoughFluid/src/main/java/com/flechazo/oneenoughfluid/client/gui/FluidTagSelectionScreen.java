package com.flechazo.oneenoughfluid.client.gui;

import com.mafuyu404.oneenoughitem.client.gui.BaseTagSelectionScreen;
import com.mafuyu404.oneenoughitem.client.gui.ReplacementEditorScreen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FluidTagSelectionScreen extends BaseTagSelectionScreen {
    public FluidTagSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        super(parent, isForMatch, Component.translatable("gui.oneenoughfluid.add_fluid_tag"));
        this.allTags = BuiltInRegistries.FLUID.getTagNames()
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