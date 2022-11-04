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

import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Meta(
        String maintainerName,
        String maintainerEmail,
        String version,
        String name,
        String uri,
        String description,
        List<Object> langs,
        String tosURL,
        String repositoryURL,
        String feedbackURL,
        boolean disableRegistration,
        boolean disableLocalTimeline,
        boolean disableGlobalTimeline,
        long driveCapacityPerLocalUserMB,
        long driveCapacityPerRemoteUserMB,
        boolean emailRequiredForSignup,
        boolean enableHcaptcha,
        UUID hcaptchaSiteKey,
        boolean enableRecaptcha,
        String recaptchaSiteKey,
        String swPublickey,
        String themeColor,
        String mascotImageURL,
        String bannerURL,
        String errorImageURL,
        String iconURL,
        Object backgroundImageURL,
        Object logoImageURL,
        long maxNoteTextLength,
        String defaultLightTheme,
        String defaultDarkTheme,
        boolean enableEmail,
        boolean enableTwitterIntegration,
        boolean enableGithubIntegration,
        boolean enableDiscordIntegration,
        boolean enableServiceWorker,
        boolean translatorAvailable,
        List<String> pinnedPages,
        Object pinnedClipID,
        boolean cacheRemoteFiles,
        boolean useStarForReactionFallback,
        List<String> pinnedUsers,
        List<Object> hiddenTags,
        List<String> blockedHosts,
        String hcaptchaSecretKey,
        String recaptchaSecretKey,
        String sensitiveMediaDetection,
        String sensitiveMediaDetectionSensitivity,
        boolean setSensitiveFlagAutomatically,
        boolean enableSensitiveMediaDetectionForVideos,
        String proxyAccountID,
        String twitterConsumerKey,
        String twitterConsumerSecret,
        String githubClientID,
        String githubClientSecret,
        String discordClientID,
        String discordClientSecret,
        Object summalyProxy,
        String email,
        boolean smtpSecure,
        String smtpHost,
        long smtpPort,
        String smtpUser,
        String smtpPass,
        String swPrivateKey,
        boolean useObjectStorage,
        String objectStorageBaseURL,
        String objectStorageBucket,
        Object objectStoragePrefix,
        String objectStorageEndpoint,
        Object objectStorageRegion,
        long objectStoragePort,
        String objectStorageAccessKey,
        String objectStorageSecretKey,
        boolean objectStorageUseSSL,
        boolean objectStorageUseProxy,
        boolean objectStorageSetPublicRead,
        boolean objectStorageS3ForcePathStyle,
        String deeplAuthKey,
        boolean deeplIsPro,
        boolean enableIPLogging,
        boolean enableActiveEmailValidation
) {
}
