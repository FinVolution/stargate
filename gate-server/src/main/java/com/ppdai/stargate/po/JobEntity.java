package com.ppdai.stargate.po;

import com.ppdai.stargate.constant.JobStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "job")
public class JobEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Basic
    @Column(name = "env", nullable = false)
    private String env;

    @Basic
    @Column(name = "app_id", nullable = false)
    private String appId;

    @Basic
    @Column(name = "app_name", nullable = false)
    private String appName;

    @Basic
    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Basic
    @Column(name = "operation_type", nullable = false, length = 45)
    private String operationType;

    @Basic
    @Column(name = "assign_instance", length = 45)
    private String assignInstance;

    @Basic
    @Column(name = "thread_id")
    private Long threadId;

    @Basic
    @Column(name = "expire_time")
    private Integer expireTime;

    @Basic
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private JobStatus status;

    @Basic
    @Column(name = "additional_info", nullable = true, length = 256)
    private String additionalInfo;

    @Column(name = "data_map", length = -1)
    private String dataMap;

    @Basic
    @Column(name = "version", nullable = false)
    private Integer version;
}
