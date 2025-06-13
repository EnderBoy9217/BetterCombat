package net.bettercombat.accessors.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface ShieldInterfaceClient {
    void displayShieldHealth(float shieldHealth);
    void setShieldHealthClient(float shieldHealth);
}
