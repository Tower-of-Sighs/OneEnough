package com.flechazo.oneenoughfluid.data;

import com.flechazo.oneenoughfluid.init.Utils;
import com.mafuyu404.oneenoughitem.data.BaseReplacementValidator;
import com.mafuyu404.oneenoughitem.data.ValidationStreams;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.material.Fluid;

public class FluidReplacementValidator extends BaseReplacementValidator<Fluid> {
    @Override
    protected boolean isResultExists(String resultId, HolderLookup.RegistryLookup<Fluid> registryLookup) {
        Fluid f = Utils.getFluidById(resultId);
        return f != null;
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainObject(String id, ResourceLocation source, HolderLookup.RegistryLookup<Fluid> registryLookup) {
        return Utils.getFluidById(id) != null
                ? ValidationStreams.Accumulator.valid(1)
                : ValidationStreams.Accumulator.invalid();
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainTag(String tagId, ResourceLocation source, HolderLookup.RegistryLookup<Fluid> registryLookup) {
        try {
            ResourceLocation tag = ResourceLocation.parse(tagId);
            if (Utils.isTagExists(tag, registryLookup)) {
                var objs = Utils.getFluidsOfTag(tag, registryLookup);
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

    @Override
    protected HolderLookup.RegistryLookup<Fluid> getRegistryLookup(MinecraftServer server) {
        return server.registryAccess().lookupOrThrow(Registries.FLUID);
    }
}
