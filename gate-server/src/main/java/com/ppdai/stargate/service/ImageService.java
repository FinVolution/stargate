package com.ppdai.stargate.service;

import com.ppdai.stargate.controller.response.MessageType;
import com.ppdai.stargate.dao.ApplicationRepository;
import com.ppdai.stargate.exception.BaseException;
import com.ppdai.stargate.po.ApplicationEntity;
import com.ppdai.stargate.po.GroupEntity;
import com.ppdai.stargate.remote.RemoteDockerRepo;
import com.ppdai.stargate.vo.ImageVO;
import com.ppdai.stargate.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ImageService {

    @Autowired
    private ApplicationRepository appRepo;

    @Autowired
    private RemoteDockerRepo remoteDockerRepo;

    private static String RESTART_SUFFIX = "-restart";

    /**
     * 获取镜像分页列表
     * @param orgName 组织名
     * @param appId 应用Id
     * @param page 页码
     * @param size 每页数量
     * @return
     */
    public PageVO<ImageVO> getImagesByPage(String orgName, String appId, Integer page, Integer size) {
        String appName = "";

        if (appId != null && !appId.equals("")) {
            ApplicationEntity app = appRepo.findByAppId(appId);
            if (app == null) {
                throw BaseException.newException(MessageType.ERROR, "指定的应用（appId=%s）不存在。", appId);
            }
            appName = app.getName();
        }

        return remoteDockerRepo.getImagesByPage(orgName, appName, page, size);
    }

    /**
     * 获取指定应用的镜像列表
     *
     * @param appName 应用名
     * @return 返回镜像列表
     */
    public List<ImageVO> getImagesByAppName(String appName) {
        List<ImageVO> imageVOList = remoteDockerRepo.getImageList(appName);
        return imageVOList.stream().filter(x -> !x.getVersion().endsWith(RESTART_SUFFIX))
                .collect(Collectors.toList());
    }

    public List<ImageVO> getImagesByAppName(String appName, Map<String, Date> recentUsedImages) {
        List<ImageVO> imageVOList = remoteDockerRepo.getImageList(appName);
        imageVOList = imageVOList.stream().filter(x -> !x.getVersion().endsWith(RESTART_SUFFIX))
                .collect(Collectors.toList());

        for (ImageVO imageVO : imageVOList) {
            if (recentUsedImages.containsKey(imageVO.getName())) {
                imageVO.setDeployAt(recentUsedImages.get(imageVO.getName()));
            }
        }

        return imageVOList;
    }

    /**
     * 获取指定id的镜像
     *
     * @param imageId 镜像id
     * @return 返回镜像列表
     */
    public ImageVO getImageById(Long imageId) {
        return remoteDockerRepo.getImageById(imageId);
    }
}
