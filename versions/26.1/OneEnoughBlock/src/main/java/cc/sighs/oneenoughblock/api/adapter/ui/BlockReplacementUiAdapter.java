package cc.sighs.oneenoughblock.api.adapter.ui;

import cc.sighs.oneenoughitem.api.DomainRuntimeCache;
import cc.sighs.oneenoughitem.api.ReplacementUiAdapter;
import cc.sighs.oneenoughitem.client.gui.cache.AbstractGlobalReplacementCache;
import cc.sighs.oneenoughitem.client.gui.util.UiTooltipRenderHelper;
import cc.sighs.oneenoughblock.Oneenoughblock;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BlockReplacementUiAdapter implements ReplacementUiAdapter {
    private final DomainRuntimeCache runtime;
    private final AbstractGlobalReplacementCache global;

    public BlockReplacementUiAdapter(DomainRuntimeCache runtime, AbstractGlobalReplacementCache global) {
        this.runtime = runtime;
        this.global = global;
    }

    @Override
    public void addDataTooltip(List<Component> tooltip, String dataId) {
        UiTooltipRenderHelper.addDataTooltip(
                tooltip, dataId, runtime, global, Oneenoughblock.MODID, "block",
                rep -> {
                    var rl = Identifier.tryParse(rep);
                    var block = rl != null ? BuiltInRegistries.BLOCK.getValue(rl) : null;
                    return block != null ? new ItemStack(block.asItem()).getHoverName().getString() : rep;
                }
        );
    }

    @Override
    public void addTagTooltip(List<Component> tooltip, Identifier tagId) {
        UiTooltipRenderHelper.addTagTooltip(
                tooltip, tagId, runtime, global, Oneenoughblock.MODID, "block",
                rep -> {
                    var rl = Identifier.tryParse(rep);
                    var block = rl != null ? BuiltInRegistries.BLOCK.getValue(rl) : null;
                    return block != null ? new ItemStack(block.asItem()).getHoverName().getString() : rep;
                }
        );
    }

    @Override
    public void renderDataIndicators(GuiGraphicsExtractor g, String dataId, int x, int y) {
        UiTooltipRenderHelper.renderDataIndicators(g, dataId, runtime, global, x, y);
    }

    @Override
    public void renderTagIndicators(GuiGraphicsExtractor g, Identifier tagId, int x, int y) {
        UiTooltipRenderHelper.renderTagIndicators(g, tagId, runtime, global, x, y);
    }
}
