package cc.sighs.oneenoughitem.api;

import net.minecraft.world.food.FoodProperties;

import javax.annotation.Nullable;

public interface EditableItem {
    void setFoodProperties(@Nullable FoodProperties foodProperties);
}
