package com.ppdai.stargate.manager;

import com.alibaba.fastjson.JSON;
import com.ppdai.stargate.constant.JobStatus;
import com.ppdai.stargate.constant.TaskStatus;
import com.ppdai.stargate.dao.TaskRepository;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.po.JobEntity;
import com.ppdai.stargate.po.TaskEntity;
import com.ppdai.stargate.service.JobService;
import com.ppdai.stargate.utils.ConvertUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class TaskManager {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private JobService jobService;

    @Transactional
    public List<TaskInfo> createTasks(Long jobId, List<TaskInfo> taskInfos) {
        taskInfos.forEach(taskInfo -> {
            taskInfo.setJobId(jobId);
            taskInfo.setStatus(TaskStatus.NEW);
            TaskEntity entity = taskRepository.save(convert(taskInfo));
            ConvertUtil.convert(entity, taskInfo);
        });
        return taskInfos;
    }

    @Transactional
    public void updateTask(TaskInfo taskInfo) {
        TaskEntity taskEntity = convert(taskInfo);
        taskRepository.save(taskEntity);
    }

    public List<TaskInfo> getTaskInfosByJobId(Long jobId) {
        List<TaskEntity> taskEntities = taskRepository.findTasksByJobId(jobId);
        return ConvertUtil.convert(taskEntities, this::convert);
    }

    private TaskInfo convert(TaskEntity taskEntity) {
        TaskInfo taskInfo = ConvertUtil.convert(taskEntity, TaskInfo.class);
        if (!StringUtils.isEmpty(taskEntity.getDataMap())) {
            Map dataMap = JSON.parseObject(taskEntity.getDataMap(), Map.class);
            taskInfo.setDataMap(dataMap);
        }
        return taskInfo;
    }

    private TaskEntity convert(TaskInfo taskInfo) {
        TaskEntity taskEntity = ConvertUtil.convert(taskInfo, TaskEntity.class);
        if (taskInfo.getDataMap() != null) {
            taskEntity.setDataMap(JSON.toJSONString(taskInfo.getDataMap()));
        }
        return taskEntity;
    }

    public Boolean hasInProcessTasksByInstance(Long instanceId) {
        List<TaskStatus> inProcessStatues = new ArrayList<>();
        inProcessStatues.add(TaskStatus.NEW);
        inProcessStatues.add(TaskStatus.RUNNING);

        List<TaskEntity> taskEntities = taskRepository.findByStatusAndInstanceId(instanceId, inProcessStatues);

        for (int i = 0; i < taskEntities.size(); i++) {
            TaskEntity taskEntity = taskEntities.get(i);
            JobEntity jobEntity = jobService.findById(taskEntity.getJobId());
            if (jobEntity.getStatus().equals(JobStatus.NEW) || jobEntity.getStatus().equals(JobStatus.RUNNING)) {
                return true;
            }
        }

        return false;
    }

    public long getActiveTaskCountByInstance(Long instanceId) {
        long taskCount = taskRepository.count((root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> list = new ArrayList<>();
            list.add(criteriaBuilder.equal(root.get("instanceId").as(String.class), instanceId));

            Predicate predicate1 = criteriaBuilder.equal(root.get("status").as(TaskStatus.class), TaskStatus.NEW);
            Predicate predicate2 = criteriaBuilder.equal(root.get("status").as(TaskStatus.class), TaskStatus.RUNNING);
            list.add(criteriaBuilder.or(predicate1, predicate2));
            list.add(criteriaBuilder.equal(root.get("isActive").as(Boolean.class), Boolean.TRUE));

            Predicate[] p = new Predicate[list.size()];
            return criteriaBuilder.and(list.toArray(p));
        });
        return taskCount;
    }
}
