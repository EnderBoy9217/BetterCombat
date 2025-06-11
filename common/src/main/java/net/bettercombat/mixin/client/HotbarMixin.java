package net.bettercombat.mixin.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.bettercombat.accessors.HudInterface;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public abstract class HotbarMixin implements HudInterface {

    @Shadow
    private int scaledWidth;

    @Shadow
    private int scaledHeight;

    @Unique
    private boolean shouldDisplayShield = false;

    @Unique
    public boolean getShouldDisplayShield() {
        return shouldDisplayShield;
    }

    @Unique
    public void setShouldDisplayShield(boolean display) {
        shouldDisplayShield = display;
    }

    @Unique
    private int amountHidden = 0;

    @Unique
    public int getAmountHidden() {
        return amountHidden;
    }

    @Unique
    public void setAmountHidden(int amount) {
        amountHidden = amount;
    }

    @Inject(method = "renderCrosshair", at = @At("HEAD"))
    private void renderCustomCrosshair(DrawContext context, CallbackInfo ci) {
        if (shouldDisplayShield) {
            // Use vanilla-style crosshair blending
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SrcFactor.ONE_MINUS_DST_COLOR,
                    GlStateManager.DstFactor.ONE_MINUS_SRC_COLOR,
                    GlStateManager.SrcFactor.ONE,
                    GlStateManager.DstFactor.ZERO
            );

            Identifier broken_texture = new Identifier("bettercombat", "textures/broken_shield.png");
            int x = this.scaledWidth / 2 - 8;
            int y = this.scaledHeight / 2 - 7 + 16;
            context.drawTexture(broken_texture, x, y, 0, 0, 16, 16, 16, 16); // x, y, u (texture-x), v (texture-y), width, height, textureWidth, textureHeight

            Identifier full_texture = new Identifier("bettercombat", "textures/full_shield.png");
            context.drawTexture(full_texture, x, y + amountHidden, 0, amountHidden, 16, 16-amountHidden, 16, 16);

            RenderSystem.defaultBlendFunc(); // Reset blending to default afterward

            //ci.cancel(); // Skip default rendering
        }
    }
}