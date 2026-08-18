package com.wudji.xplusautofish.mixin;

import com.wudji.xplusautofish.ForgeModXPlusAutofish;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.minecraft.network.TickablePacketListener;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientPlayNetworkHandler extends ClientCommonPacketListenerImpl implements TickablePacketListener, ClientGamePacketListener {
    protected MixinClientPlayNetworkHandler(Minecraft minecraft, Connection connection, CommonListenerCookie connectionState) {
        super(minecraft, connection, connectionState);
    }

    @Inject(method = "handleSoundEvent", at = @At("HEAD"))
    public void onPlaySound(ClientboundSoundPacket playSoundS2CPacket_1, CallbackInfo ci) {
        ForgeModXPlusAutofish mod = ForgeModXPlusAutofish.getInstance();
        if (mod == null || mod.getAutofish() == null || !minecraft.isSameThread()) {
            return;
        }
        mod.handlePacket(playSoundS2CPacket_1);
    }

    @Inject(method = "handleSetEntityMotion", at = @At("HEAD"))
    public void onVelocityUpdate(ClientboundSetEntityMotionPacket entityVelocityUpdateS2CPacket_1, CallbackInfo ci) {
        ForgeModXPlusAutofish mod = ForgeModXPlusAutofish.getInstance();
        if (mod == null || mod.getAutofish() == null || !minecraft.isSameThread()) {
            return;
        }
        mod.handlePacket(entityVelocityUpdateS2CPacket_1);
    }

    @Inject(method = "handleSystemChat", at = @At("HEAD"))
    public void onSysChatMessage(ClientboundSystemChatPacket p_233708_, CallbackInfo ci) {
        ForgeModXPlusAutofish mod = ForgeModXPlusAutofish.getInstance();
        if (mod == null || mod.getAutofish() == null || !minecraft.isSameThread()) {
            return;
        }
        mod.handleChat(p_233708_);
    }
}
