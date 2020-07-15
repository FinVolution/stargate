package com.ppdai.stargate.dao;

import com.ppdai.stargate.constant.JobStatus;
import com.ppdai.stargate.po.JobEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface JobRepository extends BaseJpaRepository<JobEntity, Long>, JpaSpecificationExecutor<JobEntity> {

    @Query("select a from JobEntity a where a.groupId = ?1 ")
    List<JobEntity> findByGroupId(Long groupId, Pageable pageable);

    @Query("select a from JobEntity a where a.status = ?1 ")
    List<JobEntity> findByStatus(JobStatus jobStatus, Pageable pageable);

    @Query("select a from JobEntity a where a.groupId = ?1 and a.status in ?2 ")
    List<JobEntity> findByStatusAndGroupId(Long groupId, Collection<JobStatus> jobStatus);

    @Modifying(clearAutomatically = true)
    @Query("update JobEntity e set e.version=e.version+1 , e.status=?3 ,e.assignInstance=?4 where e.id=?1 and e.version=?2")
    int occupy(Long id, Integer version, JobStatus jobStatus, String assignInstance);

    JobEntity findFirstByGroupId(Long groupId, Sort sort);

    @Query("select now() from JobEntity")
    Date findCurrentTime();

    @Query("select a from JobEntity a where a.env = ?1 and a.appId = ?2 and a.operationType in ?3 ORDER BY a.insertTime DESC")
    List<JobEntity> findRecentUsedImages(String env, String appId, List<String> operationTypes, Pageable pageable);
}
