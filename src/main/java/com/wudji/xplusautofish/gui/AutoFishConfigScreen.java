package com.wudji.xplusautofish.gui;

import com.wudji.xplusautofish.ForgeModXPlusAutofish;
import com.wudji.xplusautofish.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.LongConsumer;
import java.util.function.LongSupplier;

/** Native Minecraft configuration screen for all XPlus Autofish settings. */
public final class AutoFishConfigScreen extends Screen {
    private static final int CONTROL_WIDTH = 150;
    private static final int CONTROL_HEIGHT = 20;
    static final int ACTION_BUTTON_WIDTH = 90;
    private static final int ACTION_BUTTON_GAP = 10;

    private final ForgeModXPlusAutofish modAutofish;
    private final Screen parentScreen;
    private final ConfigDraft draft;
    private final List<Runnable> refreshers = new ArrayList<>();
    private ConfigList options;

    private AutoFishConfigScreen(ForgeModXPlusAutofish modAutofish, Screen parentScreen) {
        super(Component.translatable("options.autofish.title"));
        this.modAutofish = modAutofish;
        this.parentScreen = parentScreen;
        this.draft = new ConfigDraft(modAutofish.getConfig());
    }

    public static Screen buildScreen(ForgeModXPlusAutofish modAutofish, Screen parentScreen) {
        return new AutoFishConfigScreen(modAutofish, parentScreen);
    }

    @Override
    protected void init() {
        refreshers.clear();
        int listTop = 32;
        int listBottom = Math.max(listTop + 32, height - 38);
        options = addRenderableWidget(new ConfigList(minecraft, width, listTop, listBottom));

        addBoolean("enable", "options.autofish.enable.title", 1,
                draft.values()::isAutofishEnabled, draft.values()::setAutofishEnabled, true);
        addBoolean("multirod", "options.autofish.multirod.title", 3,
                draft.values()::isMultiRod, draft.values()::setMultiRod, true);
        addBoolean("open_water_detection", "options.autofish.open_water_detection.title", 3,
                draft.values()::isOpenWaterDetectEnabled, draft.values()::setOpenWaterDetectEnabled, true);
        addBoolean("break_protection", "options.autofish.break_protection.title", 2,
                draft.values()::isNoBreak, draft.values()::setNoBreak, true);
        addBoolean("persistent", "options.autofish.persistent.title", 6,
                draft.values()::isPersistentMode, draft.values()::setPersistentMode, true);
        addBoolean("auto_turn_view", "options.autofish.auto_turn_view.title", 2,
                draft.values()::isAutoTurnView, draft.values()::setAutoTurnView, true);
        addEditBox("turn_angle", "options.autofish.turn_angle.title", 2,
                Float.toString(draft.values().getTurnAngle()), value -> {
                    try {
                        float angle = Float.parseFloat(value);
                        if (Float.isFinite(angle)) {
                            draft.values().setTurnAngle(angle);
                        }
                    } catch (NumberFormatException ignored) {
                        // Keep the last valid draft value while the user edits the field.
                    }
                }, true);
        addSlider("turn_duration", "options.autofish.turn_duration.title", 2,
                100, 5000, draft.values()::getTurnDuration,
                value -> draft.values().setTurnDuration((int) value), true);

        addBoolean("sound", "options.autofish.sound.title", 10,
                draft.values()::isUseSoundDetection, draft.values()::setUseSoundDetection, false);
        addBoolean("multiplayer_compat", "options.autofish.multiplayer_compat.title", 3,
                draft.values()::isForceMPDetection, draft.values()::setForceMPDetection, false);
        addSlider("recast_delay", "options.autofish.recast_delay.title", 2,
                500, 5000, draft.values()::getRecastDelay, draft.values()::setRecastDelay, false);
        addSlider("random_delay", "options.autofish.random_delay.title", 4,
                0, 75, draft.values()::getRandomPercent, draft.values()::setRandomPercent, false);
        addSlider("reel_in_delay", "options.autofish.reel_in_delay.title", 2,
                1, 2000, draft.values()::getReelInDelay, draft.values()::setReelInDelay, false);
        addEditBox("clear_regex", "options.autofish.clear_regex.title", 3,
                draft.values().getClearLagRegex(), draft::trySetClearLagRegex, false);

        int buttonWidth = ACTION_BUTTON_WIDTH;
        int buttonY = height - 28;
        int[] buttonX = actionButtonX(width);
        addRenderableWidget(Button.builder(Component.translatable("options.autofish.done"), button -> done())
                .bounds(buttonX[0], buttonY, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("options.autofish.cancel"), button -> cancel())
                .bounds(buttonX[1], buttonY, buttonWidth, 20).build());
        addRenderableWidget(Button.builder(Component.translatable("options.autofish.reset"), button -> reset())
                .bounds(buttonX[2], buttonY, buttonWidth, 20).build());
    }

    static int[] actionButtonX(int screenWidth) {
        int groupWidth = ACTION_BUTTON_WIDTH * 3 + ACTION_BUTTON_GAP * 2;
        int left = (screenWidth - groupWidth) / 2;
        return new int[]{left, left + ACTION_BUTTON_WIDTH + ACTION_BUTTON_GAP,
                left + (ACTION_BUTTON_WIDTH + ACTION_BUTTON_GAP) * 2};
    }

