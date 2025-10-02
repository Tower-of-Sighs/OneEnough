package com.flechazo.oneenoughfluid.client.gui;

import com.mafuyu404.oelib.forge.client.renderer.FluidRenderUtils;
import com.mafuyu404.oneenoughitem.client.gui.BaseObjectSelectionScreen;
import com.mafuyu404.oneenoughitem.client.gui.ReplacementEditorScreen;
import com.mafuyu404.oneenoughitem.client.gui.util.GuiUtils;
import com.mafuyu404.oneenoughitem.init.ReplacementControl;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class FluidSelectionScreen extends BaseObjectSelectionScreen<Fluid> {
    public FluidSelectionScreen(ReplacementEditorScreen parent, boolean isForMatch) {
        super(parent, isForMatch, Component.translatable("gui.oneenoughfluid.add_fluid"));
        this.allObjects = loadAllObjects();
        this.filteredObjects = new ArrayList<>(this.allObjects);
    }

    @Override
    protected Component sortLabel() {
        return Component.translatable("gui.oneenoughitem.sort.name");
    }

    @Override
    protected List<Fluid> loadAllObjects() {
        return ForgeRegistries.FLUIDS.getValues().stream()
                .filter(f -> f != Fluids.EMPTY)
                .filter(f -> f.defaultFluidState().isSource())
                .collect(Collectors.toList());
    }

    @Override
    protected String getId(Fluid obj) {
        var key = ForgeRegistries.FLUIDS.getKey(obj);
        return key != null ? key.toString() : null;
    }

    @Override
    protected void renderObject(Fluid obj, GuiGraphics graphics, int x, int y) {
        GuiUtils.drawItemBox(graphics, x, y, 18, 18);

        FluidStack stack = new FluidStack(obj, 1000);
        FluidRenderUtils.renderFluid(graphics, stack, x + 1, y + 1, 16, 16);
    }

    @Override
    protected void onSelectSingle(String id) {
        var key = ResourceLocation.tryParse(id);
        Fluid fluid = key != null ? ForgeRegistries.FLUIDS.getValue(key) : null;
        String fluidId = null;
        if (fluid != null && fluid != Fluids.EMPTY) {
            fluidId = Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(fluid)).toString();
        } else {
            var itemKey = ResourceLocation.tryParse(id);
            var item = itemKey != null ? ForgeRegistries.ITEMS.getValue(itemKey) : null;
            if (item != null) {
                var contained = ReplacementControl.withSkipReplacement(() ->
                        FluidUtil.getFluidContained(new ItemStack(item)).orElse(FluidStack.EMPTY));
                if (!contained.isEmpty() && contained.getFluid() != Fluids.EMPTY) {
                    var fk = ForgeRegistries.FLUIDS.getKey(contained.getFluid());
                    if (fk != null) fluidId = fk.toString();
                }
            }
        }
        if (fluidId == null) return;
        if (this.isForMatch) this.parent.addMatchDataId(fluidId);
        else this.parent.setResultDataId(fluidId);
    }


    @Override
    protected boolean isSelectable(String id, boolean forMatch) {
        var rl = ResourceLocation.tryParse(id);
        Fluid fluid = rl != null ? ForgeRegistries.FLUIDS.getValue(rl) : null;
        return fluid != null && fluid != Fluids.EMPTY;
    }

    @Override
    protected Comparator<Fluid> getComparator(SortMode mode) {
        return switch (mode) {
            case NAME -> Comparator.comparing(f -> {
                ItemStack bucket = ReplacementControl.withSkipReplacement(() ->
                        FluidUtil.getFilledBucket(new FluidStack(f, 1000)));
                return bucket.isEmpty() ? (ForgeRegistries.FLUIDS.getKey(f).toString()) : bucket.getHoverName().getString();
            });
            case MOD ->
                    Comparator.comparing(f -> Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(f)).getNamespace());
            case ID -> Comparator.comparing(f -> Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(f)).toString());
        };
    }

    @Override
    protected void updateGrid() {
        if (this.itemGrid == null) return;

        List<ItemStack> pageItems = new ArrayList<>();
        int startIndex = this.currentPage * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, this.filteredObjects.size());
        for (int i = startIndex; i < endIndex; i++) {
            Fluid f = this.filteredObjects.get(i);
            ItemStack bucket = ReplacementControl.withSkipReplacement(() ->
                    FluidUtil.getFilledBucket(new FluidStack(f, 1000)));
            pageItems.add(bucket.isEmpty() ? ItemStack.EMPTY : bucket);
        }
        this.itemGrid.setItems(pageItems);
        this.itemGrid.setSelectedItemIds(this.selectedIds);
    }
}