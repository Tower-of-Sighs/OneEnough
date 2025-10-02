package com.mafuyu404.oneenoughitem.client.gui.util;

import com.mafuyu404.oneenoughitem.api.DomainRuntimeCache;
import com.mafuyu404.oneenoughitem.client.gui.cache.AbstractGlobalReplacementCache;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.function.Function;

public class UiTooltipRenderHelper {
    public static void addDataTooltip(
            List<Component> tooltip,
            String dataId,
            DomainRuntimeCache runtime,
            AbstractGlobalReplacementCache global,
            String modId,
            String keyPrefix,
            Function<String, String> repNameResolver
    ) {
        String runtimeRep = runtime.matchData(dataId);
        String rep = runtimeRep != null ? runtimeRep : global.getDataReplacement(dataId);
        boolean isRuntime = runtimeRep != null;
        boolean usedAsResult = global.isDataUsedAsResult(dataId);

        appendReplacementTooltip(tooltip, rep, modId, keyPrefix, repNameResolver);

        if (rep != null) {
            tooltip.add(Component.translatable(
                    isRuntime ? "tooltip." + modId + ".source_runtime"
                            : "tooltip." + modId + ".source_saved"
            ).withStyle(ChatFormatting.YELLOW));
        }
        if (usedAsResult) {
            tooltip.add(Component.translatable("tooltip." + modId + ".item_used_as_result")
                    .withStyle(ChatFormatting.GREEN));
        }
    }

    public static void addTagTooltip(
            List<Component> tooltip,
            ResourceLocation tagId,
            DomainRuntimeCache runtime,
            AbstractGlobalReplacementCache global,
            String modId,
            String keyPrefix,
            Function<String, String> repNameResolver
    ) {
        String runtimeRep = runtime.matchTag(tagId);
        String rep = runtimeRep != null ? runtimeRep : global.getTagReplacement(tagId.toString());

        appendReplacementTooltip(tooltip, rep, modId, keyPrefix, repNameResolver);
    }
    private static void appendReplacementTooltip(
            List<Component> tooltip,
            String rep,
            String modId,
            String keyPrefix,
            Function<String, String> repNameResolver
    ) {
        if (rep == null) return;

        tooltip.add(Component.translatable("tooltip." + modId + ".item_replaced")
                .withStyle(ChatFormatting.RED));

        if (rep.startsWith("#")) {
            tooltip.add(Component.translatable("tooltip." + modId + ".replaced_with_tag", rep)
                    .withStyle(ChatFormatting.RED));
        } else {
            String name = repNameResolver.apply(rep);
            tooltip.add(Component.translatable("tooltip." + modId + ".replaced_with_" + keyPrefix, name)
                    .withStyle(ChatFormatting.AQUA));
        }
    }

    public static void renderDataIndicators(GuiGraphics g, String dataId,
                                            DomainRuntimeCache runtime,
                                            AbstractGlobalReplacementCache global,
                                            int x, int y) {
        boolean replaced = dataId != null && (runtime.matchData(dataId) != null || global.getDataReplacement(dataId) != null);
        boolean used = dataId != null && global.isDataUsedAsResult(dataId);
        if (replaced) {
            ReplacementUtils.ReplacementIndicator.renderItemReplaced(g, x, y);
        } else if (used) {
            ReplacementUtils.ReplacementIndicator.renderItemUsedAsResult(g, x, y);
        }
    }

    public static void renderTagIndicators(GuiGraphics g, ResourceLocation tagId,
                                           DomainRuntimeCache runtime,
                                           AbstractGlobalReplacementCache global,
                                           int x, int y) {
        boolean replaced = runtime.matchTag(tagId) != null || global.getTagReplacement(tagId.toString()) != null;
        if (replaced) {
            ReplacementUtils.ReplacementIndicator.renderTagReplaced(g, x, y);
        }
    }
}