package com.mafuyu404.oneenoughitem.api;

import java.util.LinkedHashMap;
import java.util.Map;

public final class DomainRegistry {
    private static final Map<String, DomainAdapter> ADAPTERS = new LinkedHashMap<>();
    private static DomainAdapter CURRENT;

    public static void register(DomainAdapter adapter) {
        ADAPTERS.put(adapter.id(), adapter);
        if (CURRENT == null && adapter.isAvailable()) {
            CURRENT = adapter;
        }
    }

    public static DomainAdapter current() {
        return CURRENT;
    }

    public static boolean has(String id) {
        return ADAPTERS.containsKey(id) && ADAPTERS.get(id).isAvailable();
    }

    public static boolean switchTo(String id) {
        DomainAdapter a = ADAPTERS.get(id);
        if (a != null && a.isAvailable()) {
            CURRENT = a;
            return true;
        }
        return false;
    }

    public static Map<String, DomainAdapter> all() {
        return ADAPTERS;
    }

    public static String currentDataId() {
        return CURRENT != null ? CURRENT.dataId() : "Items";
    }

    public static String currentModId() {
        return CURRENT != null ? CURRENT.modId() : "oneenoughitem";
    }

    public static String currentId() {
        return CURRENT != null ? CURRENT.id() : "oei";
    }

    public static DomainAdapter get(String id) {
        return ADAPTERS.get(id);
    }
}