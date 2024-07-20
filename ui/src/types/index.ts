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

export interface WebpCloudProxyStats {
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
  proxy_created_at: string;
  proxy_enable_extra_params: boolean;
  proxy_visual_effects: any[];
  proxy_region: string;
  proxy_extra_headers: ProxyExtraHeader[];
  proxy_total_bytes_sent: number;
  proxy_total_top_requested_path: any[];
  proxy_daily_bytes_sent: ProxyDailyBytesSent[];
  proxy_last_logs: any[];
  proxy_custom_domain: any[];
}

export interface ProxyDailyBytesSent {
  date: string;
  bytes_sent: number;
  count: number;
  cache_hit_ratio: number;
}

export interface WebpCloudProxyFormState {
  proxy_name: string;
  proxy_origin_url: string;
  proxy_region: string;
}

export interface WebpCloudProxyEditFormState {
  proxy_name: string;
  proxy_quality: number;
  proxy_enabled: boolean;
  proxy_ua: string;
  proxy_cors_header: string;
  proxy_allowed_referer: string;
  proxy_extra_headers: ProxyExtraHeader[];
}

export interface ProxyExtraHeader {
  key: string;
  value: string;
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
