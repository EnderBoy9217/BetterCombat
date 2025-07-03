package net.bettercombat.mixin.additions;

import net.bettercombat.accessors.SwordItemInterface;
import net.minecraft.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SwordItem.class)
public class SwordItemMixin implements SwordItemInterface {

    @Unique
    private int parryCooldown = 0;

    @Unique
    private int parryTime = 0;

    @Unique
    private boolean isBlocking = false;

    @Unique
    private boolean shouldShowShield = false;

    public int getParryCooldown() {
        return parryCooldown;
    }

    public int getParryTime() {
        return parryTime;
    }

    public void setBlocking(boolean value) {
        isBlocking = value;
    }

    public boolean getBlocking() {
        return isBlocking;
    }

    public void setParryCooldown(int cooldown) {
        parryCooldown = cooldown;
    }

    public void setParryTime(int time) {
        parryTime = time;
    }

    public boolean getShouldShowShield() {
        return shouldShowShield;
    }

    public void setShouldShowShield(boolean value) {
        shouldShowShield = value;
    }
}
