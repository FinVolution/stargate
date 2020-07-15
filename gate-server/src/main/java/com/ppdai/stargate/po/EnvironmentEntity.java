package com.ppdai.stargate.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Data
@Cacheable(false)
@EqualsAndHashCode(callSuper=false)
@Table(name = "environment", catalog = "")
public class EnvironmentEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic
    @Column(name = "cmdb_env_id", nullable = true, length = 64)
    private Long cmdbEnvId;

    @Basic
    @Column(name = "name", nullable = true, length = 64)
    private String name;

    @Basic
    @Column(name = "description", nullable = true, length = 128)
    private String description;

    @Basic
    @Column(name = "consul", nullable = true, length = 128)
    private String consul;

    @Basic
    @Column(name = "nginx", nullable = true, length = 128)
    private String nginx;

    @Basic
    @Column(name = "dockeryard", nullable = true, length = 128)
    private String dockeryard;

    @Basic
    @Column(name = "dns", nullable = true, length = 128)
    private String dns;

    @Column(name = "is_in_use", nullable = true, columnDefinition = "TINYINT(1)")
    public Boolean isInUse = true;

    @Column(name = "enable_ha", nullable = false, columnDefinition = "TINYINT(1)")
    public Boolean enableHa;

}
