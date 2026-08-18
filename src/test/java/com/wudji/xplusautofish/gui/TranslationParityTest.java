package com.wudji.xplusautofish.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TranslationParityTest {
    @Test
    void enUsContainsEveryNativeConfigurationScreenKey() {
        JsonObject translations;
        try (InputStream stream = TranslationParityTest.class.getResourceAsStream(
                "/assets/autofish/lang/en_us.json")) {
            if (stream == null) {
                throw new AssertionError("en_us.json is not on the test classpath");
            }
            translations = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8))
                    .getAsJsonObject();
        } catch (Exception exception) {
            throw new AssertionError("Unable to parse en_us.json", exception);
        }

        List<String> requiredKeys = List.of(
                "options.autofish.title",
                "options.autofish.basic.title",
                "options.autofish.advanced.title",
                "options.autofish.toggle.on",
                "options.autofish.toggle.off",
                "options.autofish.enable.title",
                "options.autofish.multirod.title",
                "options.autofish.open_water_detection.title",
                "options.autofish.break_protection.title",
                "options.autofish.persistent.title",
                "options.autofish.auto_turn_view.title",
                "options.autofish.turn_angle.title",
                "options.autofish.turn_duration.title",
                "options.autofish.sound.title",
                "options.autofish.multiplayer_compat.title",
                "options.autofish.recast_delay.title",
                "options.autofish.random_delay.title",
                "options.autofish.reel_in_delay.title",
                "options.autofish.clear_regex.title",
                "options.autofish.done",
                "options.autofish.cancel",
                "options.autofish.reset");

        for (String key : requiredKeys) {
            assertTrue(translations.has(key), "Missing translation key: " + key);
        }
    }
}
