package net.bettercombat.accessors;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Unique;

public interface SwordItemInterface {
    int getParryCooldown();
    int getParryTime();
    boolean getBlocking();
    void setParryCooldown(int cooldown);
    void setParryTime(int time);
    void setBlocking(boolean value);
}
