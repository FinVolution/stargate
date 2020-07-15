package com.ppdai.stargate.po;

import com.ppdai.stargate.constant.TaskStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "task", catalog = "")
public class TaskEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic
    @Column(name = "description", nullable = true, length = 128)
    private String description;

    @Basic
    @Column(name = "name", nullable = false, length = 64)
    private String name;

    @Basic
    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Basic
    @Column(name = "instance_id", nullable = true)
    private Long instanceId;

    @Column(name = "data_map", length = -1)
    private String dataMap;

    @Basic
    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @Basic
    @Column(name = "additional_info", nullable = true, length = 256)
    private String additionalInfo;

    @Basic
    @Column(name = "step", nullable = false)
    private Integer step;

    @Basic
    @Column(name = "expire_time")
    private Integer expireTime;

    @Basic
    @Column(name = "start_time")
    private Date startTime;

    @Basic
    @Column(name = "end_time")
    private Date endTime;
}
