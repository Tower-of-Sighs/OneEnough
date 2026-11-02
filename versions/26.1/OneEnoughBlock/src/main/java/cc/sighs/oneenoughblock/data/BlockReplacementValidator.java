package cc.sighs.oneenoughblock.data;

import cc.sighs.oneenoughitem.data.BaseReplacementValidator;
import cc.sighs.oneenoughitem.data.ValidationStreams;
import cc.sighs.oneenoughblock.init.Utils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;

public class BlockReplacementValidator extends BaseReplacementValidator<Block> {
    @Override
    protected boolean isResultExists(String resultId, HolderLookup.RegistryLookup<Block> registryLookup) {
        Block b = Utils.getBlockById(resultId);
        return b != null;
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainObject(String id, Identifier source, HolderLookup.RegistryLookup<Block> registryLookup) {
        return Utils.getBlockById(id) != null
                ? ValidationStreams.Accumulator.valid(1)
                : ValidationStreams.Accumulator.invalid();
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainTag(String tagId, Identifier source, HolderLookup.RegistryLookup<Block> registryLookup) {
        try {
            Identifier tag = Identifier.parse(tagId);
            if (Utils.isTagExists(tag, registryLookup)) {
                var objs = Utils.getBlocksOfTag(tag, registryLookup);
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
    protected HolderLookup.RegistryLookup<Block> getRegistryLookup(MinecraftServer server) {
        return server.registryAccess().lookupOrThrow(Registries.BLOCK);
    }
}
