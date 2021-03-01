package com.ppdai.stargate.controller;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.ppdai.stargate.controller.response.HadoopConfigResp;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.po.HadoopConfigEntity;
import com.ppdai.stargate.service.HadoopConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Slf4j
@RestController
@RequestMapping(value = "/api/flink/hadoop")
public class HadoopClusterController {

    @Autowired
    private HadoopConfigService hadoopConfigService;

    @RequestMapping(value = "/configs", method = RequestMethod.GET)
    public Response<List<HadoopConfigResp>> listHadoopCluster(@RequestParam(value = "env") String env,
                                                              @RequestParam(value = "department", required = false) String department) {
        List<HadoopConfigEntity> hadoopConfigEntities;
        if (StringUtils.isBlank(department)) {
            hadoopConfigEntities = hadoopConfigService.listHadoopConfigs(env);
        } else {
            hadoopConfigEntities = hadoopConfigService.listHadoopConfigs(env, department);
        }
        List<HadoopConfigResp> hadoopConfigResps = hadoopConfigEntities.stream()
                .map(HadoopConfigResp::applyHadoopConfig).collect(toList());
        return Response.success(hadoopConfigResps);
    }

    @RequestMapping(value = "/configs", method = RequestMethod.POST)
    public Response<String> createHadoopCluster(MultipartFile coreSite, MultipartFile hdfsSite,
                                                String hadoopName, String env,
                                                String savepoint, String description, String department) {
        if (coreSite == null || hdfsSite == null) {
            return Response.error("创建hadoop配置失败:", "coreSite和hdfsSite不能为空");
        }
        String coreSiteStr;
        String hdfsSiteStr;
        try {
            coreSiteStr = CharStreams.toString(new InputStreamReader(coreSite.getInputStream(), Charsets.UTF_8));
            hdfsSiteStr = CharStreams.toString(new InputStreamReader(hdfsSite.getInputStream(), Charsets.UTF_8));
        } catch (IOException e) {
            log.error("添加hadoop配置失败：", e);
            return Response.error("创建hadoop配置失败:", e.getMessage());
        }
        HadoopConfigEntity hadoopConfigEntity = new HadoopConfigEntity();
        hadoopConfigEntity.setEnv(env);
        hadoopConfigEntity.setHdfsSite(hdfsSiteStr);
        hadoopConfigEntity.setCoreSite(coreSiteStr);
        hadoopConfigEntity.setName(hadoopName);
        hadoopConfigEntity.setDescription(description);
        hadoopConfigEntity.setDepartment(department);
        hadoopConfigEntity.setSavepoint(savepoint);
        try {
            hadoopConfigService.createConfig(hadoopConfigEntity);
        } catch (Exception e) {
            log.error("创建hadoop配置失败", e);
            return Response.error("创建hadoop配置失败:", e.getMessage());
        }
        return Response.success("创建成功");
    }

    @RequestMapping(value = "/configs", method = RequestMethod.PUT)
    public Response<String> updateHadoopCluster(@RequestParam(value = "id") Long id,
                                                @RequestParam(value = "savepoint") String savepoint,
                                                @RequestParam(value = "description") String description,
                                                @RequestParam(value = "department") String department) {
        HadoopConfigEntity hadoopConfigEntity = hadoopConfigService.findHadoopConfigById(id);
        hadoopConfigEntity.setDescription(description);
        hadoopConfigEntity.setSavepoint(savepoint);
        hadoopConfigEntity.setDepartment(department);
        try {
            hadoopConfigService.updateConfig(hadoopConfigEntity);
        } catch (Exception e) {
            log.error("创建hadoop配置失败", e);
            return Response.error("创建hadoop配置失败:", e.getMessage());
        }
        return Response.success("创建成功");
    }

    @DeleteMapping(value = "/configs")
    public Response<String> deleteHadoop(@RequestParam(value = "id") Long id,
                                         @RequestParam(value = "env") String env) {
        try {
            hadoopConfigService.deleteHadoopConfig(env, id);
        } catch (Exception e) {
            log.error("删除hadoop失败：", e);
            return Response.error("删除hadoop配置失败:" + e.getMessage());
        }
        return Response.success("删除hadoop配置成功");
    }

}
