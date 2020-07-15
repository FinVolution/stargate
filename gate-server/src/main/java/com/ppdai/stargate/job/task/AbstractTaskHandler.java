package com.ppdai.stargate.job.task;

import com.ppdai.stargate.constant.TaskStatus;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.job.IHandler;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.manager.TaskManager;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.AppService;
import com.ppdai.stargate.service.EnvService;
import com.ppdai.stargate.service.InstanceService;
import com.ppdai.stargate.vo.EnvVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class AbstractTaskHandler implements IHandler<TaskInfo> {

    @Autowired
    private TaskManager taskManager;

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private AppService appService;

    @Autowired
    private EnvService envService;

    public InstanceEntity getInstance(TaskInfo taskInfo) {
        JobInfo jobInfo = taskInfo.getJobInfo();
        InstanceEntity instanceEntity = instanceService.findById(taskInfo.getInstanceId());
        if (instanceEntity == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定的实例不存在, instanceName = %s", jobInfo.getName());
            log.error(getName() + ": " + ex.getMessage());
            throw ex;
        }

        return instanceEntity;
    }

    public ApplicationEntity getApplication(String appId) {
        ApplicationEntity applicationEntity = appService.getAppByCmdbId(appId);
        if (applicationEntity == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定的应用不存在, appId = %s", appId);
            log.error(getName() + ": " + ex.getMessage());
            throw ex;
        }

        return applicationEntity;
    }

    public String getAppDomain(ApplicationEntity applicationEntity, String env) {
        return appService.getAppDomainByEnv(applicationEntity, env);
    }

    public EnvVO getEnv(String env) {
        EnvVO envVO = envService.queryInUseEnvironment(env);
        if (envVO == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "环境[%s]不存在或未激活", env);
            log.error(getName() + ": " + ex.getMessage());
            throw ex;
        } else if (StringUtils.isEmpty(envVO.getDockeryard())) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "环境[%s]的镜像仓库地址为空", env);
            log.error(getName() + ": " + ex.getMessage());
            throw ex;
        }

        return envVO;
    }

    @Override
    public void onSuccess(TaskInfo taskInfo) {
        taskInfo.setStatus(TaskStatus.SUCCESS);
        taskManager.updateTask(taskInfo);
    }

    @Override
    public void onFail(TaskInfo taskInfo) {
        taskInfo.setStatus(TaskStatus.FAIL);
        taskManager.updateTask(taskInfo);
    }

    @Override
    public void onExpire(TaskInfo taskInfo) {
        taskInfo.setStatus(TaskStatus.EXPIRED);
        taskManager.updateTask(taskInfo);
    }

    @Override
    public void onInterrupt(TaskInfo taskInfo) {
        taskInfo.setStatus(TaskStatus.FAIL);
        taskManager.updateTask(taskInfo);
    }
}
