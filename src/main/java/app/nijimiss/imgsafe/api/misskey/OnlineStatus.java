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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public enum OnlineStatus {
    ACTIVE, OFFLINE, ONLINE, UNKNOWN;

    public static class Serializer extends JsonSerializer<OnlineStatus> {
        @Override
        public void serialize(OnlineStatus status, JsonGenerator generator, SerializerProvider serializers) throws IOException {
            generator.writeString(status.name().toLowerCase());
        }
    }

    public static class Deserializer extends JsonDeserializer<OnlineStatus> {
        @Override
        public OnlineStatus deserialize(JsonParser parser, DeserializationContext context) throws IOException {
            return OnlineStatus.valueOf(parser.getText().toUpperCase());
        }
    }
}
