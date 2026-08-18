package com.wudji.xplusautofish.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConfigManager {
    private static final String CONFIG_FILE_NAME = "autofish.config";
    private static final Logger LOGGER = LogUtils.getLogger();

    private final Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .excludeFieldsWithoutExposeAnnotation()
            .create();
    private final Path configDirectory;
    private final Path configFile;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "autofish-config-writer");
        thread.setDaemon(true);
        return thread;
    });
    private Config config;

    public ConfigManager(Path configDirectory) {
        this.configDirectory = configDirectory;
        this.configFile = configDirectory.resolve(CONFIG_FILE_NAME);
        readConfig();
    }

    public Config getConfig() {
        return config;
    }

    public void readConfig() {
        try {
            Files.createDirectories(configDirectory);
            if (Files.exists(configFile)) {
                Config loaded = gson.fromJson(Files.readString(configFile, StandardCharsets.UTF_8), Config.class);
                if (loaded == null) {
                    throw new JsonParseException("Config JSON contains null");
                }
                boolean changed = loaded.enforceConstraints();
                if (config == null) {
                    config = new Config();
                }
                config.copyFrom(loaded);
                if (changed) {
                    writeConfig();
                }
            } else {
                config = new Config();
                writeConfig();
            }
        } catch (Exception exception) {
            LOGGER.error("Unable to read config from {}. Restoring defaults.", configFile, exception);
            if (config == null) {
                config = new Config();
            } else {
                config.copyFrom(new Config());
            }
            writeConfig();
        }
    }

    public void writeConfig() {
        Config snapshot = config == null ? new Config() : config.copy();
        snapshot.enforceConstraints();
        try {
            Files.createDirectories(configDirectory);
            Path temporaryFile = Files.createTempFile(configDirectory, CONFIG_FILE_NAME + ".", ".tmp");
            try {
                Files.writeString(temporaryFile, gson.toJson(snapshot), StandardCharsets.UTF_8);
                moveIntoPlace(temporaryFile);
            } finally {
                Files.deleteIfExists(temporaryFile);
            }
        } catch (Exception exception) {
            LOGGER.error("Unable to write config to {}.", configFile, exception);
        }
    }

    public CompletableFuture<Void> writeConfigAsync() {
        return CompletableFuture.runAsync(this::writeConfig, executor);
    }

    private void moveIntoPlace(Path temporaryFile) throws IOException {
        try {
            Files.move(temporaryFile, configFile,
                    StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(temporaryFile, configFile, StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
