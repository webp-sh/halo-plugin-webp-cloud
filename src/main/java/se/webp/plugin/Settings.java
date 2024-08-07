package se.webp.plugin;

import lombok.Data;
import reactor.core.publisher.Mono;
import run.halo.app.plugin.ReactiveSettingFetcher;

public class Settings {

    public static Mono<BasicConfig> getBasicConfig(ReactiveSettingFetcher settingFetcher) {
        return settingFetcher.fetch(BasicConfig.GROUP, BasicConfig.class);
    }

    @Data
    public static class BasicConfig {
        public static final String GROUP = "basic";

        String apiKeySecret;

        Proxy[] proxies;
    }

    @Data
    public static class Proxy {
        String origin_url;

        String proxy_url;
    }
}
