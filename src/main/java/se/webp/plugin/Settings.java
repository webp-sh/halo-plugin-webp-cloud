package se.webp.plugin;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.net.URL;
import lombok.Data;

public class Settings {

    @Data
    public static class BasicConfig {

        public static final String GROUP = "basic";

        String apiKeySecret;

        Proxy[] proxies;

    }

    @Data
    public static class Proxy {

        @JsonProperty("origin_url")
        URL originUrl;

        @JsonProperty("proxy_url")
        URL proxyUrl;
    }
}
