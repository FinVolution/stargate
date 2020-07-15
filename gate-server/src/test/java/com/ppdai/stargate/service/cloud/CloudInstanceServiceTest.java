package com.ppdai.stargate.service.cloud;

import com.alibaba.fastjson.JSONObject;
import com.ppdai.stargate.constant.JobStatus;
import com.ppdai.stargate.dto.*;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.manager.JobManager;
import com.ppdai.stargate.vo.InstanceV2VO;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang.StringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations="classpath:test.properties")
public class CloudInstanceServiceTest {

    @Autowired
    private CloudInstanceService cloudInstanceService;

    @Autowired
    private JobManager jobManager;

    private void queryAndCheckInstance(String name, String image) throws InterruptedException, IOException {
        OkHttpClient httpClient = new OkHttpClient();
        // 查询实例
        GetInstanceRequest getInstanceRequest = new GetInstanceRequest();
        getInstanceRequest.setName(name);

        GetInstanceResponse getInstanceResponse = cloudInstanceService.get(getInstanceRequest);
        assertEquals(0, getInstanceResponse.getCode());
        assertTrue(getInstanceResponse.getInstances().size() > 0);

        InstanceV2VO instanceV2VO = getInstanceResponse.getInstances().get(0);
        assertNotNull(instanceV2VO.getInstanceIp());
        assertEquals(image, instanceV2VO.getImage().replaceAll("-restart", ""));

        // 等待实例ready
        long now = System.currentTimeMillis();
        while (!instanceV2VO.getReady()) {
            Thread.sleep(4000);
            getInstanceResponse = cloudInstanceService.get(getInstanceRequest);
            instanceV2VO = getInstanceResponse.getInstances().get(0);
            assertTrue((System.currentTimeMillis() - now) < 120000);
        }

        // 调用实例健康检测
        Request request = new Request.Builder()
                .url("http://" + instanceV2VO.getInstanceIp() + ":" + instanceV2VO.getPort() + "/hs")
                .build();

        Response response = httpClient.newCall(request).execute();
        assertEquals(response.code(), 200);

        GetInstanceLogRequest getInstanceLogRequest = new GetInstanceLogRequest();
        getInstanceLogRequest.setName(name);
        GetInstanceLogResponse getInstanceLogResponse =  cloudInstanceService.log(getInstanceLogRequest);
        assertEquals(0, getInstanceLogResponse.getCode());
        assertTrue(StringUtils.isNotEmpty(getInstanceLogResponse.getLog()));
        assertTrue(getInstanceLogResponse.getLog().length() > 1024);
    }

