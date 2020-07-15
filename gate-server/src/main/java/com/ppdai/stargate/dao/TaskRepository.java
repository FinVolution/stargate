package com.ppdai.stargate.dao;

import com.ppdai.stargate.constant.TaskStatus;
import com.ppdai.stargate.po.TaskEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface TaskRepository extends BaseJpaRepository<TaskEntity, Long>, JpaSpecificationExecutor<TaskEntity> {

    @Query("select a from TaskEntity a where a.jobId=?1 order by a.step")
    List<TaskEntity> findTasksByJobId(Long jobId);

    @Query("select a from TaskEntity a where a.isActive = true and a.instanceId = ?1 and a.status in ?2 ")
    List<TaskEntity> findByStatusAndInstanceId(Long instanceId, Collection<TaskStatus> taskStatus);

}
