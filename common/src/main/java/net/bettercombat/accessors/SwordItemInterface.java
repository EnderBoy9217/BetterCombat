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

    @Unique
    TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand);

    @Unique
    void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks);

    @Unique
    UseAction getUseAction(ItemStack stack);

    @Unique
    int getMaxUseTime(ItemStack stack);

    @Unique
    void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks);

    @Unique
    void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected);
}
