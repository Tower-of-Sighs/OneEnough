package com.mafuyu404.oneenoughitem.data;

import com.mafuyu404.oneenoughitem.util.Utils;
import net.minecraft.resources.ResourceLocation;

public class ItemReplacementValidator extends BaseReplacementValidator {
    @Override
    protected boolean isResultExists(String resultId) {
        return Utils.getItemById(resultId) != null;
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainObject(String id, ResourceLocation source) {
        return Utils.getItemById(id) != null
                ? ValidationStreams.Accumulator.valid(1)
                : ValidationStreams.Accumulator.invalid();
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainTag(String tagId, ResourceLocation source) {
        return Validators.fromTag(tagId, source);
    }
}
