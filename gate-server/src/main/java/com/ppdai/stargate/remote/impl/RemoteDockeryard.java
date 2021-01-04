package com.ppdai.stargate.remote.impl;

import com.ppdai.dockeryard.client.ApiException;
import com.ppdai.dockeryard.client.api.ImageControllerApi;
import com.ppdai.dockeryard.client.model.ImageEntity;
import com.ppdai.dockeryard.client.model.ImagePageVO;
import com.ppdai.stargate.remote.RemoteDockerRepo;
import com.ppdai.stargate.utils.ConvertUtil;
import com.ppdai.stargate.vo.ImageVO;
import com.ppdai.stargate.vo.PageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class RemoteDockeryard implements RemoteDockerRepo {

    @Autowired
    private ImageControllerApi imageControllerApi;

    @Override
    public PageVO<ImageVO> getImagesByPage(String orgName, String appName, Integer page, Integer size) {
        PageVO imagePageVO = new PageVO();

        ImagePageVO pageVO = new ImagePageVO();
        List<ImageEntity> imageList = null;

        try {
            pageVO = imageControllerApi.getImagesByParamUsingGET(null, null, null, orgName, appName, null, page - 1, size);
            imageList = pageVO.getContent();

        } catch (ApiException e) {
            log.error("received an exception when try to read images by pages from remote docker registry.");
        } finally {
        }

        List<ImageVO> imageVOList = new ArrayList<>();
        if (imageList != null) {
            imageVOList = ConvertUtil.convert(imageList, this::imageMapper);
        }

        imagePageVO.setContent(imageVOList);
        if (pageVO.getTotalElements() != null) {
            imagePageVO.setTotalElements(pageVO.getTotalElements());
        }

        return imagePageVO;
    }

    @Override
    public List<ImageVO> getImageList(String appName) {
        List<ImageEntity> imageList = null;

        try {
            imageList = imageControllerApi.getImageByAppNameUsingGET(appName);

        } catch (ApiException e) {
            log.error("received an exception when try to fetch images by app name [" + appName + "] from remote docker registry.");
        } finally {
        }

        List<ImageVO> imageVOList = new ArrayList<>();
        if (imageList != null) {
            imageVOList = ConvertUtil.convert(imageList, this::imageMapper);
        }

        return imageVOList;
    }

    @Override
    public ImageVO getImageById(Long imageId) {
        ImageEntity imageEntity = null;

        try {
            imageEntity = imageControllerApi.getImageUsingGET(imageId);

        } catch (ApiException e) {
            log.error("received an exception when try to read image by id [" + imageId + "] from remote docker registry");
        } finally {
        }

        ImageVO imageVO = new ImageVO();
        if (imageEntity != null) {
            imageVO = imageMapper(imageEntity);
        }

        return imageVO;
    }

    private ImageVO imageMapper(ImageEntity imageEntity) {
        ImageVO imageVO = new ImageVO();
        BeanUtils.copyProperties(imageEntity, imageVO);

        imageVO.setName(imageEntity.getRepoName() + ":" + imageEntity.getTag());
        imageVO.setVersion(imageEntity.getTag());

//        Long insertTime = new Long(imageEntity.getInsertTime().toInstant().toEpochMilli());
        Date createdAt = new Date(new Date().toInstant().toEpochMilli());
        imageVO.setCreatedAt(createdAt);

//        Long updateTime = new Long(imageEntity.getInsertTime().toInstant().toEpochMilli());
        Date updatedAt = new Date(new Date().toInstant().toEpochMilli());
        imageVO.setUpdatedAt(updatedAt);

        return imageVO;
    }
}
