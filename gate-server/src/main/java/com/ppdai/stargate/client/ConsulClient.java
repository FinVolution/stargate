package com.ppdai.stargate.client;

import com.alibaba.fastjson.JSON;
import com.ppdai.stargate.utils.UserInfoUtil;
import com.ppdai.stargate.vo.ConsulInstanceVO;
import com.ppdai.stargate.vo.NginxInstanceAttrVO;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@Slf4j
public class ConsulClient {

    private String consulHost;
    private JsonHttpClient httpClient;

    private String getUpstreamUrlPrefix(String upstream) {
        if (consulHost.toLowerCase().startsWith("http")) {
            return consulHost;
        } else {
            return "http://" + consulHost + "/v1/kv/upstreams/" + upstream;
        }
    }

    public ConsulClient(String consulHost, JsonHttpClient httpClient) {
        this.consulHost = consulHost;
        this.httpClient = httpClient;
    }

    public void putInstance(String upstream, String ipAndPort, NginxInstanceAttrVO nginxInstanceAttrVO) throws IOException {
        String url = getUpstreamUrlPrefix(upstream) + "/" + ipAndPort;
        String body = JSON.toJSONString(nginxInstanceAttrVO);

        log.info("开始向nginx添加实例, username={}, method=PUT, url={}, body={}", UserInfoUtil.getUsername(), url, body);
        httpClient.put(url, body);
        log.info("向nginx添加实例成功, username={}, method=PUT, url={}, body={}", UserInfoUtil.getUsername(), url, body);
    }

    public void deleteInstance(String upstream, String ipAndPort) throws IOException {
        String url = getUpstreamUrlPrefix(upstream) + "/" + ipAndPort;

        log.info("开始向nginx删除实例, username={}, method=DELETE, url={}", UserInfoUtil.getUsername(), url);
        httpClient.delete(getUpstreamUrlPrefix(upstream) + "/" + ipAndPort);
        log.info("向nginx删除实例成功, username={}, method=DELETE, url={}", UserInfoUtil.getUsername(), url);
    }

    public List<ConsulInstanceVO> getInstances(String upstream) throws IOException {
        String url = getUpstreamUrlPrefix(upstream) + "?recurse";

        log.info("开始向consul查询实例, url={}", url);
        String result = httpClient.get(url);
        log.info("向consul查询实例成功, url={}, result={}", url, result);
        return JSON.parseArray(result, ConsulInstanceVO.class);
    }
}
