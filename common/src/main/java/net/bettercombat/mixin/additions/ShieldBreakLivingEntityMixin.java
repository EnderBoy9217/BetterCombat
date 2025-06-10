package net.bettercombat.mixin.additions;

import net.bettercombat.BetterCombat;
import net.bettercombat.accessors.ShieldInterface;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class ShieldBreakLivingEntityMixin {

    @Shadow
    protected ItemStack activeItemStack;
    /**
     * @author EnderBoy9217
     * @reason Axes should now damage shields, not simply disable them.
     */
    @Overwrite
    public boolean disablesShield() { return false; }

    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
    public void damageShield(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Item item = this.activeItemStack.getItem();
        if (item.getUseAction(this.activeItemStack) == UseAction.BLOCK) {
            // Item can be assumed to be a shield
            float maxShieldHealth = BetterCombat.config.shield_max_health;

            float shieldHealth = ((ShieldInterface) item).getShieldHealth();
            shieldHealth -= amount;
            ((ShieldInterface) item).setShieldHealth(shieldHealth);
            System.out.println("Shield Health:" + shieldHealth);
            if (shieldHealth <= 0) {
                if ( ((LivingEntity)(Object)this) instanceof PlayerEntity player ) {
                    player.disableShield(true);
                    ((ShieldInterface) item).setShieldHealth(maxShieldHealth);
                }
            }
        }
    }
}
