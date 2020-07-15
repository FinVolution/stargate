package com.ppdai.stargate.dao;

import com.ppdai.stargate.constant.LockEnum;
import com.ppdai.stargate.po.GlobalLockEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

public interface GlobalLockRepository extends BaseJpaRepository<GlobalLockEntity, Long> {

    @Query("SELECT e FROM GlobalLockEntity e WHERE e.isActive=true AND e.lockKey=?1")
    GlobalLockEntity findLock(LockEnum lock);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("UPDATE GlobalLockEntity e SET e.expirationTime=?4, e.owner=?2 WHERE e.lockKey=?1 and e.owner=?3")
    int updateLockByName(LockEnum lock, String owner, String oldOwner, Long expiration);

    @Query(value = "select sysdate()", nativeQuery = true)
    Date getDBTime();
}
