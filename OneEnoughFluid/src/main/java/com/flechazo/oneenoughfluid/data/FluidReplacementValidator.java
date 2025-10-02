package com.flechazo.oneenoughfluid.data;

import com.flechazo.oneenoughfluid.init.Utils;
import com.mafuyu404.oneenoughitem.data.BaseReplacementValidator;
import com.mafuyu404.oneenoughitem.data.ValidationStreams;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;

public class FluidReplacementValidator extends BaseReplacementValidator {
    @Override
    protected boolean isResultExists(String resultId) {
        Fluid f = Utils.getFluidById(resultId);
        return f != null;
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainObject(String id, ResourceLocation source) {
        return Utils.getFluidById(id) != null
                ? ValidationStreams.Accumulator.valid(1)
                : ValidationStreams.Accumulator.invalid();
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainTag(String tagId, ResourceLocation source) {
        try {
            ResourceLocation tag = new ResourceLocation(tagId);
            if (Utils.isTagExists(tag)) {
                var objs = Utils.getFluidsOfTag(tag);
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