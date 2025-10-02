package com.flechazo.oneenoughfluid.api.adapter.ui;

import com.flechazo.oneenoughfluid.Oneenoughfluid;
import com.mafuyu404.oneenoughitem.api.DomainRuntimeCache;
import com.mafuyu404.oneenoughitem.api.ReplacementUiAdapter;
import com.mafuyu404.oneenoughitem.client.gui.cache.AbstractGlobalReplacementCache;
import com.mafuyu404.oneenoughitem.client.gui.util.UiTooltipRenderHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class FluidReplacementUiAdapter implements ReplacementUiAdapter {
    private final DomainRuntimeCache runtime;
    private final AbstractGlobalReplacementCache global;

    public FluidReplacementUiAdapter(DomainRuntimeCache runtime, AbstractGlobalReplacementCache global) {
        this.runtime = runtime;
        this.global = global;
    }

    @Override
    public void addDataTooltip(List<Component> tooltip, String dataId) {
        UiTooltipRenderHelper.addDataTooltip(
                tooltip, dataId, runtime, global, Oneenoughfluid.MODID, "fluid",
                rep -> {
                    var rl = ResourceLocation.tryParse(rep);
                    var fluid = rl != null ? ForgeRegistries.FLUIDS.getValue(rl) : null;
                    if (fluid != null && fluid != Fluids.EMPTY) {
                        return new FluidStack(fluid, 1000).getDisplayName().getString();
                    }
                    return rep;
                }
        );
    }

    @Override
    public void addTagTooltip(List<Component> tooltip, ResourceLocation tagId) {
        UiTooltipRenderHelper.addTagTooltip(
                tooltip, tagId, runtime, global, Oneenoughfluid.MODID, "fluid",
                rep -> {
                    var rl = ResourceLocation.tryParse(rep);
                    var fluid = rl != null ? ForgeRegistries.FLUIDS.getValue(rl) : null;
                    if (fluid != null && fluid != Fluids.EMPTY) {
                        return new FluidStack(fluid, 1000).getDisplayName().getString();
                    }
                    return rep;
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