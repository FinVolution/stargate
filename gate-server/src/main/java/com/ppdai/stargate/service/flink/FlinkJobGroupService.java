package com.ppdai.stargate.service.flink;

import com.ppdai.stargate.dao.GroupRepository;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.service.JobService;
import com.ppdai.stargate.vi.FlinkJobVO;
import com.ppdai.stargate.vo.DeployGroupInfoVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class FlinkJobGroupService {

    @Autowired
    private GroupRepository groupRepo;

    @Autowired
    private FlinkService flinkService;

    @Autowired
    private JobService jobService;

    /**
     * 获取指定站点的发布组列表
     *
     * @return 返回发布组实体列表
     */
    public List<DeployGroupInfoVO> listGroupByEnvAndAppId(String env, String appId) {

        List<DeployGroupInfoVO> deployGroupInfoVOs = new ArrayList<>();
        groupRepo.findByEnvAndAppId(env, appId).forEach(group -> {
            DeployGroupInfoVO groupVO = new DeployGroupInfoVO();
            BeanUtils.copyProperties(group, groupVO);

            // 获取各个发布组的流量状态
            Long groupId = group.getId();

            FlinkJobVO flinkJobVO = flinkService.getFlinkJobStatusByGroupId(groupId);

            groupVO.setExpectedCount(flinkJobVO.getRunningTaskTotal());

            // 计算实际流量
            Integer activeCount = flinkJobVO.getRunningTaskTotal();

            // 注：total以发布系统的实例数为准，因为remoteRegistry注册中心的实例总数可能会有延迟，即已下线的实例但还在保留在注册中心里
            int total = groupVO.getInstanceCount();
            groupVO.setActiveCount(activeCount);
            if (total <= 0) {
                groupVO.setInstanceUpPercentage(0);
            } else {
                groupVO.setInstanceUpPercentage(100 * activeCount / total);
            }

            JobInfo jobInfo = jobService.getCurrentJobByGroupId(groupId);
            groupVO.setJobInfo(jobInfo);

            deployGroupInfoVOs.add(groupVO);

        });

        return deployGroupInfoVOs;
    }
}
