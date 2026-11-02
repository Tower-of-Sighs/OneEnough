package cc.sighs.oneenoughitem.client.gui;

import cc.sighs.oneenoughitem.api.DomainRegistry;
import cc.sighs.oneenoughitem.client.gui.components.TagListWidget;
import cc.sighs.oneenoughitem.client.gui.util.GuiUtils;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class BaseTagSelectionScreen extends Screen {
    protected final ReplacementEditorScreen parent;
    protected final boolean isForMatch;

    protected EditBox searchBox;
    protected Button backButton;
    protected TagListWidget tagList;

    protected List<Identifier> allTags;
    protected List<Identifier> filteredTags;
    protected final Set<Identifier> selectedTags = new HashSet<>();
    protected Button confirmSelectionButton;
    protected Button clearSelectionButton;

    protected BaseTagSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch, Component title) {
        super(title);
        this.parent = parent;
        this.isForMatch = isForMatch;
        this.allTags = new ArrayList<>();
        this.filteredTags = new ArrayList<>();
    }

    protected abstract List<Identifier> loadAllTags();

    private void onSelectTag(Identifier tagId) {
        String tagStr = tagId != null ? tagId.toString() : null;
        if (tagStr == null) return;
        var global = DomainRegistry.current().globalCache();
        if (global.isTagUsedAsResult(tagStr) || global.isTagReplaced(tagStr)) {
            return;
        }
        if (this.isForMatch) this.parent.addMatchTag(tagId);
        else this.parent.setResultTag(tagId);
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        this.searchBox = new EditBox(this.font, centerX - 80, 15, 160, 18, Component.translatable("gui.oneenoughitem.search"));
        this.addRenderableWidget(this.searchBox);

        this.tagList = new TagListWidget(this.minecraft, this.width - 40, this.height - 100, 40, 22, this::handleTagClick);
        this.tagList.setX(20);
        this.tagList.setSelectedTags(this.selectedTags);
        this.addRenderableWidget(this.tagList);

        if (this.isForMatch) {
            int buttonY = this.height - 50;
            int totalWidth = 100 + 80 + 100 + 10;
            int startX = centerX - totalWidth / 2;

            this.confirmSelectionButton = GuiUtils.createButton(Component.translatable("gui.oneenoughitem.add_selected"), btn -> confirmSelectedTags(), startX, buttonY, 100, 18);
            this.addRenderableWidget(this.confirmSelectionButton);
            this.backButton = GuiUtils.createButton(Component.translatable("gui.oneenoughitem.back"), btn -> onClose(), startX + 110, buttonY, 80, 18);
            this.addRenderableWidget(this.backButton);
            this.clearSelectionButton = GuiUtils.createButton(Component.translatable("gui.oneenoughitem.clear_selected"), btn -> {
                this.selectedTags.clear();
                updateTagList();
                updateConfirmButtonsVisibility();
            }, startX + 200, buttonY, 100, 18);
            this.addRenderableWidget(this.clearSelectionButton);
            updateConfirmButtonsVisibility();
        } else {
            this.backButton = GuiUtils.createButton(Component.translatable("gui.oneenoughitem.back"), btn -> onClose(), centerX - 40, this.height - 50, 80, 18);
            this.addRenderableWidget(this.backButton);
        }
        this.allTags = loadAllTags();
        this.filteredTags = new ArrayList<>(this.allTags);
        updateTagList();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.searchBox.getValue().length() != this.getLastSearchLength()) {
            filterTags();
            updateTagList();
        }
    }

    private int lastSearchLength = 0;

    private int getLastSearchLength() {
        int current = this.lastSearchLength;
        this.lastSearchLength = this.searchBox.getValue().length();
        return current;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        int panelX = 20, panelY = 40, panelW = this.width - 40, panelH = this.height - 100;
        GuiUtils.drawPanelBackground(graphics, panelX, panelY, panelW, panelH);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        String tagCount = this.filteredTags.size() + " tags";
        graphics.text(this.font, tagCount, 10, this.height - 30, 0xFFFFFFFF);
    }

    protected void filterTags() {
        String search = this.searchBox.getValue().toLowerCase();
        if (search.isEmpty()) this.filteredTags = new ArrayList<>(this.allTags);
        else
            this.filteredTags = this.allTags.stream().filter(tag -> tag.toString().toLowerCase().contains(search)).toList();
    }

    protected void updateTagList() {
        this.tagList.setTags(this.filteredTags);
    }

    private void handleTagClick(Identifier tagId) {
        if (this.isForMatch && (com.mojang.blaze3d.platform.InputConstants.isKeyDown(this.minecraft.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_CONTROL) || com.mojang.blaze3d.platform.InputConstants.isKeyDown(this.minecraft.getWindow(), org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_CONTROL))) {
            if (this.selectedTags.contains(tagId)) this.selectedTags.remove(tagId);
            else this.selectedTags.add(tagId);
            updateTagList();
            updateConfirmButtonsVisibility();
            return;
        }
        onSelectTag(tagId);
        onClose();
    }

    protected void confirmSelectedTags() {
        if (!this.isForMatch || this.selectedTags.isEmpty()) return;
        for (Identifier tagId : new ArrayList<>(this.selectedTags)) onSelectTag(tagId);
        this.selectedTags.clear();
        updateTagList();
        updateConfirmButtonsVisibility();
        onClose();
    }

    protected void updateConfirmButtonsVisibility() {
        if (!this.isForMatch) return;
        boolean hasSelection = !this.selectedTags.isEmpty();
        if (this.confirmSelectionButton != null) this.confirmSelectionButton.active = hasSelection;
        if (this.clearSelectionButton != null) this.clearSelectionButton.active = hasSelection;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
