package cc.sighs.oneenoughitem.util;

import cc.sighs.oneenoughitem.Oneenoughitem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

public class Utils {
    public static String getItemRegistryName(Item item) {
        if (item == null) return null;
        Identifier registryName = BuiltInRegistries.ITEM.getKey(item);
        return registryName.toString();
    }

    public static Item getItemById(String registryName) {
        if (registryName == null || registryName.isEmpty()) return null;

        try {
            Identifier resourceLocation = Identifier.parse(registryName);
            if (!BuiltInRegistries.ITEM.containsKey(resourceLocation)) return null;
            return BuiltInRegistries.ITEM.getValue(resourceLocation);
        } catch (Exception e) {
            Oneenoughitem.LOGGER.debug("getItemById: Exception for {}", registryName, e);
            return null;
        }
    }

    public static Collection<Item> getItemsOfTag(Identifier tagId, HolderLookup.RegistryLookup<Item> registryLookup) {
        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
        Collection<Item> result = new HashSet<>();

        Oneenoughitem.LOGGER.debug("Attempting to resolve tag: {}", tagId);

        if (registryLookup == null) {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item.builtInRegistryHolder().is(tagKey)) result.add(item);
            }
            return result;
        }
        var tagOptional = registryLookup.get(tagKey);
        if (tagOptional.isPresent()) {
            var holderSet = tagOptional.get();
            for (var holder : holderSet) {
                result.add(holder.value());
            }
            Oneenoughitem.LOGGER.debug("Tag {} resolved to {} items: {}",
                    tagId, result.size(),
                    result.stream().map(Utils::getItemRegistryName).toList());
        } else {
            Oneenoughitem.LOGGER.warn("Tag {} not found in registry lookup", tagId);
        }

        return result;
    }

    public static boolean isTagExists(Identifier tagId, HolderLookup.RegistryLookup<Item> registryLookup) {
        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
        return registryLookup != null && registryLookup.get(tagKey).isPresent();
    }


    public static List<Item> resolveItemList(List<String> identifiers, HolderLookup.RegistryLookup<Item> registryLookup) {
        List<Item> result = new ArrayList<>();

        for (String id : identifiers) {
            if (id == null || id.isEmpty()) continue;

            if (id.startsWith("#")) {
                Identifier tagId = Identifier.tryParse(id.substring(1));
                if (tagId == null) {
                    Oneenoughitem.LOGGER.warn("Invalid tag ID format: {}", id);
                    continue;
                }

                Collection<Item> tagItems = getItemsOfTag(tagId, registryLookup);
                if (tagItems.isEmpty()) {
                    Oneenoughitem.LOGGER.warn("Tag {} is empty or not found", tagId);
                } else {
                    result.addAll(tagItems);
                }
            } else {
                Item item = getItemById(id);
                if (item != null) {
                    result.add(item);
                } else {
                    Oneenoughitem.LOGGER.warn("Item ID not found: {}", id);
                }
            }
        }

        return result;
    }

    public static boolean isItemIdEmpty(String id) {
        return id == null || id.equals("minecraft:air");
    }
}
