package com.mafuyu404.oneenoughitem.client.gui.editor;

import com.mafuyu404.oneenoughitem.client.gui.manager.ReplacementEditorManager;
import net.minecraft.resources.ResourceLocation;

public class DataController {
    private final ReplacementEditorManager manager;
    private final Runnable uiSync;

    public DataController(ReplacementEditorManager manager, Runnable uiSync) {
        this.manager = manager;
        this.uiSync = uiSync;
    }

    public void addMatchDataId(String dataId) {
        this.manager.addMatchDataId(dataId);
        this.uiSync.run();
    }

    public void addMatchTag(ResourceLocation tagId) {
        this.manager.addMatchTag(tagId);
        this.uiSync.run();
    }

    public boolean removeMatchDataId(String dataId) {
        boolean removed = this.manager.removeMatchDataId(dataId);
        if (removed) this.uiSync.run();
        return removed;
    }

    public void removeMatchTag(ResourceLocation tagId) {
        this.manager.removeMatchTag(tagId);
        this.uiSync.run();
    }

    public void setResultDataId(String dataId) {
        this.manager.setResultDataId(dataId);
        this.uiSync.run();
    }

    public void setResultTag(ResourceLocation tagId) {
        this.manager.setResultTag(tagId);
        this.uiSync.run();
    }

    public void clearMatchData() {
        this.manager.clearMatchData();
        this.uiSync.run();
    }

    public void clearResultData() {
        this.manager.clearResultData();
        this.uiSync.run();
    }
}