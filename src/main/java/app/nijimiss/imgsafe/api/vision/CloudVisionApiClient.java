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

package app.nijimiss.imgsafe.api.vision;

import app.nijimiss.imgsafe.ImgSafeTemp;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

@Slf4j
public class CloudVisionApiClient {
    private static final String API_ADDRESS = "https://vision.googleapis.com/v1/images:annotate";

    private final OkHttpClient okHttpClient;
    private final ObjectMapper mapper;

    private final String token;
    private final int limit;

    public CloudVisionApiClient(String token) {
        this(token, 1000);
    }

    public CloudVisionApiClient(String token, int limit) {
        if (StringUtils.isEmpty(token))
            throw new IllegalArgumentException();
        this.token = token;
        this.limit = limit;

        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor(log::debug))
                .build();
        mapper = new ObjectMapper();
    }

    public VisionSafeSearchResult safeSearch(VisionSafeSearchRequests requests) throws IOException {
        if (ImgSafeTemp.getRequestedCount() >= limit)
            throw new IllegalStateException("API Limit Exceeded");

        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(API_ADDRESS)).newBuilder();
        builder.addQueryParameter("key", token);
        builder.addQueryParameter("alt", "json");

        Gson gson = new Gson(); // TODO: 2022/04/07 そのうちJacksonに移行する
        Request request = new Request.Builder()
                .url(builder.build())
                .post(RequestBody.create(gson.toJson(requests),
                        MediaType.get("application/json; charset=utf-8")))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            ImgSafeTemp.setRequestedCount(ImgSafeTemp.getRequestedCount() + 1);

            if (response.code() == 200) {
                return mapper.readValue(response.body().string(), new TypeReference<>() {
                });
            }
            return new VisionSafeSearchResult(List.of());
        } catch (JsonProcessingException e) {
            log.warn("An error was returned for this image.", e);
            return new VisionSafeSearchResult(List.of());
        }
    }


    public static final class VisionSafeSearchRequests {
        private final List<VisionSafeSearchRequestBody> requests;

        public VisionSafeSearchRequests(
                List<VisionSafeSearchRequestBody> requests) {
            this.requests = requests;
        }

        public List<VisionSafeSearchRequestBody> getRequests() {
            return requests;
        }
    }

    // TODO: 2022/04/07 どうにかしたい。
    @ToString
    public static class VisionSafeSearchRequestBody {
        private final VisionSafeSearchRequestImage image;
        private final List<VisionSafeSearchRequestFeatures> features;

        public VisionSafeSearchRequestBody(VisionSafeSearchRequestImage image) {
            this.image = image;
            features = List.of(new VisionSafeSearchRequestFeatures());
        }
    }

    @ToString
    public static final class VisionSafeSearchRequestImage {
        private final String content;

        public VisionSafeSearchRequestImage(
                String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
    }

    @ToString
    public static class VisionSafeSearchRequestFeatures {
        private final String type = "SAFE_SEARCH_DETECTION";

        public String getType() {
            return type;
        }
    }
}
