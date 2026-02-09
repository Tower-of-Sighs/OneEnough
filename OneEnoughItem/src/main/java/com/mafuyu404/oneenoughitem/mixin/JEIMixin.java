package com.mafuyu404.oneenoughitem.mixin;

import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.List;

@Mixin(targets = "mezz.jei.library.ingredients.IngredientInfo")
public class JEIMixin<T> {
    @Inject(method = "getIngredientAliases", at = @At("HEAD"), cancellable = true)
    private void qq(T ingredient, CallbackInfoReturnable<Collection<String>> cir) {
        if (ingredient instanceof ItemStack itemStack) {
            if (itemStack.isEmpty()) cir.setReturnValue(List.of());
        }
    }
}
