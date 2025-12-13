package com.sighs.oneenoughblock.api.adapter.ui;

import com.mafuyu404.oneenoughitem.api.DomainRuntimeCache;
import com.mafuyu404.oneenoughitem.api.ReplacementUiAdapter;
import com.mafuyu404.oneenoughitem.client.gui.cache.AbstractGlobalReplacementCache;
import com.mafuyu404.oneenoughitem.client.gui.util.UiTooltipRenderHelper;
import com.sighs.oneenoughblock.Oneenoughblock;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
                    var rl = ResourceLocation.tryParse(rep);
                    var block = rl != null ? BuiltInRegistries.BLOCK.get(rl) : null;
                    return block != null ? new ItemStack(block.asItem()).getHoverName().getString() : rep;
                }
        );
    }

    @Override
    public void addTagTooltip(List<Component> tooltip, ResourceLocation tagId) {
        UiTooltipRenderHelper.addTagTooltip(
                tooltip, tagId, runtime, global, Oneenoughblock.MODID, "block",
                rep -> {
                    var rl = ResourceLocation.tryParse(rep);
                    var block = rl != null ? BuiltInRegistries.BLOCK.get(rl) : null;
                    return block != null ? new ItemStack(block.asItem()).getHoverName().getString() : rep;
                }
        );
    }

    @Override
    public void renderDataIndicators(GuiGraphics g, String dataId, int x, int y) {
        UiTooltipRenderHelper.renderDataIndicators(g, dataId, runtime, global, x, y);
    }

    @Override
    public void renderTagIndicators(GuiGraphics g, ResourceLocation tagId, int x, int y) {
        UiTooltipRenderHelper.renderTagIndicators(g, tagId, runtime, global, x, y);
    }
}