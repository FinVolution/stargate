package com.ppdai.stargate.vo;

import java.util.Date;
import java.util.List;

import com.ppdai.stargate.constant.JobStatus;

import com.ppdai.stargate.job.task.TaskInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class JobVO {

    private Long id;
    private String name;
    private String env;
    private String appName;
    private Long groupId;
    private String operationType;
    private String operationTypeDesc;
    private String assignInstance;
    private Long threadId;
    private Integer expireTime;
    private JobStatus status;
    private String dataMap;
    private Integer version;
    private List<TaskInfo> taskList;
    private Date insertTime;
    private String insertBy;
    private Date updateTime;
    private String updateBy;
}
