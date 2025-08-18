package net.bettercombat.mixin.client.additions;

import net.bettercombat.BetterCombat;
import net.bettercombat.accessors.HudInterface;
import net.bettercombat.accessors.LivingEntityShieldInterface;
import net.bettercombat.accessors.ShieldInterface;
import net.bettercombat.accessors.client.ShieldInterfaceClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
@Environment(EnvType.CLIENT)
public class ShieldBreakLivingEntityMixinClient {

    @Shadow
    protected ItemStack activeItemStack;

    @Inject(method = "tick", at = @At("TAIL"))
    public void checkForIcon(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        LivingEntityShieldInterface accessor = (LivingEntityShieldInterface)self;
        boolean newStatus = self.isBlocking();
        boolean shieldStatus = accessor.getShieldStatus();
        if ( shieldStatus != newStatus ) {
            displayShield(newStatus);
        }
    }

    @Unique
    private void displayShield(boolean value) {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            return;
        }

        LivingEntity self = (LivingEntity)(Object)this;
        if (self == MinecraftClient.getInstance().player) {
            InGameHud hud = MinecraftClient.getInstance().inGameHud;
            HudInterface accessor = ((HudInterface)hud);
            accessor.setShouldDisplayShield(value);
        }
        LivingEntityShieldInterface entityAccessor = (LivingEntityShieldInterface)self;
        entityAccessor.setShieldStatus(value);
    }
}
