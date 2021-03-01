package com.ppdai.stargate.config;

import com.ppdai.stargate.client.JsonHttpClient;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.concurrent.TimeUnit;

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

    @Value("${stargate.flink.connTimeout:5000}")
    private int flinkConnTimeout;

    @Value("${stargate.flink.readTimeout:30000}")
    private int flinkReadTimeout;

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

    @Bean(name = "flinkHttpClient")
    public JsonHttpClient flinkHttpClient() {
        return new JsonHttpClient(flinkConnTimeout, flinkReadTimeout);
    }

    @Bean(name = "flinkProxyClient")
    public OkHttpClient flinkProxyHttpClient() {
        String proxyIp = "";
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp, 80));
        return new OkHttpClient.Builder().connectTimeout(flinkConnTimeout, TimeUnit.MILLISECONDS)
                .proxy(proxy).readTimeout(flinkReadTimeout, TimeUnit.MILLISECONDS).build();
    }
}
