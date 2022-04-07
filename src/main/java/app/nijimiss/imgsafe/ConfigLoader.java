/*
 * Copyright 2022 NAFU_at
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package app.nijimiss.imgsafe;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;

@Slf4j
public class ConfigLoader {
    private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
    private static final String CONFIG_FILE_NAME = "config.yaml";

    private final File configFile;
    private ImgSafeConfig config;

    public ConfigLoader() {
        configFile = new File(CONFIG_FILE_NAME);
    }

    public void generateDefaultConfig() {
        if (!configFile.exists()) {
            try (InputStream original = ClassLoader.getSystemResourceAsStream(CONFIG_FILE_NAME)) {
                Files.copy(original, configFile.toPath());
                log.info("The configuration file was not found, so a new file was created.");
                log.debug("Configuration file location: {}", configFile.getPath());
            } catch (IOException e) {
                log.error("Failed to generate configuration file.\n" +
                        "Please check if this program has write permission to the directory.", e);
            }
        }
    }

    public void reloadConfig() {
        try (var configInput = new FileInputStream(configFile)) {
            config = MAPPER.readValue(configInput, ImgSafeConfig.class);
            log.info("The configuration file has been successfully loaded.");
        } catch (FileNotFoundException e) {
            log.error("The configuration file could not be found. Do not delete the configuration file after starting the program.", e);
        } catch (IOException e) {
            log.error("The configuration file could not be loaded successfully.", e);
        }
    }

    public ImgSafeConfig getConfig() {
        if (config == null)
            reloadConfig();
        return config;
    }
}
