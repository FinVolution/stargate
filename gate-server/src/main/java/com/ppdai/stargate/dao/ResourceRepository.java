package com.ppdai.stargate.dao;

import com.ppdai.stargate.po.ResourceEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ResourceRepository extends BaseJpaRepository<ResourceEntity, Long>, JpaSpecificationExecutor<ResourceEntity> {

    @Query("select a from ResourceEntity a where a.isActive = true and a.podName = ?1")
    ResourceEntity findByPodName(String podName);

    @Query("select a from ResourceEntity a where a.isActive = true and a.id = ?1")
    ResourceEntity findById(Long id);

    @Query("select a from ResourceEntity a where a.isActive = true and a.appId = ?1 and a.env = ?2")
    List<ResourceEntity> findByAppIdAndEnv(String appId, String env);

    @Query("select a from ResourceEntity a where a.isActive = true and a.isStatic = true and a.appId = ?1 and a.env = ?2 and a.spec = ?3 and a.zone = ?4 and a.podName = null")
    List<ResourceEntity> findAvailableStaticResourcesByZone(String appId, String env, String spec, String zone);

    @Query("select a from ResourceEntity a where a.isActive = true and a.isStatic = true and a.appId = ?1 and a.env = ?2")
    List<ResourceEntity> findStaticResources(String appId, String env);

    @Query("select a from ResourceEntity a where a.isActive = true and a.isStatic = true and a.appId = ?1 and a.env = ?2 and a.spec = ?3")
    List<ResourceEntity> findStaticResourcesBySpec(String appId, String env, String spec);

    @Query("select a from ResourceEntity a where a.isActive = true and a.isStatic = true and a.appId = ?1 and a.env = ?2 and a.spec = ?3 and a.ip = ?4")
    ResourceEntity findStaticResourceByIp(String appId, String env, String spec, String ip);

    @Modifying(clearAutomatically = true)
    @Query("update ResourceEntity a set a.isStatic = ?4 where a.isActive = true and a.appId = ?1 and a.env = ?2 and a.isStatic = ?3")
    int changeResourcesType(String appId, String env, boolean isStatic, boolean resultType);

    @Modifying(clearAutomatically = true)
    @Query("update ResourceEntity a set a.podName = ?2 where a.isActive = true and a.isStatic = true and a.id = ?1 and a.podName = null")
    int setPodNameForStaticResource(Long resourceId, String podName);

    @Query("select a from ResourceEntity a where a.isActive = true and a.env = ?1 and a.zone = ?2 and a.podName is not null")
    List<ResourceEntity> findResourcesByEnvAndZone(String env, String zone);

    @Query("select a from ResourceEntity a where a.isActive = true and a.appId = ?1")
    List<ResourceEntity> findByAppId(String appId);
}
