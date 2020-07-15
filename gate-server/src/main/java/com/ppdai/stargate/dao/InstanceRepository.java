package com.ppdai.stargate.dao;

import com.ppdai.stargate.po.InstanceEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface InstanceRepository extends BaseJpaRepository<InstanceEntity, Long>, JpaSpecificationExecutor<InstanceEntity> {

    @Override
    @Query("SELECT e FROM InstanceEntity e WHERE e.isActive=true AND e.id=?1")
    InstanceEntity findOne(Long id);

    @Query("SELECT e FROM InstanceEntity e WHERE e.isActive=true")
    List<InstanceEntity> findAllInstances();

    @Query("SELECT e FROM InstanceEntity e WHERE e.isActive=true AND e.env=?1 AND e.appId=?2")
    List<InstanceEntity> findInstancesByEnvAndAppId(String env, String appId);

    @Query("SELECT e FROM InstanceEntity e WHERE e.isActive=true AND e.appId=?1")
    List<InstanceEntity> findInstancesByAppId(String appId);

    @Query("SELECT e FROM InstanceEntity e WHERE e.isActive=true AND e.env=?1 AND e.appId=?2 AND e.spec=?3")
    List<InstanceEntity> findInstancesByEnvAndAppIdAndSpec(String env, String appId, String spec);

    @Query("SELECT e FROM InstanceEntity e WHERE e.isActive=true AND e.groupId=?1")
    List<InstanceEntity> findInstancesByGroupId(Long groupId);

    @Query("SELECT e FROM InstanceEntity e WHERE e.groupId=?1")
    List<InstanceEntity> findInstancesByGroupIdEx(Long groupId);

    @Query("SELECT e FROM InstanceEntity e WHERE e.isActive=true AND e.slotIp=?1")
    InstanceEntity findInstanceByIp(String ip);

    @Query("SELECT e FROM InstanceEntity e WHERE e.isActive=true AND e.name=?1")
    InstanceEntity findByName(String instanceName);

    @Query("SELECT e FROM InstanceEntity e WHERE e.isActive=true AND e.name=?1")
    List<InstanceEntity> findListByName(String instanceName);

    @Query("SELECT e FROM InstanceEntity e WHERE e.isActive=true AND e.env=?1")
    List<InstanceEntity> findByEnv(String env);

    @Query("SELECT e FROM InstanceEntity e WHERE e.name=?1")
    InstanceEntity findByNameEx(String instanceName);

    @Query("select count(a) from InstanceEntity a where a.isActive=true and a.groupId = ?1")
    Integer countByGroupId(long groupId);

    @Query("select count(e) from InstanceEntity e where e.isActive=true and e.env=?1 and e.appId=?2 and e.zone=?3")
    Integer countByEnvAndAppIdAndZone(String env, String appId, String zone);

    @Query("select count(e) from InstanceEntity e where e.isActive=true and e.groupId=?1 and e.zone=?2")
    Integer countByGroupIdAndZone(Long groupId, String zone);

    @Query("SELECT DISTINCT(e.image), e.releaseTime FROM InstanceEntity e WHERE e.appId=?1 AND e.env=?2 ORDER BY e.releaseTime DESC")
    List<Object[]> findRecentUsedImages(String appId, String env, Pageable pageable);

    @Query("select e from InstanceEntity e where e.isActive=true and e.groupId=0 and e.env=?1 and e.zone=?2")
    List<InstanceEntity> findCloudInstancesByEnvAndZone(String env, String zone);

    @Query("select e from InstanceEntity e where e.isActive=true and e.groupId<>0 and e.env=?1")
    List<InstanceEntity> findGroupInstancesByEnv(String env);
}
