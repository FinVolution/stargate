package com.ppdai.stargate.dao;

import com.ppdai.stargate.po.HadoopConfigEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import javax.transaction.Transactional;
import java.util.List;

public interface HadoopConfigRepository extends BaseJpaRepository<HadoopConfigEntity, Long> {

    List<HadoopConfigEntity> findAllByEnvAndIsActiveTrue(String env);

    HadoopConfigEntity findByEnvAndNameAndIsActiveTrue(String env, String name);

    @Modifying
    @Transactional(rollbackOn = Exception.class)
    @Query("update HadoopConfigEntity a set a.isActive = false where a.id=?1")
    void deleteById(Long id);
}
