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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

@Slf4j
public class TempFileManager {
    private final ObjectMapper mapper;
    private final File tempFile;

    public TempFileManager() {
        mapper = new ObjectMapper();
        tempFile = new File("ImgSafe.json");
    }

    public Temp load() {
        if (!tempFile.exists()) {
            try {
                tempFile.createNewFile();
            } catch (IOException e) {
                log.error("Failed to create temporary file.\n" +
                        "Please check if this program has write permission to the directory.", e);
            }
        }

        try (var fileInput = new FileInputStream(tempFile)) {
            return mapper.readValue(fileInput, Temp.class);
        } catch (FileNotFoundException e) {
            log.error("The temporary file could not be found. Do not delete the configuration file after starting the program.", e);
        } catch (IOException e) {
            log.error("The temporary file could not be loaded successfully.", e);
        }

        return null;
    }

    public void save(Temp temp) {
        try {
            mapper.writeValue(tempFile, temp);
        } catch (IOException e) {
            log.error("The temporary file could not be saved successfully.", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Temp(String lastCheckedFile, long lastChecked, int requestedCount) {
    }
}
