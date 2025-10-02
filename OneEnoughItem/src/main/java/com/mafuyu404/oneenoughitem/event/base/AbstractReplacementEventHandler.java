package com.mafuyu404.oneenoughitem.event.base;

import com.mafuyu404.oelib.forge.data.DataManager;
import com.mafuyu404.oneenoughitem.Oneenoughitem;
import com.mafuyu404.oneenoughitem.data.Replacements;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public abstract class AbstractReplacementEventHandler {

    protected abstract void clearModuleCache();

    protected abstract void putToModuleCache(Replacements r);

    protected abstract boolean tryResolveData(String id);

    protected abstract boolean tryResolveTag(String tagId);

    protected Replacements buildReplacements(Replacements r) {
        return r;
    }

    private boolean isValidForCurrentDomain(Replacements r) {
        String result = r.result();
        if (result == null || result.isBlank()) return false;
        boolean resultOk = result.startsWith("#")
                ? tryResolveTag(result.substring(1))
                : tryResolveData(result);
        if (!resultOk) return false;
        for (String id : r.match()) {
            if (id == null || id.isBlank()) continue;
            boolean ok = id.startsWith("#")
                    ? tryResolveTag(id.substring(1))
                    : tryResolveData(id);
            if (ok) return true;
        }
        return false;
    }

    protected boolean acceptLocation(ResourceLocation location) {
        return true;
    }

    public void rebuildReplacementCache(String reason, DataManager<Replacements> manager) {
        if (manager == null) {
            Oneenoughitem.LOGGER.warn("No replacement data manager found (reason: {})", reason);
            return;
        }
        clearModuleCache();
        for (Map.Entry<ResourceLocation, Replacements> e : manager.getAllData().entrySet()) {
            ResourceLocation location = e.getKey();
            if (!acceptLocation(location)) continue;
            Replacements rr = buildReplacements(e.getValue());
            if (isValidForCurrentDomain(rr)) {
                putToModuleCache(rr);
            }
        }
        Oneenoughitem.LOGGER.debug("Rebuilt replacement cache (reason: {})", reason);
    }
}