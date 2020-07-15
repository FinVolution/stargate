package com.ppdai.stargate.po;

import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

@Entity
@Data
@EqualsAndHashCode(callSuper = false)
@Table(name = "task_log", catalog = "")
public class TaskLogEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Basic
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    @Basic
    @Column(name = "job_id", nullable = false)
    private Long jobId;

    @Basic
    @Column(name = "logLevel", nullable = false, length = 20)
    private String logLevel;

    @Basic
    @Column(name = "log_time")
    private Date logTime;

    @Basic
    @Column(name = "message", length = -1)
    private String message;

}
