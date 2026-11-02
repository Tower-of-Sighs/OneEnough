package cc.sighs.oneenoughitem.api;

import cc.sighs.oneenoughitem.client.gui.ReplacementEditorScreen;
import cc.sighs.oneenoughitem.client.gui.cache.AbstractGlobalReplacementCache;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public interface DomainAdapter {
    String modId();

    String id();

    String dataId();

    Component selectObjectLabel();

    Component selectTagLabel();

    Screen createObjectSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch);

    Screen createTagSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch);

    boolean isAvailable();

    AbstractGlobalReplacementCache globalCache();

    DomainRuntimeCache runtimeCache();

    String dataIdFromItem(Item item);

    ItemStack iconForDataId(String dataId);

    void renderDataId(GuiGraphicsExtractor graphics, String dataId, int x, int y);

    Component displayName(String dataId);

    ReplacementUiAdapter uiAdapter();
}
