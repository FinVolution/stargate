package com.ppdai.stargate.dao;

import com.ppdai.stargate.po.DnsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DnsRepository extends JpaRepository<DnsEntity, Long>, JpaSpecificationExecutor<DnsEntity> {

    @Query("delete from DnsEntity e where e.name=?1 and e.envId=?2")
    void deleteByNameAndEnvId(String name, String envId);

    @Query("SELECT e FROM DnsEntity e WHERE e.isActive=true AND e.name=?1 and e.envId=?2")
    DnsEntity findByNameAndEnvId(String name, String envId);

    @Query("SELECT e FROM DnsEntity e WHERE e.isActive=true AND e.envId=?1")
    List<DnsEntity> findByEnvId(String envId);
}
