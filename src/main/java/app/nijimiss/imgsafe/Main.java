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

import app.nijimiss.imgsafe.api.misskey.File;
import app.nijimiss.imgsafe.api.misskey.MisskeyApiClient;
import app.nijimiss.imgsafe.api.vision.CloudVisionApiClient;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

@Slf4j
public class Main {
    private static boolean debug = false;
    private static ImgSafeConfig config;

    public static void main(String[] args) {
        log.info("Starting ImgSafe...");
        log.debug(getDebugInfo());

        for (String prop : args) {
            switch (prop.toLowerCase()) {
                case "debug":
                    debug = true;
                    break;

                default:
                    break;
            }
        }

        ConfigLoader configLoader = new ConfigLoader();
        configLoader.generateDefaultConfig();
        config = configLoader.getConfig();

        debug = debug || config.isDebug();
        if (!debug) {
            var root = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);
            root.setLevel(Level.INFO);
        }

        // Connect to Misskey instance
        MisskeyApiClient misskey = new MisskeyApiClient(config.getAuthentication().getInstanceHostname(), config.getAuthentication().getInstanceKey());
        CloudVisionApiClient vision = new CloudVisionApiClient(config.getAuthentication().getGoogleAPIKey());


        TimerTask imageCheckTask = new TimerTask() {
            private final TempFileManager tempFileManager = new TempFileManager();

            @Override
            public void run() {
                var temp = tempFileManager.load();
                String lastCheckedImage = temp != null ? temp.lastCheckedFile() : null;
                int requestedCount = temp != null ? temp.requestedCount() : 0;
                var lastChecked = temp != null ? new Date(temp.lastChecked()) : new Date();
                int limit = 10;

                if (requestedCount + limit > config.getSettings().getLimitPerMonth()) {
                    var lastCheckedCalendar = Calendar.getInstance();
                    lastCheckedCalendar.setTime(lastChecked);

                    var nowCalender = Calendar.getInstance();

                    if (lastCheckedCalendar.get(Calendar.MONTH) < nowCalender.get(Calendar.MONTH)) {
                        requestedCount = 0;
                    } else {
                        return;
                    }
                }

                log.info("Checking for new images...");
                try {
                    var files = misskey.getFiles(limit, lastCheckedImage);
                    Collections.reverse(files);

                    for (File file : files) {
                        if (!(file.type().equals("image/png") || file.type().equals("image/jpeg") || file.type().equals("image/gif")))
                            continue;

                        if (file.isSensitive())
                            continue;

                        var advancedFile = misskey.getFile(file.id());

                        log.debug("Checking file... {}", advancedFile.toString());

                        var requestBody = new CloudVisionApiClient.VisionSafeSearchRequestBody(
                                new CloudVisionApiClient.VisionSafeSearchRequestImage(
                                        Base64.encodeBase64String(IOUtils.toByteArray(new URL(StringUtils.defaultIfEmpty(advancedFile.webpublicUrl(), advancedFile.url()))))));

                        var request = new CloudVisionApiClient.VisionSafeSearchRequests(List.of(requestBody));
                        var safeSearchResult = vision.safeSearch(request);
                        var response = safeSearchResult.responses().get(0);

                        log.debug(response.safeSearchAnnotation().toString());
                        if (response.safeSearchAnnotation().adult().getLevel() >= config.getSettings().getJudgingScore()
                                || response.safeSearchAnnotation().violence().getLevel() >= config.getSettings().getJudgingScore()) {
                            misskey.updateFile(file, true);

                            log.info("Found unsafe file.: {} ", file.id());
                        }

                        lastCheckedImage = file.id();
                        lastChecked = new Date();

                        Thread.sleep(1000); // クールタイム
                    }

                    tempFileManager.save(new TempFileManager.Temp(lastCheckedImage, lastChecked.getTime(), requestedCount + files.size()));
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(imageCheckTask, 0, 600000); // 10分ごとにチェック
    }

    private static String getDebugInfo() {
        long max = Runtime.getRuntime().maxMemory() / 1048576L;
        long total = Runtime.getRuntime().totalMemory() / 1048576L;
        long free = Runtime.getRuntime().freeMemory() / 1048576L;
        long used = total - free;

        StringBuilder builder = new StringBuilder();
        builder.append("\n====== System Info ======\n");
        builder.append("Operating System:      ").append(System.getProperty("os.name")).append("\n");
        builder.append("JVM Version:           ").append(System.getProperty("java.version")).append("\n");
        builder.append("ImgSafe Version:    ").append(Main.class.getPackage().getImplementationVersion()).append("\n");
        builder.append("====== Memory Info ======\n");
        builder.append("Reserved memory:       ").append(total).append("MB\n");
        builder.append("  -> Used:             ").append(used).append("MB\n");
        builder.append("  -> Free:             ").append(free).append("MB\n");
        builder.append("Max. reserved memory:  ").append(max).append("MB");

        return builder.toString();
    }
}
