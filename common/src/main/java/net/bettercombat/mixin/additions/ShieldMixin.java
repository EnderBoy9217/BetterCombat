package net.bettercombat.mixin.additions;

import net.bettercombat.BetterCombat;
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
    private float maxShieldHealth = 10;

    @Unique
    private float getMaxShieldHealth() {
        maxShieldHealth = BetterCombat.config.shield_max_health;
        return maxShieldHealth;
    }

    @Unique
    private float shieldHealth = maxShieldHealth;
    @Unique
    private int shieldRegenTime = 0;

    @Unique
    public float getShieldHealth() {
        return shieldHealth;
    }
    @Unique
    public void setShieldHealth(float shieldHealth) {
        this.shieldHealth = shieldHealth;
        restartShieldRegenTime();
    }

    @Unique
    public int getShieldRegenTime() {
        return shieldRegenTime;
    }

    @Unique
    public void restartShieldRegenTime() {
        this.shieldRegenTime = 0;
    }

    @Unique
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        if ( shieldHealth != getMaxShieldHealth() && entity instanceof LivingEntity mob) {
            if (shieldRegenTime >= BetterCombat.config.shield_regen_time) {
                System.out.println("Healing Shield " + shieldRegenTime);
                setShieldHealth(getMaxShieldHealth());
            }
        }
        shieldRegenTime++;
    }

}
