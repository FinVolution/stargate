package com.ppdai.stargate.vi;

import lombok.Data;

@Data
public class FlinkJobVO {
    private int id;
    private String name;
    private String version;
    private int taskTotal;
    private int runningTaskTotal;
    private String status = "NOTRUNNING";
    private String dashboardAddress;
    private String savepointPath;
    private boolean savepointSwitch;
    private String savepointDir;
    private String instanceName;
    private String cmd;
    private String variable;
    private String hadoopName;
}
