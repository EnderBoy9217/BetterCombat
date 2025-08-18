package net.bettercombat.mixin.additions;

import net.bettercombat.BetterCombat;
import net.bettercombat.accessors.HudInterface;
import net.bettercombat.accessors.ShieldInterface;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
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
    private float maxShieldHealth = BetterCombat.config.shield_max_health + (BetterCombat.config.unbreaking_bonus * EnchantmentHelper.getLevel(Enchantments.UNBREAKING, ((ShieldItem)(Object)this).getDefaultStack()));


    public float getMaxShieldHealth() {
        float maxShieldHealth = BetterCombat.config.shield_max_health + (BetterCombat.config.unbreaking_bonus * EnchantmentHelper.getLevel(Enchantments.UNBREAKING, ((ShieldItem)(Object)this).getDefaultStack()));
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
        boolean decrease = shieldHealth < this.shieldHealth;
        this.shieldHealth = shieldHealth;
        //displayShieldHealth();
        if ( shieldHealth == maxShieldHealth || decrease ) {
            restartShieldRegenTime();
        }
    }

    public int getShieldRegenTime() {
        return shieldRegenTime;
    }

    public void setShieldRegenTime(int shieldRegenTime) {
        this.shieldRegenTime = shieldRegenTime;
    }

    public void restartShieldRegenTime() {
        this.shieldRegenTime = 0;
    }
}
