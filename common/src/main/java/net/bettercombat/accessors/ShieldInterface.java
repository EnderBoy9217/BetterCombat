package net.bettercombat.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Unique;

public interface ShieldInterface {
    void initMaxShieldHealth(ItemStack stack);
    float getShieldHealth(ItemStack stack);
    float getMaxShieldHealth(ItemStack stack);
    void setShieldHealth(float shieldHealth, ItemStack stack);
    int getShieldRegenTime(ItemStack stack);
    void restartShieldRegenTime(ItemStack stack);
    void setShieldRegenTime(int time, ItemStack stack);
}
