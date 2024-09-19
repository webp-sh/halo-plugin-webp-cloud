package se.webp.plugin;

import static org.thymeleaf.templatemode.TemplateMode.HTML;
import static se.webp.plugin.Settings.BasicConfig.GROUP;

import java.net.URI;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.context.ITemplateContext;
import org.thymeleaf.engine.ElementNames;
import org.thymeleaf.model.IAttribute;
import org.thymeleaf.model.IProcessableElementTag;
import org.thymeleaf.processor.element.MatchingElementName;
import org.thymeleaf.spring6.context.SpringContextUtils;
import org.thymeleaf.spring6.context.webflux.SpringWebFluxThymeleafRequestContext;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import run.halo.app.infra.ExternalUrlSupplier;
import run.halo.app.plugin.ReactiveSettingFetcher;
import run.halo.app.theme.dialect.ElementTagPostProcessor;

@Component
public class ImageTagProcessor implements ElementTagPostProcessor {

    private final MatchingElementName matchingElementName;

    private final ReactiveSettingFetcher settingFetcher;

    private final ExternalUrlSupplier externalUrlSupplier;

    public ImageTagProcessor(ReactiveSettingFetcher settingFetcher,
        ExternalUrlSupplier externalUrlSupplier) {
        this.settingFetcher = settingFetcher;
        this.externalUrlSupplier = externalUrlSupplier;
        this.matchingElementName =
            MatchingElementName.forElementName(HTML, ElementNames.forHTMLName("img"));
    }

    private static boolean urlMatches(URL firstUrl, URL secondUrl) {
        return Objects.equals(firstUrl.getAuthority(), secondUrl.getAuthority());
    }

    @Override
    public Mono<IProcessableElementTag> process(ITemplateContext context,
        IProcessableElementTag tag) {
        if (!matchingElementName.matches(tag.getElementDefinition().getElementName())) {
            return Mono.empty();
        }
        var requestContext = SpringContextUtils.getRequestContext(context);
        if (!(requestContext instanceof SpringWebFluxThymeleafRequestContext springWebContext)) {
            return Mono.empty();
        }
        var srcValue = Optional.ofNullable(tag.getAttribute("src"))
            .map(IAttribute::getValue)
            .filter(StringUtils::hasText)
            .map(URI::create);
        if (srcValue.isEmpty()) {
            return Mono.empty();
        }
        var exchange = springWebContext.getServerWebExchange();
        var externalUrl = externalUrlSupplier.getURL(exchange.getRequest());

        return settingFetcher.fetch(GROUP, Settings.BasicConfig.class)
            .filter(config -> !ArrayUtils.isEmpty(config.getProxies()))
            .flatMapMany(config -> Flux.fromArray(config.getProxies()))
            .filter(proxy ->
                Objects.nonNull(proxy.getProxyUrl()) && Objects.nonNull(proxy.getOriginUrl())
            )
            .filter(proxy -> urlMatches(externalUrl, proxy.getOriginUrl()))
            .next()
            .map(proxy -> {
                var srcBuilder = UriComponentsBuilder.fromHttpUrl(proxy.getProxyUrl().toString())
                    .path(srcValue.get().getPath())
                    .query(srcValue.get().getQuery())
                    .fragment(srcValue.get().getFragment());
                var newSrc = srcBuilder.toUriString();
                var modelFactory = context.getModelFactory();
                var newTag = tag;
                if (!tag.hasAttribute("srcset")) {
                    // calculate srcset and sizes
                    newTag = modelFactory.setAttribute(newTag, "sizes",
                        """
                            (max-width: 400px) 400px, (max-width: 800px) 800px, \
                            (max-width: 1200px) 1200px, (max-width: 1600px) 1600px\
                            """);
                    var w400Src = srcBuilder.cloneBuilder().queryParam("width", 400)
                        .toUriString();
                    var w800Src = srcBuilder.cloneBuilder().queryParam("width", 800)
                        .toUriString();
                    var w1200Src = srcBuilder.cloneBuilder().queryParam("width", 1200)
                        .toUriString();
                    var w1600Src = srcBuilder.cloneBuilder().queryParam("width", 1600)
                        .toUriString();
                    newTag = modelFactory.setAttribute(newTag, "srcset",
                        w400Src + " 400w, "
                            + w800Src + " 800w, "
                            + w1200Src + " 1200w, "
                            + w1600Src + " 1600w"
                    );
                }
                newTag = modelFactory.setAttribute(newTag, "src",
                    newSrc);
                return newTag;
            });
    }
}
