package com.ppdai.stargate.po;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.Date;

@Entity
@Cacheable(false)
@Data
@EqualsAndHashCode(callSuper=false)
@ToString
@Table(name = "instance")
public class InstanceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Basic
    @Column(name = "name")
    private String name;

    @Basic
    @Column(name = "env")
    private String env;

    @Basic
    @Column(name = "group_id")
    private Long groupId;

    @Basic
    @Column(name = "app_id")
    private String appId;

    @Basic
    @Column(name = "app_name")
    private String appName;

    @Basic
    @Column(name = "slot_ip")
    private String slotIp;

    @Basic
    @Column(name = "port")
    private Integer port;

    @Basic
    @Column(name = "env_vars")
    private String envVars;

    @Basic
    @Column(name = "status")
    private String status;

    @Basic
    @Column(name = "has_pulled_in")
    private Boolean hasPulledIn;

    @Basic
    @Transient
    private Long flags = 0L;

    @Basic
    @Column(name = "image")
    private String image;

    @Basic
    @Column(name = "zone")
    private String zone;

    @Basic
    @Column(name = "spec")
    private String spec;

    @Basic
    @Column(name = "namespace")
    private String namespace;

    @Basic
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "release_time")
    private Date releaseTime;
}
