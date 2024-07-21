package se.webp.plugin;

import org.springframework.stereotype.Component;

import run.halo.app.plugin.BasePlugin;
import run.halo.app.plugin.PluginContext;

@Component
public class WebpCloudPlugin extends BasePlugin {

    public WebpCloudPlugin(PluginContext content) {
        super(content);
    }
}
