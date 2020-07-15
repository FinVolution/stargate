package com.ppdai.stargate.dao;

import com.ppdai.stargate.po.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ApplicationRepository extends BaseJpaRepository<ApplicationEntity, Long>, JpaSpecificationExecutor<ApplicationEntity> {

    @Override
    @Query("SELECT e FROM ApplicationEntity e WHERE e.isActive=true AND e.id=?1")
    ApplicationEntity findOne(Long id);

    @Query("SELECT e FROM ApplicationEntity e WHERE e.isActive=true AND e.name=?1")
    ApplicationEntity findByName(String name);
    
    @Query("SELECT e FROM ApplicationEntity e WHERE e.isActive=true AND e.cmdbAppId=?1")
    ApplicationEntity findByAppId(String appCmdbId);

    @Query("SELECT e FROM ApplicationEntity e WHERE e.cmdbAppId=?1")
    ApplicationEntity findByAppIdEx(String appCmdbId);

    @Query("SELECT e FROM ApplicationEntity e WHERE e.isActive=true AND e.department=?1")
    List<ApplicationEntity> findByDepartment(String department);

    @Query("SELECT e FROM ApplicationEntity e WHERE e.isActive=true")
    List<ApplicationEntity> findAllApps();
}
