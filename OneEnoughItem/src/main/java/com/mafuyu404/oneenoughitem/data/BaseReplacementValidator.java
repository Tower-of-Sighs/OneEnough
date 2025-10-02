package com.mafuyu404.oneenoughitem.data;

import com.mafuyu404.oelib.api.data.DataValidator;
import net.minecraft.resources.ResourceLocation;

public abstract class BaseReplacementValidator implements DataValidator<Replacements> {
    protected abstract boolean isResultExists(String resultId);

    protected abstract ValidationStreams.Accumulator fromDomainObject(String id, ResourceLocation source);

    protected abstract ValidationStreams.Accumulator fromDomainTag(String tagId, ResourceLocation source);

    @Override
    public ValidationResult validate(Replacements replacement, ResourceLocation source) {
        if (!isResultExists(replacement.result())) {
            return ValidationResult.failure("Target not exists: '" + replacement.result() + "'");
        }
        var acc = replacement.match().stream()
                .map(item -> {
                    if (item.startsWith("#")) {
                        return fromDomainTag(item.substring(1), source);
                    } else {
                        return fromDomainObject(item, source);
                    }
                })
                .reduce(ValidationStreams.Accumulator.identity(), ValidationStreams.Accumulator::combine);
        return acc.toResult(
                "Contains unresolved tags, validation deferred until tag system is ready",
                "No valid sources found for target '" + replacement.result() + "'"
        );
    }
}