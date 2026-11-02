package com.flechazo.oneenoughfluid.init;

import com.flechazo.oneenoughfluid.Oneenoughfluid;
import com.mafuyu404.oneenoughitem.Oneenoughitem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;

import java.util.*;

public class Utils {
    public static String getFluidRegistryName(Fluid fluid) {
        if (fluid == null) return null;
        ResourceLocation id = BuiltInRegistries.FLUID.getKey(fluid);
        return id.toString();
    }

    public static Fluid getFluidById(String id) {
        if (id == null || id.isEmpty()) return null;
        try {
            ResourceLocation rl = ResourceLocation.parse(id);
            return BuiltInRegistries.FLUID.get(rl);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isTagExists(ResourceLocation tagId, HolderLookup.RegistryLookup<Fluid> registryLookup) {
        TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, tagId);
        return registryLookup.get(tagKey).isPresent();
    }

    public static Collection<Fluid> getFluidsOfTag(ResourceLocation tagId, HolderLookup.RegistryLookup<Fluid> registryLookup) {
        TagKey<Fluid> tagKey = TagKey.create(Registries.FLUID, tagId);
        Collection<Fluid> result = new HashSet<>();

        Oneenoughfluid.LOGGER.debug("Attempting to resolve tag: {}", tagId);

        var tagOptional = registryLookup.get(tagKey);
        if (tagOptional.isPresent()) {
            var holderSet = tagOptional.get();
            for (var holder : holderSet) {
                result.add(holder.value());
            }
            Oneenoughfluid.LOGGER.debug("Tag {} resolved to {} fluid: {}",
                    tagId, result.size(),
                    result.stream().map(Utils::getFluidRegistryName).toList());
        } else {
            Oneenoughfluid.LOGGER.warn("Tag {} not found in registry lookup", tagId);
        }

        return result;
    }


    public static List<Fluid> resolveFluidList(List<String> identifiers, HolderLookup.RegistryLookup<Fluid> registryLookup) {
        List<Fluid> result = new ArrayList<>();

        for (String id : identifiers) {
            if (id == null || id.isEmpty()) continue;

            if (id.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(id.substring(1));
                if (tagId == null) {
                    Oneenoughfluid.LOGGER.warn("Invalid tag ID format: {}", id);
                    continue;
                }

                Collection<Fluid> tagFluid = getFluidsOfTag(tagId, registryLookup);
                if (tagFluid.isEmpty()) {
                    Oneenoughfluid.LOGGER.warn("Tag {} is empty or not found", tagId);
                } else {
                    result.addAll(tagFluid);
                }
            } else {
                Fluid fluid = getFluidById(id);
                if (fluid != null) {
                    result.add(fluid);
                } else {
                    Oneenoughfluid.LOGGER.warn("Fluid ID not found: {}", id);
                }
            }
        }

        return result;
    }
}