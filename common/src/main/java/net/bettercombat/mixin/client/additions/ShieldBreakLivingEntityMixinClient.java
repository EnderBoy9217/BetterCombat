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
        InGameHud hud = MinecraftClient.getInstance().inGameHud;
        HudInterface accessor = ((HudInterface)hud);
        accessor.setShouldDisplayShield(value);
        LivingEntity self = (LivingEntity)(Object)this;
        LivingEntityShieldInterface entityAccessor = (LivingEntityShieldInterface)self;
        entityAccessor.setShieldStatus(value);
    }

    /*
    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
    public void damageShield(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Item item = this.activeItemStack.getItem();
        if (item.getUseAction(this.activeItemStack) == UseAction.BLOCK && item instanceof ShieldItem) {
            // Shield Blocking
            float maxShieldHealth = BetterCombat.config.shield_max_health;

            float damageAmount = amount;
            if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
                damageAmount *= 0.2F;
            }

            float shieldHealth = ((ShieldInterface) item).getShieldHealth();
            shieldHealth -= damageAmount;
            ((ShieldInterfaceClient) item).setShieldHealthClient(shieldHealth);
            if (shieldHealth <= 0) {
                if (((LivingEntity) (Object) this) instanceof PlayerEntity player) {
                    ((ShieldInterfaceClient) item).setShieldHealthClient(maxShieldHealth);
                }
            }
        }
    }
     */
}
