package com.ppdai.stargate.config;

import com.ppdai.atlas.client.api.AppControllerApi;
import com.ppdai.dockeryard.client.api.ImageControllerApi;
import com.ppdai.pauth.client.api.OAuth2EndpointApi;
import com.ppdai.stargate.utils.TokenValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class BeanConfig {

    @Value("${dockeryard.api.url}")
    public String dockeryardUrl;

    @Autowired
    private Environment environment;

    @Autowired
    private OAuth2EndpointApi pAuthApi;

    @Bean
    public ImageControllerApi imageControllerApi() {
        ImageControllerApi imageControllerApi = new ImageControllerApi();
        imageControllerApi.getApiClient().setBasePath(dockeryardUrl);
        return imageControllerApi;
    }

    @Bean
    public TokenValidator tokenValidator() {
        return new TokenValidator(pAuthApi, environment);
    }
}
