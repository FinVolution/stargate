package com.ppdai.stargate.manager;

import com.alibaba.fastjson.JSON;
import com.ppdai.stargate.constant.JobStatus;
import com.ppdai.stargate.constant.JobTypeEnum;
import com.ppdai.stargate.constant.OperatorType;
import com.ppdai.stargate.dao.JobRepository;
import com.ppdai.stargate.job.JobInfo;
import com.ppdai.stargate.job.task.TaskInfo;
import com.ppdai.stargate.po.JobEntity;
import com.ppdai.stargate.utils.ConvertUtil;
import com.ppdai.stargate.utils.IPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;

@Component
public class JobManager {

    @Autowired
    private JobRepository jobRepository;
    @Autowired
    private TaskManager taskManager;
    @Autowired
    private Environment environment;

    @Transactional
    public boolean lock(JobInfo jobInfo) {
        String port = environment.getProperty("server.port");
        String ip = IPUtil.localIP();
        int n = jobRepository.occupy(jobInfo.getId(), jobInfo.getVersion(), JobStatus.RUNNING, ip + ":" + port);
        jobRepository.flush();
        if (n > 0) {
            Optional<JobInfo> optional = getJobInfoById(jobInfo.getId());
            optional.ifPresent(newJobInfo -> ConvertUtil.convert(newJobInfo, jobInfo));
        }
        return n > 0;
    }

    /**
     * 创建任务
     *
     * @param jobInfo 任务信息
     * @return 返回创建后的任务信息
     */
    @Transactional
    public JobInfo createJobWithTasks(JobInfo jobInfo) {
        if (StringUtils.isEmpty(jobInfo.getName())) {
            jobInfo.setName(JobTypeEnum.defaultType.name());
        }
        jobInfo.setStatus(JobStatus.NEW);
        JobEntity jobEntity = convert(jobInfo);
        JobEntity newEntity = jobRepository.save(jobEntity);
        ConvertUtil.convert(newEntity, jobInfo);
        List<TaskInfo> taskInfos = taskManager.createTasks(jobInfo.getId(), jobInfo.getTaskInfos());
        jobInfo.setTaskInfos(taskInfos);
        return jobInfo;
    }

    public JobInfo createJobNoTasks(JobInfo jobInfo) {
        if (StringUtils.isEmpty(jobInfo.getName())) {
            jobInfo.setName(JobTypeEnum.defaultType.name());
        }
        JobEntity jobEntity = convert(jobInfo);
        JobEntity newEntity = jobRepository.save(jobEntity);
        ConvertUtil.convert(newEntity, jobInfo);
        return jobInfo;
    }

    //获取新建任务
    public List<JobInfo> getNewJob() {
        PageRequest pageRequest = new PageRequest(0, 10);
        List<JobEntity> jobEntities = jobRepository.findByStatus(JobStatus.NEW, pageRequest);
        return ConvertUtil.convert(jobEntities, this::convert);
    }

    //获取正在执行中的任务
    public List<JobInfo> getRunningJob() {
        PageRequest pageRequest = new PageRequest(0, 10);
        List<JobEntity> jobEntities = jobRepository.findByStatus(JobStatus.RUNNING, pageRequest);
        return ConvertUtil.convert(jobEntities, this::convert);
    }

    public void fetchJobTask(JobInfo jobInfo) {
        jobInfo.setTaskInfos(taskManager.getTaskInfosByJobId(jobInfo.getId()));
    }

    /**
     * 更新任务结果
     *
     * @param jobInfo 任务信息
     * @return 返回更新后的任务信息
     */
    @Transactional
    public JobInfo updateJob(JobInfo jobInfo) {
        JobEntity jobEntity = convert(jobInfo);
        jobRepository.save(jobEntity);
        return jobInfo;
    }

    public Boolean hasInProcessJobs(Long groupId) {
        List<JobStatus> inProcessStatues = new ArrayList<>();
        inProcessStatues.add(JobStatus.NEW);
        inProcessStatues.add(JobStatus.RUNNING);

        List<JobEntity> jobEntities = jobRepository.findByStatusAndGroupId(groupId, inProcessStatues);
        return jobEntities.size() != 0;
    }

    public Optional<JobInfo> getJobInfoById(Long id) {
        JobEntity jobEntity = jobRepository.findOne(id);
        if (jobEntity == null) {
            return Optional.empty();
        }
        return Optional.of(convert(jobEntity));
    }

    public JobInfo getCurrentJobByGroupId(Long groupId) {
        JobInfo jobInfo = new JobInfo();

        Sort sort = new Sort(Direction.DESC, "id");
        JobEntity job = jobRepository.findFirstByGroupId(groupId, sort);
        if (job != null) {
            Long jobId = job.getId();
            List<TaskInfo> taskList = taskManager.getTaskInfosByJobId(jobId);

            jobInfo = convert(job);
            jobInfo.setTaskInfos(taskList);
        }

        return jobInfo;
    }

    private JobInfo convert(JobEntity jobEntity) {
        JobInfo jobInfo = ConvertUtil.convert(jobEntity, JobInfo.class);
        if (!StringUtils.isEmpty(jobEntity.getDataMap())) {
            Map dataMap = JSON.parseObject(jobEntity.getDataMap(), Map.class);
            jobInfo.setDataMap(dataMap);
        }
        jobInfo.setOperationTypeDesc(OperatorType.valueOf(jobEntity.getOperationType()).getDescription());
        return jobInfo;
    }

    private JobEntity convert(JobInfo jobInfo) {
        JobEntity jobEntity = ConvertUtil.convert(jobInfo, JobEntity.class);
        if (jobInfo.getDataMap() != null) {
            jobEntity.setDataMap(JSON.toJSONString(jobInfo.getDataMap()));
        }
        return jobEntity;
    }
}
