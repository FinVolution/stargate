package com.ppdai.stargate.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.controller.response.Response;
import com.ppdai.stargate.po.GroupEntity;
import com.ppdai.stargate.po.JobEntity;
import com.ppdai.stargate.service.GroupService;
import com.ppdai.stargate.service.ImageService;
import com.ppdai.stargate.service.JobService;
import com.ppdai.stargate.vo.ImageVO;
import com.ppdai.stargate.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/images")
@Slf4j
public class ImageController {

    @Autowired
    private ImageService imageService;


    @Autowired
    private GroupService groupService;

    @Autowired
    private JobService jobService;

    @RequestMapping(value = "/condition", method = RequestMethod.GET)
    public Response<PageVO<ImageVO>> fetchImageListByPage(@RequestParam(value = "orgName", required = false) String orgName,
                                                          @RequestParam(value = "appId") String appId,
                                                          @RequestParam(value = "page") Integer page,
                                                          @RequestParam(value = "size") Integer size) {
        PageVO<ImageVO> imagePageVO = imageService.getImagesByPage(orgName, appId, page, size);
        return Response.mark(MessageType.SUCCESS, imagePageVO);
    }

    @RequestMapping(method = RequestMethod.GET)
    public Response<List<ImageVO>> fetchImagesByAppName(@RequestParam(value = "appName") String appName) {
        List<ImageVO> images = imageService.getImagesByAppName(appName);
        return Response.mark(MessageType.SUCCESS, images);
    }

    @RequestMapping(value = "/valuableImages", method = RequestMethod.GET)
    public Response<List<ImageVO>> valuableImages(@RequestParam(value = "appId") String appId,
                                                  @RequestParam(value = "appName") String appName,
                                                  @RequestParam(value = "env") String env) {
        Map<String, Date> recentUsedImages = new HashMap<>();

        List<JobEntity> jobEntities = jobService.findRecentUsedImages(env, appId);
        List<GroupEntity> groupEntities = groupService.findRecentUsedImages(env, appId);


        for (JobEntity jobEntity : jobEntities) {
            JSONObject jsonObject = JSON.parseObject(jobEntity.getDataMap());
            String image = jsonObject.getString("image");
            if (image != null && !recentUsedImages.containsKey(image)) {
                recentUsedImages.put(image, jobEntity.getInsertTime());
            }
        }

        for (GroupEntity groupEntity : groupEntities) {
            if (!recentUsedImages.containsKey(groupEntity.getReleaseTarget())) {
                recentUsedImages.put(groupEntity.getReleaseTarget(), groupEntity.getInsertTime());
            }
        }

        List<ImageVO> images = imageService.getImagesByAppName(appName, recentUsedImages);
        return Response.mark(MessageType.SUCCESS, images);
    }
}
