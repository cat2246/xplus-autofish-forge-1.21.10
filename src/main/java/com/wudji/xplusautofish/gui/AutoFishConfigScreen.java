package com.wudji.xplusautofish.gui;

import com.wudji.xplusautofish.ForgeModXPlusAutofish;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Temporary native screen; Task 6 supplies the complete settings UI. */
public final class AutoFishConfigScreen extends Screen {
    private final Screen parentScreen;

    private AutoFishConfigScreen(Screen parentScreen) {
        super(Component.translatable("options.autofish.title"));
        this.parentScreen = parentScreen;
    }

    public static Screen buildScreen(ForgeModXPlusAutofish modAutofish, Screen parentScreen) {
        return new AutoFishConfigScreen(parentScreen);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(parentScreen);
    }
}
