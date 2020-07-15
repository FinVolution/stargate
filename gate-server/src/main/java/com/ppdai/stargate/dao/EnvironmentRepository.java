package com.ppdai.stargate.dao;

import com.ppdai.stargate.po.EnvironmentEntity;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EnvironmentRepository extends BaseJpaRepository<EnvironmentEntity, Long> {


    @Override
    @Query("SELECT e FROM EnvironmentEntity e WHERE e.isActive=true AND e.id=?1")
    EnvironmentEntity findOne(Long id);

    @Query("SELECT e FROM EnvironmentEntity e WHERE e.isActive=true AND e.name=?1")
    EnvironmentEntity findByName(String name);

    @Query("SELECT e FROM EnvironmentEntity e WHERE e.isActive=true AND e.isInUse=true AND e.name=?1")
    EnvironmentEntity findInUseEnvByName(String name);

    @Query("SELECT e FROM EnvironmentEntity e WHERE e.isActive=true AND e.cmdbEnvId=?1")
    EnvironmentEntity findByCmdbEnvId(Long envCmdbId);

    @Query("SELECT e FROM EnvironmentEntity e WHERE e.isActive=true")
    List<EnvironmentEntity> findAllEnvs();

    @Query("SELECT e FROM EnvironmentEntity e WHERE e.isActive=true and e.isInUse=true")
    List<EnvironmentEntity> findInUseEnvs();

}
