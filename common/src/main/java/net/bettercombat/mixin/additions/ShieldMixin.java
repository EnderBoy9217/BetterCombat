package net.bettercombat.mixin.additions;

import net.bettercombat.accessors.ShieldInterface;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ShieldItem.class)
public class ShieldMixin implements ShieldInterface {

    @Unique
    private float maxShieldHealth = 10F;
    @Unique
    private float shieldHealth = 10F;

    @Unique
    public float getShieldHealth() {
        return shieldHealth;
    }
    @Unique
    public void setShieldHealth(float shieldHealth) {
        this.shieldHealth = shieldHealth;
    }

    @Unique
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if ( shieldHealth != maxShieldHealth && entity instanceof LivingEntity mob) {
            if (mob.getLastAttackedTime() >= 40) {
                shieldHealth = maxShieldHealth;
            }
        }
    }

}
