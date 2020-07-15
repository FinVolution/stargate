package com.ppdai.stargate.config;

import com.ppdai.atlas.client.api.*;
import com.ppdai.atlas.client.invoker.ApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AtlasBeanConfig {

    @Value("${atlas.api.url}")
    private String atlasUrl;

    @Value("${atlas.api.token}")
    private String atlasToken;

    @Value("${atlas.api.connTimeout}")
    private Integer atlasConnTimeout;

    @Value("${atlas.api.readTimeout}")
    private Integer atlasReadTimeout;

    @Autowired
    private ApiClient atlasApiClient;

    @Bean
    public ApiClient atlasApiClient() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(atlasUrl);
        apiClient.setConnectTimeout(atlasConnTimeout);
        apiClient.setReadTimeout(atlasReadTimeout);
        apiClient.addDefaultHeader("jwt-token", atlasToken);
        return apiClient;
    }

    @Bean
    public AppControllerApi atlasAppControllerApi() {
        AppControllerApi appControllerApi = new AppControllerApi(atlasApiClient);
        return appControllerApi;
    }

    @Bean
    public OrgControllerApi atlasOrgControllerApi() {
        OrgControllerApi orgControllerApi = new OrgControllerApi(atlasApiClient);
        return orgControllerApi;
    }

    @Bean
    public SpecTypeControllerApi atlasSpecTypeControllerApi() {
        SpecTypeControllerApi specTypeControllerApi = new SpecTypeControllerApi(atlasApiClient);
        return specTypeControllerApi;
    }

    @Bean
    public UserControllerApi atlasUserControllerApi() {
        UserControllerApi userControllerApi = new UserControllerApi(atlasApiClient);
        return userControllerApi;
    }

    @Bean
    public EnvControllerApi atlasEnvControllerApi() {
        EnvControllerApi envControllerApi = new EnvControllerApi(atlasApiClient);
        return envControllerApi;
    }

    @Bean
    public AppQuotaControllerApi atlasAppQuotaControllerApi() {
        AppQuotaControllerApi appQuotaControllerApi = new AppQuotaControllerApi(atlasApiClient);
        return appQuotaControllerApi;
    }

    @Bean
    public ZoneControllerApi atlasZoneControllerApi() {
        ZoneControllerApi zoneControllerApi = new ZoneControllerApi(atlasApiClient);
        return zoneControllerApi;
    }

    @Bean
    public ApplyControllerApi atlasApplyControllerApi() {
        ApplyControllerApi applyControllerApi = new ApplyControllerApi(atlasApiClient);
        return applyControllerApi;
    }

}
