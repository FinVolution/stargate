package com.ppdai.stargate.po;

import com.ppdai.stargate.constant.SecurityActionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

@Entity
@Data
@Cacheable(false)
@EqualsAndHashCode(callSuper = false)
@Table(name = "user_security_action")
public class UserSecurityActionEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private SecurityActionType type;

    @Column(name = "once_flag", nullable = false)
    private String onceFlag;

    @Column(name = "user_id", nullable = true)
    private Long userId;

    @Column(name = "user_name", nullable = true)
    private String userName;

}
