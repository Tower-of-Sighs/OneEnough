package cc.sighs.oneenoughfluid.data;

import cc.sighs.oneenoughfluid.init.Utils;
import cc.sighs.oneenoughitem.data.BaseReplacementValidator;
import cc.sighs.oneenoughitem.data.ValidationStreams;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.material.Fluid;

public class FluidReplacementValidator extends BaseReplacementValidator<Fluid> {
    @Override
    protected boolean isResultExists(String resultId, HolderLookup.RegistryLookup<Fluid> registryLookup) {
        Fluid f = Utils.getFluidById(resultId);
        return f != null;
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainObject(String id, Identifier source, HolderLookup.RegistryLookup<Fluid> registryLookup) {
        return Utils.getFluidById(id) != null
                ? ValidationStreams.Accumulator.valid(1)
                : ValidationStreams.Accumulator.invalid();
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainTag(String tagId, Identifier source, HolderLookup.RegistryLookup<Fluid> registryLookup) {
        try {
            Identifier tag = Identifier.parse(tagId);
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
