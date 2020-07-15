package com.ppdai.stargate.service;

import com.ppdai.stargate.constant.JobStatus;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.manager.JobManager;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.vo.ExecCommandResultVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ExecCommandService {

    @Autowired
    private InstanceService instanceService;

    @Autowired
    private ContainerService containerService;

    @Autowired
    private JobManager jobManager;

    @Autowired
    private InstanceJobService instanceJobService;

    public ExecCommandResultVO execCurl(String instance, String endpoint) {
        ExecCommandResultVO execCommandResultVO = new ExecCommandResultVO();

        String[] commands = new String[]{"sh", "-c", "curl -vv \"" + endpoint + "\""};

        JobInfo jobInfo = null;
        try {
            InstanceEntity instanceEntity = instanceService.findByName(instance);
            if (instanceEntity == null) {
                execCommandResultVO.setStdout("实例不存在, instance=" + instance);
                return execCommandResultVO;
            }


            jobInfo = instanceJobService.addExecCommandJob(instanceEntity, StringUtils.join(commands, " "));

            String output = containerService.execCommand(commands,
                    instanceEntity.getEnv(), instanceEntity.getZone(), instanceEntity.getNamespace(), instance);

            execCommandResultVO.setStdout(output);

        } catch (Throwable throwable) {
            log.error("failed to exec command, command=" + StringUtils.join(commands, " ")
                    + ", instance=" + instance + ", err=" + throwable.getMessage(), throwable);
            execCommandResultVO.setStdout(throwable.getMessage());
        }

        try {
            if (jobInfo != null) {
                jobInfo.setStatus(JobStatus.SUCCESS);
                jobManager.updateJob(jobInfo);
            }
        } catch (Throwable throwable) {
            log.error("failed to update job, jobId=" + jobInfo.getId() + ", err=" + throwable.getMessage(), throwable);
        }

        return execCommandResultVO;
    }

    public ExecCommandResultVO execPing(String instance, String endpoint) {
        ExecCommandResultVO execCommandResultVO = new ExecCommandResultVO();

        String[] commands = new String[]{"sh", "-c", "ping -c 4 \"" + endpoint + "\""};

        JobInfo jobInfo = null;
        try {
            InstanceEntity instanceEntity = instanceService.findByName(instance);
            if (instanceEntity == null) {
                execCommandResultVO.setStdout("实例不存在, instance=" + instance);
                return execCommandResultVO;
            }

            jobInfo = instanceJobService.addExecCommandJob(instanceEntity, StringUtils.join(commands, " "));

            String output = containerService.execCommand(commands,
                    instanceEntity.getEnv(), instanceEntity.getZone(), instanceEntity.getNamespace(), instance);

            execCommandResultVO.setStdout(output);

        } catch (Throwable throwable) {
            log.error("failed to exec command, command=" + StringUtils.join(commands, " ")
                    + ", instance=" + instance + ", err=" + throwable.getMessage(), throwable);
            execCommandResultVO.setStdout(throwable.getMessage());
        }

        try {
            if (jobInfo != null) {
                jobInfo.setStatus(JobStatus.SUCCESS);
                jobManager.updateJob(jobInfo);
            }
        } catch (Throwable throwable) {
            log.error("failed to update job, jobId=" + jobInfo.getId() + ", err=" + throwable.getMessage(), throwable);
        }

        return execCommandResultVO;
    }
}
