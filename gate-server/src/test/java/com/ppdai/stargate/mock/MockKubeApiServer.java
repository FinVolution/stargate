package com.ppdai.stargate.mock;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import java.io.IOException;

public class MockKubeApiServer {
    private MockWebServer mockServer;

    public void start() throws IOException {
        mockServer = new MockWebServer();
        final Dispatcher dispatcher = new Dispatcher() {

            @Override
            public MockResponse dispatch (RecordedRequest request) throws InterruptedException {

                switch (request.getPath()) {
                    case "/api/v1/namespaces?includeUninitialized=false&pretty=true":
                        return new MockResponse().setResponseCode(201).setBody("{\n" +
                                "  \"kind\": \"Namespace\",\n" +
                                "  \"apiVersion\": \"v1\",\n" +
                                "  \"metadata\": {\n" +
                                "    \"name\": \"default-develop\",\n" +
                                "    \"selfLink\": \"/api/v1/namespaces/default-develop1\",\n" +
                                "    \"uid\": \"e8f052c4-9690-11ea-b786-fa120a903300\",\n" +
                                "    \"resourceVersion\": \"70082604\",\n" +
                                "    \"creationTimestamp\": \"2020-05-15T09:46:11Z\"\n" +
                                "  },\n" +
                                "  \"spec\": {\n" +
                                "    \"finalizers\": [\n" +
                                "      \"kubernetes\"\n" +
                                "    ]\n" +
                                "  },\n" +
                                "  \"status\": {\n" +
                                "    \"phase\": \"Active\"\n" +
                                "  }\n" +
                                "}");
                    case "/api/v1/namespaces/default-develop/pods?includeUninitialized=false&pretty=true":
                        return new MockResponse().setResponseCode(201).setBody("{\n" +
                                "  \"kind\": \"Pod\",\n" +
                                "  \"apiVersion\": \"v1\",\n" +
                                "  \"metadata\": {\n" +
                                "    \"name\": \"ecs-zhang\",\n" +
                                "    \"namespace\": \"default-develop\",\n" +
                                "    \"selfLink\": \"/api/v1/namespaces/default-develop/pods/ecs-zhang\",\n" +
                                "    \"uid\": \"947ae6b7-968d-11ea-b786-fa120a903300\",\n" +
                                "    \"resourceVersion\": \"70080176\",\n" +
                                "    \"creationTimestamp\": \"2020-05-15T09:22:21Z\",\n" +
                                "    \"labels\": {\n" +
                                "      \"app\": \"hello.test.com\",\n" +
                                "      \"appid\": \"10000001234\",\n" +
                                "      \"instance\": \"ecs-zhang\",\n" +
                                "      \"ip\": \"10.254.130.18\"\n" +
                                "    }\n" +
                                "  },\n" +
                                "  \"spec\": {\n" +
                                "    \"volumes\": [\n" +
                                "      {\n" +
                                "        \"name\": \"cpuinfo\",\n" +
                                "        \"hostPath\": {\n" +
                                "          \"path\": \"/var/lib/lxcfs/proc/cpuinfo\",\n" +
                                "          \"type\": \"\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      {\n" +
                                "        \"name\": \"diskstats\",\n" +
                                "        \"hostPath\": {\n" +
                                "          \"path\": \"/var/lib/lxcfs/proc/diskstats\",\n" +
                                "          \"type\": \"\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      {\n" +
                                "        \"name\": \"meminfo\",\n" +
                                "        \"hostPath\": {\n" +
                                "          \"path\": \"/var/lib/lxcfs/proc/meminfo\",\n" +
                                "          \"type\": \"\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      {\n" +
                                "        \"name\": \"stat\",\n" +
                                "        \"hostPath\": {\n" +
                                "          \"path\": \"/var/lib/lxcfs/proc/stat\",\n" +
                                "          \"type\": \"\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      {\n" +
                                "        \"name\": \"swaps\",\n" +
                                "        \"hostPath\": {\n" +
                                "          \"path\": \"/var/lib/lxcfs/proc/swaps\",\n" +
                                "          \"type\": \"\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      {\n" +
                                "        \"name\": \"uptime\",\n" +
                                "        \"hostPath\": {\n" +
                                "          \"path\": \"/var/lib/lxcfs/proc/uptime\",\n" +
                                "          \"type\": \"\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      {\n" +
                                "        \"name\": \"localtime\",\n" +
                                "        \"hostPath\": {\n" +
                                "          \"path\": \"/usr/share/zoneinfo/Asia/Shanghai\",\n" +
                                "          \"type\": \"\"\n" +
                                "        }\n" +
                                "      },\n" +
                                "      {\n" +
                                "        \"name\": \"default-token-5fh7q\",\n" +
                                "        \"secret\": {\n" +
                                "          \"secretName\": \"default-token-5fh7q\",\n" +
                                "          \"defaultMode\": 420\n" +
                                "        }\n" +
                                "      }\n" +
                                "    ],\n" +
                                "    \"containers\": [\n" +
                                "      {\n" +
                                "        \"name\": \"hello-test-com\",\n" +
                                "        \"image\": \"hello.test.com:0.0.7_19\",\n" +
                                "        \"env\": [\n" +
                                "          {\n" +
                                "            \"name\": \"APP_ID\",\n" +
                                "            \"value\": \"10000001234\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"APP_NAME\",\n" +
                                "            \"value\": \"hello.test.com\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"INSTANCE_NAME\",\n" +
                                "            \"value\": \"ecs-zhang\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"ENV\",\n" +
                                "            \"value\": \"fat\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"TZ\",\n" +
                                "            \"value\": \"Asia/Shanghai\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"LANG\",\n" +
                                "            \"value\": \"en_US.UTF-8\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"LANGUAGE\",\n" +
                                "            \"value\": \"en_US:en\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"LC_ALL\",\n" +
                                "            \"value\": \"en_US.UTF-8\"\n" +
                                "          }\n" +
                                "        ],\n" +
                                "        \"resources\": {\n" +
                                "          \"limits\": {\n" +
                                "            \"cpu\": \"2\",\n" +
                                "            \"memory\": \"4Gi\"\n" +
                                "          },\n" +
                                "          \"requests\": {\n" +
                                "            \"cpu\": \"2\",\n" +
                                "            \"memory\": \"4Gi\"\n" +
                                "          }\n" +
                                "        },\n" +
                                "        \"volumeMounts\": [\n" +
                                "          {\n" +
                                "            \"name\": \"cpuinfo\",\n" +
                                "            \"mountPath\": \"/proc/cpuinfo\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"diskstats\",\n" +
                                "            \"mountPath\": \"/proc/diskstats\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"meminfo\",\n" +
                                "            \"mountPath\": \"/proc/meminfo\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"stat\",\n" +
                                "            \"mountPath\": \"/proc/stat\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"swaps\",\n" +
                                "            \"mountPath\": \"/proc/swaps\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"uptime\",\n" +
                                "            \"mountPath\": \"/proc/uptime\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"localtime\",\n" +
                                "            \"mountPath\": \"/etc/localtime\"\n" +
                                "          },\n" +
                                "          {\n" +
                                "            \"name\": \"default-token-5fh7q\",\n" +
                                "            \"readOnly\": true,\n" +
                                "            \"mountPath\": \"/var/run/secrets/kubernetes.io/serviceaccount\"\n" +
                                "          }\n" +
                                "        ],\n" +
                                "        \"readinessProbe\": {\n" +
                                "          \"httpGet\": {\n" +
                                "            \"path\": \"/hs\",\n" +
                                "            \"port\": 8080,\n" +
                                "            \"scheme\": \"HTTP\"\n" +
                                "          },\n" +
                                "          \"initialDelaySeconds\": 60,\n" +
                                "          \"timeoutSeconds\": 3,\n" +
                                "          \"periodSeconds\": 5,\n" +
                                "          \"successThreshold\": 3,\n" +
                                "          \"failureThreshold\": 12\n" +
                                "        },\n" +
                                "        \"terminationMessagePath\": \"/dev/termination-log\",\n" +
                                "        \"terminationMessagePolicy\": \"File\",\n" +
                                "        \"imagePullPolicy\": \"IfNotPresent\"\n" +
                                "      }\n" +
                                "    ],\n" +
                                "    \"restartPolicy\": \"Always\",\n" +
                                "    \"terminationGracePeriodSeconds\": 30,\n" +
                                "    \"dnsPolicy\": \"Default\",\n" +
                                "    \"serviceAccountName\": \"default\",\n" +
                                "    \"serviceAccount\": \"default\",\n" +
                                "    \"securityContext\": {\n" +
                                "      \n" +
                                "    },\n" +
                                "    \"imagePullSecrets\": [\n" +
                                "      {\n" +
                                "        \"name\": \"dockeryardkey\"\n" +
                                "      }\n" +
                                "    ],\n" +
                                "    \"hostname\": \"ecs-zhang\",\n" +
                                "    \"schedulerName\": \"default-scheduler\",\n" +
                                "    \"hostAliases\": [\n" +
                                "      {\n" +
                                "        \"ip\": \"127.0.0.1\",\n" +
                                "        \"hostnames\": [\n" +
                                "          \"localhost.localdomain\",\n" +
                                "          \"localhost4\",\n" +
                                "          \"localhost4.localdomain4\"\n" +
                                "        ]\n" +
                                "      },\n" +
                                "      {\n" +
                                "        \"ip\": \"::1\",\n" +
                                "        \"hostnames\": [\n" +
                                "          \"localhost.localdomain\",\n" +
                                "          \"localhost6\",\n" +
                                "          \"localhost6.localdomain6\"\n" +
                                "        ]\n" +
                                "      }\n" +
                                "    ],\n" +
                                "    \"priorityClassName\": \"medium\"\n" +
                                "  },\n" +
                                "  \"status\": {\n" +
                                "    \"phase\": \"Pending\",\n" +
                                "    \"qosClass\": \"Guaranteed\"\n" +
                                "  }\n" +
                                "}");
                    case "/api/v1/namespaces/default-develop/pods?includeUninitialized=false&pretty=false&labelSelector=instance%3Decs-zhang&limit=9999&timeoutSeconds=-1&watch=false":
                        return new MockResponse().setResponseCode(200).setBody("{\"kind\":\"PodList\",\"apiVersion\":\"v1\",\"metadata\":{\"selfLink\":\"/api/v1/namespaces/default-develop/pods\",\"resourceVersion\":\"70080179\"},\"items\":[{\"metadata\":{\"name\":\"ecs-zhang\",\"namespace\":\"default-develop\",\"selfLink\":\"/api/v1/namespaces/default-develop/pods/ecs-zhang\",\"uid\":\"947ae6b7-968d-11ea-b786-fa120a903300\",\"resourceVersion\":\"70080179\",\"creationTimestamp\":\"2020-05-15T09:22:21Z\",\"labels\":{\"app\":\"hello.test.com\",\"appid\":\"10000001234\",\"instance\":\"ecs-zhang\",\"ip\":\"10.254.130.18\"}},\"spec\":{\"volumes\":[{\"name\":\"cpuinfo\",\"hostPath\":{\"path\":\"/var/lib/lxcfs/proc/cpuinfo\",\"type\":\"\"}},{\"name\":\"diskstats\",\"hostPath\":{\"path\":\"/var/lib/lxcfs/proc/diskstats\",\"type\":\"\"}},{\"name\":\"meminfo\",\"hostPath\":{\"path\":\"/var/lib/lxcfs/proc/meminfo\",\"type\":\"\"}},{\"name\":\"stat\",\"hostPath\":{\"path\":\"/var/lib/lxcfs/proc/stat\",\"type\":\"\"}},{\"name\":\"swaps\",\"hostPath\":{\"path\":\"/var/lib/lxcfs/proc/swaps\",\"type\":\"\"}},{\"name\":\"uptime\",\"hostPath\":{\"path\":\"/var/lib/lxcfs/proc/uptime\",\"type\":\"\"}},{\"name\":\"localtime\",\"hostPath\":{\"path\":\"/usr/share/zoneinfo/Asia/Shanghai\",\"type\":\"\"}},{\"name\":\"default-token-5fh7q\",\"secret\":{\"secretName\":\"default-token-5fh7q\",\"defaultMode\":420}}],\"containers\":[{\"name\":\"hello-test-com\",\"image\":\"hello.test.com:0.0.7_19\",\"env\":[{\"name\":\"APP_ID\",\"value\":\"10000001234\"},{\"name\":\"APP_NAME\",\"value\":\"hello.test.com\"},{\"name\":\"INSTANCE_NAME\",\"value\":\"ecs-zhang\"},{\"name\":\"ENV\",\"value\":\"fat\"},{\"name\":\"TZ\",\"value\":\"Asia/Shanghai\"},{\"name\":\"LANG\",\"value\":\"en_US.UTF-8\"},{\"name\":\"LANGUAGE\",\"value\":\"en_US:en\"},{\"name\":\"LC_ALL\",\"value\":\"en_US.UTF-8\"}],\"resources\":{\"limits\":{\"cpu\":\"2\",\"memory\":\"4Gi\"},\"requests\":{\"cpu\":\"2\",\"memory\":\"4Gi\"}},\"volumeMounts\":[{\"name\":\"cpuinfo\",\"mountPath\":\"/proc/cpuinfo\"},{\"name\":\"diskstats\",\"mountPath\":\"/proc/diskstats\"},{\"name\":\"meminfo\",\"mountPath\":\"/proc/meminfo\"},{\"name\":\"stat\",\"mountPath\":\"/proc/stat\"},{\"name\":\"swaps\",\"mountPath\":\"/proc/swaps\"},{\"name\":\"uptime\",\"mountPath\":\"/proc/uptime\"},{\"name\":\"localtime\",\"mountPath\":\"/etc/localtime\"},{\"name\":\"default-token-5fh7q\",\"readOnly\":true,\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\"}],\"readinessProbe\":{\"httpGet\":{\"path\":\"/hs\",\"port\":8080,\"scheme\":\"HTTP\"},\"initialDelaySeconds\":60,\"timeoutSeconds\":3,\"periodSeconds\":5,\"successThreshold\":3,\"failureThreshold\":12},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"IfNotPresent\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"Default\",\"serviceAccountName\":\"default\",\"serviceAccount\":\"default\",\"nodeName\":\"10.113.2.31\",\"securityContext\":{},\"imagePullSecrets\":[{\"name\":\"dockeryardkey\"}],\"hostname\":\"ecs-zhang\",\"schedulerName\":\"default-scheduler\",\"hostAliases\":[{\"ip\":\"127.0.0.1\",\"hostnames\":[\"localhost.localdomain\",\"localhost4\",\"localhost4.localdomain4\"]},{\"ip\":\"::1\",\"hostnames\":[\"localhost.localdomain\",\"localhost6\",\"localhost6.localdomain6\"]}],\"priorityClassName\":\"medium\"},\"status\":{\"phase\":\"Pending\",\"conditions\":[{\"type\":\"Initialized\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2020-05-15T09:22:21Z\"},{\"type\":\"Ready\",\"status\":\"False\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2020-05-15T09:22:21Z\",\"reason\":\"ContainersNotReady\",\"message\":\"containers with unready status: [hello-test-com]\"},{\"type\":\"ContainersReady\",\"status\":\"False\",\"lastProbeTime\":null,\"lastTransitionTime\":null,\"reason\":\"ContainersNotReady\",\"message\":\"containers with unready status: [hademo-ppdapi-com]\"},{\"type\":\"PodScheduled\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2020-05-15T09:22:21Z\"}],\"hostIP\":\"10.113.2.31\",\"startTime\":\"2020-05-15T09:22:21Z\",\"containerStatuses\":[{\"name\":\"hademo-ppdapi-com\",\"state\":{\"waiting\":{\"reason\":\"ContainerCreating\"}},\"lastState\":{},\"ready\":false,\"restartCount\":0,\"image\":\"hello.test.com:0.0.7_19\",\"imageID\":\"\"}],\"qosClass\":\"Guaranteed\"}}]}");
                    case "/api/v1/namespaces/default-develop/pods/ecs-zhang?pretty=false&gracePeriodSeconds=0&orphanDependents=false&propagationPolicy=Background":
                        return new MockResponse().setResponseCode(200).setBody("{\"kind\":\"Pod\",\"apiVersion\":\"v1\",\"metadata\":{\"name\":\"ecs-zhang\",\"namespace\":\"default-develop\",\"selfLink\":\"/api/v1/namespaces/default-develop/pods/ecs-zhang\",\"uid\":\"947ae6b7-968d-11ea-b786-fa120a903300\",\"resourceVersion\":\"70080212\",\"creationTimestamp\":\"2020-05-15T09:22:21Z\",\"deletionTimestamp\":\"2020-05-15T09:23:11Z\",\"deletionGracePeriodSeconds\":30,\"labels\":{\"app\":\"hello.test.com\",\"appid\":\"10000001234\",\"instance\":\"ecs-zhang\",\"ip\":\"10.254.130.18\"}},\"spec\":{\"volumes\":[{\"name\":\"cpuinfo\",\"hostPath\":{\"path\":\"/var/lib/lxcfs/proc/cpuinfo\",\"type\":\"\"}},{\"name\":\"diskstats\",\"hostPath\":{\"path\":\"/var/lib/lxcfs/proc/diskstats\",\"type\":\"\"}},{\"name\":\"meminfo\",\"hostPath\":{\"path\":\"/var/lib/lxcfs/proc/meminfo\",\"type\":\"\"}},{\"name\":\"stat\",\"hostPath\":{\"path\":\"/var/lib/lxcfs/proc/stat\",\"type\":\"\"}},{\"name\":\"swaps\",\"hostPath\":{\"path\":\"/var/lib/lxcfs/proc/swaps\",\"type\":\"\"}},{\"name\":\"uptime\",\"hostPath\":{\"path\":\"/var/lib/lxcfs/proc/uptime\",\"type\":\"\"}},{\"name\":\"localtime\",\"hostPath\":{\"path\":\"/usr/share/zoneinfo/Asia/Shanghai\",\"type\":\"\"}},{\"name\":\"default-token-5fh7q\",\"secret\":{\"secretName\":\"default-token-5fh7q\",\"defaultMode\":420}}],\"containers\":[{\"name\":\"hello-test-com\",\"image\":\"hello.test.com:0.0.7_19\",\"env\":[{\"name\":\"APP_ID\",\"value\":\"10000001234\"},{\"name\":\"APP_NAME\",\"value\":\"hello.test.com\"},{\"name\":\"INSTANCE_NAME\",\"value\":\"ecs-zhang\"},{\"name\":\"ENV\",\"value\":\"fat\"},{\"name\":\"TZ\",\"value\":\"Asia/Shanghai\"},{\"name\":\"LANG\",\"value\":\"en_US.UTF-8\"},{\"name\":\"LANGUAGE\",\"value\":\"en_US:en\"},{\"name\":\"LC_ALL\",\"value\":\"en_US.UTF-8\"}],\"resources\":{\"limits\":{\"cpu\":\"2\",\"memory\":\"4Gi\"},\"requests\":{\"cpu\":\"2\",\"memory\":\"4Gi\"}},\"volumeMounts\":[{\"name\":\"cpuinfo\",\"mountPath\":\"/proc/cpuinfo\"},{\"name\":\"diskstats\",\"mountPath\":\"/proc/diskstats\"},{\"name\":\"meminfo\",\"mountPath\":\"/proc/meminfo\"},{\"name\":\"stat\",\"mountPath\":\"/proc/stat\"},{\"name\":\"swaps\",\"mountPath\":\"/proc/swaps\"},{\"name\":\"uptime\",\"mountPath\":\"/proc/uptime\"},{\"name\":\"localtime\",\"mountPath\":\"/etc/localtime\"},{\"name\":\"default-token-5fh7q\",\"readOnly\":true,\"mountPath\":\"/var/run/secrets/kubernetes.io/serviceaccount\"}],\"readinessProbe\":{\"httpGet\":{\"path\":\"/hs\",\"port\":8080,\"scheme\":\"HTTP\"},\"initialDelaySeconds\":60,\"timeoutSeconds\":3,\"periodSeconds\":5,\"successThreshold\":3,\"failureThreshold\":12},\"terminationMessagePath\":\"/dev/termination-log\",\"terminationMessagePolicy\":\"File\",\"imagePullPolicy\":\"IfNotPresent\"}],\"restartPolicy\":\"Always\",\"terminationGracePeriodSeconds\":30,\"dnsPolicy\":\"Default\",\"serviceAccountName\":\"default\",\"serviceAccount\":\"default\",\"nodeName\":\"10.113.2.31\",\"securityContext\":{},\"imagePullSecrets\":[{\"name\":\"dockeryardkey\"}],\"hostname\":\"ecs-zhang\",\"schedulerName\":\"default-scheduler\",\"hostAliases\":[{\"ip\":\"127.0.0.1\",\"hostnames\":[\"localhost.localdomain\",\"localhost4\",\"localhost4.localdomain4\"]},{\"ip\":\"::1\",\"hostnames\":[\"localhost.localdomain\",\"localhost6\",\"localhost6.localdomain6\"]}],\"priorityClassName\":\"medium\"},\"status\":{\"phase\":\"Pending\",\"conditions\":[{\"type\":\"Initialized\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2020-05-15T09:22:21Z\"},{\"type\":\"Ready\",\"status\":\"False\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2020-05-15T09:22:21Z\",\"reason\":\"ContainersNotReady\",\"message\":\"containers with unready status: [hademo-ppdapi-com]\"},{\"type\":\"ContainersReady\",\"status\":\"False\",\"lastProbeTime\":null,\"lastTransitionTime\":null,\"reason\":\"ContainersNotReady\",\"message\":\"containers with unready status: [hademo-ppdapi-com]\"},{\"type\":\"PodScheduled\",\"status\":\"True\",\"lastProbeTime\":null,\"lastTransitionTime\":\"2020-05-15T09:22:21Z\"}],\"hostIP\":\"10.113.2.31\",\"startTime\":\"2020-05-15T09:22:21Z\",\"containerStatuses\":[{\"name\":\"hademo-ppdapi-com\",\"state\":{\"waiting\":{\"reason\":\"ContainerCreating\"}},\"lastState\":{},\"ready\":false,\"restartCount\":0,\"image\":\"hello.test.com:0.0.7_19\",\"imageID\":\"\"}],\"qosClass\":\"Guaranteed\"}}");
                }
                return new MockResponse().setResponseCode(404);
            }
        };

        mockServer.setDispatcher(dispatcher);
        mockServer.start(8080);
    }

    public void shutdown() throws IOException {
        mockServer.shutdown();
    }
}
