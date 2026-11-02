package cc.sighs.oneenoughitem.api.adapter;

import cc.sighs.oneenoughitem.Oneenoughitem;
import cc.sighs.oneenoughitem.api.DomainAdapter;
import cc.sighs.oneenoughitem.api.DomainRuntimeCache;
import cc.sighs.oneenoughitem.api.ReplacementUiAdapter;
import cc.sighs.oneenoughitem.api.adapter.ui.ItemReplacementUiAdapter;
import cc.sighs.oneenoughitem.client.gui.ItemSelectionScreen;
import cc.sighs.oneenoughitem.client.gui.ItemTagSelectionScreen;
import cc.sighs.oneenoughitem.client.gui.ReplacementEditorScreen;
import cc.sighs.oneenoughitem.client.gui.cache.AbstractGlobalReplacementCache;
import cc.sighs.oneenoughitem.client.gui.cache.ItemGlobalReplacementCache;
import cc.sighs.oneenoughitem.client.gui.util.GuiUtils;
import cc.sighs.oneenoughitem.init.ItemReplacementCache;
import cc.sighs.oneenoughitem.util.ReplacementControl;
import cc.sighs.oneenoughitem.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;

public class ItemDomainAdapter implements DomainAdapter {
    @Override
    public String modId() {
        return Oneenoughitem.MODID;
    }

    @Override
    public String id() {
        return "oei";
    }

    @Override
    public String dataId() {
        return "Items";
    }

    @Override
    public Component selectObjectLabel() {
        return Component.translatable("gui.oneenoughitem.add_item");
    }

    @Override
    public Component selectTagLabel() {
        return Component.translatable("gui.oneenoughitem.add_item_tag");
    }

    @Override
    public Screen createObjectSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        return new ItemSelectionScreen(parent, isForMatch);
    }

    @Override
    public Screen createTagSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        return new ItemTagSelectionScreen(parent, isForMatch);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public AbstractGlobalReplacementCache globalCache() {
        return ItemGlobalReplacementCache.get();
    }

    @Override
    public DomainRuntimeCache runtimeCache() {
        return new DomainRuntimeCache() {
            @Override
            public String matchData(String id) {
                return ItemReplacementCache.matchItem(id);
            }

            @Override
            public String matchTag(Identifier tagId) {
                return ItemReplacementCache.matchTag(tagId);
            }

            @Override
            public void removeReplacements(Collection<String> dataIds, Collection<String> tagIds) {
                ItemReplacementCache.removeReplacements(dataIds, tagIds);
            }

            @Override
            public boolean isTagReplaced(String tagId) {
                return ItemReplacementCache.isTagReplaced(tagId);
            }

            @Override
            public boolean isTagReplaced(Identifier tagId) {
                return ItemReplacementCache.isTagReplaced(tagId);
            }
        };
    }

    @Override
    public String dataIdFromItem(Item item) {
        return Utils.getItemRegistryName(item);
    }

    @Override
    public ItemStack iconForDataId(String dataId) {
        var rl = Identifier.tryParse(dataId);
        var item = rl != null ? BuiltInRegistries.ITEM.getValue(rl) : null;
        return item != null ? ReplacementControl.withSkipReplacement(() -> new ItemStack(item)) : ItemStack.EMPTY;
    }

    @Override
    public void renderDataId(GuiGraphicsExtractor graphics, String dataId, int x, int y) {
        var stack = iconForDataId(dataId);
        GuiUtils.drawItemBox(graphics, x, y, 18, 18);
        graphics.item(stack, x + 1, y + 1);
        graphics.itemDecorations(Minecraft.getInstance().font, stack, x + 1, y + 1);
    }

    @Override
    public Component displayName(String dataId) {
        if (dataId == null || dataId.isEmpty()) {
            return Component.literal("");
        }
        Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(dataId));
        ItemStack stack = new ItemStack(item);
        return stack.getHoverName();
    }

    @Override
    public ReplacementUiAdapter uiAdapter() {
        return new ItemReplacementUiAdapter();
    }
}
