package cc.sighs.oneenoughitem.client.gui;

import cc.sighs.oneenoughitem.client.gui.components.FileListWidget;
import cc.sighs.oneenoughitem.client.gui.util.GuiUtils;
import cc.sighs.oneenoughitem.client.gui.util.PathUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FileSelectionScreen extends Screen {
    private static final int PANEL_WIDTH = 280;
    private static final int PANEL_HEIGHT = 200;
    private static final int BUTTON_WIDTH = 45;
    private static final int BUTTON_HEIGHT = 16;

    private final ReplacementEditorScreen parent;
    private EditBox searchBox;
    private Button backButton;
    private Button refreshButton;
    private FileListWidget fileList;
    private final List<PathUtils.FileInfo> allFiles;
    private List<PathUtils.FileInfo> filteredFiles;
    private String lastSearchText = "";

    public FileSelectionScreen(ReplacementEditorScreen parent) {
        super(Component.translatable("gui.oneenoughitem.file_selection.title"));
        this.parent = parent;
        this.allFiles = new ArrayList<>();
        this.filteredFiles = new ArrayList<>();
        this.loadJsonFiles();
    }

    private void loadJsonFiles() {
        this.allFiles.clear();
        this.allFiles.addAll(PathUtils.scanAllReplacementFiles());
        this.filteredFiles = new ArrayList<>(this.allFiles);
    }

    @Override
    protected void init() {
        super.init();

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - PANEL_WIDTH / 2;
        int panelY = centerY - PANEL_HEIGHT / 2;

        this.searchBox = new EditBox(this.font, panelX, panelY - 25, PANEL_WIDTH - 100, 18,
                Component.translatable("gui.oneenoughitem.file_selection.search"));
        this.searchBox.setHint(Component.translatable("gui.oneenoughitem.file_selection.search.hint"));
        this.addRenderableWidget(this.searchBox);

        this.refreshButton = GuiUtils.createButton(Component.translatable("gui.oneenoughitem.file_selection.refresh"),
                button -> this.refreshFileList(), panelX + PANEL_WIDTH - 95, panelY - 25, BUTTON_WIDTH, 18);
        this.addRenderableWidget(this.refreshButton);

        this.backButton = GuiUtils.createButton(Component.translatable("gui.oneenoughitem.back"),
                button -> this.minecraft.setScreen(this.parent), panelX + PANEL_WIDTH - 45, panelY - 25, BUTTON_WIDTH, 18);
        this.addRenderableWidget(this.backButton);

        this.fileList = new FileListWidget(this.minecraft, PANEL_WIDTH, PANEL_HEIGHT - 20, panelY + 5, 28,
                this::onFileSelected);
        this.fileList.setX(panelX);
        this.addRenderableWidget(this.fileList);

        this.updateFileList();
    }

    private void refreshFileList() {
        this.loadJsonFiles();
        this.updateFileList();
    }

    private void updateFileList() {
        String searchText = this.searchBox.getValue().toLowerCase();
        this.filteredFiles.clear();

        for (PathUtils.FileInfo file : this.allFiles) {
            if (searchText.isEmpty() ||
                    file.displayName().toLowerCase().contains(searchText) ||
                    file.fullPath().toLowerCase().contains(searchText) ||
                    file.datapackName().toLowerCase().contains(searchText)) {
                this.filteredFiles.add(file);
            }
        }

        this.fileList.setFiles(this.filteredFiles);
    }

    private void onFileSelected(Path filePath, int mode) {
        this.parent.onFileSelected(filePath, mode);
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public void tick() {
        super.tick();

        String currentSearchText = this.searchBox.getValue();
        if (!currentSearchText.equals(this.lastSearchText)) {
            this.updateFileList();
            this.lastSearchText = currentSearchText;
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {

        int centerX = this.width / 2;
        int centerY = this.height / 2;
        int panelX = centerX - PANEL_WIDTH / 2;
        int panelY = centerY - PANEL_HEIGHT / 2;

        GuiUtils.drawPanelBackground(graphics, panelX, panelY, PANEL_WIDTH, PANEL_HEIGHT);

        // Match the pre-26.1.2 render order: the list and controls are below labels.
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);

        graphics.centeredText(this.font, this.title, centerX, panelY - 35, 0xFFFFFFFF);

        Component fileCountText = Component.translatable("gui.oneenoughitem.file_selection.files_found", this.filteredFiles.size());
        graphics.text(this.font, fileCountText, panelX, panelY + PANEL_HEIGHT + 5, 0xFFAAAAAA);

        if (this.allFiles.isEmpty()) {
            Component helpText = Component.translatable("gui.oneenoughitem.file_selection.no_files");
            graphics.centeredText(this.font, helpText, centerX, centerY, 0xFFFF6666);
        } else {
            Component helpText = Component.translatable("gui.oneenoughitem.file_selection.help");
            graphics.centeredText(this.font, helpText, centerX, panelY + PANEL_HEIGHT + 15, 0xFFCCCCCC);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
