package net.bettercombat.mixin.additions;

import net.bettercombat.BetterCombat;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HungerManager.class)
public class HungerManagerMixin {

    @Shadow
    private int foodLevel;

    @Shadow
    private int foodTickTimer;

    @Unique
    private int secondaryFoodTickTimer = 0;

    @Inject(method = "update", at = @At("TAIL"))
    private void healOnLessHunger(PlayerEntity player, CallbackInfo ci) {
        if (player.getWorld().getGameRules().getBoolean(GameRules.NATURAL_REGENERATION) && this.foodLevel < 18 && this.foodLevel >= BetterCombat.config.minimumHealingHunger && player.canFoodHeal()) {
            ++this.secondaryFoodTickTimer;
            if (this.foodTickTimer == 0 && this.secondaryFoodTickTimer >= 80) {
                player.heal(1.0f);
                ((HungerManager) (Object) this).addExhaustion(6.0f);
                this.secondaryFoodTickTimer = 0;
            }
        }
    }

}
