package com.ppdai.stargate.dao;

import com.ppdai.stargate.po.UserSecurityActionEntity;
import org.springframework.data.jpa.repository.Query;

public interface SecurityActionRepository extends BaseJpaRepository<UserSecurityActionEntity, Long> {

}
