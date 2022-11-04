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
public class ImgSafeTemp {
    private static final ObjectMapper mapper;
    private static final File tempFile;
    private static ImgSafeTemp.Temp temp;

    static {
        mapper = new ObjectMapper();
        tempFile = new File("ImgSafe.json");

        load();
    }

    public static synchronized String getLastCheckedFile() {
        return temp.getLastCheckedFile();
    }

    public static synchronized long getLastChecked() {
        return temp.getLastChecked();
    }

    public static synchronized int getRequestedCount() {
        return temp.getRequestedCount();
    }

    public static synchronized void setLastCheckedFile(String lastCheckedFile) {
        temp.setLastCheckedFile(lastCheckedFile);
        save();
    }

    public static synchronized void setLastChecked(long lastChecked) {
        temp.setLastChecked(lastChecked);
        save();
    }

    public static synchronized void setRequestedCount(int requestedCount) {
        temp.setRequestedCount(requestedCount);
        save();
    }


    private static void load() {
        if (!tempFile.exists()) {
            try {
                tempFile.createNewFile();
            } catch (IOException e) {
                log.error("Failed to create temporary file.\n" +
                        "Please check if this program has write permission to the directory.", e);
            }
        }

        try (var fileInput = new FileInputStream(tempFile)) {
            temp = mapper.readValue(fileInput, Temp.class);
        } catch (FileNotFoundException e) {
            log.error("The temporary file could not be found. Do not delete the configuration file after starting the program.", e);
        } catch (IOException e) {
            log.error("The temporary file could not be loaded successfully.", e);
        }
    }

    private static void save() {
        try {
            mapper.writeValue(tempFile, temp);
        } catch (IOException e) {
            log.error("The temporary file could not be saved successfully.", e);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static final class Temp {
        private String lastCheckedFile;
        private long lastChecked;
        private int requestedCount;

        public Temp() {
            // Jackson deserialization
        }

        public Temp(String lastCheckedFile, long lastChecked, int requestedCount) {
            this.lastCheckedFile = lastCheckedFile;
            this.lastChecked = lastChecked;
            this.requestedCount = requestedCount;
        }

        public String getLastCheckedFile() {
            return lastCheckedFile;
        }

        public void setLastCheckedFile(String lastCheckedFile) {
            this.lastCheckedFile = lastCheckedFile;
        }

        public long getLastChecked() {
            return lastChecked;
        }

        public void setLastChecked(long lastChecked) {
            this.lastChecked = lastChecked;
        }

        public int getRequestedCount() {
            return requestedCount;
        }

        public void setRequestedCount(int requestedCount) {
            this.requestedCount = requestedCount;
        }
    }
}
