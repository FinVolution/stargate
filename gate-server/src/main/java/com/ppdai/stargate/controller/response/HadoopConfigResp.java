package com.ppdai.stargate.controller.response;

import com.ppdai.stargate.po.HadoopConfigEntity;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.util.Date;

@Data
public class HadoopConfigResp {
    private Long id;
    private String name;
    private String coreSite;
    private String hdfsSite;
    private String env;
    private String savepoint;
    private String description;
    private String updateBy;
    public Date updateTime;
    private String department;



    public static HadoopConfigResp applyHadoopConfig(HadoopConfigEntity hadoopConfigEntity) {
        HadoopConfigResp hadoopConfigResp = new HadoopConfigResp();
        BeanUtils.copyProperties(hadoopConfigEntity,hadoopConfigResp);
        return hadoopConfigResp;
    }

}
