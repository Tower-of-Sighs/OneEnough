package com.mafuyu404.oneenoughitem.api;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;

public interface DomainRuntimeCache {
    String matchData(String id);

    String matchTag(ResourceLocation tagId);

    void removeReplacements(Collection<String> dataIds, Collection<String> tagIds);

    boolean isTagReplaced(String tagId);

    boolean isTagReplaced(ResourceLocation tagId);
}