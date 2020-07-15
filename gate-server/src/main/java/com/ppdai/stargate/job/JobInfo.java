package com.ppdai.stargate.job;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.ppdai.stargate.constant.JobStatus;
import com.ppdai.stargate.job.task.TaskInfo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobInfo {

    private Long id;

    private String name;

    private String env;

    private String appId;

    private String appName;

    private Long groupId;

    private String operationType;

    private String operationTypeDesc;

    private String assignInstance;

    private Long threadId;

    private JobStatus status;

    private String additionalInfo;

    private Map<String, Object> dataMap;

    private Integer expireTime = 300;

    private int version;

    private Date insertTime;

    private String insertBy;

    private Date updateTime;

    private String updateBy;

    private List<TaskInfo> taskInfos = new ArrayList<>();
}
