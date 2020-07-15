package com.ppdai.stargate.remote.impl;

import com.alibaba.fastjson.JSON;
import com.ppdai.stargate.client.ConsulClient;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.po.InstanceEntity;
import com.ppdai.stargate.remote.RemoteRegistry;
import com.ppdai.stargate.utils.ConvertUtil;
import com.ppdai.stargate.vo.ConsulInstanceVO;
import com.ppdai.stargate.vo.InstanceV2VO;
import com.ppdai.stargate.vo.InstanceVO;
import com.ppdai.stargate.vo.NginxInstanceAttrVO;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class RemoteConsul implements RemoteRegistry  {

    protected abstract String getUpstream(String domain);
    protected abstract ConsulClient getConsulClient(String env, String appId, String appName);

    private static String RESTART_SUFFIX = "-restart";

    @Override
    public void register(String env, String appId, String appName, String domain, InstanceEntity instanceEntity) {
        try {
            ConsulClient consulClient = getConsulClient(env, appId, appName);
            NginxInstanceAttrVO nginxInstanceAttrVO = new NginxInstanceAttrVO();
            nginxInstanceAttrVO.setName(instanceEntity.getName());
            nginxInstanceAttrVO.setDown(1);

            consulClient.putInstance(getUpstream(domain),
                    instanceEntity.getSlotIp() + ":" + instanceEntity.getPort(),
                    nginxInstanceAttrVO);

            log.info("注册nginx实例成功: instance={}", instanceEntity);
        } catch (Exception ex) {
            log.error("注册nginx实例失败: err=" + ex.getMessage(), ex);
            throw BaseException.newException(MessageType.ERROR, "注册nginx实例失败: err=%s", ex.getMessage());
        }
    }

    @Override
    public void deregister(String env, String appId, String appName, String domain, InstanceEntity instanceEntity) {
        try {
            ConsulClient consulClient = getConsulClient(env, appId, appName);

            consulClient.deleteInstance(getUpstream(domain),
                    instanceEntity.getSlotIp() + ":" + instanceEntity.getPort());

            log.info("注销nginx实例成功: instance={}", instanceEntity);
        } catch (Exception ex) {
            log.error("注销nginx实例失败: err=" + ex.getMessage(), ex);
            throw BaseException.newException(MessageType.ERROR, "注销nginx实例失败: err=%s", ex.getMessage());
        }
    }

    @Override
    public void pullIn(String domain, InstanceEntity instance) {
        ConsulClient consulClient = getConsulClient(instance.getEnv(), instance.getAppId(), instance.getAppName());
        NginxInstanceAttrVO nginxInstanceAttrVO = new NginxInstanceAttrVO();
        nginxInstanceAttrVO.setName(instance.getName());
        nginxInstanceAttrVO.setDown(0);

        try {
            consulClient.putInstance(getUpstream(domain), instance.getSlotIp() + ":" + instance.getPort(), nginxInstanceAttrVO);
            log.info("实例上线成功: instance={}", instance.getName());
        } catch (Exception ex) {
            log.error("实例上线失败: instance={}, err={}", instance.getName(), ex.getMessage(), ex);
            throw BaseException.newException(MessageType.ERROR, "实例上线失败: instance=%s, err=%s", instance.getName(), ex.getMessage());
        }
    }

    @Override
    public void pullOut(String domain, InstanceEntity instance) {
        ConsulClient consulClient = getConsulClient(instance.getEnv(), instance.getAppId(), instance.getAppName());
        NginxInstanceAttrVO nginxInstanceAttrVO = new NginxInstanceAttrVO();
        nginxInstanceAttrVO.setName(instance.getName());
        nginxInstanceAttrVO.setDown(1);

        try {
            consulClient.putInstance(getUpstream(domain), instance.getSlotIp() + ":" + instance.getPort(), nginxInstanceAttrVO);
            log.info("实例下线成功: instance={}", instance.getName());
        } catch (Exception ex) {
            log.error("实例下线失败: instance={}, err={}", instance.getName(), ex.getMessage(), ex);
            throw BaseException.newException(MessageType.ERROR, "实例下线失败: instance=%s, err=%s", instance.getName(), ex.getMessage());
        }
    }

    @Override
    public List<InstanceVO> getInstanceStatus(String environment, String domain, List<InstanceEntity> instances) {

        List<InstanceVO> instanceVOS = new ArrayList<>();

        try {
            ConsulClient consulClient = getConsulClient(environment,
                    instances.get(0).getAppId(), instances.get(0).getAppName());

            List<ConsulInstanceVO> consulInstanceVOS = new ArrayList<>();
            try {
                consulInstanceVOS = consulClient.getInstances(getUpstream(domain));
            } catch (Exception ex) {
                log.error("查询consul实例失败: env={}, app={}, err={}", environment, instances.get(0).getAppName(), ex.getMessage(), ex);
            }

            for (InstanceEntity instance : instances) {
                InstanceVO instanceVO = ConvertUtil.convert(instance, InstanceVO.class);

                instanceVO.setIp(instance.getSlotIp());

                // 若实例表中releaseTime为空，则使用insertTime代替
                if (instance.getReleaseTime() == null) {
                    instanceVO.setReleaseTime(instance.getInsertTime());
                }

                // 添加实例表中的image信息，去掉重启后缀
                String image = instance.getImage();
                if (image != null) {
                    if (image.endsWith(RESTART_SUFFIX)) {
                        image = image.substring(0, image.length() - RESTART_SUFFIX.length());
                    }
                    instanceVO.setReleaseTarget(image);

                    String[] splits = image.split(":");
                    instanceVO.setReleaseVersion(splits[1]);
                }

                // 遍历consul实例，若找到则设置consul的流量状态
                for (ConsulInstanceVO consulInstanceVO : consulInstanceVOS) {
                    if (StringUtils.isEmpty(consulInstanceVO.getValue())) {
                        continue;
                    }

                    String attr = new String(Base64.decodeBase64(consulInstanceVO.getValue()));
                    NginxInstanceAttrVO nginxInstanceAttrVO = JSON.parseObject(attr, NginxInstanceAttrVO.class);

                    if (StringUtils.isEmpty(nginxInstanceAttrVO.getName())) {
                        continue;
                    }

                    if (instance.getName().equals(nginxInstanceAttrVO.getName())) {
                        instanceVO.setStatus(nginxInstanceAttrVO.getDown() > 0 ? 0 : 1);
                        instanceVO.setOpsPulledIn(instanceVO.getStatus());
                        break;
                    }
                }

                instanceVOS.add(instanceVO);
            }

            return instanceVOS;
        } catch (Exception ex) {
            log.error("从consul实例获取失败: err=" + ex.getMessage(), ex);
        }

        return instanceVOS;
    }

    @Override
    public InstanceV2VO getInstanceStatus(String domain, String environment, String appId, String appName, InstanceEntity instanceEntity) {
        InstanceV2VO instanceV2VO = ConvertUtil.convert(instanceEntity, InstanceV2VO.class);

        instanceV2VO.setInstanceIp(instanceEntity.getSlotIp());

        // 若实例表中releaseTime为空，则使用insertTime代替
        if (instanceEntity.getReleaseTime() == null) {
            instanceV2VO.setReleaseTime(instanceEntity.getInsertTime());
        }

        try {
            ConsulClient consulClient = getConsulClient(environment, appId, appName);

            List<ConsulInstanceVO> consulInstanceVOS = consulClient.getInstances(getUpstream(domain));

            // 遍历consul实例，若找到则设置consul的流量状态
            for (ConsulInstanceVO consulInstanceVO : consulInstanceVOS) {
                if (StringUtils.isEmpty(consulInstanceVO.getValue())) {
                    continue;
                }

                String attr = new String(Base64.decodeBase64(consulInstanceVO.getValue()));
                NginxInstanceAttrVO nginxInstanceAttrVO = JSON.parseObject(attr, NginxInstanceAttrVO.class);

                if (StringUtils.isEmpty(nginxInstanceAttrVO.getName())) {
                    continue;
                }

                if (instanceEntity.getName().equals(nginxInstanceAttrVO.getName())) {
                    instanceV2VO.setOpsPulledIn(nginxInstanceAttrVO.getDown() > 0 ? false : true);
                    break;
                }
            }
        } catch (Exception ex) {
            log.error("查询consul实例失败: env={}, app={}, err={}", environment, appName, ex.getMessage(), ex);
        }

        return instanceV2VO;
    }
}
