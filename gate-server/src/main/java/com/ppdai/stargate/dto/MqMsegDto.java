package com.ppdai.stargate.dto;

import lombok.Data;

@Data
public class MqMsegDto {
    private String topicName;
    private String lan;
    private String sdkVersion;
    private String clientIp;
    private MqMsgBody[] msgs;

    @Data
    public static class MqMsgBody {
        private String body;
    }
}
