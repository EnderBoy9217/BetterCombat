package net.bettercombat.mixin.parry;

import net.bettercombat.BetterCombat;
import net.bettercombat.accessors.HudInterface;
import net.bettercombat.accessors.SwordItemInterface;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.UseAction;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(SwordItem.class)
public class SwordItemMixin implements SwordItemInterface {

    @Unique
    private int parryCooldown = 0;

    @Unique
    private int parryTime = 0;

    @Unique
    private boolean isBlocking = false;

    @Unique
    public int getParryCooldown() {
        return parryCooldown;
    }

    @Unique
    public int getParryTime() {
        return parryTime;
    }

    @Unique
    public boolean getBlocking() {
        return isBlocking;
    }

    @Unique
    public void setParryCooldown(int cooldown) {
        parryCooldown = cooldown;
    }

    @Unique
    public void setParryTime(int time) {
        parryTime = time;
    }

    @Unique
    private void changeShieldDisplay(boolean full) {
        if (FabricLoader.getInstance().getEnvironmentType() != EnvType.CLIENT) {
            return;
        }
        InGameHud hud = MinecraftClient.getInstance().inGameHud;
        HudInterface accessor = ((HudInterface)hud);

        if (full) {
            accessor.setAmountHidden(0); // Full
        } else {
            accessor.setAmountHidden(16); //Empty
        }
    }

    @Unique
    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);
        if (this.parryCooldown == 0) {
            this.parryTime = BetterCombat.config.parry_timing;
            this.parryCooldown = parryTime + BetterCombat.config.parry_cooldown;
            changeShieldDisplay(true);
        }

        this.isBlocking = true;
        user.setCurrentHand(hand); // <-- triggers usage/blocking
        return TypedActionResult.consume(stack);
    }

    @Unique
    @Override
    public void onStoppedUsing(ItemStack stack, World world, LivingEntity user, int remainingUseTicks) {
        this.parryTime = 0;
        this.isBlocking = false;
    }


    @Unique
    @Override
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.BLOCK;
    }

    @Unique
    @Override
    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }

    @Unique
    @Override
    public void usageTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks) {
        if ( parryTime > 0 ) {
            parryTime--;
        } else {
            changeShieldDisplay(false);
        }
    }

    @Unique
    @Override
    public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean selected) {
        SwordItem self = ((SwordItem)(Object)this);
        if (parryCooldown > 0) {
            parryCooldown--;
        }
    }

}
