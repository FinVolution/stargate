package com.ppdai.stargate.constant;

public final class FlinkConfigOptions {

    public static final FlinkConfig<String> KUBE_CONFIG = FlinkConfig.<String>builder().key("flink.kube.config")
            .defaultValue(null).build();
    public static final FlinkConfig<Integer> JOBMANAGER_CPU = FlinkConfig.<Integer>builder().key("flink.jobmanager.cpu")
            .defaultValue(2).build();
    public static final FlinkConfig<String> JOBMANAGER_HEAPSIZE = FlinkConfig.<String>builder().key("flink.jobmanager.heapsize")
            .defaultValue("1024m").build();

    public static final FlinkConfig<String> JOBMANAGER_PROCESSSIZE = FlinkConfig.<String>builder().key("flink.jobmanager.processsize")
            .defaultValue("2048m").build();

    public static final FlinkConfig<String> HADOOP_CONFIG_MAP = FlinkConfig.<String>builder().key("kubernetes.hadoop.conf.config-map.name")
            .defaultValue("flink-job-hadoop").build();

    public static final FlinkConfig<String> SAVEPOINT_BASE = FlinkConfig.<String>builder().key("flink.job.savepoint.base")
            .defaultValue("hdfs://nameservice1/user/flink/checkpoint").build();
}
