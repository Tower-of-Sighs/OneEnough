package com.mafuyu404.oneenoughitem.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public final class ReplacementIndex<T> {
    private final Map<T, T> targets = new IdentityHashMap<>();
    private final Map<T, List<T>> sources = new IdentityHashMap<>();

    public ReplacementIndex(Map<T, T> mappings) {
        mappings.forEach((source, ignored) -> {
            T target = resolve(source, mappings::get);
            if (source != target) {
                targets.put(source, target);
                sources.computeIfAbsent(target, key -> new ArrayList<>()).add(source);
            }
        });
        sources.replaceAll((target, values) -> List.copyOf(values));
    }

    public static <T> T resolve(T source, Function<T, T> nextTarget) {
        Set<T> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        T target = source;
        while (visited.add(target)) {
            T next = nextTarget.apply(target);
            if (next == null || next == target) return target;
            target = next;
        }
        return source;
    }

    public T replacementOf(T source) {
        return targets.get(source);
    }

    public boolean matches(T actual, T expected) {
        return actual == expected || (actual != null && targets.get(expected) == actual);
    }

    public List<T> sourcesOf(T target) {
        return sources.getOrDefault(target, List.of());
    }
}
