package com.ppdai.stargate.vo;

import lombok.Data;

import java.util.Date;
import java.util.Map;

@Data
public class AppVO {

    private Long id;
    private String name;
    private String description;
    private String serviceType;
    private String appType;
    private String owner;
    private String developer;
    private String developerNames;
    private String tester;
    private String testerNames;
    private String department;
    private String departmentCode;
    private String cmdbAppId;
    private Boolean enableHa;
    private Map<String, String> envUrlMap;
    public Date insertTime;
    public Date updateTime;

}
