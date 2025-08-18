package net.bettercombat.mixin.additions;

import net.bettercombat.BetterCombat;
import net.bettercombat.accessors.LivingEntityShieldInterface;
import net.bettercombat.accessors.ShieldInterface;
import net.bettercombat.accessors.SwordItemInterface;
import net.bettercombat.network.Packets;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.UseAction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class ShieldBreakLivingEntityMixin implements LivingEntityShieldInterface {

    @Shadow
    protected ItemStack activeItemStack;

    @Shadow
    protected int itemUseTimeLeft;

    @Shadow
    public abstract boolean isUsingItem();

    @Unique
    private boolean shieldStatus = false;

    public boolean getShieldStatus() {
        return shieldStatus;
    }

    public void setShieldStatus(boolean value) {
        this.shieldStatus = value;
    }

    /**
     * @author EnderBoy9217
     * @reason Axes should now damage shields, not simply disable them.
     */
    @Overwrite
    public boolean disablesShield() { return false; }

    @ModifyVariable(
            method = "damage",
            at = @At("HEAD"),
            argsOnly = true
    )
    private float swordBlocking(float amount, DamageSource source) {
        Item item = this.activeItemStack.getItem();
        if (item.getUseAction(this.activeItemStack) == UseAction.BLOCK && item instanceof SwordItem
            && !(source.isIn(DamageTypeTags.IS_PROJECTILE)
            || source.isIn(DamageTypeTags.BYPASSES_ARMOR)
            || source.isIn(DamageTypeTags.IS_EXPLOSION))) {
            return amount * 0.75F;
        } else {
            return amount;
        }
    }

    @ModifyVariable(
            method = "takeKnockback",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0 //(Strength)
    )
    private double swordBlocksKnockback(double strength, double x, double z) {
        Item item = this.activeItemStack.getItem();
        if (item.getUseAction(this.activeItemStack) == UseAction.BLOCK && item instanceof SwordItem) {
            return strength * 0.75D;
        } else {
            return strength;
        }
    }


    @Inject(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;damageShield(F)V"))
    public void damageShield(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Item item = this.activeItemStack.getItem();
        ItemStack stack = this.activeItemStack;
        if (item.getUseAction(this.activeItemStack) == UseAction.BLOCK) {
            if (item instanceof ShieldItem) {
                // Shield Blocking

                float maxShieldHealth = BetterCombat.config.shield_max_health + (BetterCombat.config.unbreaking_bonus * EnchantmentHelper.getLevel(Enchantments.UNBREAKING, this.activeItemStack));

                float damageAmount = amount;
                if (source.isIn(DamageTypeTags.IS_PROJECTILE)) {
                    damageAmount *= 0.2F;
                }

                float shieldHealth = ((ShieldInterface) item).getShieldHealth(stack);
                shieldHealth -= damageAmount;
                ((ShieldInterface) item).setShieldHealth(shieldHealth, stack);
                if (((LivingEntity) (Object) this) instanceof ServerPlayerEntity player) {
                    Packets.ShieldHealthUpdate packet = new Packets.ShieldHealthUpdate(shieldHealth, stack);
                    ServerPlayNetworking.send(player, Packets.ShieldHealthUpdate.ID, packet.write());
                }
                if (shieldHealth <= 0) {
                    if (((LivingEntity) (Object) this) instanceof PlayerEntity player) {
                        player.disableShield(true);
                        ((ShieldInterface) item).setShieldHealth(maxShieldHealth,stack);
                        if (((LivingEntity) (Object) this) instanceof ServerPlayerEntity serverPlayer) {
                            Packets.ShieldHealthUpdate packet = new Packets.ShieldHealthUpdate(maxShieldHealth, stack);
                            ServerPlayNetworking.send(serverPlayer, Packets.ShieldHealthUpdate.ID, packet.write());
                        }
                    }
                }
            }
        }
    }

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    public void isBlocking(CallbackInfoReturnable<Boolean> cir) {
        if (this.isUsingItem() && !this.activeItemStack.isEmpty()) {
            Item item = this.activeItemStack.getItem();
            if (item.getUseAction(this.activeItemStack) != UseAction.BLOCK || item instanceof SwordItem) {
                cir.setReturnValue(false);
            } else {
                cir.setReturnValue(item.getMaxUseTime(this.activeItemStack) - this.itemUseTimeLeft >= 5);
            }
        } else {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    public void parryAttackCancel(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir){
        LivingEntity entity = (LivingEntity) (Object) this;

        ItemStack mainHandStack = entity.getMainHandStack();
        ItemStack offHandStack = entity.getOffHandStack();
        Item item = null;

        if (mainHandStack.getItem() instanceof SwordItem) {
            item = mainHandStack.getItem();
        } else if (offHandStack.getItem() instanceof SwordItem) {
            item = offHandStack.getItem();
        }

        if (item instanceof SwordItem sword) {
            SwordItemInterface accessor = (SwordItemInterface) sword;
            //System.out.print("Checking for parry, parry time remaining:");
            //System.out.println(accessor.getParryTime());
            //System.out.print("Is blocking:");
            //System.out.println(accessor.getBlocking());
            //System.out.print("Bypasses Shield:");
            //System.out.println(source.isIn(DamageTypeTags.BYPASSES_SHIELD) || source.isIn(DamageTypeTags.IS_EXPLOSION));
            if (accessor.getBlocking() && accessor.getParryTime() > 0 && !(source.isIn(DamageTypeTags.BYPASSES_SHIELD) || source.isIn(DamageTypeTags.IS_EXPLOSION)) ) {

                entity.getWorld().playSound(
                        null, // Player (null to play for all nearby players)
                        entity.getX(), entity.getY(), entity.getZ(), // Position
                        SoundEvent.of(Identifier.of("bettercombat", "block")), // Your custom sound event
                        SoundCategory.PLAYERS, // Sound category
                        1.0F, // Volume
                        (float) (Math.random() * 0.4) + 0.7F  // Pitch
                );

                if (source.getAttacker() instanceof LivingEntity attacker) {
                    double knockbackStrength = BetterCombat.config.shield_knockback;
                    double dx = attacker.getX() - entity.getX();
                    double dz = attacker.getZ() - entity.getZ();
                    double distance = Math.sqrt(dx * dx + dz * dz);
                    if (distance > 0.0) {
                        dx /= distance;
                        dz /= distance;
                        attacker.addVelocity(dx * knockbackStrength, 0.2, dz * knockbackStrength);
                        attacker.velocityModified = true;
                    }
                }

                cir.cancel(); // Remove Damage
            }
        }
    }

    @ModifyVariable(
            method = "damage",
            at = @At("HEAD"),
            index = 2,
            argsOnly = true
    )
    private float modifyIncomingDamage(float amount, DamageSource source) {
        LivingEntity entity = (LivingEntity) (Object) this;

        // Check both main hand and offhand for a sword
        ItemStack mainHandStack = entity.getMainHandStack();
        ItemStack offHandStack = entity.getOffHandStack();
        Item item = null;

        if (mainHandStack.getItem() instanceof SwordItem) {
            item = mainHandStack.getItem();
        } else if (offHandStack.getItem() instanceof SwordItem) {
            item = offHandStack.getItem();
        }

        if (item instanceof SwordItem sword) {
            SwordItemInterface accessor = (SwordItemInterface) sword;
            if ( accessor.getBlocking() && !(source.isIn(DamageTypeTags.BYPASSES_SHIELD) || source.isIn(DamageTypeTags.IS_EXPLOSION) && !source.isIn(DamageTypeTags.IS_PROJECTILE)) ) {
                // Can't block projectiles unlike parries
                amount *= 0.5F;
            }
        }

        return amount;
    }

    @Inject(method = "takeKnockback", at = @At("HEAD"), cancellable = true)
    public void blockKnockback(double strength, double x, double z, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        ItemStack mainHandStack = entity.getMainHandStack();
        ItemStack offHandStack = entity.getOffHandStack();
        Item item = null;

        if (mainHandStack.getItem() instanceof SwordItem) {
            item = mainHandStack.getItem();
        } else if (offHandStack.getItem() instanceof SwordItem) {
            item = offHandStack.getItem();
        }

        if (item instanceof SwordItem sword) {
            SwordItemInterface accessor = (SwordItemInterface) sword;
            if (accessor.getBlocking()) {
                ci.cancel(); // Remove Knockback
            }
        }
    }


    @Inject(method = "swingHand", at = @At("HEAD"), cancellable = true)
    public void onSwingHand(Hand hand, CallbackInfo ci) {
        if ( ((LivingEntity)(Object)this) instanceof PlayerEntity player ) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof SwordItem && player.isUsingItem() && stack.getUseAction() == UseAction.BLOCK) {
                ci.cancel(); // cancel swing animation
            }
        }
    }
}
