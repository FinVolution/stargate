package com.ppdai.stargate.k8s;

import com.alibaba.fastjson.JSON;
import com.ppdai.stargate.vo.PodConfigVO;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class PodConfigVOTest {

    @Test
    public void generate() {
        PodConfigVO podConfigVO = new PodConfigVO();
        List<PodConfigVO.PodVolumeVO> podVolumeList = new ArrayList<>();

        PodConfigVO.PodVolumeVO podVolumeVO = new PodConfigVO.PodVolumeVO();
        podVolumeVO.setName("jacoco");
        podVolumeVO.setPath("/usr/local/jacocoagent.jar");
        podVolumeList.add(podVolumeVO);

        podVolumeVO = new PodConfigVO.PodVolumeVO();
        podVolumeVO.setName("finvpod");
        podVolumeVO.setPath("/usr/local/finvpod");
        podVolumeList.add(podVolumeVO);

        podConfigVO.setPodVolumeList(podVolumeList);

        List<PodConfigVO.PodVolumeMountVO> podVolumeMountList = new ArrayList<>();

        PodConfigVO.PodVolumeMountVO podVolumeMountVO = new PodConfigVO.PodVolumeMountVO();
        podVolumeMountVO.setName("jacoco");
        podVolumeMountVO.setMountPath("/usr/local/jacocoagent.jar");

        podVolumeMountList.add(podVolumeMountVO);

        podVolumeMountVO = new PodConfigVO.PodVolumeMountVO();
        podVolumeMountVO.setName("finvpod");
        podVolumeMountVO.setMountPath("/usr/local/finvpod");

        podVolumeMountList.add(podVolumeMountVO);
        podConfigVO.setPodVolumeMountList(podVolumeMountList);

        List<String> javaOpts = new ArrayList<>();
        javaOpts.add("-javaagent:/usr/local/jacocoagent.jar=includes=*,output=tcpserver,address=*,port=41606");
        javaOpts.add("-Dcat.plugins.path=/usr/local/finvpod/cat/cat-plugins -Dorg.aspectj.weaver.loadtime.configuration=file:/usr/local/finvpod/cat/aop.xml -javaagent:/usr/local/finvpod/cat/cat-javaagent-1.0-SNAPSHOT.jar -javaagent:/usr/local/finvpod/cat/aspectjweaver-1.8.2.jar");

        podConfigVO.setJavaOptsList(javaOpts);

        System.out.println(JSON.toJSONString(podConfigVO));
    }
}
