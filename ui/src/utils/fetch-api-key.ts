import { PLUGIN_NAME } from "@/constant";
import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";

export async function fetchApiKey(): Promise<string | undefined> {
  const { data: configMap } =
    await consoleApiClient.plugin.plugin.fetchPluginJsonConfig({
      name: PLUGIN_NAME,
    });

  if (!configMap) {
    return undefined;
  }

  const apiKeySecret = (configMap as any)?.basic?.apiKeySecret;

  if (!apiKeySecret) {
    return undefined;
  }

  const { data: secret } = await coreApiClient.secret.getSecret({
    name: apiKeySecret,
  });

  if (!secret) {
    return undefined;
  }

  return secret.stringData?.apiKey;
}
