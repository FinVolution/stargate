package com.ppdai.stargate.vo;

import lombok.Data;

import java.util.Map;

@Data
public class OrgQuotaVO {

    String environment;
    String organization;
    String organizationName;
    Integer limit = 0;
    Integer used = 0;
    Map<String, Integer> siteQuotas;

}
