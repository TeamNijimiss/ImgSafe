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

import app.nijimiss.imgsafe.api.misskey.Meta;
import app.nijimiss.imgsafe.api.misskey.MisskeyApiClient;
import app.nijimiss.imgsafe.api.vision.CloudVisionApiClient;
import app.nijimiss.imgsafe.webhook.WebhookManager;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static org.slf4j.Logger.ROOT_LOGGER_NAME;

@Slf4j
public class Main {
    private static boolean debug = false;
    private static ImgSafeConfig config;

    public static void main(String[] args) {
        log.info("Starting ImgSafe");
        log.debug(getSystemInfo());

        for (String prop : args) {
            if ("debug".equalsIgnoreCase(prop)) {
                debug = true;
                break;
            }
        }

        log.info("Loading configuration file...");
        ConfigLoader configLoader = new ConfigLoader();
        configLoader.generateDefaultConfig();
        config = configLoader.getConfig();

        debug = debug || config.isDebug();
        if (!debug) {
            var root = (Logger) LoggerFactory.getLogger(ROOT_LOGGER_NAME);
            root.setLevel(Level.INFO);
        } else {
            log.debug("Boot with debug mode");
        }

        // Create API clients
        MisskeyApiClient misskey = new MisskeyApiClient(config.getAuthentication().getInstanceHostname(), config.getAuthentication().getInstanceKey());
        CloudVisionApiClient vision = new CloudVisionApiClient(config.getAuthentication().getGoogleAPIKey(), config.getSettings().getLimitPerMonth());
        WebhookManager webhookManage = config.getSettings().getWebhook().isEnable() ? new WebhookManager(config.getSettings().getWebhook().getUrl(),
                config.getSettings().getWebhook().getTemplate()) : null;

        try {
            Meta meta = misskey.getMeta();
            log.info("API connection: Misskey... OK (v{})", meta.version());
        } catch (IOException e) {
            log.error("\"API connection: Misskey... Failed", e);
            System.exit(1);
        }

        // TODO: 2022/11/05 Vision API Connection check.

        log.info("Starting ImageCheckTask...");
        TimerTask imageCheckTask = new ImageCheckTask(misskey, vision, webhookManage, config.getSettings().getJudgingScore());
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(imageCheckTask, 0, 600000); // 10分ごとにチェック
    }

    private static String getSystemInfo() {
        long max = Runtime.getRuntime().maxMemory() / 1048576L;
        long total = Runtime.getRuntime().totalMemory() / 1048576L;
        long free = Runtime.getRuntime().freeMemory() / 1048576L;
        long used = total - free;

        return "\n====== System Info ======\n" +
                "Operating System:      " + System.getProperty("os.name") + "\n" +
                "JVM Version:           " + System.getProperty("java.version") + "\n" +
                "ImgSafe Version:    " + Main.class.getPackage().getImplementationVersion() + "\n" +
                "====== Memory Info ======\n" +
                "Reserved memory:       " + total + "MB\n" +
                "  -> Used:             " + used + "MB\n" +
                "  -> Free:             " + free + "MB\n" +
                "Max. reserved memory:  " + max + "MB";
    }
}
