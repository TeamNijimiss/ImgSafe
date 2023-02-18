/*
 * Copyright 2023 NAFU_at
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

package app.nijimiss.imgsafe.webhook;

import app.nijimiss.imgsafe.api.misskey.File;
import app.nijimiss.imgsafe.api.vision.SafeSearchAnnotation;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Objects;

@Slf4j
public class WebhookManager {
    private final String webhookUrl;
    private final String webhookTemplate;

    private final OkHttpClient okHttpClient;

    public WebhookManager(String webhookUrl, String webhookTemplate) {
        this.webhookUrl = webhookUrl;
        this.webhookTemplate = webhookTemplate;

        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor(log::debug))
                .build();
    }

    public void sendWebhook(File file, String author, SafeSearchAnnotation result) throws IOException {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(webhookUrl)).newBuilder();

        var messageBody = webhookTemplate.replace("{fileId}", file.id());
        messageBody = messageBody.replace("{fileUrl}", StringUtils.defaultIfEmpty(file.url(), "unknown"));
        messageBody = messageBody.replace("{fileSize}", Long.toString(ObjectUtils.defaultIfNull(file.size(), 0L)));
        messageBody = messageBody.replace("{authorUser}", StringUtils.defaultIfEmpty(author, "unknown"));
        //messageBody = messageBody.replace("{authorName}", author.name());
        messageBody = messageBody.replace("{checkResult}", "adult=" + result.adult() +
                ", spoof=" + result.spoof() +
                ", medical=" + result.medical() +
                ", violence=" + result.violence() +
                ", racy=" + result.racy());

        Request request = new Request.Builder()
                .url(builder.build())
                .post(RequestBody.create(messageBody,
                        MediaType.get("application/json; charset=utf-8")))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            log.debug("Webhook requested!");

            if (response.code() == 200) {
                log.info("Webhook sent successfully");
            }
        }
    }
}
