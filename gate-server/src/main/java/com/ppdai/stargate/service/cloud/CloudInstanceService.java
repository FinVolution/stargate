package com.ppdai.stargate.service.cloud;

import com.ppdai.stargate.dto.*;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.manager.TaskManager;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.*;
import com.ppdai.stargate.utils.ConvertUtil;
import com.ppdai.stargate.vo.InstanceV2VO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class CloudInstanceService {

    @Autowired
    private AppService appService;

    @Autowired
    private ContainerService containerService;

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private InstanceJobService instanceJobService;

    @Autowired
    private TaskManager taskManager;

    private ExecutorService parallelQueryExecutor = Executors.newFixedThreadPool(50);

    /**
     * 更新实例版本
     * @param request
     * @return
     */
    public UpdateInstanceResponse update(UpdateInstanceRequest request) {
        try {
            if (StringUtils.isEmpty(request.getName())) {
                return new UpdateInstanceResponse(-1, "参数[name]为空");
            }

            if (StringUtils.isEmpty(request.getImage())) {
                return new UpdateInstanceResponse(-1, "参数[image]为空");
            }

            InstanceEntity instance = instanceService.findByName(request.getName());

            if (instance == null) {
                return new UpdateInstanceResponse(-1, "无法找到实例, name=" + request.getName());
            }

            if (instance.getImage().equals(request.getImage())) {
                return new UpdateInstanceResponse(-1, "更新实例不能选择原镜像, name=" + request.getName() + ", image=" + request.getImage());
            }

            if (taskManager.hasInProcessTasksByInstance(instance.getId())) {
                return new UpdateInstanceResponse(-1, "实例有任务正在处理中, 请稍后再试, name=" + request.getName());
            }

            instanceJobService.checkPullInInstance(instance);
            JobInfo jobInfo = instanceJobService.addUpdateJob(instance, request.getImage());

            UpdateInstanceResponse response = new UpdateInstanceResponse(0, "success");
            response.setJobId(jobInfo.getId());
            response.setInstance(ConvertUtil.convert(instance, InstanceV2VO.class));

            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new UpdateInstanceResponse(-1, ex.getMessage());
        }
    }

    /**
     * 实例拉入流量
     * @param request
     * @return
     */
    public UpInstanceResponse up(UpInstanceRequest request) {
        try {
            if (StringUtils.isEmpty(request.getName())) {
                return new UpInstanceResponse(-1, "参数[name]为空");
            }

            InstanceEntity instance = instanceService.findByName(request.getName());

            if (instance == null) {
                return new UpInstanceResponse(-1, "无法找到实例, name=" + request.getName());
            }

            InstanceV2VO instanceV2VO = instanceService.getInstanceStatus(instance);
            if (instanceV2VO.getReady() == null || instanceV2VO.getReady() == false) {
                return new UpInstanceResponse(-1, "上线前请保证实例健康检查Ready, name=" + request.getName());
            }

            if (taskManager.hasInProcessTasksByInstance(instance.getId())) {
                return new UpInstanceResponse(-1, "实例有任务正在处理中, 请稍后再试, name=" + request.getName());
            }

            JobInfo jobInfo = instanceJobService.addUpJob(instance);

            UpInstanceResponse response = new UpInstanceResponse(0, "success");
            response.setJobId(jobInfo.getId());
            response.setInstance(ConvertUtil.convert(instance, InstanceV2VO.class));

            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new UpInstanceResponse(-1, ex.getMessage());
        }
    }

    /**
     * 实例拉出流量
     * @param request
     * @return
     */
    public DownInstanceResponse down(DownInstanceRequest request) {
        try {
            if (StringUtils.isEmpty(request.getName())) {
                return new DownInstanceResponse(-1, "参数[name]为空");
            }

            InstanceEntity instance = instanceService.findByName(request.getName());

            if (instance == null) {
                return new DownInstanceResponse(-1, "无法找到实例, name=" + request.getName());
            }

            if (taskManager.hasInProcessTasksByInstance(instance.getId())) {
                return new DownInstanceResponse(-1, "实例有任务正在处理中, 请稍后再试, name=" + request.getName());
            }

            JobInfo jobInfo = instanceJobService.addDownJob(instance);

            DownInstanceResponse response = new DownInstanceResponse(0, "success");
            response.setJobId(jobInfo.getId());
            response.setInstance(ConvertUtil.convert(instance, InstanceV2VO.class));

            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new DownInstanceResponse(-1, ex.getMessage());
        }
    }

    /**
     * 重启实例
     * @param request
     * @return
     */
    public RestartInstanceResponse restart(RestartInstanceRequest request) {
        try {
            if (StringUtils.isEmpty(request.getName())) {
                return new RestartInstanceResponse(-1, "参数[name]为空");
            }

            InstanceEntity instance = instanceService.findByName(request.getName());

            if (instance == null) {
                return new RestartInstanceResponse(-1, "无法找到实例, name=" + request.getName());
            }

            if (taskManager.hasInProcessTasksByInstance(instance.getId())) {
                return new RestartInstanceResponse(-1, "实例有任务正在处理中, 请稍后再试, name=" + request.getName());
            }

            instanceJobService.checkPullInInstance(instance);
            JobInfo jobInfo = instanceJobService.addRestartJob(instance);

            RestartInstanceResponse response = new RestartInstanceResponse(0, "success");
            response.setJobId(jobInfo.getId());
            response.setInstance(ConvertUtil.convert(instance, InstanceV2VO.class));

            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new RestartInstanceResponse(-1, ex.getMessage());
        }
    }

    /**
     * 部署实例
     * @param request
     * @return
     */
    public DeployInstanceResponse deploy( DeployInstanceRequest request){
        try{
            if (StringUtils.isEmpty(request.getName())) {
                return new DeployInstanceResponse(-1, "参数[name]为空");
            }
            if (StringUtils.isEmpty(request.getEnv())) {
                return new DeployInstanceResponse(-1, "参数[env]为空");
            }
            if (StringUtils.isEmpty(request.getAppId())) {
                return new DeployInstanceResponse(-1, "参数[appId]为空");
            }
            if (request.getPort() == null) {
                return new DeployInstanceResponse(-1, "参数[port]为空");
            }
            if (StringUtils.isEmpty(request.getSpec())) {
                return new DeployInstanceResponse(-1, "参数[spec]为空");
            }
            if (StringUtils.isEmpty(request.getImage())) {
                return new DeployInstanceResponse(-1, "参数[image]为空");
            }
            if (StringUtils.isEmpty(request.getEnvVars())) {
                return new DeployInstanceResponse(-1, "参数[envVars]为空");
            }

            InstanceEntity instance = instanceService.findByNameEx(request.getName());
            if (instance != null && instance.getIsActive()) {
                return new DeployInstanceResponse(-1, "实例已存在, name=" + request.getName());
            }

            ApplicationEntity applicationEntity = appService.getAppByCmdbId(request.getAppId());
            if (applicationEntity == null) {
                return new DeployInstanceResponse(-1, "无法找到对应的应用, appId=" + request.getAppId());
            }

            if (instance == null) {
                instance = new InstanceEntity();
            }
            instance.setName(request.getName());
            instance.setEnv(request.getEnv());
            instance.setGroupId(request.getGroupId());
            instance.setAppId(request.getAppId());
            instance.setAppName(applicationEntity.getName());
            instance.setPort(request.getPort());
            instance.setImage(request.getImage());
            instance.setEnvVars(request.getEnvVars());
            instance.setZone(request.getZone());
            instance.setSpec(request.getSpec());
            instance.setHasPulledIn(false);
            instance.setFlags(0L);
            instance.setIsActive(true);
            instance.setSlotIp(request.getIp());
            if (StringUtils.isEmpty(request.getNamespace())) {
                instance.setNamespace("default-" + applicationEntity.getDepartmentCode());
            } else {
                instance.setNamespace(request.getNamespace());
            }

            instanceService.checkInstanceQuota(instance);

            instance = instanceService.saveInstance(instance);

            JobInfo jobInfo = instanceJobService.addDeployJob(instance);

            DeployInstanceResponse response = new DeployInstanceResponse(0, "success");
            response.setJobId(jobInfo.getId());
            response.setInstance(ConvertUtil.convert(instance, InstanceV2VO.class));
            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new DeployInstanceResponse(-1, ex.getMessage());
        }
    }

    /**
     * 销毁实例
     * @param request
     * @return
     */
    public DestroyInstanceResponse destroy(DestroyInstanceRequest request) {
        try {
            if (StringUtils.isEmpty(request.getName())) {
                return new DestroyInstanceResponse(-1, "参数[name]为空");
            }

            InstanceEntity instance = instanceService.findByName(request.getName());

            if (instance == null) {
                return new DestroyInstanceResponse(-1, "无法找到实例, name=" + request.getName());
            }

            if (taskManager.hasInProcessTasksByInstance(instance.getId())) {
                return new DestroyInstanceResponse(-1, "实例有任务正在处理中, 请稍后再试, name=" + request.getName());
            }

            instanceJobService.checkPullInInstance(instance);
            JobInfo jobInfo = instanceJobService.addDestroyJob(instance);

            DestroyInstanceResponse response = new DestroyInstanceResponse(0, "success");
            response.setJobId(jobInfo.getId());
            response.setInstance(ConvertUtil.convert(instance, InstanceV2VO.class));
            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new DestroyInstanceResponse(-1, ex.getMessage());
        }
    }

    /**
     * 查询实例
     * @param request
     * @return
     */
    public GetInstanceResponse get(GetInstanceRequest request) {
        if (StringUtils.isEmpty(request.getName())) {
            return new GetInstanceResponse(-1, "参数[name]为空");
        }

        List<InstanceV2VO> instanceV2VOList = new ArrayList<>();

        String[] instanceNames = request.getName().split(",");

        List<InstanceEntity> instanceEntities = new ArrayList<>();

        for (int i = 0; i < instanceNames.length; i++) {
            String instanceName = instanceNames[i].trim();
            InstanceEntity instance = instanceService.findByName(instanceName);

            if (instance != null) {
                instanceEntities.add(instance);
            }
        }

        CountDownLatch waitAllQuery = new CountDownLatch(instanceEntities.size());

        for (InstanceEntity instanceEntity : instanceEntities) {
            try {
                parallelQueryExecutor.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            InstanceV2VO instanceV2VO = instanceService.getInstanceStatus(instanceEntity);
                            synchronized (instanceV2VOList) {
                                instanceV2VOList.add(instanceV2VO);
                            }
                        } catch (Throwable throwable) {
                            log.error("查询私有云实例失败, instance=" + instanceEntity.getName() + ", err=" + throwable.getMessage(), throwable);
                        } finally {
                            waitAllQuery.countDown();
                        }
                    }
                });
            } catch (Throwable t) {
                waitAllQuery.countDown();
                log.error("添加查询私有云实例任务失败, instance=" + instanceEntity.getName() + ", err=" + t.getMessage(), t);
            }
        }

        try {
            waitAllQuery.await();
        } catch (InterruptedException e) {
        }

        GetInstanceResponse response = new GetInstanceResponse(0, "success");
        response.setInstances(instanceV2VOList);

        return response;
    }

    /**
     * 根据IP查询实例
     * @param request
     * @return
     */
    public QueryInstanceResponse query(QueryInstanceRequest request) {
        if (StringUtils.isEmpty(request.getIp())) {
            return new QueryInstanceResponse(-1, "参数[ip]为空");
        }

        InstanceEntity instanceEntity = instanceService.findByIp(request.getIp());

        if (instanceEntity == null) {
            return new QueryInstanceResponse(-1, "无法找到实例, ip=" + request.getIp());
        }

        InstanceV2VO instanceV2VO = instanceService.getInstanceStatus(instanceEntity);

        QueryInstanceResponse response = new QueryInstanceResponse(0, "success");
        response.setInstance(instanceV2VO);

        return response;
    }

    /**
     * 查询实例日志
     * @param request
     * @return
     */
    public GetInstanceLogResponse log(GetInstanceLogRequest request) {
        try {
            if (StringUtils.isEmpty(request.getName())) {
                return new GetInstanceLogResponse(-1, "参数[name]为空");
            }

            InstanceEntity instance = instanceService.findByName(request.getName());

            if (instance == null) {
                return new GetInstanceLogResponse(-1, "无法找到实例, name=" + request.getName());
            }

            String containerLog = containerService.getContainerLog(instance);

            GetInstanceLogResponse response = new GetInstanceLogResponse(0, "success");
            response.setLog(containerLog);

            return response;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            return new GetInstanceLogResponse(-1, ex.getMessage());
        }
    }
}