    private void deployUpdateRestartDestroy(String name,
                                            int port,
                                            String appId,
                                            String env,
                                            String image,
                                            String newImage,
                                            String spec,
                                            String zone,
                                            String ip,
                                            String apollo) throws IOException, InterruptedException {
        // 创建实例
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("ci_located_code", apollo);
        String envVars = jsonObject.toJSONString();

        DeployInstanceRequest deployInstanceRequest = new DeployInstanceRequest();
        deployInstanceRequest.setName(name);
        deployInstanceRequest.setAppId(appId);
        deployInstanceRequest.setEnv(env);
        deployInstanceRequest.setImage(image);
        deployInstanceRequest.setPort(port);
        deployInstanceRequest.setSpec(spec);
        deployInstanceRequest.setZone(zone);
        deployInstanceRequest.setIp(ip);
        deployInstanceRequest.setEnvVars(envVars);

        DeployInstanceResponse deployInstanceResponse = cloudInstanceService.deploy(deployInstanceRequest);
        assertEquals(0, deployInstanceResponse.getCode());
        assertTrue(deployInstanceResponse.getJobId() > 0);

        // 等待创建实例任务完成
        Optional<JobInfo> jobInfoOptional =  jobManager.getJobInfoById(deployInstanceResponse.getJobId());
        assertTrue(jobInfoOptional.isPresent());

        while (jobInfoOptional.get().getStatus().equals(JobStatus.NEW)
                || jobInfoOptional.get().getStatus().equals(JobStatus.RUNNING)) {
            Thread.sleep(4000);
            jobInfoOptional =  jobManager.getJobInfoById(deployInstanceResponse.getJobId());
        }

        assertTrue(jobInfoOptional.get().getStatus().equals(JobStatus.SUCCESS));

        // 查询实例
        queryAndCheckInstance(deployInstanceRequest.getName(), deployInstanceRequest.getImage());

        // 更新实例版本
        UpdateInstanceRequest updateInstanceRequest = new UpdateInstanceRequest();
        updateInstanceRequest.setName(deployInstanceRequest.getName());
        updateInstanceRequest.setImage(newImage);
        UpdateInstanceResponse updateInstanceResponse = cloudInstanceService.update(updateInstanceRequest);
        assertEquals(0, updateInstanceResponse.getCode());
        assertTrue(updateInstanceResponse.getJobId() > 0);

        // 等待更新实例任务完成
        jobInfoOptional =  jobManager.getJobInfoById(updateInstanceResponse.getJobId());
        while (jobInfoOptional.get().getStatus().equals(JobStatus.NEW)
                || jobInfoOptional.get().getStatus().equals(JobStatus.RUNNING)) {
            jobInfoOptional =  jobManager.getJobInfoById(updateInstanceResponse.getJobId());
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
        }

        assertTrue(jobInfoOptional.get().getStatus().equals(JobStatus.SUCCESS));

        // 查询实例
        queryAndCheckInstance(updateInstanceRequest.getName(), updateInstanceRequest.getImage());

        // 重启实例
        RestartInstanceRequest restartInstanceRequest = new RestartInstanceRequest();
        restartInstanceRequest.setName(deployInstanceRequest.getName());
        RestartInstanceResponse restartInstanceResponse = cloudInstanceService.restart(restartInstanceRequest);
        assertEquals(0, restartInstanceResponse.getCode());
        assertTrue(restartInstanceResponse.getJobId() > 0);

        // 等待更新实例任务完成
        jobInfoOptional =  jobManager.getJobInfoById(restartInstanceResponse.getJobId());
        while (jobInfoOptional.get().getStatus().equals(JobStatus.NEW)
                || jobInfoOptional.get().getStatus().equals(JobStatus.RUNNING)) {
            jobInfoOptional =  jobManager.getJobInfoById(restartInstanceResponse.getJobId());
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
        }

        assertTrue(jobInfoOptional.get().getStatus().equals(JobStatus.SUCCESS));

        // 查询实例
        queryAndCheckInstance(updateInstanceRequest.getName(), updateInstanceRequest.getImage());

        // 销毁实例
        DestroyInstanceRequest destroyInstanceRequest = new DestroyInstanceRequest();
        destroyInstanceRequest.setName(deployInstanceRequest.getName());
        DestroyInstanceResponse destroyInstanceResponse = cloudInstanceService.destroy(destroyInstanceRequest);
        assertEquals(0, destroyInstanceResponse.getCode());
        assertTrue(destroyInstanceResponse.getJobId() > 0);

        // 等待销毁实例任务完成
        jobInfoOptional =  jobManager.getJobInfoById(destroyInstanceResponse.getJobId());
        while (jobInfoOptional.get().getStatus().equals(JobStatus.NEW)
                || jobInfoOptional.get().getStatus().equals(JobStatus.RUNNING)) {
            jobInfoOptional =  jobManager.getJobInfoById(destroyInstanceResponse.getJobId());
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
        }

        assertTrue(jobInfoOptional.get().getStatus().equals(JobStatus.SUCCESS));

        // 再次查询实例
        GetInstanceRequest getInstanceRequest = new GetInstanceRequest();
        getInstanceRequest.setName(deployInstanceRequest.getName());
        GetInstanceResponse getInstanceResponse = cloudInstanceService.get(getInstanceRequest);
        assertEquals(0, getInstanceResponse.getCode());
    }

    /**
     * 测试部署、更新、重启和销毁
     * 测试自动分配zone, 分配IP
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testDeployOneSimpleCase() throws IOException, InterruptedException {
        deployUpdateRestartDestroy("zhang-jc3a8gdm",
                6062,
                "9900000002",
                "fat",
                "hello.test.com:0.0.7_9",
                "hello.test.com:0.0.7_6",
                "C-2C4G",
                null,
                null,
                "default");
    }

    /**
     * 测试部署、更新、重启和销毁
     * 测试指定zone和IP
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testDeployOneStaticResourceCase() throws IOException, InterruptedException {
        deployUpdateRestartDestroy("yin-jc3a8gdm",
                8080,
                "1000003574",
                "fat",
                "hello.test.com:1.0-SNAPSHOT_1",
                "hello.test.com:1.0-SNAPSHOT_2",
                "B-2C2G",
                "万国",
                "10.254.145.4",
                "default");
        deployUpdateRestartDestroy("yin-jc3a8gdn",
                8080,
                "1000003574",
                "fat",
                "hello.test.com:1.0-SNAPSHOT_1",
                "hello.test.com:1.0-SNAPSHOT_2",
                "B-2C2G",
                "万国2",
                "10.254.145.6",
                "default");
    }

    /**
     * 测试部署、更新、重启和销毁
     * 测试指定zone, 但不指定IP
     * @throws IOException
     * @throws InterruptedException
     */
    @Test
    public void testDeployOneForZoneNotStaticResourceCase() throws IOException, InterruptedException {
        deployUpdateRestartDestroy("yin-jc3a8gdm",
                8080,
                "1000003574",
                "fat",
                "hello.test.com:1.0-SNAPSHOT_1",
                "hello.test.com:1.0-SNAPSHOT_2",
                "B-2C2G",
                "万国",
                null,
                "default");
        deployUpdateRestartDestroy("yin-jc3a8gdn",
                8080,
                "1000003574",
                "fat",
                "hello.test.com:1.0-SNAPSHOT_1",
                "hello.test.com:1.0-SNAPSHOT_2",
                "B-2C2G",
                "万国2",
                null,
                "default");
    }

}
