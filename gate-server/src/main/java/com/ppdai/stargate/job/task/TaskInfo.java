package com.ppdai.stargate.job.task;

import com.ppdai.stargate.constant.JobTaskTypeEnum;
import com.ppdai.stargate.constant.TaskStatus;
import com.ppdai.stargate.job.JobInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskInfo {

    private Long id;

    private String name;

    private String description;

    private Long jobId;

    private Long instanceId;

    private Map<String, Object> dataMap;

    private TaskStatus status;

    private String additionalInfo;

    private int step;

    private Integer expireTime = 0;

    private Date startTime;

    private Date endTime;

    private Date insertTime;

    private String insertBy;

    private Date updateTime;

    private String updateBy;

    private JobInfo jobInfo;

    public TaskInfo(int step, JobTaskTypeEnum taskType) {
        this.step = step;
        this.name = taskType.name();
        this.description = taskType.getDescription();
        status = TaskStatus.NEW;
    }

    public TaskInfo(int step, JobTaskTypeEnum taskType, long instanceId) {
        this.step = step;
        this.name = taskType.name();
        this.description = taskType.getDescription();
        this.instanceId = instanceId;
        status = TaskStatus.NEW;
    }

    public TaskInfo(int step, JobTaskTypeEnum taskType, Map<String, Object> dataMap) {
        this.step = step;
        this.name = taskType.name();
        this.description = taskType.getDescription();
        this.dataMap = dataMap;
        status = TaskStatus.NEW;
    }

    public TaskInfo(int step, JobTaskTypeEnum taskType, int expireTime) {
        this.step = step;
        this.name = taskType.name();
        this.description = taskType.getDescription();
        this.expireTime = expireTime;
        status = TaskStatus.NEW;
    }

    public TaskInfo(int step, JobTaskTypeEnum taskType, long instanceId, int expireTime) {
        this.step = step;
        this.name = taskType.name();
        this.description = taskType.getDescription();
        this.instanceId = instanceId;
        this.expireTime = expireTime;
        status = TaskStatus.NEW;
    }

    public TaskInfo(int step, JobTaskTypeEnum taskType, long instanceId, int expireTime, Map<String, Object> dataMap) {
        this.step = step;
        this.name = taskType.name();
        this.description = taskType.getDescription();
        this.instanceId = instanceId;
        this.expireTime = expireTime;
        this.dataMap = dataMap;
        status = TaskStatus.NEW;
    }

    public TaskInfo(int step, JobTaskTypeEnum taskType, int expireTime, Map<String, Object> dataMap) {
        this.step = step;
        this.name = taskType.name();
        this.description = taskType.getDescription();
        this.expireTime = expireTime;
        this.dataMap = dataMap;
        status = TaskStatus.NEW;
    }
}
