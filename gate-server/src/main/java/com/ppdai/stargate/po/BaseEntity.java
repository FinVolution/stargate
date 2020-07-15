package com.ppdai.stargate.po;

import lombok.Data;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Data
@Cacheable(false)
@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
public class BaseEntity {

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "insert_time", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    public Date insertTime;

    @CreatedBy
    @Column(name = "insert_by", nullable = true, length = 64)
    public String insertBy;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "update_time", insertable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    public Date updateTime;

    @LastModifiedBy
    @Column(name = "update_by", nullable = true, length = 64)
    public String updateBy;

    @Column(name = "is_active", nullable = false, columnDefinition = "TINYINT(1)")
    public Boolean isActive = true;

}
