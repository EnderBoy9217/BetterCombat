package net.bettercombat.mixin.additions;

import net.bettercombat.BetterCombat;
import net.bettercombat.accessors.ShieldInterface;
import net.bettercombat.accessors.SwordItemInterface;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {


    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if ( (Item)(Object)this instanceof SwordItem sword ) {
            SwordItemInterface accessor = (SwordItemInterface)sword;
            ItemStack stack = user.getStackInHand(hand);
            if (accessor.getParryCooldown() == 0) {
                accessor.setParryTime( BetterCombat.config.parry_timing  );
                accessor.setParryCooldown( BetterCombat.config.parry_timing + BetterCombat.config.parry_cooldown );
                //accessor.changeShieldDisplay(true);
            }

            accessor.setBlocking(true);
            user.setCurrentHand(hand); // <-- triggers usage/blocking
            cir.setReturnValue( TypedActionResult.consume(stack) );
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if ( (Item)(Object)this instanceof SwordItem sword ) {
            SwordItemInterface accessor = (SwordItemInterface) sword;
            accessor.setParryTime(0);
            accessor.setBlocking(false);
        }
    }


    @Inject(method = "getUseAction", at = @At("HEAD"), cancellable = true)
    public void getUseAction(ItemStack stack, CallbackInfoReturnable<UseAction> cir) {
        if ( (Item)(Object)this instanceof SwordItem sword ) {
            cir.setReturnValue(UseAction.BLOCK);
        }
    }


    @Inject(method = "getMaxUseTime", at = @At("HEAD"), cancellable = true)
    public void getMaxUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if ( (Item)(Object)this instanceof SwordItem sword ) {
            cir.setReturnValue(72000);
        }
    }

    /*
    @Inject(method = "usageTick", at = @At("HEAD"))
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        if ( (Item)(Object)this instanceof SwordItem sword ) {
            SwordItemInterface accessor = (SwordItemInterface) sword;
            int parryTime = accessor.getParryTime();
            if ( parryTime > 0 ) {
                accessor.setParryTime(parryTime - 1);
            } else {
                accessor.changeShieldDisplay(false);
            }
        }
    }
     */

    @Inject(method = "usageTick", at = @At("HEAD"))
    public void inventoryTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        if ( (Item)(Object)this instanceof SwordItem sword ) {
            SwordItemInterface accessor = (SwordItemInterface) sword;
            int parryCooldown = accessor.getParryCooldown();
            if (parryCooldown > 0) {
                accessor.setParryCooldown(parryCooldown - 1);
            }
            int parryTime = accessor.getParryTime();
            if ( parryTime > 0 ) {
                accessor.setParryTime(parryTime - 1);
            } else {
                //accessor.changeShieldDisplay(false);
            }
        } else if ((Item)(Object)this instanceof SwordItem shield ) {
            ShieldInterface accessor = (ShieldInterface)shield;
            float currentMaxShieldHealth = accessor.getMaxShieldHealth();
            float shieldHealth = accessor.getShieldHealth();
            if ( shieldHealth != currentMaxShieldHealth ) {
                int shieldRegenTime = accessor.getShieldRegenTime();
                if (shieldRegenTime >= BetterCombat.config.shield_regen_time) {
                    accessor.setShieldHealth(Math.min(currentMaxShieldHealth, shieldHealth+0.25F) );
                }
                accessor.setShieldHealth(shieldRegenTime + 1);
            }
        }
    }

}
