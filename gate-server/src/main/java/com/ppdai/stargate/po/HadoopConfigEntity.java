package com.ppdai.stargate.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Data
@Cacheable(false)
@EqualsAndHashCode(callSuper = false)
@Table(name = "hadoop_config", catalog = "")
public class HadoopConfigEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private String coreSite;
    private String hdfsSite;
    private String env;
    private String savepoint;
    private String description;
    private String department;



}
