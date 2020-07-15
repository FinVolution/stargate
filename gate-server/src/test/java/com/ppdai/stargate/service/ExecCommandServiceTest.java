package com.ppdai.stargate.service;

import com.alibaba.fastjson.JSONObject;
import com.ppdai.stargate.constant.JobStatus;
import com.ppdai.stargate.dto.*;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.manager.JobManager;
import com.ppdai.stargate.service.cloud.CloudInstanceService;
import com.ppdai.stargate.vo.ExecCommandResultVO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest
@TestPropertySource(locations="classpath:test.properties")
public class ExecCommandServiceTest {

    @Autowired
    private CloudInstanceService cloudInstanceService;

    @Autowired
    private JobManager jobManager;

    @Autowired
    private ExecCommandService execCommandService;

    private void deploy(String name,
                        int port,
                        String appId,
                        String env,
                        String image,
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
    }

    private void destroy(String name) {
        // 销毁实例
        DestroyInstanceRequest destroyInstanceRequest = new DestroyInstanceRequest();
        destroyInstanceRequest.setName(name);
        DestroyInstanceResponse destroyInstanceResponse = cloudInstanceService.destroy(destroyInstanceRequest);
        assertEquals(0, destroyInstanceResponse.getCode());
        assertTrue(destroyInstanceResponse.getJobId() > 0);

        // 等待销毁实例任务完成
        Optional<JobInfo> jobInfoOptional =  jobManager.getJobInfoById(destroyInstanceResponse.getJobId());
        while (jobInfoOptional.get().getStatus().equals(JobStatus.NEW)
                || jobInfoOptional.get().getStatus().equals(JobStatus.RUNNING)) {
            jobInfoOptional =  jobManager.getJobInfoById(destroyInstanceResponse.getJobId());
            try {
                Thread.sleep(4000);
            } catch (InterruptedException e) {
            }
        }

        assertTrue(jobInfoOptional.get().getStatus().equals(JobStatus.SUCCESS));
    }

    @After
    public void destroyOne() {
        destroy("zhang-jc3a8gdm");
    }

    @Before
    public void createOne() throws IOException, InterruptedException {
        deploy("zhang-jc3a8gdm",
                6062,
                "9900000002",
                "fat",
                "hello.test.com:0.0.7_9",
                "C-2C4G",
                null,
                null,
                "default");
    }

    @Test
    public void testExecCurl() {
        ExecCommandResultVO execCommandResultVO = execCommandService.execCurl("zhang-jc3a8gdm", "127.0.0.1:80");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>result: " + execCommandResultVO.getStdout());
    }

    @Test
    public void testExecPing() {
        ExecCommandResultVO execCommandResultVO = execCommandService.execPing("zhang-jc3a8gdm", "127.0.0.1");
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>result: " + execCommandResultVO.getStdout());
    }
}
