package com.ppdai.stargate.service;

import com.ppdai.stargate.dao.EnvironmentRepository;
import com.ppdai.stargate.po.EnvironmentEntity;
import com.ppdai.stargate.remote.RemoteCmdb;
import com.ppdai.stargate.utils.ConvertUtil;
import com.ppdai.stargate.vo.EnvVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EnvService {

    @Autowired
    private RemoteCmdb remoteCmdb;
    @Autowired
    private EnvironmentRepository environmentRepo;

    public List<EnvVO> getAllEnvironments() {
        List<EnvironmentEntity> environmentList = environmentRepo.findAllEnvs();
        return ConvertUtil.convert(environmentList, EnvVO.class);
    }

    /**
     * 获取所有可用的环境列表（除去被禁用的环境）
     *
     * @return 可用的环境列表
     */
    public List<EnvVO> getInUseEnvironments() {
        List<EnvironmentEntity> environmentList = environmentRepo.findInUseEnvs();
        return ConvertUtil.convert(environmentList, EnvVO.class);
    }

    /**
     * 根据环境名获取可用的环境信息
     *
     * @return 可用的环境信息
     */
    public EnvVO queryInUseEnvironment(String envName) {
        EnvironmentEntity environmentEntity = environmentRepo.findInUseEnvByName(envName);
        if (environmentEntity == null) {
            return null;
        }

        return ConvertUtil.convert(environmentEntity, EnvVO.class);
    }

    public Boolean setEnvironmentInUse(Long envId, Boolean isInUse) {
        Boolean bSuccess = false;

        EnvironmentEntity environment = environmentRepo.findOne(envId);
        if (environment != null) {
            environment.setIsInUse(isInUse);
            environmentRepo.save(environment);
            bSuccess = true;
        }

        return bSuccess;
    }

    public Boolean setEnvironmentEnableHa(Long envId, Boolean enable) {
        Boolean bSuccess = false;

        EnvironmentEntity environment = environmentRepo.findOne(envId);
        if (environment != null) {
            environment.setEnableHa(enable);
            environmentRepo.save(environment);
            bSuccess = true;
        }

        return bSuccess;
    }

    public Boolean syncEnvs() {
        Boolean bSuccess = false;

        try {
            final int[] syncEnvCnt = {0};

            List<EnvVO> envVOs = remoteCmdb.fetchEnvironments();
            envVOs.forEach(envVO -> {
                EnvironmentEntity environment = environmentRepo.findByCmdbEnvId(envVO.getId());

                // 若数据库没有该条环境，则创建
                if (environment == null) {
                    environment = new EnvironmentEntity();
                    environment.setIsInUse(false);
                    environment.setEnableHa(false);
                }

                String oldEnvName = environment.getName();

                BeanUtils.copyProperties(envVO, environment, "id", "isInUse", "enableHa", "insertTime");
                environment.setCmdbEnvId(envVO.getId());
                environmentRepo.save(environment);
                syncEnvCnt[0]++;

                //todo 更新其他表的环境名
                if (oldEnvName != null && !envVO.getName().equals(oldEnvName)) {

                }
            });

            bSuccess = Boolean.TRUE;

            log.info("同步环境数={},", syncEnvCnt[0]);
        } catch (Exception e) {
            log.error("同步环境列表时发生错误, err=" + e.getMessage(), e);
        }

        return bSuccess;
    }


}
