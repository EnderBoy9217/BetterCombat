package net.bettercombat.mixin.additions;

import net.bettercombat.BetterCombat;
import net.bettercombat.accessors.ShieldInterface;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class ShieldBreakPlayerEntityMixin {

    @Shadow
    public void disableShield(boolean sprinting) {}

    @Inject(method = "takeShieldHit", at = @At("TAIL"))
    protected void takeShieldHit(LivingEntity attacker, CallbackInfo ci) {
        // Already Confirmed is not a projectile, safe to knockback
        LivingEntity self = (LivingEntity) (Object) this;
        attacker.takeKnockback(BetterCombat.config.shield_knockback, self.getX() - attacker.getX(), self.getZ() - attacker.getZ());
    }

}
