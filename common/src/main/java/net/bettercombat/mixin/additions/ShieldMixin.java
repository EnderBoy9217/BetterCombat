package net.bettercombat.mixin.additions;

import net.bettercombat.BetterCombat;
import net.bettercombat.accessors.HudInterface;
import net.bettercombat.accessors.ShieldInterface;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.world.World;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ShieldItem.class)
public class ShieldMixin implements ShieldInterface {

    @Unique
    private float maxShieldHealth = 10.0F;

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
        displayShieldHealth();
        if ( shieldHealth == maxShieldHealth) {
            restartShieldRegenTime();
        }
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
    private void displayShieldHealth() {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            return;
        }
        InGameHud hud = MinecraftClient.getInstance().inGameHud;
        HudInterface accessor = ((HudInterface)hud);
        float percentage = 16 * (shieldHealth / maxShieldHealth);

        accessor.setAmountHidden(16-(int)percentage);
    }

    @Unique
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        float currentMaxShieldHealth = getMaxShieldHealth();
        if ( shieldHealth != currentMaxShieldHealth && entity instanceof LivingEntity mob) {
            if (shieldRegenTime >= BetterCombat.config.shield_regen_time) {
                setShieldHealth(Math.min(currentMaxShieldHealth, shieldHealth+0.25F) );
            }
            shieldRegenTime++;
        }
    }

}
