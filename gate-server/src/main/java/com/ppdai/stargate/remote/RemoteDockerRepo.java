package com.ppdai.stargate.remote;

import com.ppdai.stargate.vo.ImageVO;
import com.ppdai.stargate.vo.PageVO;

import java.util.List;

public interface RemoteDockerRepo {

    /**
     * 获取镜像分页列表
     * @param orgName 组织名
     * @param appName 应用名
     * @param page 页码
     * @param size 每页数量
     * @return 返回镜像分页列表
     */
    public PageVO<ImageVO> getImagesByPage(String orgName, String appName, Integer page, Integer size);

    /**
     * 获取指定应用的镜像列表
     *
     * @param appName 应用名字
     * @return 返回镜像列表
     */
    public List<ImageVO> getImageList(String appName);

    /**
     * 获取指定id的镜像
     *
     * @param imageId 镜像id
     * @return 返回镜像列表
     */
    public ImageVO getImageById(Long imageId);

}
