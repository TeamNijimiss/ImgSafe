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

public enum LikelinessLevel {
    UNKNOWN(-1),
    VERY_UNLIKELY(0),
    UNLIKELY(5),
    POSSIBLE(10),
    LIKELY(15),
    VERY_LIKELY(20);

    private final int level;


    LikelinessLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }
}
