package net.bettercombat.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Unique;

public interface ShieldInterface {
    float getShieldHealth();
    float getMaxShieldHealth();
    void setShieldHealth(float shieldHealth);
    int getShieldRegenTime();
    void restartShieldRegenTime();
}
