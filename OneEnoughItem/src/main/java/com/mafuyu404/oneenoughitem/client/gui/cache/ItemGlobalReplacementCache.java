package com.mafuyu404.oneenoughitem.client.gui.cache;

import com.mafuyu404.oneenoughitem.Oneenoughitem;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ItemGlobalReplacementCache extends AbstractGlobalReplacementCache {
    private static final ItemGlobalReplacementCache INSTANCE = new ItemGlobalReplacementCache();

    private ItemGlobalReplacementCache() {
        super("oei", "oei");
    }

    @Override
    protected String domainId() {
        return "oei";
    }

    public static AbstractGlobalReplacementCache get() {
        return INSTANCE;
    }

    private final Map<String, String> replacedItems = new ConcurrentHashMap<>();
    private final Map<String, String> replacedTags = new ConcurrentHashMap<>();
    private final Map<String, String> resultItems = new ConcurrentHashMap<>();
    private final Map<String, String> resultTags = new ConcurrentHashMap<>();
    private volatile boolean needsRebuild = false;

    @Override
    protected void onInitialized() {
        Oneenoughitem.LOGGER.info("Global replacement cache initialized with {} items and {} tags",
                replacedItems.size(), replacedTags.size());
        if (needsRebuild) {
            Oneenoughitem.LOGGER.warn("Global replacement cache scheduled rebuild due to version mismatch or old format; rebuilding now");
            needsRebuild = false;
            rebuild();
        }
    }

    @Override
    protected void onVersionMismatch(int foundVersion) {
        Oneenoughitem.LOGGER.warn("Global replacement cache version mismatch (found {} != expected {}), will rebuild after initialization", foundVersion, this.cacheVersion);
        needsRebuild = true;
    }

    @Override
    protected void onLoadError(IOException e) {
        clearAllMaps();
    }

    @Override
    protected void loadData(DataInputStream dis) throws IOException {
        readStringMap(dis, replacedItems);
        readStringMap(dis, replacedTags);

        try {
            readStringMap(dis, resultItems);
            readStringMap(dis, resultTags);
        } catch (IOException e) {
            Oneenoughitem.LOGGER.info("Old cache format detected, scheduling rebuild to include result tracking");
            needsRebuild = true;
        }
    }

    @Override
    protected void saveData(DataOutputStream dos) throws IOException {
        writeStringMap(dos, replacedItems);
        writeStringMap(dos, replacedTags);
        writeStringMap(dos, resultItems);
        writeStringMap(dos, resultTags);
    }

    public static boolean isItemUsedAsResult(String itemId) {
        return INSTANCE.withInitializedReadLock(() ->
                isValidString(itemId) && INSTANCE.resultItems.containsKey(itemId));
    }

    public static Set<String> getAllReplacedItems() {
        return INSTANCE.withInitializedReadLock(() ->
                new HashSet<>(INSTANCE.replacedItems.keySet()));
    }
}