    private void addBoolean(String field, String titleKey, int tooltipCount, BooleanSupplier getter,
                            Consumer<Boolean> setter, boolean basic) {
        CycleButton<Boolean> button = CycleButton.booleanBuilder(
                        Component.translatable("options.autofish.toggle.on"),
                        Component.translatable("options.autofish.toggle.off"))
                .withInitialValue(getter.getAsBoolean())
                .create(Component.translatable(titleKey), (ignored, value) -> setter.accept(value));
        button.setWidth(CONTROL_WIDTH);
        addRow(titleKey, tooltipCount, button, basic);
        refreshers.add(() -> button.setValue(getter.getAsBoolean()));
    }

    private void addSlider(String field, String titleKey, int tooltipCount, long minimum, long maximum,
                           LongSupplier getter, LongConsumer setter, boolean basic) {
        ConfigSlider slider = new ConfigSlider(titleKey, minimum, maximum, getter.getAsLong(), setter);
        addRow(titleKey, tooltipCount, slider, basic);
        refreshers.add(() -> slider.setActual(getter.getAsLong()));
    }

    private void addEditBox(String field, String titleKey, int tooltipCount, String initial,
                            Consumer<String> setter, boolean basic) {
        EditBox editBox = new EditBox(font, 0, 0, CONTROL_WIDTH, CONTROL_HEIGHT,
                Component.translatable(titleKey));
        editBox.setValue(initial == null ? "" : initial);
        editBox.setResponder(setter);
        addRow(titleKey, tooltipCount, editBox, basic);
        refreshers.add(() -> editBox.setValue(field.equals("turn_angle")
                ? Float.toString(draft.values().getTurnAngle())
                : draft.values().getClearLagRegex()));
    }

    private void addRow(String titleKey, int tooltipCount, AbstractWidget control, boolean basic) {
        Component section = basic ? options.basicSection() : options.advancedSection();
        options.addRow(new OptionRow(font, section, Component.translatable(titleKey), control,
                tooltip(titleKey, tooltipCount)));
    }

    private List<Component> tooltip(String titleKey, int count) {
        String prefix = titleKey.substring(0, titleKey.lastIndexOf(".title"));
        List<Component> lines = new ArrayList<>();
        for (int index = 0; index < count; index++) {
            lines.add(Component.translatable(prefix + ".tooltip" + (count == 1 ? "" : "_" + index)));
        }
        return lines;
    }

    private void reset() {
        draft.reset();
        refreshers.forEach(Runnable::run);
    }

    private void cancel() {
        Minecraft.getInstance().setScreen(parentScreen);
    }

    private void done() {
        Config live = modAutofish.getConfig();
        boolean detectionChanged = draft.values().isUseSoundDetection() != live.isUseSoundDetection();
        draft.applyTo(live);
        if (detectionChanged) {
            modAutofish.getAutofish().setDetection();
        }
        modAutofish.getConfigManager().writeConfigAsync();
        Minecraft.getInstance().setScreen(parentScreen);
    }

    @Override
    public void onClose() {
        cancel();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFFFF);
        super.render(graphics, mouseX, mouseY, delta);
    }

    private final class ConfigList extends ContainerObjectSelectionList<OptionRow> {
        private boolean basicUsed;
        private boolean advancedUsed;

        private ConfigList(Minecraft minecraft, int width, int top, int bottom) {
            super(minecraft, width - 24, top, bottom, 32);
            setX(12);
        }

        private Component basicSection() {
            if (!basicUsed) {
                basicUsed = true;
                return Component.translatable("options.autofish.basic.title");
            }
            return null;
        }

        private Component advancedSection() {
            if (!advancedUsed) {
                advancedUsed = true;
                return Component.translatable("options.autofish.advanced.title");
            }
            return null;
        }

        private void addRow(OptionRow row) {
            addEntry(row);
        }
    }

    private static final class ConfigSlider extends AbstractSliderButton {
        private final String titleKey;
        private final long minimum;
        private final long maximum;
        private final LongConsumer setter;

        private ConfigSlider(String titleKey, long minimum, long maximum, long actual, LongConsumer setter) {
            super(0, 0, CONTROL_WIDTH, CONTROL_HEIGHT, Component.empty(), normalize(actual, minimum, maximum));
            this.titleKey = titleKey;
            this.minimum = minimum;
            this.maximum = maximum;
            this.setter = setter;
            updateMessage();
        }

        private static double normalize(long value, long minimum, long maximum) {
            return (double) (Math.max(minimum, Math.min(maximum, value)) - minimum) / (maximum - minimum);
        }

        private long actual() {
            return Math.round(minimum + value * (maximum - minimum));
        }

        private void setActual(long actual) {
            value = normalize(actual, minimum, maximum);
            updateMessage();
        }

        @Override
        protected void updateMessage() {
            setMessage(CommonComponents.optionNameValue(
                    Component.translatable(titleKey),
                    Component.translatable(titleKey.replace(".title", ".value"), actual())));
        }

        @Override
        protected void applyValue() {
            setter.accept(actual());
            updateMessage();
        }
    }
}
