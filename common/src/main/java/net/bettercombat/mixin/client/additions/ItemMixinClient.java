package net.bettercombat.mixin.client.additions;

import net.bettercombat.BetterCombat;
import net.bettercombat.accessors.ShieldInterface;
import net.bettercombat.accessors.SwordItemInterface;
import net.bettercombat.accessors.client.ShieldInterfaceClient;
import net.bettercombat.accessors.client.SwordItemInterfaceClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
@Environment(EnvType.CLIENT)
public class ItemMixinClient {

    @Inject(method = "usageTick", at = @At("HEAD"))
    public void showShield(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        if ( (Item)(Object)this instanceof SwordItem sword ) {
            SwordItemInterface accessor = (SwordItemInterface) sword;
            SwordItemInterfaceClient clientAccessor = (SwordItemInterfaceClient) sword;
            clientAccessor.changeShieldDisplay(accessor.getShouldShowShield());
        }
    }

    /*
    @Inject(method = "use", at = @At("HEAD"))
    public void use(World world, PlayerEntity user, Hand hand, CallbackInfoReturnable<TypedActionResult<ItemStack>> cir) {
        if ( (Item)(Object)this instanceof SwordItem sword ) {
            SwordItemInterface accessor = (SwordItemInterface)sword;
            SwordItemInterfaceClient clientAccessor = (SwordItemInterfaceClient)sword;
            if (accessor.getParryCooldown() == 0) {
                clientAccessor.changeShieldDisplay(true);
            }
        }
    }*/

    @Inject(method = "usageTick", at = @At("HEAD"))
    public void inventoryTick(World world, LivingEntity user, ItemStack stack, int remainingUseTicks, CallbackInfo ci) {
        if ((Item) (Object) this instanceof SwordItem sword) {
            SwordItemInterface accessor = (SwordItemInterface) sword;
            SwordItemInterfaceClient clientAccessor = (SwordItemInterfaceClient)sword;

            int parryTime = accessor.getParryTime();
            if (parryTime == 0) {
                clientAccessor.changeShieldDisplay(false);
            }
        }  else if ((Item)(Object)this instanceof ShieldItem shield ) {
            ShieldInterface accessor = (ShieldInterface)shield;
            ShieldInterfaceClient clientAccessor = (ShieldInterfaceClient)shield;
            //float currentMaxShieldHealth = accessor.getMaxShieldHealth();
            float shieldHealth = accessor.getShieldHealth();
            clientAccessor.setShieldHealthClient(shieldHealth);
        }
    }

}
