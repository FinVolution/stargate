package com.ppdai.stargate.service.flink;

import com.google.common.collect.Maps;
import com.ppdai.auth.utils.PauthTokenUtil;
import com.ppdai.stargate.constant.JobTaskTypeEnum;
import com.ppdai.stargate.controller.request.FlinkDeployRequest;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.manager.JobManager;
import com.ppdai.stargate.po.GroupEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.GroupService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class FlinkJobService {

    @Autowired
    private GroupService groupService;
    @Autowired
    private PauthTokenUtil pauthTokenUtil;
    @Autowired
    private JobManager jobManager;


    public JobInfo addDeployFlinkJob(String operatorType, Long groupId, Long instanceId, List<Long> jobIdList) {
        GroupEntity groupEntity = groupService.getGroupById(groupId);
        if (groupEntity == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定发布组[%s]不存在。", groupId);
            log.error("<<addDeployFlinkJob>> " + ex.getMessage());
            throw ex;
        }

        Map<String, Object> data = Maps.newHashMap();
        String token = pauthTokenUtil.getToken();
        data.put("token", token);

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(groupEntity.getName());
        jobInfo.setGroupId(groupId);
        jobInfo.setOperationType(operatorType);
        jobInfo.setDataMap(data);

        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;

        if (jobIdList.size() > 0) {
            Map<String, Object> taskData = Maps.newHashMap();
            taskData.put("jobIdList", org.apache.commons.lang.StringUtils.join(jobIdList, ","));
            taskList.add(new TaskInfo(step++, JobTaskTypeEnum.SYNC_INSTANCE_JOB, taskData));
        }

        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.DEPLOY_FLINKJOB, instanceId));

        return jobManager.createJobWithTasks(jobInfo);
    }

    public JobInfo addCreateGroupJob(String operatorType, Long groupId, Long instanceId) {
        GroupEntity groupEntity = groupService.getGroupById(groupId);
        if (groupEntity == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定发布组[%s]不存在。", groupId);
            log.error("<<addDeployFlinkJob>> " + ex.getMessage());
            throw ex;
        }

        Map<String, Object> data = Maps.newHashMap();
//        String token = pauthTokenUtil.getToken();
        data.put("token", "stargate");

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(groupEntity.getName());
        jobInfo.setGroupId(groupId);
        jobInfo.setOperationType(operatorType);
        jobInfo.setDataMap(data);
        jobInfo.setAppId(groupEntity.getAppId());
        jobInfo.setAppName(groupEntity.getAppName());
        jobInfo.setEnv(groupEntity.getEnvironment());
        return jobManager.createJobWithTasks(jobInfo);
    }


    /**
     * flink1.11
     *
     * @param operatorType
     * @param instanceId
     * @return
     */
    public JobInfo addDeployApplicationFlinkJob(String operatorType, FlinkDeployRequest flinkDeployRequest,
                                                Long instanceId) {
        GroupEntity groupEntity = groupService.getGroupById(flinkDeployRequest.getGroupId());
        if (groupEntity == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定发布组[%s]不存在。", flinkDeployRequest.getGroupId());
            log.error("<<addDeployFlinkJob>> " + ex.getMessage());
            throw ex;
        }

        Map<String, Object> data = Maps.newHashMap();
        String token = pauthTokenUtil.getToken();
        data.put("token", token);
        data.put("savepointSwitch", flinkDeployRequest.isSavepointSwitch());
        data.put("savepointPath", StringUtils.trim(flinkDeployRequest.getSavepointPath()));
        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(groupEntity.getName());
        jobInfo.setGroupId(flinkDeployRequest.getGroupId());
        jobInfo.setOperationType(operatorType);
        jobInfo.setDataMap(data);
        jobInfo.setAppId(groupEntity.getAppId());
        jobInfo.setAppName(groupEntity.getAppName());
        jobInfo.setEnv(groupEntity.getEnvironment());

        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;

        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.DEPLOY_FLINKJOB, instanceId));
        return jobManager.createJobWithTasks(jobInfo);
    }

    public JobInfo addUpdateFlinkJob(String operatorType, Long groupId,
                                     Long instanceId, FlinkDeployRequest flinkDeployRequest) {
        GroupEntity groupEntity = groupService.getGroupById(groupId);
        if (groupEntity == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定发布组[%s]不存在。", groupId);
            log.error("<<addDeployFlinkJob>> " + ex.getMessage());
            throw ex;
        }

        Map<String, Object> data = Maps.newHashMap();
        String token = pauthTokenUtil.getToken();
        data.put("token", token);
        data.put("savepointSwitch", flinkDeployRequest.isSavepointSwitch());
        data.put("savepointPath", StringUtils.trim(flinkDeployRequest.getSavepointPath()));

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(groupEntity.getName());
        jobInfo.setGroupId(groupId);
        jobInfo.setOperationType(operatorType);
        jobInfo.setDataMap(data);
        jobInfo.setAppId(groupEntity.getAppId());
        jobInfo.setAppName(groupEntity.getAppName());
        jobInfo.setEnv(groupEntity.getEnvironment());

        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;

        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.REMOVE_HC_ONE, instanceId));
        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.STOP_FLINKJOB, instanceId));
        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.DEPLOY_FLINKJOB, instanceId));
        return jobManager.createJobWithTasks(jobInfo);
    }


    public JobInfo addStopFlinkJob(String operatorType, GroupEntity groupEntity, Long instanceId) {
        Map<String, Object> data = Maps.newHashMap();
        String token = pauthTokenUtil.getToken();
        data.put("token", token);

        JobInfo jobInfo = new JobInfo();
        jobInfo.setName(groupEntity.getName());
        jobInfo.setGroupId(groupEntity.getId());
        jobInfo.setOperationType(operatorType);
        jobInfo.setDataMap(data);
        jobInfo.setAppId(groupEntity.getAppId());
        jobInfo.setAppName(groupEntity.getAppName());
        jobInfo.setEnv(groupEntity.getEnvironment());

        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;
        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.REMOVE_HC_ONE, instanceId));
        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.STOP_FLINKJOB, instanceId));

        return jobManager.createJobWithTasks(jobInfo);
    }

    public void addDestroyFlinkJob(String operatorType, Long groupId, InstanceEntity instanceEntity) {
        GroupEntity groupEntity = groupService.getGroupById(groupId);
        if (groupEntity == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定发布组[%s]不存在。", groupId);
            log.error("<<addStopFlinkJob>> " + ex.getMessage());
            throw ex;
        }

        Map<String, Object> data = Maps.newHashMap();
        String token = pauthTokenUtil.getToken();
        data.put("token", token);
        data.put("instanceId", instanceEntity.getId());

        JobInfo jobInfo = new JobInfo();
        jobInfo.setAppId(groupEntity.getAppId());
        jobInfo.setAppName(groupEntity.getAppName());
        jobInfo.setEnv(groupEntity.getEnvironment());
        jobInfo.setName(groupEntity.getName());
        jobInfo.setGroupId(groupId);
        jobInfo.setOperationType(operatorType);
        jobInfo.setDataMap(data);
        List<TaskInfo> taskList = jobInfo.getTaskInfos();
        int step = 1;
        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.STOP_FLINKJOB, instanceEntity.getId()));
        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.DESTROY_FLINKJOB, instanceEntity.getId()));
        taskList.add(new TaskInfo(step++, JobTaskTypeEnum.removeGroup));
        jobManager.createJobWithTasks(jobInfo);
    }

    public JobInfo addRestartFlinkJob(String operatorType, Long instanceId, Long groupId) {
        GroupEntity groupEntity = groupService.getGroupById(groupId);
        if (groupEntity == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定发布组[%s]不存在。", groupId);
            log.error("<<addDeployFlinkJob>> " + ex.getMessage());
            throw ex;
        }

        Map<String, Object> data = Maps.newHashMap();
        String token = pauthTokenUtil.getToken();
        data.put("token", token);
        data.put("zone", "");

        JobInfo jobInfo = new JobInfo();
        jobInfo.setAppId(groupEntity.getAppId());
        jobInfo.setAppName(groupEntity.getAppName());
        jobInfo.setEnv(groupEntity.getEnvironment());
        jobInfo.setName(groupEntity.getName());
        jobInfo.setGroupId(groupId);
        jobInfo.setOperationType(operatorType);
        jobInfo.setDataMap(data);
        List<TaskInfo> taskInfos = jobInfo.getTaskInfos();
        int step = 1;
        taskInfos.add(new TaskInfo(step++, JobTaskTypeEnum.REMOVE_HC_ONE, instanceId));
        taskInfos.add(new TaskInfo(step++, JobTaskTypeEnum.STOP_FLINKJOB, instanceId));
        taskInfos.add(new TaskInfo(step++, JobTaskTypeEnum.DEPLOY_FLINKJOB, instanceId));
        return jobManager.createJobWithTasks(jobInfo);
    }

}
