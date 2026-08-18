package com.wudji.xplusautofish.mointor;

import com.wudji.xplusautofish.XPlusAutofish;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.projectile.FishingHook;

public interface FishMonitorMP {
    void hookTick(XPlusAutofish autofish, Minecraft minecraft, FishingHook hook);

    void handleHookRemoved();

    void handlePacket(XPlusAutofish autofish, Packet<?> packet, Minecraft minecraft);
}
