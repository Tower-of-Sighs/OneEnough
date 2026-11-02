package cc.sighs.oneenoughfluid.api.adapter.ui;

import cc.sighs.oneenoughfluid.Oneenoughfluid;
import cc.sighs.oneenoughitem.api.DomainRuntimeCache;
import cc.sighs.oneenoughitem.api.ReplacementUiAdapter;
import cc.sighs.oneenoughitem.client.gui.cache.AbstractGlobalReplacementCache;
import cc.sighs.oneenoughitem.client.gui.util.UiTooltipRenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

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
                    var rl = Identifier.tryParse(rep);
                    var fluid = rl != null ? BuiltInRegistries.FLUID.getValue(rl) : null;
                    if (fluid != null && fluid != Fluids.EMPTY) {
                        return new FluidStack(fluid, 1000).getHoverName().getString();
                    }
                    return rep;
                }
        );
    }

    @Override
    public void addTagTooltip(List<Component> tooltip, Identifier tagId) {
        UiTooltipRenderHelper.addTagTooltip(
                tooltip, tagId, runtime, global, Oneenoughfluid.MODID, "fluid",
                rep -> {
                    var rl = Identifier.tryParse(rep);
                    var fluid = rl != null ? BuiltInRegistries.FLUID.getValue(rl) : null;
                    if (fluid != null && fluid != Fluids.EMPTY) {
                        return new FluidStack(fluid, 1000).getHoverName().getString();
                    }
                    return rep;
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