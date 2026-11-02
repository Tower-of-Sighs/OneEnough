package com.mafuyu404.oneenoughitem.client.gui.cache;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.mafuyu404.oneenoughitem.Oneenoughitem;
import com.mafuyu404.oneenoughitem.client.gui.util.PathUtils;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mojang.serialization.JsonOps;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public abstract class AbstractGlobalReplacementCache extends BaseCache {
    protected final Map<String, String> replacedData = new ConcurrentHashMap<>();
    protected final Map<String, String> replacedTags = new ConcurrentHashMap<>();
    protected final Map<String, String> resultData = new ConcurrentHashMap<>();
    protected final Map<String, String> resultTags = new ConcurrentHashMap<>();
    protected volatile boolean needsRebuild = false;

    protected AbstractGlobalReplacementCache(String dirName, String domainId) {
        super(dirName, domainId + "_global_replacement_cache.dat", 2);
    }

    protected abstract String domainId();

    @Override
    protected void onInitialized() {
        Oneenoughitem.LOGGER.info("{} global replacement cache initialized (data: {}, tags: {})",
                domainId(), replacedData.size(), replacedTags.size());
        if (needsRebuild) {
            needsRebuild = false;
            rebuild();
        }
    }

    @Override
    protected void onVersionMismatch(int foundVersion) {
        Oneenoughitem.LOGGER.warn("{} cache version mismatch (found {} != expected {}), scheduling rebuild",
                domainId(), foundVersion, this.cacheVersion);
        needsRebuild = true;
    }

    @Override
    protected void onLoadError(IOException e) {
        clearAllMaps();
    }

    @Override
    protected void loadData(DataInputStream dis) throws IOException {
        readStringMap(dis, replacedData);
        readStringMap(dis, replacedTags);
        try {
            readStringMap(dis, resultData);
            readStringMap(dis, resultTags);
        } catch (IOException e) {
            Oneenoughitem.LOGGER.info("{} old cache format detected, scheduling rebuild", domainId());
            needsRebuild = true;
        }
    }

    @Override
    protected void saveData(DataOutputStream dos) throws IOException {
        writeStringMap(dos, replacedData);
        writeStringMap(dos, replacedTags);
        writeStringMap(dos, resultData);
        writeStringMap(dos, resultTags);
    }

    public void addReplacement(Collection<String> matchData, Collection<String> matchTags,
                               String resultDataStr, String resultTagStr, String sourceFile) {
        withInitializedWriteLock(() -> {
            String result = resultDataStr != null ? resultDataStr : resultTagStr;
            if (result == null || result.isEmpty()) {
                Oneenoughitem.LOGGER.warn("Cannot add replacement with null/empty result from file: {}", sourceFile);
                return;
            }
            processValidStrings(matchData, id -> replacedData.put(id, result));
            processValidStrings(matchTags, tagId -> replacedTags.put(tagId, result));
            addToResultMap(resultDataStr, sourceFile, resultData);
            addToResultMap(resultTagStr, sourceFile, resultTags);
            saveToFileAsync();
        });
    }

    public void removeReplacement(Collection<String> matchData, Collection<String> matchTags) {
        withInitializedWriteLock(() -> {
            boolean changed = false;
            changed |= removeFromReplacementMap(matchData, replacedData, "data");
            changed |= removeFromReplacementMap(matchTags, replacedTags, "tag");
            if (changed) saveToFileAsync();
        });
    }

    public boolean isDataReplaced(String id) {
        return withInitializedReadLock(() -> isValidString(id) && replacedData.containsKey(id));
    }

    public boolean isTagReplaced(String tagId) {
        return withInitializedReadLock(() -> isValidString(tagId) && replacedTags.containsKey(tagId));
    }

    public String getDataReplacement(String id) {
        return withInitializedReadLock(() -> isValidString(id) ? replacedData.get(id) : null);
    }

    public String getTagReplacement(String tagId) {
        return withInitializedReadLock(() -> isValidString(tagId) ? replacedTags.get(tagId) : null);
    }

    public boolean isDataUsedAsResult(String id) {
        return withInitializedReadLock(() -> isValidString(id) && resultData.containsKey(id));
    }

    public boolean isTagUsedAsResult(String tagId) {
        return withInitializedReadLock(() -> isValidString(tagId) && resultTags.containsKey(tagId));
    }

    public Set<String> getAllReplacedData() {
        return withInitializedReadLock(() -> new HashSet<>(replacedData.keySet()));
    }

    public Set<String> getAllReplacedTags() {
        return withInitializedReadLock(() -> new HashSet<>(replacedTags.keySet()));
    }

    public void clearAll() {
        withInitializedWriteLock(() -> {
            clearAllMaps();
            saveToFileAsync();
        });
    }

    public void rebuild() {
        withWriteLock(() -> {
            int oldDataCount = replacedData.size();
            int oldTagCount = replacedTags.size();
            clearAllMaps();
            rebuildFromJsonFiles();
            saveToFileAsync();
            Oneenoughitem.LOGGER.info("{} global replacement cache rebuilt: {} -> {} data, {} -> {} tags",
                    domainId(), oldDataCount, replacedData.size(), oldTagCount, replacedTags.size());
        });
    }

    protected void rebuildFromJsonFiles() {
        try {
            var fileInfos = PathUtils.scanAllReplacementFiles(domainId());
            fileInfos.forEach(this::processReplacementFile);
            Oneenoughitem.LOGGER.info("{} rebuilt from {} files: data {}, tags {}, resultData {}, resultTags {}",
                    domainId(), fileInfos.size(), replacedData.size(), replacedTags.size(), resultData.size(), resultTags.size());
        } catch (Exception e) {
            Oneenoughitem.LOGGER.error("{} failed to rebuild global replacement cache", domainId(), e);
        }
    }

    protected void processReplacementFile(PathUtils.FileInfo fileInfo) {
        try {
            String content = Files.readString(fileInfo.filePath());
            if (content.trim().isEmpty()) return;
            JsonElement parsed = new Gson().fromJson(content, JsonElement.class);
            if (parsed != null && parsed.isJsonArray()) {
                JsonArray jsonArray = parsed.getAsJsonArray();
                for (JsonElement element : jsonArray) processReplacementElement(element, fileInfo);
            }
        } catch (Exception e) {
            Oneenoughitem.LOGGER.warn("{} failed to read replacement file: {}", domainId(), fileInfo.filePath(), e);
        }
    }

    protected void processReplacementElement(JsonElement element, PathUtils.FileInfo fileInfo) {
        try {
            var result = Replacements.CODEC.parse(JsonOps.INSTANCE, element);
            if (result.result().isPresent()) {
                var replacement = result.result().get();
                String resultString = replacement.result();
                for (String matchItem : replacement.match()) {
                    if (isTag(matchItem)) {
                        replacedTags.put(removeTagPrefix(matchItem), resultString);
                    } else {
                        replacedData.put(matchItem, resultString);
                    }
                }
                if (isTag(resultString)) {
                    resultTags.put(removeTagPrefix(resultString), fileInfo.filePath().toString());
                } else {
                    resultData.put(resultString, fileInfo.filePath().toString());
                }
            }
        } catch (Exception e) {
            Oneenoughitem.LOGGER.warn("{} failed to parse replacement rule in file: {}", domainId(), fileInfo.filePath(), e);
        }
    }

    protected static void readStringMap(DataInputStream dis, Map<String, String> map) throws IOException {
        int count = dis.readInt();
        for (int i = 0; i < count; i++) {
            String key = dis.readUTF();
            String value = dis.readUTF();
            if (isValidString(key) && isValidString(value)) map.put(key, value);
        }
    }

    protected static void writeStringMap(DataOutputStream dos, Map<String, String> map) throws IOException {
        dos.writeInt(map.size());
        for (Map.Entry<String, String> entry : map.entrySet()) {
            dos.writeUTF(entry.getKey());
            dos.writeUTF(entry.getValue());
        }
    }

    protected static boolean isValidString(String str) {
        return str != null && !str.isEmpty();
    }

    protected static boolean isTag(String str) {
        return str != null && str.startsWith("#");
    }

    protected static String removeTagPrefix(String tag) {
        return isTag(tag) ? tag.substring(1) : tag;
    }

    protected static void processValidStrings(Collection<String> strings, java.util.function.Consumer<String> processor) {
        if (strings != null) strings.stream().filter(AbstractGlobalReplacementCache::isValidString).forEach(processor);
    }

    protected static void addToResultMap(String item, String source, Map<String, String> resultMap) {
        if (isValidString(item)) resultMap.put(item, source);
    }

    protected boolean removeFromReplacementMap(Collection<String> items, Map<String, String> replacementMap, String itemType) {
        boolean changed = false;
        if (items != null) {
            for (String item : items) {
                if (isValidString(item)) {
                    String removedResult = replacementMap.remove(item);
                    if (removedResult != null) {
                        changed = true;
                        removeFromResultMaps(removedResult);
                    }
                }
            }
        }
        return changed;
    }

    protected void removeFromResultMaps(String result) {
        if (isTag(result)) resultTags.remove(removeTagPrefix(result));
        else resultData.remove(result);
    }

    protected void clearAllMaps() {
        replacedData.clear();
        replacedTags.clear();
        resultData.clear();
        resultTags.clear();
    }

    protected <T> T withInitializedReadLock(Supplier<T> operation) {
        initialize();
        return withReadLock(operation);
    }

    protected void withInitializedWriteLock(Runnable operation) {
        initialize();
        withWriteLock(operation);
    }
}