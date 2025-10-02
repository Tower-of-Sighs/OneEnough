package com.sighs.oneenoughblock.data;

import com.mafuyu404.oneenoughitem.data.BaseReplacementValidator;
import com.mafuyu404.oneenoughitem.data.ValidationStreams;
import com.sighs.oneenoughblock.init.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

public class BlockReplacementValidator extends BaseReplacementValidator {
    @Override
    protected boolean isResultExists(String resultId) {
        Block b = Utils.getBlockById(resultId);
        return b != null;
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainObject(String id, ResourceLocation source) {
        return Utils.getBlockById(id) != null
                ? ValidationStreams.Accumulator.valid(1)
                : ValidationStreams.Accumulator.invalid();
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainTag(String tagId, ResourceLocation source) {
        try {
            ResourceLocation tag = new ResourceLocation(tagId);
            if (Utils.isTagExists(tag)) {
                var objs = Utils.getBlocksOfTag(tag);
                return !objs.isEmpty()
                        ? ValidationStreams.Accumulator.valid(objs.size())
                        : ValidationStreams.Accumulator.invalid();
            } else {
                return ValidationStreams.Accumulator.deferred();
            }
        } catch (Exception e) {
            return ValidationStreams.Accumulator.failure("Invalid tag format: " + tagId);
        }
    }
}