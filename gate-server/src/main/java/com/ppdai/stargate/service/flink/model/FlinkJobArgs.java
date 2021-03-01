package com.ppdai.stargate.service.flink.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created by chenlang on 2020/6/11
 **/
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlinkJobArgs {
    private String jarId;
    private String clusterId;
    private String k8sMasterUrl;
    private boolean allowNonRestoredState = true;
    private String savepointPath;
    /**
     * 多参数逗号分隔
     */
    private String programArg;
    private String entryClass;
    private Integer parallelism;

}
