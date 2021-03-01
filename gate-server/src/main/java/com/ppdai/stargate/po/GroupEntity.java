package com.ppdai.stargate.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Data
@Cacheable(false)
@EqualsAndHashCode(callSuper = false)
@Table(name = "sgroup", catalog = "")
public class GroupEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Basic
    @Column(name = "app_id", nullable = false)
    private String appId;

    @Basic
    @Column(name = "app_name", nullable = false)
    private String appName;

    @Basic
    @Column(name = "env", nullable = true)
    private String environment;

    @Basic
    @Column(name = "release_target", nullable = true, length = 64)
    private String releaseTarget;

    @Basic
    @Column(name = "instance_spec", nullable = false, length = 64)
    private String instanceSpec;

    @Basic
    @Column(name = "port_count", nullable = true)
    private Integer portCount;
}
