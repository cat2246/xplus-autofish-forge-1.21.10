package com.wudji.xplusautofish;

import com.wudji.xplusautofish.config.Config;
import com.wudji.xplusautofish.config.ConfigManager;
import com.wudji.xplusautofish.gui.AutoFishConfigScreen;
import com.wudji.xplusautofish.input.KeyPressLatch;
import com.wudji.xplusautofish.scheduler.AutofishScheduler;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.lwjgl.glfw.GLFW;

@Mod(ForgeModXPlusAutofish.MODID)
public class ForgeModXPlusAutofish {
    public static final String MODID = "autofish";
    public static final KeyMapping.Category XPLUS_CATEGORY =
            new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath(MODID, "category"));
    public static final KeyMapping CONFIG_SCREEN_MAPPING =
            new KeyMapping("key.autofish.open_gui", GLFW.GLFW_KEY_V, XPLUS_CATEGORY);

    private static ForgeModXPlusAutofish instance;
    private final KeyPressLatch configScreenLatch = new KeyPressLatch();
    private XPlusAutofish autofish;
    private AutofishScheduler scheduler;
    private ConfigManager configManager;

    public ForgeModXPlusAutofish(FMLJavaModLoadingContext context) {
        var modBusGroup = context.getModBusGroup();
        FMLClientSetupEvent.getBus(modBusGroup).addListener(this::clientSetup);
        RegisterKeyMappingsEvent.BUS.addListener(this::registerBindings);
        TickEvent.ClientTickEvent.Post.BUS.addListener(this::tick);
    }

    private void clientSetup(FMLClientSetupEvent event) {
        instance = this;
        configManager = new ConfigManager(FMLPaths.CONFIGDIR.get());
        scheduler = new AutofishScheduler(this);
        autofish = new XPlusAutofish(this);
    }

    private void registerBindings(RegisterKeyMappingsEvent event) {
        event.register(CONFIG_SCREEN_MAPPING);
    }

    private void tick(TickEvent.ClientTickEvent.Post event) {
        if (autofish == null) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        if (configScreenLatch.update(CONFIG_SCREEN_MAPPING.isDown())) {
            client.setScreen(AutoFishConfigScreen.buildScreen(this, client.screen));
        }
        autofish.tick(client);
        scheduler.tick(client);
    }

    public void handlePacket(Packet<?> packet) {
        autofish.handlePacket(packet);
    }

    public void handleChat(ClientboundSystemChatPacket packet) {
        autofish.handleChat(packet);
    }

    public void tickFishingLogic(Entity owner, int ticksCatchable) {
        autofish.tickFishingLogic(owner, ticksCatchable);
    }

    public static ForgeModXPlusAutofish getInstance() {
        return instance;
    }

    public XPlusAutofish getAutofish() {
        return autofish;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public Config getConfig() {
        return configManager.getConfig();
    }

    public AutofishScheduler getScheduler() {
        return scheduler;
    }
}
