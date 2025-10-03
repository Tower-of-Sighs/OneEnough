package com.flechazo.oneenoughfluid.api.adapter;

import com.flechazo.oneenoughfluid.Oneenoughfluid;
import com.flechazo.oneenoughfluid.api.adapter.ui.FluidReplacementUiAdapter;
import com.flechazo.oneenoughfluid.client.gui.FluidSelectionScreen;
import com.flechazo.oneenoughfluid.client.gui.FluidTagSelectionScreen;
import com.flechazo.oneenoughfluid.client.gui.cache.GlobalFluidReplacementCache;
import com.flechazo.oneenoughfluid.init.FluidReplacementCache;
import com.mafuyu404.oelib.forge.client.renderer.FluidRenderUtils;
import com.mafuyu404.oneenoughitem.api.DomainAdapter;
import com.mafuyu404.oneenoughitem.api.DomainRuntimeCache;
import com.mafuyu404.oneenoughitem.api.ReplacementUiAdapter;
import com.mafuyu404.oneenoughitem.client.gui.ReplacementEditorScreen;
import com.mafuyu404.oneenoughitem.client.gui.cache.AbstractGlobalReplacementCache;
import com.mafuyu404.oneenoughitem.client.gui.util.GuiUtils;
import com.mafuyu404.oneenoughitem.util.ReplacementControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Collection;

public class FluidDomainAdapter implements DomainAdapter {
    @Override
    public String modId() {
        return Oneenoughfluid.MODID;
    }

    @Override
    public String id() {
        return "oef";
    }

    @Override
    public String dataId() {
        return "fluid";
    }

    @Override
    public Component selectObjectLabel() {
        return Component.translatable("gui.oneenoughfluid.add_fluid");
    }

    @Override
    public Component selectTagLabel() {
        return Component.translatable("gui.oneenoughfluid.add_fluid_tag");
    }

    @Override
    public Screen createObjectSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        return new FluidSelectionScreen(parent, isForMatch);
    }

    @Override
    public Screen createTagSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        return new FluidTagSelectionScreen(parent, isForMatch);
    }

    @Override
    public boolean isAvailable() {
        return true;
    }

    @Override
    public AbstractGlobalReplacementCache globalCache() {
        return GlobalFluidReplacementCache.get();
    }

    @Override
    public DomainRuntimeCache runtimeCache() {
        return new DomainRuntimeCache() {
            @Override
            public String matchData(String id) {
                return FluidReplacementCache.matchFluid(id);
            }

            @Override
            public String matchTag(ResourceLocation tagId) {
                return FluidReplacementCache.matchTag(tagId);
            }

            @Override
            public void removeReplacements(Collection<String> dataIds, Collection<String> tagIds) {
                FluidReplacementCache.removeReplacements(dataIds, tagIds);
            }

            @Override
            public boolean isTagReplaced(String tagId) {
                return FluidReplacementCache.isTagReplaced(tagId);
            }

            @Override
            public boolean isTagReplaced(ResourceLocation tagId) {
                return FluidReplacementCache.isTagReplaced(tagId);
            }
        };
    }

    @Override
    public String dataIdFromItem(Item item) {
        ItemStack stack = new ItemStack(item);
        var contained = ReplacementControl.withSkipReplacement(
                () -> FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY)
        );
        var fluid = contained.getFluid();
        var key = ForgeRegistries.FLUIDS.getKey(fluid);
        return (fluid != Fluids.EMPTY && key != null) ? key.toString() : null;
    }

    @Override
    public ItemStack iconForDataId(String dataId) {
        var rl = ResourceLocation.tryParse(dataId);
        var fluid = rl != null ? ForgeRegistries.FLUIDS.getValue(rl) : null;
        if (fluid == null || fluid == Fluids.EMPTY) return ItemStack.EMPTY;
        return ReplacementControl.withSkipReplacement(
                () -> FluidUtil.getFilledBucket(new FluidStack(fluid, 1000))
        );
    }

    @Override
    public void renderDataId(GuiGraphics graphics, String dataId, int x, int y) {
        GuiUtils.drawItemBox(graphics, x, y, 18, 18);
        var rl = ResourceLocation.tryParse(dataId);
        var fluid = rl != null ? ForgeRegistries.FLUIDS.getValue(rl) : null;
        if (fluid == null || fluid == Fluids.EMPTY) {
            return;
        }
        ReplacementControl.withSkipReplacement(() -> {
            FluidStack stack = new FluidStack(fluid, 1000);
            FluidRenderUtils.renderFluid(graphics, stack, x + 1, y + 1, 16, 16);
        });
    }

    @Override
    public Component displayName(String dataId) {
        var rl = ResourceLocation.tryParse(dataId);
        var fluid = rl != null ? ForgeRegistries.FLUIDS.getValue(rl) : null;
        if (fluid == null || fluid == Fluids.EMPTY) return Component.literal(dataId);
        return ReplacementControl.withSkipReplacement(() -> {
            FluidStack fs = new FluidStack(fluid, 1000);
            return fs.getDisplayName();
        });
    }

    @Override
    public ReplacementUiAdapter uiAdapter() {
        return new FluidReplacementUiAdapter(runtimeCache(), globalCache());
    }
}