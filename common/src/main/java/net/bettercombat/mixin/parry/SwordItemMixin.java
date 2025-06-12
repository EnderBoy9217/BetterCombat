package net.bettercombat.mixin.parry;

import net.bettercombat.accessors.SwordItemInterface;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SwordItem.class)
public class SwordItemMixin implements SwordItemInterface {


    @Unique
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        user.setCurrentHand(hand); // <-- triggers usage/blocking
        return TypedActionResult.consume(stack);
    }

    @Unique
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    @Unique
    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Unique
    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
    }
}
