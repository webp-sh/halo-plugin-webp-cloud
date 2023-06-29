package se.webp.plugin;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.infra.utils.PathUtils;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.ReactivePostContentHandler;

@RequiredArgsConstructor
@Component
public class WebpCloudPostContentHandler implements ReactivePostContentHandler {

    private final ReactiveSettingFetcher reactiveSettingFetcher;

    private final ExternalUrlSupplier externalUrlSupplier;

    @Override
    public Mono<PostContentContext> handle(PostContentContext postContent) {
        return reactiveSettingFetcher.fetch("basic", BasicConfig.class)
            .defaultIfEmpty(new BasicConfig())
            .map(basicConfig -> {
                String oldContent = postContent.getContent();
                Document document = Jsoup.parse(oldContent);
                document.select("img").forEach(img -> {
                    String src = img.attr("src");

                    if (!PathUtils.isAbsoluteUri(src)) {
                        img.attr("src", basicConfig.proxy_address + src);
                        return;
                    }

                    String externalUrl = externalUrlSupplier.get().toString();

                    if (src.startsWith(externalUrl)) {
                        img.attr("src",
                            basicConfig.proxy_address + src.substring(externalUrl.length()));
                    }
                });

                postContent.setContent(document.outerHtml());
                return postContent;
            });
    }

    @Data
    public static class BasicConfig {
        String proxy_address;
    }
}