package com.flechazo.oneenoughfluid.init;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.tags.ITag;
import net.minecraftforge.registries.tags.ITagManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Utils {
    public static String getFluidRegistryName(Fluid fluid) {
        if (fluid == null) return null;
        ResourceLocation id = ForgeRegistries.FLUIDS.getKey(fluid);
        return id != null ? id.toString() : null;
    }

    public static Fluid getFluidById(String id) {
        if (id == null || id.isEmpty()) return null;
        try {
            ResourceLocation rl = new ResourceLocation(id);
            return ForgeRegistries.FLUIDS.getValue(rl);
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isTagExists(ResourceLocation tagId) {
        ITagManager<Fluid> mgr = ForgeRegistries.FLUIDS.tags();
        TagKey<Fluid> key = ForgeRegistries.FLUIDS.tags().createTagKey(tagId);
        return mgr != null && mgr.isKnownTagName(key);
    }

    public static Collection<Fluid> getFluidsOfTag(ResourceLocation tagId) {
        ITagManager<Fluid> mgr = ForgeRegistries.FLUIDS.tags();
        if (mgr == null) return Collections.emptyList();
        TagKey<Fluid> key = ForgeRegistries.FLUIDS.tags().createTagKey(tagId);

        ITag<Fluid> tag = mgr.getTag(key);
        return tag.stream().toList();
    }


    public static List<Fluid> resolveFluidList(List<String> identifiers) {
        List<Fluid> result = new ArrayList<>();
        for (String id : identifiers) {
            if (id == null || id.isEmpty()) continue;
            if (id.startsWith("#")) {
                ResourceLocation tag = new ResourceLocation(id.substring(1));
                result.addAll(getFluidsOfTag(tag));
            } else {
                Fluid f = getFluidById(id);
                if (f != null) result.add(f);
            }
        }
        return result;
    }
}