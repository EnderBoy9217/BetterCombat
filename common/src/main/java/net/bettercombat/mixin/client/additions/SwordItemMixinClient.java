package net.bettercombat.mixin.client.additions;

import net.bettercombat.accessors.HudInterface;
import net.bettercombat.accessors.client.SwordItemInterfaceClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SwordItem.class)
public class SwordItemMixinClient implements SwordItemInterfaceClient {


    public void changeShieldDisplay(boolean full) {
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
}
