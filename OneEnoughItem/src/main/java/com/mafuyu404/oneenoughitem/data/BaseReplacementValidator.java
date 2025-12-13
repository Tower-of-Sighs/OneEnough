package com.mafuyu404.oneenoughitem.data;

import com.mafuyu404.oelib.api.data.DataValidator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.core.HolderLookup;

public abstract class BaseReplacementValidator<T> implements DataValidator.ServerContextAware<Replacements> {
    protected abstract HolderLookup.RegistryLookup<T> getRegistryLookup(MinecraftServer server);

    protected abstract boolean isResultExists(String resultId, HolderLookup.RegistryLookup<T> registryLookup);

    protected abstract ValidationStreams.Accumulator fromDomainObject(String id, ResourceLocation source, HolderLookup.RegistryLookup<T> registryLookup);

    protected abstract ValidationStreams.Accumulator fromDomainTag(String tagId, ResourceLocation source, HolderLookup.RegistryLookup<T> registryLookup);

    @Override
    public ValidationResult validateWithContext(Replacements replacement, ResourceLocation source, MinecraftServer server) {
        if (server == null) {
            return ValidationResult.deferred("No server context, deferred validation for " + source);
        }

        HolderLookup.RegistryLookup<T> registryLookup = getRegistryLookup(server);

        if (!isResultExists(replacement.result(), registryLookup)) {
            return ValidationResult.failure("Target not exists: '" + replacement.result() + "'");
        }

        var acc = replacement.match().stream()
                .map(item -> item.startsWith("#")
                        ? fromDomainTag(item.substring(1), source, registryLookup)
                        : fromDomainObject(item, source, registryLookup))
                .reduce(ValidationStreams.Accumulator.identity(), ValidationStreams.Accumulator::combine);

        return acc.toResult(
                "Contains unresolved tags, validation deferred until tag system is ready",
                "No valid sources found for target '" + replacement.result() + "'"
        );
    }
}
