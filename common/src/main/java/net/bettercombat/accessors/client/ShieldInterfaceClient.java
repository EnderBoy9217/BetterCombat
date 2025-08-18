package net.bettercombat.accessors.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.ItemStack;

@Environment(EnvType.CLIENT)
public interface ShieldInterfaceClient {
    void displayShieldHealth(float shieldHealth, ItemStack stack);
}
