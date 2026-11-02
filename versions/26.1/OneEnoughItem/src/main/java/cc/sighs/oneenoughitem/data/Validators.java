package cc.sighs.oneenoughitem.data;

import cc.sighs.oneenoughitem.Oneenoughitem;
import cc.sighs.oneenoughitem.util.Utils;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;

public final class Validators {
    private Validators() {
    }

    public static ValidationStreams.Accumulator fromItem(String itemId, Identifier source, HolderLookup.RegistryLookup<Item> registryLookup) {
        if (itemId.startsWith("#")) {
            return fromTag(itemId.substring(1), source, registryLookup);
        }
        return Utils.getItemById(itemId) != null
                ? ValidationStreams.Accumulator.valid(1)
                : ValidationStreams.Accumulator.invalid();
    }

    public static ValidationStreams.Accumulator fromTag(String tagIdString, Identifier source, HolderLookup.RegistryLookup<Item> registryLookup) {
        try {
            Identifier tagId = Identifier.parse(tagIdString);
            if (Utils.isTagExists(tagId, registryLookup)) {
                var tagItems = Utils.getItemsOfTag(tagId, registryLookup);
                if (!tagItems.isEmpty()) {
                    Oneenoughitem.LOGGER.debug("Valid tag in {}: '{}' contains {} items", source, tagId, tagItems.size());
                    return ValidationStreams.Accumulator.valid(tagItems.size());
                } else {
                    Oneenoughitem.LOGGER.warn("Tag in {} is empty: '{}'", source, tagId);
                    return ValidationStreams.Accumulator.invalid();
                }
            } else {
                Oneenoughitem.LOGGER.debug("Tag in {} not found (may be uninitialized): '{}'", source, tagId);
                return ValidationStreams.Accumulator.deferred();
            }
        } catch (Exception e) {
            Oneenoughitem.LOGGER.error("Invalid tag format in {}: '{}'", source, tagIdString, e);
            return ValidationStreams.Accumulator.failure("Invalid tag format: " + tagIdString);
        }
    }


    public static HolderLookup.RegistryLookup<Item> getItemRegistryLookup(MinecraftServer server) {
        return server.registryAccess().lookupOrThrow(Registries.ITEM);
    }
}
