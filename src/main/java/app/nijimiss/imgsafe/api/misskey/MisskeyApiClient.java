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

package app.nijimiss.imgsafe.api.misskey;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.logging.HttpLoggingInterceptor;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.List;
import java.util.Objects;

import static com.fasterxml.jackson.core.JsonParser.Feature.*;

@Slf4j
public class MisskeyApiClient {
    private static final String API_ENDPOINT_FILES = "/api/admin/drive/files";
    private static final String API_ENDPOINT_SHOW_FILE = "/api/admin/drive/show-file";
    private static final String API_ENDPOINT_FILE_UPDATE = "/api/drive/files/update";

    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendOptional(DateTimeFormatter.ISO_DATE_TIME)
            .appendOptional(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
            .appendOptional(DateTimeFormatter.ISO_INSTANT)
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SX"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX"))
            .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .toFormatter()
            .withZone(ZoneOffset.UTC);

    public static OffsetDateTime parseDateTimeString(String str) {
        return ZonedDateTime.from(DATE_TIME_FORMATTER.parse(str)).toOffsetDateTime();
    }


    private final OkHttpClient okHttpClient;
    private final ObjectMapper mapper;

    private final String hostname;
    private final String token;

    public MisskeyApiClient(String hostname, String token) {
        this.hostname = hostname.startsWith("https://") ? hostname : "https://" + hostname;
        if (StringUtils.isEmpty(token))
            throw new IllegalArgumentException();
        this.token = token;

        okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new HttpLoggingInterceptor(log::debug))
                .build();
        mapper = new ObjectMapper();
        mapper.enable(
                ALLOW_UNQUOTED_FIELD_NAMES,
                ALLOW_TRAILING_COMMA,
                ALLOW_SINGLE_QUOTES,
                ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER,
                ALLOW_NON_NUMERIC_NUMBERS,
                ALLOW_LEADING_DECIMAL_POINT_FOR_NUMBERS
        ); // Misskey APIがJson5形式で応答する可能性があるため、これらの設定を有効にする。
        SimpleModule module = new SimpleModule();
        module.addDeserializer(OffsetDateTime.class, new JsonDeserializer<>() {
            @Override
            public OffsetDateTime deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                String value = jsonParser.getText();
                return parseDateTimeString(value);
            }
        });
        mapper.registerModule(module); // OffsetDateTimeをdeserializeするために必要
    }

    public List<File> getFiles(int limit, @Nullable String sinceId) throws IOException {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(hostname + API_ENDPOINT_FILES)).newBuilder();
        Request request = new Request.Builder()
                .url(builder.build())
                .post(RequestBody.create(mapper.writeValueAsString(new GetFilesRequestBody(token, limit, sinceId, "local")),
                        MediaType.get("application/json; charset=utf-8")))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() == 200) {
                return mapper.readValue(response.body().string(), new TypeReference<>() {
                });
            }
            return null;
        }
    }

    public File getFile(String fileId) throws IOException {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(hostname + API_ENDPOINT_SHOW_FILE)).newBuilder();
        Request request = new Request.Builder()
                .url(builder.build())
                .post(RequestBody.create(mapper.writeValueAsString(new GetFileRequestBody(token, fileId)),
                        MediaType.get("application/json; charset=utf-8")))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            if (response.code() == 200) {
                return mapper.readValue(response.body().string(), File.class);
            }
            return null;
        }
    }

    public boolean updateFile(File file, boolean isSensitive) throws IOException {
        HttpUrl.Builder builder = Objects.requireNonNull(HttpUrl.parse(hostname + API_ENDPOINT_FILE_UPDATE)).newBuilder();
        Request request = new Request.Builder()
                .url(builder.build())
                .post(RequestBody.create(mapper.writeValueAsString(new UpdateFileRequestBody(token, file.id(), isSensitive)),
                        MediaType.get("application/json; charset=utf-8")))
                .build();

        try (Response response = okHttpClient.newCall(request).execute()) {
            return response.code() == 204;
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GetFilesRequestBody(String i, int limit, String sinceId, String origin) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record GetFileRequestBody(String i, String fileId) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record UpdateFileRequestBody(String i, String fileId, boolean isSensitive) {
    }
}
