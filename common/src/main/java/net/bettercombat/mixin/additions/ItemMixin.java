package net.bettercombat.mixin.additions;

import net.bettercombat.BetterCombat;
import net.bettercombat.accessors.HudInterface;
import net.bettercombat.accessors.ShieldInterface;
import net.bettercombat.accessors.SwordItemInterface;
import net.bettercombat.accessors.client.SwordItemInterfaceClient;
import net.bettercombat.network.Packets;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public class ItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if ( (Item)(Object)this instanceof SwordItem sword ) {
            SwordItemInterface accessor = (SwordItemInterface)sword;
            ItemStack stack = user.getStackInHand(hand);
            //System.out.println(accessor.getParryCooldown());
            if (accessor.getParryCooldown() == 0 && user == MinecraftClient.getInstance().player) {
                accessor.setShouldShowShield(true);
                Packets.C2S_BlockRequest packet = new Packets.C2S_BlockRequest(true,hand);
                ClientPlayNetworking.send(Packets.C2S_BlockRequest.ID, packet.write());
            }

            user.setCurrentHand(hand); // <-- triggers usage/blocking
            cir.setReturnValue( TypedActionResult.consume(stack) );
        }
    }

    @Inject(method = "onStoppedUsing", at = @At("HEAD"))
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks, CallbackInfo ci) {
        if ( (Item)(Object)this instanceof SwordItem sword ) {
            SwordItemInterface accessor = (SwordItemInterface) sword;
            accessor.setParryTime(0);
            accessor.setShouldShowShield(false);
            if (user instanceof ClientPlayerEntity player) {
                if (stack == user.getStackInHand(Hand.MAIN_HAND)) {
                    Packets.C2S_BlockRequest packet = new Packets.C2S_BlockRequest(false,Hand.MAIN_HAND);
                    ClientPlayNetworking.send(Packets.C2S_BlockRequest.ID, packet.write());
                } else if (stack == user.getStackInHand(Hand.OFF_HAND)) {
                    Packets.C2S_BlockRequest packet = new Packets.C2S_BlockRequest(false,Hand.OFF_HAND);
                    ClientPlayNetworking.send(Packets.C2S_BlockRequest.ID, packet.write());
                }  // If not in any hand doesn't send packet
            }
        }
    }


    @Inject(method = "getUseAction", at = @At("HEAD"), cancellable = true)
    public void getUseAction(ItemStack stack, CallbackInfoReturnable<UseAction> cir) {
        if ( (Item)(Object)this instanceof SwordItem sword ) {
            cir.setReturnValue(UseAction.BLOCK);
        }
    }


    @Inject(method = "getMaxUseTime", at = @At("HEAD"), cancellable = true)
    public void getMaxUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if ( (Item)(Object)this instanceof SwordItem sword ) {
            cir.setReturnValue(72000);
        }
    }

    @Inject(method = "inventoryTick", at = @At("HEAD"))
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected, CallbackInfo ci) {
        if ( (Item)(Object)this instanceof SwordItem sword ) {
            if (entity instanceof ServerPlayerEntity player) {
                SwordItemInterface accessor = (SwordItemInterface) sword;
                int parryCooldown = accessor.getParryCooldown();
                if (parryCooldown > 0) {
                    accessor.setParryCooldown(parryCooldown - 1);
                }
                int parryTime = accessor.getParryTime();
                if (parryTime > 0) {
                    accessor.setParryTime(parryTime - 1);
                    if ( (parryTime-1) == 0 ) {
                        Item main = player.getMainHandStack().getItem();
                        Item offhand = player.getOffHandStack().getItem();
                        // Prevent from overriding shield
                        if (!(main instanceof ShieldItem || offhand instanceof ShieldItem) && (sword == main || sword == offhand) )
                        {
                            Packets.ShieldHealthUpdate packet = new Packets.ShieldHealthUpdate(0.0F, stack);
                            ServerPlayNetworking.send(player, Packets.ShieldHealthUpdate.ID, packet.write());
                        }
                    }
                }
            } else {
                // Client Sword Item
                if (entity instanceof ClientPlayerEntity player && player == MinecraftClient.getInstance().player) {
                    Item main = player.getMainHandStack().getItem();
                    Item offhand = player.getOffHandStack().getItem();
                    // Prevent from overriding shield
                    if (!(main instanceof ShieldItem || offhand instanceof ShieldItem) && (sword == main || sword == offhand) ) {
                        SwordItemInterface accessor = (SwordItemInterface) sword;
                        InGameHud hud = MinecraftClient.getInstance().inGameHud;
                        HudInterface hudaccessor = ((HudInterface) hud);
                        hudaccessor.setShouldDisplayShield(accessor.getShouldShowShield());
                    }
                }
            }
        } else if ((Item)(Object)this instanceof ShieldItem shield ) {
            ShieldInterface accessor = (ShieldInterface)shield;
            float currentMaxShieldHealth = accessor.getMaxShieldHealth(stack);
            float shieldHealth = accessor.getShieldHealth(stack);
            if ( shieldHealth != currentMaxShieldHealth ) {
                int shieldRegenTime = accessor.getShieldRegenTime(stack);
                if (shieldRegenTime >= BetterCombat.config.shield_regen_time) {
                    float newShieldHealth = Math.min(currentMaxShieldHealth, shieldHealth+0.25F);
                    if ( newShieldHealth != accessor.getShieldHealth(stack) ) {
                        accessor.setShieldHealth(newShieldHealth,stack);
                        if (entity instanceof ServerPlayerEntity player && stack == player.getActiveItem()) {
                            Packets.ShieldHealthUpdate packet = new Packets.ShieldHealthUpdate(newShieldHealth, stack);
                            ServerPlayNetworking.send(player, Packets.ShieldHealthUpdate.ID, packet.write());
                        }
                    }

                }
                accessor.setShieldRegenTime(shieldRegenTime + 1, stack);
            }
        }
    }

}
