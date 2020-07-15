package com.ppdai.stargate.dao;

import com.ppdai.stargate.po.IpEntity;
import com.ppdai.stargate.vo.PageVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface IpRepository extends BaseJpaRepository<IpEntity, Long>, JpaSpecificationExecutor<IpEntity> {

    @Modifying(clearAutomatically = true)
    @Query("update IpEntity a set a.occupied = true where a.ip = ?1 and a.occupied = false and a.isActive = true")
    int occupy(String ip);

    @Query("select a from IpEntity a where a.isActive = true and a.id = ?1")
    IpEntity findById(Long id);

    @Query("select a from IpEntity a where a.ip = ?1 and a.isActive = true")
    IpEntity findByIp(String ip);

    @Query("select a from IpEntity a where a.ip = ?1")
    IpEntity findByIpEx(String ip);

    @Query("select a from IpEntity a where a.network = ?1 and a.occupied = false and a.isActive = true")
    List<IpEntity> findUnoccupiedIp(String network);

}
