package com.mafuyu404.oneenoughitem.client.gui.cache;

import com.mafuyu404.oneenoughitem.Oneenoughitem;
import net.minecraft.resources.ResourceLocation;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class EditorCache extends BaseCache {
    private static final EditorCache INSTANCE = new EditorCache();


    public record CacheData(Set<String> matchItems, Set<String> matchTags,
                            String resultItem, String resultTag, String fileName) {
    }

    private EditorCache() {
        super("oei", "editor_cache.dat", 2);
    }

    @Override
    protected void onInitialized() {
    }

    @Override
    protected void onVersionMismatch(int foundVersion) {
        Oneenoughitem.LOGGER.warn("Editor cache version mismatch (expected {}, found {}), ignoring cache", cacheVersion, foundVersion);
    }

    @Override
    protected void onLoadError(IOException e) {
        Oneenoughitem.LOGGER.error("Failed to load editor cache from: {}", cacheFile, e);
    }

    @Override
    protected void loadData(DataInputStream dis) throws IOException {
        throw new UnsupportedOperationException("Use loadCache() method instead");
    }

    @Override
    protected void saveData(DataOutputStream dos) throws IOException {
        throw new UnsupportedOperationException("Use saveCache() method instead");
    }

    public static void saveCache(Set<String> matchDataIds,
                                 Set<ResourceLocation> matchTags,
                                 String resultDataId,
                                 ResourceLocation resultTag,
                                 String fileName) {
        INSTANCE.withWriteLock(() -> {
            Path tempFile = INSTANCE.cacheFile.resolveSibling(INSTANCE.cacheFile.getFileName() + ".tmp");
            try (DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(tempFile)))) {

                dos.writeInt(INSTANCE.cacheVersion);
                writeStringSet(dos, matchDataIds);

                Set<String> tagStrings = matchTags != null
                        ? matchTags.stream().map(ResourceLocation::toString).collect(Collectors.toSet())
                        : Collections.emptySet();
                writeStringSet(dos, tagStrings);

                dos.writeUTF(nullToEmpty(resultDataId));
                dos.writeUTF(nullToEmpty(resultTag != null ? resultTag.toString() : null));
                dos.writeUTF(nullToEmpty(fileName));

                dos.flush();

            } catch (IOException e) {
                logSaveError(e);
                return;
            }

            try {
                Files.move(tempFile, INSTANCE.cacheFile, StandardCopyOption.REPLACE_EXISTING);
                Oneenoughitem.LOGGER.debug("Editor cache saved successfully to: {}", INSTANCE.cacheFile);
            } catch (IOException e) {
                Oneenoughitem.LOGGER.error("Failed to replace editor cache file: {}", INSTANCE.cacheFile, e);
            }
        });
    }

    public static CacheData loadCache() {
        return INSTANCE.withReadLock(() -> {
            if (!Files.exists(INSTANCE.cacheFile)) {
                Oneenoughitem.LOGGER.debug("Editor cache file not found: {}", INSTANCE.cacheFile);
                return null;
            }

            try (DataInputStream dis = new DataInputStream(
                    new BufferedInputStream(Files.newInputStream(INSTANCE.cacheFile)))) {

                int version = dis.readInt();
                if (version != INSTANCE.cacheVersion) {
                    Oneenoughitem.LOGGER.warn("Editor cache version mismatch (expected {}, found {}), ignoring cache",
                            INSTANCE.cacheVersion, version);
                    return null;
                }

                Set<String> matchItems = readStringSet(dis);
                Set<String> matchTags = readStringSet(dis);

                String resultItem = emptyToNull(dis.readUTF());
                String resultTag = emptyToNull(dis.readUTF());
                String fileName = emptyToNull(dis.readUTF());

                Oneenoughitem.LOGGER.debug("Editor cache loaded successfully from: {}", INSTANCE.cacheFile);
                return new CacheData(matchItems, matchTags, resultItem, resultTag, fileName);

            } catch (IOException e) {
                Oneenoughitem.LOGGER.error("Failed to load editor cache from: {}", INSTANCE.cacheFile, e);
                return null;
            }
        });
    }

    public static void clearCache() {
        INSTANCE.clearCacheFile();
    }

    public Path getCacheFilePath() {
        return INSTANCE.cacheFile;
    }


    private static void writeStringSet(DataOutputStream dos, Set<String> stringSet) throws IOException {
        Set<String> nonNullSet = stringSet != null ? stringSet : Collections.emptySet();
        dos.writeInt(nonNullSet.size());
        for (String s : nonNullSet) {
            if (s != null && !s.isEmpty()) {
                dos.writeUTF(s);
            }
        }
    }

    private static Set<String> readStringSet(DataInputStream dis) throws IOException {
        int count = dis.readInt();
        Set<String> result = new HashSet<>(count);
        for (int i = 0; i < count; i++) {
            String str = dis.readUTF();
            if (!str.isEmpty()) {
                result.add(str);
            }
        }
        return result;
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }

    private static String emptyToNull(String s) {
        return s.isEmpty() ? null : s;
    }

    private static void logSaveError(IOException e) {
        Oneenoughitem.LOGGER.error("Failed to save editor cache to: {}", INSTANCE.cacheFile, e);
    }
}
