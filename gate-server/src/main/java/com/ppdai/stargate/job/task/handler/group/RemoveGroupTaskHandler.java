package com.ppdai.stargate.job.task.handler.group;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.po.GroupEntity;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.service.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.ppdai.stargate.constant.JobTaskTypeEnum;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.AbstractTaskHandler;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.service.GroupService;

import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * RemoveGroupTaskHandler
 *
 * @author huangyinhuang
 * @date 2018/5/25
 */
@Component
@Slf4j
public class RemoveGroupTaskHandler extends AbstractTaskHandler {

    @Autowired
    private GroupService groupService;

    @Autowired
    private InstanceService instanceService;

    @Override
    public String getName() {
        return JobTaskTypeEnum.removeGroup.name();
    }

    @Override
    public void execute(TaskInfo taskInfo) throws Exception {
        JobInfo jobInfo = taskInfo.getJobInfo();
        log.info("<<RemoveGroupTaskHandler>> 开始移除发布组:  groupId={}, jobId={}, taskId={}",
                jobInfo.getGroupId(), jobInfo.getId(), taskInfo.getId());

        Long groupId = jobInfo.getGroupId();
        GroupEntity group = groupService.getGroupById(groupId);
        if (group == null) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定的发布组不存在，groupId = " + groupId);
            log.error("<<RemoveGroupTaskHandler>> " + ex.getMessage());
            throw ex;
        }

        List<InstanceEntity> instances = instanceService.getInstancesByGroupId(groupId);
        if (!instances.isEmpty()) {
            BaseException ex = BaseException.newException(MessageType.ERROR, "指定的发布组还有实例未删除，groupId = " + groupId);
            log.error("<<RemoveGroupTaskHandler>> " + ex.getMessage());
            throw ex;
        }

        log.info("<<RemoveGroupTaskHandler>> 删除Service成功: Service={}", group.getName());

        // 从数据库中删除发布组记录
        Boolean bSuccess = groupService.deleteGroup(groupId);
        if (!bSuccess){
            BaseException ex = BaseException.newException(MessageType.ERROR, "删除数据库中发布组记录失败, groupId = " + groupId);
            log.error("<<RemoveGroupTaskHandler>> " + ex.getMessage());
            throw ex;
        }

        log.info("<<RemoveGroupTaskHandler>> 移除发布组完成:  groupId={}, jobId={}, taskId={}",
                jobInfo.getGroupId(), jobInfo.getId(), taskInfo.getId());

    }

}
