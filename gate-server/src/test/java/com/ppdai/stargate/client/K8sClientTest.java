package com.ppdai.stargate.client;

import com.ppdai.stargate.mock.MockKubeApiServer;
import com.ppdai.stargate.vi.AppPodTemplateVI;
import com.ppdai.stargate.vo.K8sQuota2C4GVO;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.models.V1PodList;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertTrue;

public class K8sClientTest {

    private MockKubeApiServer mockKubeApiServer;

    @Before
    public void start() throws IOException {
        mockKubeApiServer = new MockKubeApiServer();
        mockKubeApiServer.start();
        System.out.println("start MockKubeApiServer on 8080 port ==================================================>");
    }

    @After
    public void shutdown() throws IOException {
        mockKubeApiServer.shutdown();
        System.out.println("shutdown MockKubeApiServer on 8080 port ==================================================>");
    }

    @Test
    public void testDeployUpdateDeletePod() throws ApiException, InterruptedException {
        K8sClient k8sClient = new K8sClient("fat", "http://127.0.0.1:8080", "");

        String namespace = "default-develop";

        k8sClient.createNamespace("default-develop");

        AppPodTemplateVI appPodTemplateVI = new AppPodTemplateVI();
        appPodTemplateVI.setAppId("10000001234");
        appPodTemplateVI.setAppName("hello.test.com");
        appPodTemplateVI.setDns(null);
        appPodTemplateVI.setImage("hello.test.com:0.0.7_19");
        appPodTemplateVI.setK8sQuotaVO(new K8sQuota2C4GVO());
        appPodTemplateVI.setFlags(0l);
        appPodTemplateVI.setEnvJson("{}");
        appPodTemplateVI.setPodName("ecs-zhang");
        appPodTemplateVI.setPodIp("10.254.130.18");
        appPodTemplateVI.setPort(6062);
        appPodTemplateVI.setNamespace(namespace);
        appPodTemplateVI.setSysctls("");

        k8sClient.deployAppPod(appPodTemplateVI);

        Map<String, String> labelSelectorMap = new HashMap<>();
        labelSelectorMap.put("instance", appPodTemplateVI.getPodName());

        V1PodList v1PodList = k8sClient.queryAppPods(namespace, labelSelectorMap);
        assertTrue(v1PodList.getItems().size() > 0);

        k8sClient.deleteAppPod(namespace, appPodTemplateVI.getPodName());
    }

    @Test
    public void testQueryAppPods() throws ApiException {
        K8sClient k8sClient = new K8sClient("fat", "http://127.0.0.1:8080", "");

        Map<String, String> labelSelectorMap = new HashMap<>();
        labelSelectorMap.put("ip", "10.254.5.202");

        Map<String, String> fieldSelector = new HashMap<>();
        fieldSelector.put("status.podIP", "10.254.5.202");

        k8sClient.queryAllPods(fieldSelector);
    }
}
