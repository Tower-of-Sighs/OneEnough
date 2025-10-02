package com.mafuyu404.oneenoughitem.client.gui.util;

import com.mafuyu404.oneenoughitem.api.DomainAdapter;
import com.mafuyu404.oneenoughitem.api.DomainRegistry;

import java.util.Set;
import java.util.stream.Collectors;

public class ModUtils {

    public static boolean hasAnyDomainModLoaded() {
        return DomainRegistry.all().values().stream()
                .anyMatch(DomainAdapter::isAvailable);
    }

    public static Set<String> getLoadedModIds() {
        return DomainRegistry.all().values().stream()
                .filter(DomainAdapter::isAvailable)
                .map(DomainAdapter::modId)
                .collect(Collectors.toSet());
    }
}