package net.bettercombat.accessors;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Unique;

public interface SwordItemInterface {
    @Unique
    TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand);

    @Unique
    UseAction getUseAction(ItemStack stack);

    @Unique
    int getMaxUseTime(ItemStack stack);

    @Unique
    void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks);
}
