package com.ppdai.stargate.vo;

import lombok.Data;

import java.util.List;

@Data
public class PodConfigVO {

    private List<PodVolumeVO> podVolumeList;
    private List<PodVolumeMountVO> podVolumeMountList;
    private List<String> javaOptsList;

    @Data
    public static class PodVolumeVO {
        private String name;
        private String path;
    }

    @Data
    public static class PodVolumeMountVO {
        private String name;
        private String mountPath;
    }
}
