package net.bettercombat.mixin.additions;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import net.minecraft.util.UseAction;
import net.minecraft.util.math.RotationAxis;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"), cancellable = true)
    public void onRenderFirstPersonItem(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand, float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        if (item.getItem() instanceof SwordItem && player.isUsingItem() && player.getActiveHand() == hand && item.getUseAction() == UseAction.BLOCK) {
            matrices.push(); // Save matrix state
            Arm arm = hand == Hand.MAIN_HAND ? player.getMainArm() : player.getMainArm().getOpposite();

            // Apply arm transform for hand positioning
            applyItemArmTransform(matrices, arm, equipProgress);

            // Apply custom transformations for blocking
            renderSwordBlock(matrices, arm);

            // Render the item
            HeldItemRenderer renderer = (HeldItemRenderer)(Object)this;
            renderer.renderItem(player, item, hand == Hand.MAIN_HAND ? ModelTransformationMode.FIRST_PERSON_RIGHT_HAND : ModelTransformationMode.FIRST_PERSON_LEFT_HAND, hand == Hand.OFF_HAND, matrices, vertexConsumers, light);

            matrices.pop(); // Restore matrix state
            ci.cancel(); // Cancel vanilla rendering
        }
    }

    @Unique
    private static void renderSwordBlock(MatrixStack matrixStack, Arm hand) {
        int direction = hand == Arm.RIGHT ? 1 : -1;
        matrixStack.translate(direction * -0.14142136F, 0.08F, 0.14142136F);
        matrixStack.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-102.25F));
        matrixStack.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(direction * 13.365F));
        matrixStack.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(direction * 78.05F));
    }

    @Unique
    private static void applyItemArmTransform(MatrixStack matrices, Arm arm, float equipProgress) {
        int direction = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate(direction * 0.56F, -0.52F + equipProgress * -0.6F, -0.72F);
    }
}