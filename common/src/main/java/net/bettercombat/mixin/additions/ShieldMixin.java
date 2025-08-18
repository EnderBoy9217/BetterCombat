package net.bettercombat.mixin.additions;

import net.bettercombat.BetterCombat;
import net.bettercombat.accessors.ShieldInterface;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShieldItem.class)
public class ShieldMixin implements ShieldInterface {

    private static final String TAG_MAX_HEALTH = "ShieldMaxHealth";
    private static final String TAG_HEALTH = "ShieldHealth";
    private static final String TAG_REGEN = "ShieldRegenTime";

    /** Initialize or recalc max shield health based on config + unbreaking */
    @Override
    public void initMaxShieldHealth(ItemStack stack) {
        int unbreakingLevel = EnchantmentHelper.getLevel(Enchantments.UNBREAKING, stack);
        float maxHealth = BetterCombat.config.shield_max_health +
                (BetterCombat.config.unbreaking_bonus * unbreakingLevel);

        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putFloat(TAG_MAX_HEALTH, maxHealth);

        // If no shield health stored yet, set it to max
        if (!nbt.contains(TAG_HEALTH)) {
            nbt.putFloat(TAG_HEALTH, maxHealth);
        }
        if (!nbt.contains(TAG_REGEN)) {
            nbt.putInt(TAG_REGEN, 0);
        }
    }

    @Override
    public float getMaxShieldHealth(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains(TAG_MAX_HEALTH)) {
            initMaxShieldHealth(stack);
        }
        return nbt.getFloat(TAG_MAX_HEALTH);
    }

    @Override
    public float getShieldHealth(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains(TAG_HEALTH)) {
            initMaxShieldHealth(stack);
        }
        return nbt.getFloat(TAG_HEALTH);
    }

    @Override
    public void setShieldHealth(float newHealth, ItemStack stack) {
        initMaxShieldHealth(stack);
        NbtCompound nbt = stack.getOrCreateNbt();

        float oldHealth = getShieldHealth(stack);
        nbt.putFloat(TAG_HEALTH, newHealth);

        boolean decrease = newHealth < oldHealth;
        if (newHealth == getMaxShieldHealth(stack) || decrease) {
            restartShieldRegenTime(stack);
        }
    }

    @Override
    public int getShieldRegenTime(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        if (!nbt.contains(TAG_REGEN)) {
            initMaxShieldHealth(stack);
        }
        return nbt.getInt(TAG_REGEN);
    }

    @Override
    public void setShieldRegenTime(int time, ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        nbt.putInt(TAG_REGEN, time);
    }

    @Override
    public void restartShieldRegenTime(ItemStack stack) {
        setShieldRegenTime(0,stack);
    }
}