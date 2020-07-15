package com.ppdai.stargate.vo;

import com.ppdai.stargate.job.JobInfo;
import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class DeployGroupInfoVO {

    private Long id;
    private String name;
    private String environment;
    private String appId;
    private String releaseTarget;
    private String instanceSpec;
    private Integer instanceCount;
    private Integer activeCount;
    private Integer expectedCount;
    private Integer instanceUpPercentage;
    private List<InstanceVO> instances;
    private JobInfo jobInfo;
    private Date insertTime;
    private String insertBy;
}
