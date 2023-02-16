package com.braintreepayments.api;

import android.content.Context;
import android.net.Uri;
import android.util.Base64;

import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;

import org.json.JSONException;

class ConfigurationLoader {

    private final BraintreeHttpClient httpClient;
    private final ConfigurationCache configurationCache;

    ConfigurationLoader(Context context, BraintreeHttpClient httpClient) {
        this(httpClient, ConfigurationCache.getInstance(context));
    }

    @VisibleForTesting
    ConfigurationLoader(BraintreeHttpClient httpClient, ConfigurationCache configurationCache) {
        this.httpClient = httpClient;
        this.configurationCache = configurationCache;
    }

    void loadConfiguration(final Authorization authorization, final ConfigurationLoaderCallback callback) {
        if (authorization instanceof InvalidAuthorization) {
            String message = ((InvalidAuthorization) authorization).getErrorMessage();
            callback.onResult(null, new BraintreeException(message));
            return;
        }

        final String configUrl = Uri.parse(authorization.getConfigUrl())
                .buildUpon()
                .appendQueryParameter("configVersion", "3")
                .build()
                .toString();


        Configuration cachedConfig = getCachedConfiguration(authorization, configUrl);
        if (cachedConfig != null) {
            callback.onResult(cachedConfig, null);
        } else {

            httpClient.get(configUrl, null, authorization, HttpClient.RETRY_MAX_3_TIMES, new HttpResponseCallback() {

                @Override
                public void onResult(String responseBody, Exception httpError) {
                    if (responseBody != null) {
                        try {
                            Configuration configuration = Configuration.fromJson(responseBody);
                            saveConfigurationToCache(configuration, authorization, configUrl);
                            callback.onResult(configuration, null);
                        } catch (JSONException jsonException) {
                            callback.onResult(null, jsonException);
                        }
                    } else {
                        String errorMessageFormat = "Request for configuration has failed: %s";
                        String errorMessage = String.format(errorMessageFormat, httpError.getMessage());

                        ConfigurationException configurationException = new ConfigurationException(errorMessage, httpError);
                        callback.onResult(null, configurationException);
                    }
                }
            });
        }
    }

    private void saveConfigurationToCache(Configuration configuration, Authorization authorization, String configUrl) {
        String cacheKey = createCacheKey(authorization, configUrl);
        configurationCache.saveConfiguration(configuration, cacheKey);
    }

     private Configuration getCachedConfiguration(Authorization authorization, String configUrl) {
        String cacheKey = createCacheKey(authorization, configUrl);
        String cachedConfigResponse = configurationCache.getConfiguration(cacheKey);
        try {
            return Configuration.fromJson(cachedConfigResponse);
        } catch (JSONException e) {
            return null;
        }
    }

    private static String createCacheKey(Authorization authorization, String configUrl) {
        return Base64.encodeToString(String.format("%s%s", configUrl, authorization.getBearer()).getBytes(), 0);
    }
}
