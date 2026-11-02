package cc.sighs.oneenoughfluid.api.adapter;

import cc.sighs.oelib.renderer.FluidRenderers;
import cc.sighs.oneenoughfluid.Oneenoughfluid;
import cc.sighs.oneenoughfluid.api.adapter.ui.FluidReplacementUiAdapter;
import cc.sighs.oneenoughfluid.client.gui.FluidSelectionScreen;
import cc.sighs.oneenoughfluid.client.gui.FluidTagSelectionScreen;
import cc.sighs.oneenoughfluid.client.gui.cache.GlobalFluidReplacementCache;
import cc.sighs.oneenoughfluid.init.FluidReplacementCache;
import cc.sighs.oneenoughitem.api.DomainAdapter;
import cc.sighs.oneenoughitem.api.DomainRuntimeCache;
import cc.sighs.oneenoughitem.api.ReplacementUiAdapter;
import cc.sighs.oneenoughitem.client.gui.ReplacementEditorScreen;
import cc.sighs.oneenoughitem.client.gui.cache.AbstractGlobalReplacementCache;
import cc.sighs.oneenoughitem.client.gui.util.GuiUtils;
import cc.sighs.oneenoughitem.util.ReplacementControl;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;

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
            public String matchTag(Identifier tagId) {
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
            public boolean isTagReplaced(Identifier tagId) {
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
        var key = BuiltInRegistries.FLUID.getKey(fluid);
        return fluid != Fluids.EMPTY ? key.toString() : null;
    }

    @Override
    public ItemStack iconForDataId(String dataId) {
        var rl = Identifier.tryParse(dataId);
        var fluid = rl != null ? BuiltInRegistries.FLUID.getValue(rl) : null;
        if (fluid == null || fluid == Fluids.EMPTY) return ItemStack.EMPTY;
        return ReplacementControl.withSkipReplacement(
                () -> FluidUtil.getFilledBucket(new FluidStack(fluid, 1000))
        );
    }

    @Override
    public void renderDataId(GuiGraphicsExtractor graphics, String dataId, int x, int y) {
        GuiUtils.drawItemBox(graphics, x, y, 18, 18);
        var rl = Identifier.tryParse(dataId);
        var fluid = rl != null ? BuiltInRegistries.FLUID.getValue(rl) : null;
        if (fluid == null || fluid == Fluids.EMPTY) {
            return;
        }
        ReplacementControl.withSkipReplacement(() -> {
            long capacity = FluidType.BUCKET_VOLUME;
            FluidRenderers.render(graphics, FluidRenderers.of(fluid), 1000, capacity, x + 1, y + 1, 16, 16);
        });
    }

    @Override
    public Component displayName(String dataId) {
        var rl = Identifier.tryParse(dataId);
        var fluid = rl != null ? BuiltInRegistries.FLUID.getValue(rl) : null;
        if (fluid == null || fluid == Fluids.EMPTY) return Component.literal(dataId);
        return ReplacementControl.withSkipReplacement(() -> {
            FluidStack fs = new FluidStack(fluid, 1000);
            return fs.getHoverName();
        });
    }

    @Override
    public ReplacementUiAdapter uiAdapter() {
        return new FluidReplacementUiAdapter(runtimeCache(), globalCache());
    }
}
