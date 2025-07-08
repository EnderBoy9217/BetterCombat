package net.bettercombat.mixin.additions;

import net.bettercombat.BetterCombat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class EatingCancelLivingEntityMixin {

    @Shadow
    protected ItemStack activeItemStack;

    @Inject(method = "damage" , at = @At("TAIL"), cancellable = true)
    public void cancelEating(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (BetterCombat.config.eatingInterruption && !source.isIn(DamageTypeTags.BYPASSES_ARMOR)) {
            LivingEntity self = (LivingEntity)(Object)this;
            this.activeItemStack.getItem();
            if (this.activeItemStack.getItem().getUseAction(this.activeItemStack) == UseAction.EAT || this.activeItemStack.getItem().getUseAction(this.activeItemStack) == UseAction.DRINK) {
                self.stopUsingItem();
            }
        }
    }

}
