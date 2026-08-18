package com.wudji.xplusautofish.config;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConfigManagerTest {
    private static final String FILE_NAME = "autofish.config";

    @TempDir
    Path tempDir;

    @Test
    void createsDefaultConfigFileInProvidedDirectory() throws Exception {
        ConfigManager manager = new ConfigManager(tempDir);

        assertEquals(1500L, manager.getConfig().getRecastDelay());
        assertTrue(Files.exists(tempDir.resolve(FILE_NAME)));
    }

    @Test
    void roundTripsExistingJsonFieldNames() {
        ConfigManager manager = new ConfigManager(tempDir);
        manager.getConfig().setRecastDelay(2222);
        manager.writeConfig();

        ConfigManager reloaded = new ConfigManager(tempDir);

        assertEquals(2222L, reloaded.getConfig().getRecastDelay());
    }

    @Test
    void writesAsynchronously() {
        ConfigManager manager = new ConfigManager(tempDir);
        manager.getConfig().setRecastDelay(3333);

        manager.writeConfigAsync().join();

        assertEquals(3333L, new ConfigManager(tempDir).getConfig().getRecastDelay());
    }

    @Test
    void clampsValuesLoadedFromJsonAndRewritesFile() throws Exception {
        Files.writeString(tempDir.resolve(FILE_NAME),
                "{\"recastDelay\":50,\"randomPercent\":99}", StandardCharsets.UTF_8);

        ConfigManager manager = new ConfigManager(tempDir);

        assertEquals(500L, manager.getConfig().getRecastDelay());
        assertEquals(75L, manager.getConfig().getRandomPercent());
        assertDoesNotThrow(() -> JsonParser.parseString(
                Files.readString(tempDir.resolve(FILE_NAME), StandardCharsets.UTF_8)));
    }

    @Test
    void sanitizesInvalidLegacyRegexAndRewritesPersistedJson() throws Exception {
        Files.writeString(tempDir.resolve(FILE_NAME),
                "{\"clearLagRegex\":\"[\"}", StandardCharsets.UTF_8);

        ConfigManager manager = new ConfigManager(tempDir);

        assertEquals("", manager.getConfig().getClearLagRegex());
        assertEquals("", JsonParser.parseString(
                Files.readString(tempDir.resolve(FILE_NAME), StandardCharsets.UTF_8))
                .getAsJsonObject().get("clearLagRegex").getAsString());
    }

    @Test
    void partialJsonPreservesOmittedUpstreamDefaults() throws Exception {
        Files.writeString(tempDir.resolve(FILE_NAME), "{\"recastDelay\":2222}", StandardCharsets.UTF_8);

        Config config = new ConfigManager(tempDir).getConfig();

        assertEquals(2222L, config.getRecastDelay());
        assertTrue(config.isAutofishEnabled());
        assertTrue(config.isOpenWaterDetectEnabled());
        assertEquals(50L, config.getRandomPercent());
        assertEquals(1L, config.getReelInDelay());
        assertEquals(30.0f, config.getTurnAngle());
        assertEquals(500, config.getTurnDuration());
        assertEquals("\\[ClearLag\\] Removed [0-9]+ Entities!", config.getClearLagRegex());
    }

    @Test
    void malformedJsonFallsBackToDefaultsAndRewritesFile() throws Exception {
        Files.writeString(tempDir.resolve(FILE_NAME), "{not-json", StandardCharsets.UTF_8);

        ConfigManager manager = new ConfigManager(tempDir);

        assertEquals(1500L, manager.getConfig().getRecastDelay());
        assertDoesNotThrow(() -> JsonParser.parseString(
                Files.readString(tempDir.resolve(FILE_NAME), StandardCharsets.UTF_8)));
    }

    @Test
    void nullJsonFallsBackToDefaultsAndRewritesFile() throws Exception {
        Files.writeString(tempDir.resolve(FILE_NAME), "null", StandardCharsets.UTF_8);

        ConfigManager manager = new ConfigManager(tempDir);

        assertEquals(1500L, manager.getConfig().getRecastDelay());
        assertDoesNotThrow(() -> JsonParser.parseString(
                Files.readString(tempDir.resolve(FILE_NAME), StandardCharsets.UTF_8)));
    }
}
