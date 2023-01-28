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
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

@Slf4j
public class ImageCheckTask extends TimerTask {
    private final MisskeyApiClient misskey;
    private final CloudVisionApiClient vision;
    private final int judgingScore;

    public ImageCheckTask(MisskeyApiClient misskey, CloudVisionApiClient vision, int judgingScore) {
        this.misskey = misskey;
        this.vision = vision;
        this.judgingScore = judgingScore;
    }

    @Override
    public void run() {
        Date lastChecked = new Date(ImgSafeTemp.getLastChecked());
        int limit = 10;

        val lastCheckedCalendar = Calendar.getInstance();
        lastCheckedCalendar.setTime(lastChecked);
        val nowCalender = Calendar.getInstance();

        if (lastCheckedCalendar.get(Calendar.MONTH) < nowCalender.get(Calendar.MONTH))
            ImgSafeTemp.setRequestedCount(0);

        log.debug("Start checking images...");
        log.debug("Last checked image: {}, requested count: {}, last checked: {}", ImgSafeTemp.getLastCheckedFile(), ImgSafeTemp.getRequestedCount(), DateFormat.getInstance().format(lastChecked));

        try {
            List<File> images;
            do {
                do {
                    val files = misskey.getFiles(limit, ImgSafeTemp.getLastCheckedFile());
                    ImgSafeTemp.setLastChecked(new Date().getTime());

                    if (files.isEmpty())
                        return;

                    images = files.stream()
                            .filter(file -> file.type().equals("image/png") || file.type().equals("image/jpeg") || file.type().equals("image/gif"))
                            .filter(file -> !file.isSensitive())
                            .sorted(Comparator.comparing(File::createdAt))
                            .toList();

                    if (images.isEmpty()) {
                        ImgSafeTemp.setLastCheckedFile(files.get(0).id());
                    }
                } while (images.isEmpty());

                log.debug("Found {} newly added images.", images.size());

                for (File file : images) {
                    val fullFileInfo = misskey.getFile(file.id());

                    log.debug("Checking image: {}", fullFileInfo.name());
                    log.debug("Encoding image to base64...");

                    String base64Image;
                    try {
                        base64Image = Base64.encodeBase64String(IOUtils.toByteArray(new URL(StringUtils.defaultIfEmpty(fullFileInfo.webpublicUrl(), fullFileInfo.url()))));
                    } catch (FileNotFoundException e) {
                        log.warn("Image {} is not found.", fullFileInfo.name());
                        continue;
                    }

                    val requestBody = new CloudVisionApiClient.VisionSafeSearchRequestBody(
                            new CloudVisionApiClient.VisionSafeSearchRequestImage(base64Image));

                    val request = new CloudVisionApiClient.VisionSafeSearchRequests(List.of(requestBody));
                    val safeSearchResult = vision.safeSearch(request);
                    val response = safeSearchResult.responses().get(0);

                    log.debug("Safe search result: {}", response.safeSearchAnnotation());

                    if (response.safeSearchAnnotation().adult().getLevel() >= judgingScore
                            || response.safeSearchAnnotation().violence().getLevel() >= judgingScore) {
                        misskey.updateFile(file, true);

                        log.debug("Image {} is sensitive.", fullFileInfo.name());
                        log.debug("Marked as sensitive on Misskey.");
                    }

                    ImgSafeTemp.setLastCheckedFile(file.id());
                }
            } while (true);
        } catch (IllegalStateException e) {
            log.warn("Execution is suspended until the next month because the limit is exceeded.", e);

            // Calc next month first day millis
            val nextMonthCalendar = Calendar.getInstance();
            nextMonthCalendar.add(Calendar.MONTH, 1);
            nextMonthCalendar.set(Calendar.DAY_OF_MONTH, 1);
            nextMonthCalendar.set(Calendar.HOUR_OF_DAY, 0);
            nextMonthCalendar.set(Calendar.MINUTE, 0);
            nextMonthCalendar.set(Calendar.SECOND, 0);
            nextMonthCalendar.set(Calendar.MILLISECOND, 0);

            // Calc next month first day millis - now millis
            try {
                Thread.sleep(nextMonthCalendar.getTimeInMillis() - new Date().getTime());
            } catch (InterruptedException e1) {
                Thread.currentThread().interrupt();
            }
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }
}
