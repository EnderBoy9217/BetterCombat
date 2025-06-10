package net.bettercombat.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Unique;

public interface ShieldInterface {
    float getShieldHealth();
    void setShieldHealth(float shieldHealth);

    @Unique
    void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected);
}
