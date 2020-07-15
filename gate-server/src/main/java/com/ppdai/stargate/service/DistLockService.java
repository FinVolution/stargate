package com.ppdai.stargate.service;

import com.ppdai.stargate.constant.LockEnum;
import com.ppdai.stargate.dao.GlobalLockRepository;
import com.ppdai.stargate.po.GlobalLockEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.Date;

@Service
public class DistLockService {

    @Autowired
    private GlobalLockRepository globalLockRepo;

    @Transactional
    public GlobalLockEntity tryLock(LockEnum lock, String owner, Long expiration) {
        GlobalLockEntity globalLockEntity = globalLockRepo.findLock(lock);

        Date dbTime = globalLockRepo.getDBTime();

        if (globalLockEntity == null) {
            globalLockEntity = new GlobalLockEntity();
            globalLockEntity.setLockKey(lock);
            globalLockEntity.setOwner(owner);
            globalLockEntity.setExpirationTime(dbTime.getTime() + expiration);
            globalLockRepo.save(globalLockEntity);
        } else {
            if (dbTime.getTime() <= globalLockEntity.getExpirationTime()) {
                if(!globalLockEntity.getOwner().equals(owner)){
                    globalLockEntity = null;
                }
            }
        }

        return globalLockEntity;
    }

    @Transactional
    public boolean lock(LockEnum lock, String owner, String oldOwner, Long expiration) {
        Date dbTime = globalLockRepo.getDBTime();
        int count = globalLockRepo.updateLockByName(lock, owner, oldOwner, dbTime.getTime() + expiration);
        return count == 1;
    }
}
