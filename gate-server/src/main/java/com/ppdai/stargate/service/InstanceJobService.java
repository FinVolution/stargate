package com.ppdai.stargate.service;

import com.google.common.collect.Maps;
import com.ppdai.stargate.constant.*;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.manager.JobManager;
import com.ppdai.stargate.manager.TaskManager;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.remote.RemoteRegistry;
import com.ppdai.stargate.remote.RemoteRegistryManager;
import com.ppdai.stargate.vo.InstanceV2VO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class InstanceJobService {

    @Autowired
    private JobManager jobManager;

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private AppService appService;

    @Autowired
    private RemoteRegistryManager remoteRegistryManager;

    @Autowired
    private Environment environment;

    /**
     * 添加部署实例任务
     * @param instanceEntity
     * @return
     */
    public JobInfo addDeployJob(InstanceEntity instanceEntity) {
        Map<String, Object> data = Maps.newHashMap();
        data.put("image", instanceEntity.getImage());

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(instanceEntity.getName());
        jobInfo.setEnv(instanceEntity.getEnv());
        jobInfo.setGroupId(instanceEntity.getGroupId());
        jobInfo.setAppId(instanceEntity.getAppId());
        jobInfo.setAppName(instanceEntity.getAppName());
        jobInfo.setOperationType(OperatorType.DEPLOY_INSTANCE.name());
        jobInfo.setDataMap(data);

        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;

        Map<String, Object> taskData = Maps.newHashMap();

        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.DEPLOY_ONE, instanceEntity.getId(),
                Integer.parseInt(environment.getProperty("stargate.galaxy.deployTimeout", "60000")), taskData));
        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.REGISTRY_ONE, instanceEntity.getId()));

        return jobManager.createJobWithTasks(jobInfo);
    }

    /**
     * 添加销毁实例任务
     * @param instanceEntity
     * @return
     */
    public JobInfo addDestroyJob(InstanceEntity instanceEntity) {
        if (taskManager.hasInProcessTasksByInstance(instanceEntity.getId())) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "当前实例[%s]有任务正在执行中，请等待片刻后重新执行。",
                    instanceEntity.getName());
            log.error("<<addDestroyJob>> " + ex.getMessage());
            throw ex;
        }

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(instanceEntity.getName());
        jobInfo.setEnv(instanceEntity.getEnv());
        jobInfo.setGroupId(instanceEntity.getGroupId());
        jobInfo.setAppId(instanceEntity.getAppId());
        jobInfo.setAppName(instanceEntity.getAppName());
        jobInfo.setOperationType(OperatorType.DESTROY_INSTANCE.name());

        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;

        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.DEREGISTRY_ONE, instanceEntity.getId()));
        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.DESTROY_ONE, instanceEntity.getId(),
                Integer.parseInt(environment.getProperty("stargate.galaxy.deployTimeout", "60000"))));


        return jobManager.createJobWithTasks(jobInfo);
    }

    /**
     * 添加拉入流量任务
     * @param instanceEntity
     * @return
     */
    public JobInfo addUpJob(InstanceEntity instanceEntity) {
        if (taskManager.hasInProcessTasksByInstance(instanceEntity.getId())) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "当前实例[%s]有任务正在执行中，请等待片刻后重新执行。",
                    instanceEntity.getName());
            log.error("<<addUpJob>> " + ex.getMessage());
            throw ex;
        }

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(instanceEntity.getName());
        jobInfo.setEnv(instanceEntity.getEnv());
        jobInfo.setGroupId(instanceEntity.getGroupId());
        jobInfo.setAppId(instanceEntity.getAppId());
        jobInfo.setAppName(instanceEntity.getAppName());
        jobInfo.setOperationType(OperatorType.UP_INSTANCE.name());

        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;

        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.UP_ONE, instanceEntity.getId()));

        return jobManager.createJobWithTasks(jobInfo);
    }

    /**
     * 添加拉出流量任务
     * @param instanceEntity
     * @return
     */
    public JobInfo addDownJob(InstanceEntity instanceEntity) {
        if (taskManager.hasInProcessTasksByInstance(instanceEntity.getId())) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "当前实例[%s]有任务正在执行中，请等待片刻后重新执行。",
                    instanceEntity.getName());
            log.error("<<addDownJob>> " + ex.getMessage());
            throw ex;
        }

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(instanceEntity.getName());
        jobInfo.setEnv(instanceEntity.getEnv());
        jobInfo.setGroupId(instanceEntity.getGroupId());
        jobInfo.setAppId(instanceEntity.getAppId());
        jobInfo.setAppName(instanceEntity.getAppName());
        jobInfo.setOperationType(OperatorType.DOWN_INSTANCE.name());

        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;

        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.DOWN_ONE, instanceEntity.getId()));

        return jobManager.createJobWithTasks(jobInfo);
    }

    /**
     * 添加更新实例任务
     * @param instanceEntity
     * @return
     */
    public JobInfo addUpdateJob(InstanceEntity instanceEntity, String image) {
        if (taskManager.hasInProcessTasksByInstance(instanceEntity.getId())) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "当前实例[%s]有任务正在执行中，请等待片刻后重新执行。",
                    instanceEntity.getName());
            log.error("<<addUpdateJob>> " + ex.getMessage());
            throw ex;
        }

        Map<String, Object> data = Maps.newHashMap();
        data.put("image", image);

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(instanceEntity.getName());
        jobInfo.setEnv(instanceEntity.getEnv());
        jobInfo.setGroupId(instanceEntity.getGroupId());
        jobInfo.setAppId(instanceEntity.getAppId());
        jobInfo.setAppName(instanceEntity.getAppName());
        jobInfo.setOperationType(OperatorType.UPDATE_INSTANCE.name());
        jobInfo.setDataMap(data);

        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;

        Map<String, Object> taskData = Maps.newHashMap();

        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.UPDATE_ONE, instanceEntity.getId(),
                Integer.parseInt(environment.getProperty("stargate.galaxy.deployTimeout", "60000")), taskData));

        return jobManager.createJobWithTasks(jobInfo);
    }

    /**
     * 添加重启实例任务
     * @param instanceEntity
     * @return
     */
    public JobInfo addRestartJob(InstanceEntity instanceEntity) {
        if (taskManager.hasInProcessTasksByInstance(instanceEntity.getId())) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "当前实例[%s]有任务正在执行中，请等待片刻后重新执行。",
                    instanceEntity.getName());
            log.error("<<addRestartJob>> " + ex.getMessage());
            throw ex;
        }

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(instanceEntity.getName());
        jobInfo.setEnv(instanceEntity.getEnv());
        jobInfo.setGroupId(instanceEntity.getGroupId());
        jobInfo.setAppId(instanceEntity.getAppId());
        jobInfo.setAppName(instanceEntity.getAppName());
        jobInfo.setOperationType(OperatorType.RESTART_INSTANCE.name());

        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;

        Map<String, Object> taskData = Maps.newHashMap();

        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.RESTART_ONE, instanceEntity.getId(),
                Integer.parseInt(environment.getProperty("stargate.galaxy.deployTimeout", "60000")), taskData));

        return jobManager.createJobWithTasks(jobInfo);
    }

    public JobInfo recoverInstance1Step(InstanceEntity badInstance) {

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(badInstance.getName());
        jobInfo.setEnv(badInstance.getEnv());
        jobInfo.setGroupId(badInstance.getGroupId());
        jobInfo.setAppId(badInstance.getAppId());
        jobInfo.setAppName(badInstance.getAppName());
        jobInfo.setOperationType(OperatorType.RECOVER_INSTANCE_FRONT.name());

        Map<String, Object> data = Maps.newHashMap();
        data.put("delete", "false");

        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;

        if (badInstance.getHasPulledIn()) {
            taskList.add(new TaskInfo(step++, JobTaskTypeEnum.DOWN_ONE, badInstance.getId()));
            taskList.add(new TaskInfo(step++, JobTaskTypeEnum.NOOP, badInstance.getId()));
        }

        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.DEREGISTRY_ONE, badInstance.getId()));
        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.DESTROY_ONE, badInstance.getId(),
                Integer.parseInt(environment.getProperty("stargate.galaxy.deployTimeout", "60000")), data));

        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.DEPLOY_ONE, badInstance.getId(),
                Integer.parseInt(environment.getProperty("stargate.galaxy.deployTimeout", "60000"))));
        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.REGISTRY_ONE, badInstance.getId()));

        return jobManager.createJobWithTasks(jobInfo);
    }

    public JobInfo recoverInstance2Step(InstanceEntity okInstance) {
        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(okInstance.getName());
        jobInfo.setEnv(okInstance.getEnv());
        jobInfo.setGroupId(okInstance.getGroupId());
        jobInfo.setAppId(okInstance.getAppId());
        jobInfo.setAppName(okInstance.getAppName());
        jobInfo.setOperationType(OperatorType.RECOVER_INSTANCE_REAR.name());

        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;

        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.UP_ONE, okInstance.getId()));

        return jobManager.createJobWithTasks(jobInfo);
    }

    public JobInfo addExecCommandJob(InstanceEntity instanceEntity, String command) {
        Map<String, Object> data = Maps.newHashMap();
        data.put("command", command);

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(instanceEntity.getName());
        jobInfo.setEnv(instanceEntity.getEnv());
        jobInfo.setGroupId(instanceEntity.getGroupId());
        jobInfo.setAppId(instanceEntity.getAppId());
        jobInfo.setAppName(instanceEntity.getAppName());
        jobInfo.setOperationType(OperatorType.EXEC_COMMAND.name());
        jobInfo.setDataMap(data);
        jobInfo.setStatus(JobStatus.RUNNING);
        return jobManager.createJobNoTasks(jobInfo);
    }

    public void checkPullInInstance(InstanceEntity instanceEntity) {

        String domain = appService.getAppDomainByEnv(instanceEntity.getAppId(), instanceEntity.getEnv());

        RemoteRegistry remoteRegistry = remoteRegistryManager.getRemoteRegistryByDomain(domain);

        InstanceV2VO instanceV2VO = remoteRegistry.getInstanceStatus(domain, instanceEntity.getEnv(),
                instanceEntity.getAppId(), instanceEntity.getAppName(), instanceEntity);

        if (instanceV2VO.getOpsPulledIn() != null && instanceV2VO.getOpsPulledIn() == true) {
            throw BaseException.newException(MessageType.ERROR, "操作实例前请将流量拉出, name=" + instanceEntity.getName());
        }
    }
}
