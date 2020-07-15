package com.ppdai.stargate.service;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.dao.*;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.po.*;
import com.ppdai.stargate.remote.RemoteCmdb;
import com.ppdai.stargate.vo.InstanceSpecVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class TaskService {

    @Autowired
    private RemoteCmdb remoteCmdb;

    /**
     * 从Remote PAAS中获取instance实例规格列表
     */
    public List<InstanceSpecVO> readInstanceSpecs() {
        return remoteCmdb.fetchInstanceSpecs();
    }


}
