package com.ppdai.stargate.po;

import com.ppdai.stargate.constant.LockEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Data
@EqualsAndHashCode(callSuper=false)
@Table(name = "global_lock", catalog = "")
@Cacheable(value = false)
public class GlobalLockEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic
    @Column(name = "lock_key", nullable = false, length = 64)
    @Enumerated(EnumType.STRING)
    private LockEnum lockKey;

    @Basic
    @Column(name = "expiration_time", nullable = false)
    private Long expirationTime;

    @Basic
    @Column(name = "owner", nullable = true, length = 128)
    private String owner;

    @Basic
    @Column(name = "note", nullable = true, length = 256)
    private String note;

}
