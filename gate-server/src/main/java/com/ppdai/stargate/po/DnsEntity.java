package com.ppdai.stargate.po;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(name = "dns", catalog = "")
public class DnsEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Basic
    @Column(name = "name", nullable = false, length = 255)
    private String name;

    @Basic
    @Column(name = "type", nullable = false, length = 10)
    private String type;

    @Basic
    @Column(name = "content", nullable = false)
    private String content;

    @Basic
    @Column(name = "env_id", nullable = false, length = 255)
    private String envId;

    @Basic
    @Column(name = "ttl", nullable = false)
    private Integer ttl;
}
