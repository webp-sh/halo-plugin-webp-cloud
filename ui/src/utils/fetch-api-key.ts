import { consoleApiClient, coreApiClient } from "@halo-dev/api-client";

export async function fetchApiKey(): Promise<string | undefined> {
  const { data: configMap } =
    await consoleApiClient.plugin.plugin.fetchPluginConfig({
      name: "plugin-webp-se-cloud",
    });

  if (!configMap) {
    return undefined;
  }

  const configMapData = JSON.parse(configMap.data?.["basic"] || "{}");
  const apiKeySecret = configMapData.apiKeySecret;

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
