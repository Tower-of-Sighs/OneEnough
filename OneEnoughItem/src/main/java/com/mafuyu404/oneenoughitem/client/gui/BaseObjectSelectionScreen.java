package com.mafuyu404.oneenoughitem.client.gui;

import com.mafuyu404.oneenoughitem.api.DomainRegistry;
import com.mafuyu404.oneenoughitem.client.gui.components.ItemGridWidget;
import com.mafuyu404.oneenoughitem.client.gui.util.GuiUtils;
import com.mafuyu404.oneenoughitem.init.ReplacementControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public abstract class BaseObjectSelectionScreen<T> extends Screen {
    protected static final int ITEMS_PER_PAGE = 45;
    protected static final int GRID_WIDTH = 9;
    protected static final int GRID_HEIGHT = 5;

    protected final ReplacementEditorScreen parent;
    protected final boolean isForMatch;

    protected EditBox searchBox;
    protected Button sortButton;
    protected Button prevPageButton;
    protected Button nextPageButton;
    protected Button backButton;

    protected ItemGridWidget itemGrid;
    protected List<T> allObjects;
    protected List<T> filteredObjects;
    protected int currentPage = 0;

    protected final Set<String> selectedIds = new HashSet<>();
    protected Button confirmSelectionButton;
    protected Button clearSelectionButton;

    protected enum SortMode {NAME, MOD, ID}

    protected SortMode sortMode = SortMode.NAME;

    protected BaseObjectSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch, Component title) {
        super(title);
        this.parent = parent;
        this.isForMatch = isForMatch;
        this.allObjects = new ArrayList<>();
        this.filteredObjects = new ArrayList<>();
        this.updateGrid();
    }

    protected abstract Component sortLabel();

    protected abstract List<T> loadAllObjects();

    protected abstract String getId(T obj);

    protected abstract void renderObject(T obj, GuiGraphics graphics, int x, int y);

    protected abstract void onSelectSingle(String id);

    protected abstract boolean isSelectable(String id, boolean forMatch);

    @Override
    protected void init() {
        int centerX = this.width / 2;
        this.searchBox = new EditBox(this.font, centerX - 80, 15, 160, 18, Component.translatable("gui.oneenoughitem.search"));
        this.addRenderableWidget(this.searchBox);

        this.sortButton = GuiUtils.createButton(getSortLabel(this.sortMode), btn -> this.onSort(), centerX + 90, 15, 70, 18);
        this.addRenderableWidget(this.sortButton);

        int gridStartX = centerX - (GRID_WIDTH * 18) / 2;
        int gridStartY = 45;
        this.itemGrid = new ItemGridWidget(gridStartX, gridStartY, GRID_WIDTH, GRID_HEIGHT, this::handleGridItemClick);
        this.addRenderableWidget(this.itemGrid);

        int buttonY = gridStartY + GRID_HEIGHT * 18 + 10;
        this.prevPageButton = GuiUtils.createButton(Component.literal("<"), btn -> previousPage(), centerX - 80, buttonY, 25, 18);
        this.addRenderableWidget(this.prevPageButton);
        this.nextPageButton = GuiUtils.createButton(Component.literal(">"), btn -> nextPage(), centerX + 55, buttonY, 25, 18);
        this.addRenderableWidget(this.nextPageButton);
        this.backButton = GuiUtils.createButton(Component.translatable("gui.oneenoughitem.back"), btn -> onClose(), centerX - 40, buttonY, 80, 18);
        this.addRenderableWidget(this.backButton);

        if (this.isForMatch) {
            this.confirmSelectionButton = GuiUtils.createButton(Component.translatable("gui.oneenoughitem.add_selected"), btn -> confirmSelected(), centerX - 120, buttonY + 25, 100, 18);
            this.addRenderableWidget(this.confirmSelectionButton);
            this.clearSelectionButton = GuiUtils.createButton(Component.translatable("gui.oneenoughitem.clear_selected"), btn -> {
                this.selectedIds.clear();
                updateGrid();
                updateConfirmButtonsVisibility();
            }, centerX + 20, buttonY + 25, 100, 18);
            this.addRenderableWidget(this.clearSelectionButton);
        }

        this.allObjects = loadAllObjects();
        this.filteredObjects = new ArrayList<>(this.allObjects);
        applySort();
        updateGrid();
        updateNavigationButtons();
        updateConfirmButtonsVisibility();
    }

    protected Component getSortLabel(SortMode mode) {
        return switch (mode) {
            case NAME -> Component.translatable("gui.oneenoughitem.sort.name");
            case MOD -> Component.translatable("gui.oneenoughitem.sort.mod");
            case ID -> Component.translatable("gui.oneenoughitem.sort.id");
        };
    }

    protected Comparator<T> getComparator(SortMode mode) {
        return (a, b) -> 0;
    }

    protected void onSort() {
        this.sortMode = switch (this.sortMode) {
            case NAME -> SortMode.MOD;
            case MOD -> SortMode.ID;
            case ID -> SortMode.NAME;
        };
        applySort();
        updateGrid();
        updateNavigationButtons();
        this.sortButton.setMessage(getSortLabel(this.sortMode));
    }

    protected void applySort() {
        Comparator<T> cmp = getComparator(this.sortMode);
        if (cmp != null) {
            this.filteredObjects = new ArrayList<>(this.filteredObjects);
            this.filteredObjects.sort(cmp);
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.searchBox.getValue().length() != this.getLastSearchLength()) {
            filterObjects();
            this.currentPage = 0;
            updateGrid();
            updateNavigationButtons();
        }
    }

    private int lastSearchLength = 0;

    private int getLastSearchLength() {
        int current = this.lastSearchLength;
        this.lastSearchLength = this.searchBox.getValue().length();
        return current;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        int totalPages = (this.filteredObjects.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        String pageInfo = (this.currentPage + 1) + " / " + Math.max(1, totalPages);
        int buttonY = 45 + GRID_HEIGHT * 18 + 10;
        graphics.drawCenteredString(this.font, pageInfo, this.width / 2, buttonY + 25, 0xFFFFFF);

        String count = this.filteredObjects.size() + " entries";
        graphics.drawString(this.font, count, 10, buttonY + 35, 0xFFFFFF);
    }

    protected void filterObjects() {
        String search = this.searchBox.getValue().toLowerCase();
        if (search.isEmpty()) {
            this.filteredObjects = new ArrayList<>(this.allObjects);
        } else {
            this.filteredObjects = this.allObjects.stream()
                    .filter(obj -> {
                        String id = Optional.ofNullable(getId(obj)).orElse("").toLowerCase();
                        return id.contains(search);
                    }).toList();
        }
    }

    protected void updateGrid() {
    }

    protected void updateNavigationButtons() {
        int totalPages = (this.filteredObjects.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        this.prevPageButton.active = this.currentPage > 0;
        this.nextPageButton.active = this.currentPage < totalPages - 1;
    }

    protected void previousPage() {
        if (this.currentPage > 0) {
            this.currentPage--;
            updateGrid();
            updateNavigationButtons();
        }
    }

    protected void nextPage() {
        int totalPages = (this.filteredObjects.size() + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;
        if (this.currentPage < totalPages - 1) {
            this.currentPage++;
            updateGrid();
            updateNavigationButtons();
        }
    }

    protected void confirmSelected() {
        if (!this.isForMatch || this.selectedIds.isEmpty()) return;
        int added = 0;
        for (String id : new ArrayList<>(this.selectedIds)) {
            if (!isSelectable(id, true)) continue;
            onSelectSingle(id);
            added++;
        }
        this.selectedIds.clear();
        updateGrid();
        updateConfirmButtonsVisibility();
        this.onClose();
    }

    protected void updateConfirmButtonsVisibility() {
        if (!this.isForMatch) return;
        boolean hasSelection = !this.selectedIds.isEmpty();
        if (this.confirmSelectionButton != null) this.confirmSelectionButton.active = hasSelection;
        if (this.clearSelectionButton != null) this.clearSelectionButton.active = hasSelection;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void handleGridItemClick(ItemStack stack) {
        String id = ReplacementControl.withSkipReplacement(
                () -> DomainRegistry.current().dataIdFromItem(stack.getItem())
        );
        if (id == null) return;

        if (this.isForMatch && hasControlDown()) {
            if (this.selectedIds.contains(id)) this.selectedIds.remove(id);
            else this.selectedIds.add(id);
            updateGrid();
            updateConfirmButtonsVisibility();
            return;
        }

        if (!isSelectable(id, this.isForMatch)) return;
        onSelectSingle(id);
        this.onClose();
    }
}