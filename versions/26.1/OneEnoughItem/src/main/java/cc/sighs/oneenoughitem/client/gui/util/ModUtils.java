package cc.sighs.oneenoughitem.client.gui.util;

import cc.sighs.oneenoughitem.api.DomainAdapter;
import cc.sighs.oneenoughitem.api.DomainRegistry;

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