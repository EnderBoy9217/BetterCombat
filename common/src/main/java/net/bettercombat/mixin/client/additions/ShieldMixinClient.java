package net.bettercombat.mixin.client.additions;

import net.bettercombat.accessors.HudInterface;
import net.bettercombat.accessors.ShieldInterface;
import net.bettercombat.accessors.client.ShieldInterfaceClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShieldItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ShieldItem.class)
public class ShieldMixinClient implements ShieldInterfaceClient {

    public void displayShieldHealth(float shieldHealth, ItemStack stack) {
        ShieldInterface shieldAccessor = (ShieldInterface) ((ShieldItem)(Object)this);
        InGameHud hud = MinecraftClient.getInstance().inGameHud;
        HudInterface accessor = ((HudInterface)hud);
        float percentage = 16 * (shieldHealth / shieldAccessor.getMaxShieldHealth(stack));

        accessor.setAmountHidden(16-(int)percentage);
    }
}
