package com.ppdai.stargate.po;

import lombok.Data;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@Cacheable(false)
@Table(name = "audit_log")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "user_name", nullable = true, length = 32)
    private String userName;

    @Column(name = "client_ip", nullable = true, length = 32)
    private String clientIp;

    @Column(name = "http_method", nullable = true, length = 64)
    private String httpMethod;

    @Column(name = "http_uri", nullable = true, length = 256)
    private String httpUri;

    @Column(name = "class_method", nullable = true, length = 128)
    private String classMethod;

    @Column(name = "class_method_args", nullable = true, length = 256)
    private String classMethodArgs;

    @Column(name = "class_method_return", nullable = true, length = 1024)
    private String classMethodReturn;

    @Column(name = "code", nullable = false)
    private Integer code = 0;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "insert_time", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    protected Date insertTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    protected Date updateTime;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1)")
    protected Boolean isActive = true;

}
