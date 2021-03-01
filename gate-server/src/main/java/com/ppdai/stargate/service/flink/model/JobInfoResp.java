package com.ppdai.stargate.service.flink.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobInfoResp {

    /**
     * job运行状态
     */
    private FlinkJobStatus status;
    /**
     * 任务总数
     */
    private int taskTotal;
    /**
     * 正在运行的任务总数
     */
    private int runningTaskTotal;

    /**
     * job持续时间
     */
    private long duration;

    private String jobName;

}
