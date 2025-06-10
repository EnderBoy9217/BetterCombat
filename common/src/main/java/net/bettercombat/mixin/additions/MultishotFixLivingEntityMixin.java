package net.bettercombat.mixin.additions;

import net.bettercombat.BetterCombat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class MultishotFixLivingEntityMixin {
    @Inject(method = "damage" , at = @At("TAIL"))
    public void endProjectileImmunity(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!BetterCombat.config.noProjectileImmunity && source.isIn(DamageTypeTags.IS_PROJECTILE)) { // Better fix might just be to give the projectiles a BYPASSES_COOLDOWN tag, but oh well
            LivingEntity self = (LivingEntity)(Object)this;
            self.timeUntilRegen = 0;

        }
    }
}
