package net.bettercombat.accessors.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public interface SwordItemInterfaceClient {
    void changeShieldDisplay(boolean full);
}