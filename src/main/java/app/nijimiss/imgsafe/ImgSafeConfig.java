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

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ImgSafeConfig {
    private Authentication authentication;
    private Settings settings;
    private boolean debug;

    @Data
    public static class Authentication {
        @JsonProperty("instance_hostname")
        private String instanceHostname;
        @JsonProperty("instance_key")
        private String instanceKey;
        @JsonProperty("google_api_key")
        private String googleAPIKey;
    }

    @Data
    public static class Settings {
        //@JsonProperty("judging_item")
        //private List<JudgingItem> judgingItem;
        @JsonProperty("judging_score")
        private int judgingScore;
        @JsonProperty("limit_per_month")
        private int limitPerMonth;
        @JsonProperty("checking_image_size_min")
        private int checkingImageSizeMin;
        @JsonProperty("webhook")
        private Webhook webhook;
    }

    @Data
    public static class Webhook {
        @JsonProperty("enable")
        private boolean enable;
        @JsonProperty("url")
        private String url;
        @JsonProperty("template")
        private String template;
    }
}
