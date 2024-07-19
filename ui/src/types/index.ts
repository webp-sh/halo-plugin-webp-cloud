export interface WebpCloudResponse<T> {
  data: T;
  success: boolean;
}

export interface WebpCloudUser {
  user_uuid: string;
  name: string;
  email: string;
  avatar_url: string;
  api_key: string;
  daily_quota: number;
  daily_quota_limit: number;
  permanent_quota: number;
  user_plan: string;
  balance: number;
  address: string;
  is_checked_in: boolean;
}

export interface WebpCloudProxy {
  proxy_uuid: string;
  proxy_name: string;
  proxy_origin_url: string;
  proxy_proxy_url: string;
  proxy_ua: string;
  proxy_cors_header: string;
  proxy_allowed_referer: string;
  proxy_quality: number;
  proxy_cache_expire: number;
  proxy_cache_size: number;
  proxy_cache_size_limit: number;
  proxy_enabled: boolean;
  proxy_operation_on_disabled: string;
  proxy_visual_effects: any[];
  proxy_created_at: string;
  proxy_enable_extra_params: boolean;
  proxy_region: string;
  proxy_extra_headers: any[];
}

export interface PluginConfigMap {
  basic: PluginConfigMapBasic;
}

export interface PluginConfigMapBasic {
  apiKeySecret?: string;
  proxies: PluginConfigMapProxy[];
}

export interface PluginConfigMapProxy {
  proxy_url: string;
  origin_url: string;
}
