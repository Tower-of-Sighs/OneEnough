package cc.sighs.oneenoughitem.api;

import net.minecraft.resources.Identifier;

import java.util.Collection;

public interface DomainRuntimeCache {
    String matchData(String id);

    String matchTag(Identifier tagId);

    void removeReplacements(Collection<String> dataIds, Collection<String> tagIds);

    boolean isTagReplaced(String tagId);

    boolean isTagReplaced(Identifier tagId);
}