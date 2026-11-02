package com.mafuyu404.oneenoughitem.client.gui.util;

import com.mafuyu404.oneenoughitem.Oneenoughitem;
import com.mafuyu404.oneenoughitem.api.DomainRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PathUtils {
    private static final String FOLDER_DATAPACKS = "datapacks";
    private static final String DEFAULT_DATA_PACK_NAME = "OEIBF";
    private static final String SAVES_FOLDER = "saves";

    public static Path getDatapackPath(String datapackName) {
        Minecraft minecraft = Minecraft.getInstance();
        Path datapacksPath = getDatapacksPath();

        String finalDatapackName = DEFAULT_DATA_PACK_NAME;
        if (datapackName != null && !datapackName.trim().isEmpty()) {
            finalDatapackName = datapackName.trim();
        }

        return datapacksPath.resolve(finalDatapackName);
    }

    public static Path getReplacementsPath() {
        return getDatapackPath(null).resolve(getReplacementsSubPath());
    }

    private static String getReplacementsSubPath() {
        String domainId;
        try {
            var current = DomainRegistry.current();
            domainId = current != null ? current.id().toLowerCase() : "oei";
        } catch (Throwable t) {
            domainId = "oei";
        }
        return "data/" + domainId + "/replacements";
    }

    public static Path getDatapacksPath() {
        Minecraft minecraft = Minecraft.getInstance();

        if (minecraft.getSingleplayerServer() != null) {
            Path worldPath = minecraft.getSingleplayerServer().getWorldPath(LevelResource.ROOT);
            return worldPath.resolve(FOLDER_DATAPACKS);
        } else if (minecraft.level != null) {
            try {
                Path savesPath = minecraft.gameDirectory.toPath().resolve(SAVES_FOLDER);
                Path worldPath = findCurrentWorldPath(savesPath);
                if (worldPath != null) {
                    return worldPath.resolve(FOLDER_DATAPACKS);
                }
            } catch (Exception e) {
                Oneenoughitem.LOGGER.warn("Failed to determine world path", e);
            }
        }

        Path savesPath = minecraft.gameDirectory.toPath().resolve(SAVES_FOLDER);
        return savesPath.resolve(FOLDER_DATAPACKS);
    }

    public static String getReplacementsSubPath(String domainId) {
        String id = (domainId != null && !domainId.isEmpty()) ? domainId.toLowerCase() : "oei";
        return "data/" + id + "/replacements";
    }

    public static List<FileInfo> scanAllReplacementFiles() {
        return scanAllReplacementFiles(DomainRegistry.currentId());
    }

    /**
     * 根据指定的 domainId 扫描所有替换文件
     */
    public static List<FileInfo> scanAllReplacementFiles(String domainId) {
        List<FileInfo> jsonFiles = new ArrayList<>();
        Path datapacksPath = getDatapacksPath();
        if (!Files.exists(datapacksPath)) {
            Oneenoughitem.LOGGER.warn("Datapacks directory does not exist: {}", datapacksPath);
            return jsonFiles;
        }

        String replacementsSubPath = getReplacementsSubPath(domainId);
        scanReplacementFilesInPath(datapacksPath, replacementsSubPath, jsonFiles);

        return jsonFiles;
    }

    /**
     * 在指定路径下扫描替换文件
     */
    private static void scanReplacementFilesInPath(Path datapacksPath, String replacementsSubPath, List<FileInfo> jsonFiles) {
        try (Stream<Path> datapackDirs = Files.list(datapacksPath)) {
            datapackDirs
                    .filter(Files::isDirectory)
                    .forEach(datapackDir -> {
                        Path replacementsPath = datapackDir.resolve(replacementsSubPath);
                        if (Files.exists(replacementsPath)) {
                            scanJsonFilesInDirectory(replacementsPath, datapackDir, jsonFiles);
                        }
                    });
        } catch (IOException e) {
            Oneenoughitem.LOGGER.error("Failed to list datapack directories", e);
        }
    }

    private static void scanJsonFilesInDirectory(Path replacementsPath, Path datapackRoot, List<FileInfo> jsonFiles) {
        try (Stream<Path> paths = Files.walk(replacementsPath)) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                    .forEach(path -> {
                        String fileName = path.getFileName().toString();
                        String displayName = fileName.endsWith(".json") ?
                                fileName.substring(0, fileName.length() - 5) : fileName;

                        String datapackName = datapackRoot.getFileName().toString();
                        Path relativeToReplacements = replacementsPath.relativize(path.getParent());
                        String relativePathStr = relativeToReplacements.toString().equals(".") ? "" : relativeToReplacements.toString();

                        String fullPath = String.join("/",
                                FOLDER_DATAPACKS,
                                datapackName,
                                getReplacementsSubPath(),
                                relativePathStr
                        ).replaceAll("\\\\", "/");

                        jsonFiles.add(new FileInfo(displayName, path, fullPath, datapackName));
                    });
        } catch (IOException e) {
            Oneenoughitem.LOGGER.error("Failed to scan files in directory: {}", replacementsPath, e);
        }
    }

    /**
     * 查找当前世界路径（基于最后修改时间）
     */
    public static Path findCurrentWorldPath(Path savesPath) {
        try {
            if (!Files.exists(savesPath)) {
                return null;
            }

            try (Stream<Path> stream = Files.list(savesPath)) {
                return stream
                        .filter(Files::isDirectory)
                        .filter(path -> Files.exists(path.resolve("level.dat")))
                        .max((p1, p2) -> {
                            try {
                                return Files.getLastModifiedTime(p1).compareTo(Files.getLastModifiedTime(p2));
                            } catch (IOException e) {
                                return 0;
                            }
                        })
                        .orElse(null);
            }
        } catch (IOException e) {
            Oneenoughitem.LOGGER.error("Error finding current world path", e);
            return null;
        }
    }

    public record FileInfo(String displayName, Path filePath, String fullPath, String datapackName) {
    }
}