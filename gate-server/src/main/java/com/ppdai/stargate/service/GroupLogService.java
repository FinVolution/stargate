package com.ppdai.stargate.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ppdai.stargate.client.JsonHttpClient;
import com.ppdai.stargate.dao.JobRepository;
import com.ppdai.stargate.po.JobEntity;
import com.ppdai.stargate.utils.IPUtil;
import com.ppdai.stargate.vo.GroupLogVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.PostConstruct;
import java.io.*;

@Service
@Slf4j
public class GroupLogService {

    @Value("${stargate.task.log:}")
    private String taskLog;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private JsonHttpClient commonHttpClient;

    private String readFromLocal(String groupName, long jobId) throws IOException {
        File logFile = new File(taskLog, groupName + "/" + jobId + ".log");
        if (!logFile.exists()) {
            return "无法获取日志文件";
        }

        StringBuilder sb = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line).append("\r\n");
            }
        }

        return sb.toString();
    }

    private String readFromRemote(String groupName, long groupId, long jobId, String host) throws Exception {
        String url = "http://" + host + "/api/groups/releaselog?groupId=" + groupId + "&groupName=" + groupName + "&jobId=" + jobId + "&fromUI=false";

        JSONObject jsonObject = JSON.parseObject(commonHttpClient.get(url));
        return jsonObject.getJSONObject("details").getString("logs");
    }

    public GroupLogVO getReleaseLog(String groupName, long groupId, long jobId, boolean fromUI) {
        GroupLogVO groupLogVO = new GroupLogVO();
        groupLogVO.setGroup(groupName);

        try {
            JobEntity job;
            if (jobId > 0) {
                job = jobRepository.findOne(jobId);
            } else {
                job = jobRepository.findFirstByGroupId(groupId, new Sort(Sort.Direction.DESC, "id"));
            }

            if (job == null) {
                groupLogVO.setLogs("无法找到Job");
                return groupLogVO;
            }

            if (StringUtils.isEmpty(job.getAssignInstance())) {
                groupLogVO.setLogs("该Job没有被处理");
                return groupLogVO;
            }

            String[] ipPort = job.getAssignInstance().split(":");
            if (ipPort[0].equals(IPUtil.getLocalIP())) {
                // 从本地读日志
                groupLogVO.setLogs(readFromLocal(groupName, jobId));
            } else {
                // 不是从页面上来的,直接返回失败,防止递归调用
                if (!fromUI) {
                    groupLogVO.setLogs("无法找到日志");
                    return groupLogVO;
                }
                // 从远程读取
                groupLogVO.setLogs(readFromRemote(groupName, groupId, jobId, job.getAssignInstance()));
            }
        } catch (Exception ex) {
            groupLogVO.setLogs("读取日志发生异常: " + ex.getMessage());
            log.error(ex.getMessage(), ex);
        }

        return groupLogVO;
    }

    @PostConstruct
    public void init() {
        File dir = new File(taskLog);
        dir.mkdirs();
    }
}
