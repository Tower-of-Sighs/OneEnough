package com.mafuyu404.oneenoughitem.event.base;

import cc.sighs.oelib.neoforge.data.DataManager;
import com.mafuyu404.oneenoughitem.Oneenoughitem;
import com.mafuyu404.oneenoughitem.data.Replacements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.HolderLookup;

import java.util.Map;

public abstract class AbstractReplacementEventHandler<T> {

    protected abstract void clearModuleCache();

    protected abstract void putToModuleCache(Replacements r, HolderLookup.RegistryLookup<T> registryLookup);

    protected abstract boolean tryResolveData(String id, HolderLookup.RegistryLookup<T> registryLookup);

    protected abstract boolean tryResolveTag(String tagId, HolderLookup.RegistryLookup<T> registryLookup);

    protected Replacements buildReplacements(Replacements r) {
        return r;
    }

    protected boolean acceptLocation(ResourceLocation location) {
        return true;
    }

    private boolean isValidForCurrentDomain(Replacements r, HolderLookup.RegistryLookup<T> registryLookup) {
        String result = r.result();
        if (result == null || result.isBlank()) return false;
        boolean resultOk = result.startsWith("#")
                ? tryResolveTag(result.substring(1), registryLookup)
                : tryResolveData(result, registryLookup);
        if (!resultOk) return false;
        for (String id : r.match()) {
            if (id == null || id.isBlank()) continue;
            boolean ok = id.startsWith("#")
                    ? tryResolveTag(id.substring(1), registryLookup)
                    : tryResolveData(id, registryLookup);
            if (ok) return true;
        }
        return false;
    }

    public void rebuildReplacementCache(String reason, DataManager<Replacements> manager, HolderLookup.RegistryLookup<T> registryLookup) {
        if (manager == null) {
            Oneenoughitem.LOGGER.warn("No replacement data manager found (reason: {})", reason);
            return;
        }
        clearModuleCache();
        for (Map.Entry<ResourceLocation, Replacements> e : manager.getAllData().entrySet()) {
            ResourceLocation location = e.getKey();
            if (!acceptLocation(location)) continue;
            Replacements rr = buildReplacements(e.getValue());
            if (isValidForCurrentDomain(rr, registryLookup)) {
                putToModuleCache(rr, registryLookup);
            }
        }
        Oneenoughitem.LOGGER.debug("Rebuilt replacement cache (reason: {})", reason);
    }
}