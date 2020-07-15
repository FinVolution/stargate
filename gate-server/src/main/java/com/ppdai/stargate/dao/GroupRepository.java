package com.ppdai.stargate.dao;

import com.ppdai.stargate.po.GroupEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface GroupRepository extends BaseJpaRepository<GroupEntity, Long> {

    @Override
    @Query("SELECT e FROM GroupEntity e WHERE e.isActive=true AND e.id=?1")
    GroupEntity findOne(Long id);

    @Query("SELECT e FROM GroupEntity e WHERE e.isActive=true AND e.environment=?1 AND e.appId=?2 ORDER BY e.id DESC")
    List<GroupEntity> findByEnvAndAppId(String env, String appId);

    @Query("SELECT e FROM GroupEntity e WHERE e.appId=?1")
    List<GroupEntity> findByAppIdEx(String appId);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update GroupEntity e set e.isActive=false where e.id=?1")
    int removeById(Long id);

    @Query("select e from GroupEntity e where e.name=?1")
    GroupEntity findGroupByNameEx(String name);

    @Query("select e from GroupEntity e where e.isActive=true and e.environment=?1")
    List<GroupEntity> findByEnvironment(String environment);

    @Query("select e from GroupEntity e where e.environment=?1 and e.appId=?2 ORDER BY e.insertTime DESC")
    List<GroupEntity> findRecentUsedImages(String env, String appId, Pageable pageable);

    @Query("select e from GroupEntity e where e.isActive=true")
    List<GroupEntity> findAll();
}
