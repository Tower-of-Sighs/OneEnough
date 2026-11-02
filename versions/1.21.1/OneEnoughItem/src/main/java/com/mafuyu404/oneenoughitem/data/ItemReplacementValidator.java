package com.mafuyu404.oneenoughitem.data;

import com.mafuyu404.oneenoughitem.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;

public class ItemReplacementValidator extends BaseReplacementValidator<Item> {
    @Override
    protected boolean isResultExists(String resultId, HolderLookup.RegistryLookup<Item> registryLookup) {
        return Utils.getItemById(resultId) != null;
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainObject(String id, ResourceLocation source, HolderLookup.RegistryLookup<Item> registryLookup) {
        return Utils.getItemById(id) != null
                ? ValidationStreams.Accumulator.valid(1)
                : ValidationStreams.Accumulator.invalid();
    }

    @Override
    protected ValidationStreams.Accumulator fromDomainTag(String tagId, ResourceLocation source, HolderLookup.RegistryLookup<Item> registryLookup) {
        return Validators.fromTag(tagId, source, registryLookup);
    }

    @Override
    protected HolderLookup.RegistryLookup<Item> getRegistryLookup(MinecraftServer server) {
        return Validators.getItemRegistryLookup(server);
    }
}
