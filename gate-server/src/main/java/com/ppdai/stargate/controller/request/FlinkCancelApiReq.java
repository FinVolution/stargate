package com.ppdai.stargate.controller.request;

import lombok.Data;

/**
 * Created by chenlang on 2020/9/11
 **/
@Data
public class FlinkCancelApiReq {

    private String savepointDirectory;
    private String appId;
    private String env;
    private String jobId;

}
