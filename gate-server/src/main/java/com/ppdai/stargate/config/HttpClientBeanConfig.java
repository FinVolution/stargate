package com.ppdai.stargate.config;

import com.ppdai.stargate.client.JsonHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpClientBeanConfig {

    @Value("${stargate.consul.connTimeout:1000}")
    private int consulConnTimeout;

    @Value("${stargate.consul.readTimeout:1000}")
    private int consulReadTimeout;

    @Value("${stargate.hc.connTimeout:500}")
    private int hcConnTimeout;

    @Value("${stargate.hc.readTimeout:500}")
    private int hcReadTimeout;

    @Value("${stargate.common.connTimeout:5000}")
    private int commonConnTimeout;

    @Value("${stargate.common.readTimeout:5000}")
    private int commonReadTimeout;

    @Value("${stargate.docker.connTimeout:1000}")
    private int dockerConnTimeout;

    @Value("${stargate.docker.readTimeout:1000}")
    private int dockerReadTimeout;

    @Bean(name = "commonHttpClient")
    public JsonHttpClient commonHttpClient() {
        return new JsonHttpClient(commonConnTimeout, commonReadTimeout);
    }

    @Bean(name = "consulHttpClient")
    public JsonHttpClient consulHttpClient() {
        return new JsonHttpClient(consulConnTimeout, consulReadTimeout);
    }

    @Bean(name = "hcHttpClient")
    public JsonHttpClient hcHttpClient() {
        return new JsonHttpClient(hcConnTimeout, hcReadTimeout);
    }

    @Bean(name = "dockerHttpClient")
    public JsonHttpClient dockerHttpClient() {
        return new JsonHttpClient(dockerConnTimeout, dockerReadTimeout);
    }
}
