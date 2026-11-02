package com.sighs.oneenoughblock.data;

import com.mafuyu404.oneenoughitem.data.BaseReplacementValidator;
import com.mafuyu404.oneenoughitem.data.ValidationStreams;
import com.sighs.oneenoughblock.init.Utils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.Block;

public class BlockReplacementValidator extends BaseReplacementValidator<Block> {
    @Override
    protected boolean isResultExists(String resultId, HolderLookup.RegistryLookup<Block> registryLookup) {
        Block b = Utils.getBlockById(resultId);
        return b != null;
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainObject(String id, ResourceLocation source, HolderLookup.RegistryLookup<Block> registryLookup) {
        return Utils.getBlockById(id) != null
                ? ValidationStreams.Accumulator.valid(1)
                : ValidationStreams.Accumulator.invalid();
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainTag(String tagId, ResourceLocation source, HolderLookup.RegistryLookup<Block> registryLookup) {
        try {
            ResourceLocation tag = ResourceLocation.parse(tagId);
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
