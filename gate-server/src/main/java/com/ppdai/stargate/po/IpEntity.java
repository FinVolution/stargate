package com.ppdai.stargate.po;


import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "ip")
public class IpEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic
    @Column(name = "network", length = 64)
    private String network;

    @Basic
    @Column(name = "network_segment", length = 64)
    private String networkSegment;

    @Basic
    @Column(name = "ip", nullable = false)
    private String ip;

    @Column(name = "occupied", nullable = false, columnDefinition = "TINYINT(1)")
    private Boolean occupied = false;

}
