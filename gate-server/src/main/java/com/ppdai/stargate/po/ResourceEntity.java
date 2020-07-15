package com.ppdai.stargate.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "resource")
public class ResourceEntity extends BaseEntity  {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic
    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Basic
    @Column(name = "app_name", length = 64)
    private String appName;

    @Basic
    @Column(name = "env", length = 64)
    private String env;

    @Basic
    @Column(name = "zone", length = 64)
    private String zone;

    @Basic
    @Column(name = "spec",length = 64)
    private String spec;

    @Basic
    @Column(name = "ip", nullable = false, length = 64)
    private String ip;

    @Basic
    @Column(name = "pod_name", length = 64)
    private String podName;

    @Basic
    @Column(name = "is_static", columnDefinition = "TINYINT(1)")
    private Boolean isStatic = false;
}
