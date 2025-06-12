package net.bettercombat.mixin.client;

import net.bettercombat.BetterCombat;
import net.bettercombat.logic.PlayerAttackHelper;
import net.bettercombat.mixin.LivingEntityAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Shadow @Final private MinecraftClient client;

    @Inject(method = "cancelBlockBreaking", at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/network/ClientPlayerEntity;resetLastAttackedTicks()V",
            shift = At.Shift.AFTER))
    public void cancelBlockBreaking_FixAttackCD(CallbackInfo ci) {
        try {
            var player = client.player;
            var cooldownLength = PlayerAttackHelper.getAttackCooldownTicksCapped(player); // `getAttackCooldownProgressPerTick` should be called `getAttackCooldownLengthTicks`
            float typicalUpswing = 0.5F;
            int reducedCooldown = Math.round(cooldownLength * typicalUpswing * BetterCombat.config.upswing_multiplier);
            ((LivingEntityAccessor)player).setLastAttackedTicks(reducedCooldown);
        } catch (Exception ignored) { } // We may get random exceptions when trying to access weapon cooldown
    }


    @Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
    public void checkForSwordBlock(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
        ItemStack stack = player.getStackInHand(hand);
        ItemStack offhandStack = player.getOffHandStack();

        // Only intercept if the item in use is a sword
        if (!(stack.getItem() instanceof SwordItem)) {
            return;
        }

        // Allow blocking if offhand is empty, a sword, or non-edible food
        if (!offhandStack.isEmpty()) {
            if (offhandStack.getUseAction() == UseAction.EAT && ( player.canConsume(false))
                || (offhandStack.getUseAction() != UseAction.NONE && !(offhandStack.getItem() instanceof SwordItem) && !(offhandStack.getUseAction() == UseAction.EAT))
            ) {
                cir.setReturnValue(ActionResult.PASS);
                cir.cancel();
                return;
            }
        }

        // Trigger sword use (blocking)
        TypedActionResult<ItemStack> result = stack.use(player.getWorld(), player, hand);
        player.setStackInHand(hand, result.getValue()); // Update stack if modified (e.g., durability)
        cir.setReturnValue(result.getResult());
        cir.cancel();
    }




}
