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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.OffsetDateTime;

@JsonIgnoreProperties(ignoreUnknown = true)
public record File(
        String id,
        OffsetDateTime createdAt,
        String name,
        String type,
        String md5,
        long size,
        boolean isSensitive,
        String blurhash,
        Properties properties,
        String url,
        String thumbnailUrl,
        String webpublicUrl,
        Object comment,
        String folderId,
        Folder folder,
        String userId
) {
}
