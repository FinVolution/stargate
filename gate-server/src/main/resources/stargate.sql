CREATE DATABASE IF NOT EXISTS `stargate` DEFAULT CHARSET utf8 COLLATE utf8_general_ci;

USE `stargate`;

DROP TABLE IF EXISTS `application`;
CREATE TABLE IF NOT EXISTS `application`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT '应用服务名',
  `description` varchar(128) NULL COMMENT '描述',
  `owner` varchar(512) NULL COMMENT '所有者',
  `department` varchar(64) NULL COMMENT '所属部门组织',
  `department_code` varchar(64) NULL COMMENT '所属部门组织代码',
  `cmdb_app_id` varchar(64) NOT NULL DEFAULT '0' COMMENT 'CMDB注册应用ID',
  `enable_ha` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否激活HA',
  `env_urls` varchar(1024) DEFAULT NULL COMMENT '各个env访问域名',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  `insert_by` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `app_id_UNIQUE` (`cmdb_app_id`),
  INDEX `idx_inserttime` (`insert_time`),
  INDEX `idx_updatetime` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='应用服务';

DROP TABLE IF EXISTS `environment`;
CREATE TABLE IF NOT EXISTS `environment`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cmdb_env_id` bigint(20) NOT NULL,
  `name` varchar(64) DEFAULT NULL COMMENT '环境名',
  `description` varchar(128) DEFAULT NULL COMMENT '描述',
  `consul` varchar(128) DEFAULT NULL COMMENT 'consul地址',
  `nginx` varchar(128) DEFAULT NULL COMMENT 'nginx地址',
  `dockeryard` varchar(128) DEFAULT NULL COMMENT '镜像仓库地址',
  `dns` varchar(128) DEFAULT NULL COMMENT 'dns地址',
  `is_in_use` tinyint(1) DEFAULT '0' COMMENT '是否激活管理',
  `enable_ha` tinyint(1) NOT NULL DEFAULT '0' COMMENT '是否激活HA',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  `insert_by` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_inserttime` (`insert_time`),
  INDEX `idx_updatetime` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='环境';

DROP TABLE IF EXISTS `sgroup`;
CREATE TABLE IF NOT EXISTS `sgroup`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT '发布组名',
  `app_id` varchar(64) NOT NULL COMMENT 'appId',
  `app_name` varchar(256) NOT NULL COMMENT 'app名称',
  `env` varchar(64) NULL COMMENT '所属环境',
  `release_target` varchar(64) NULL COMMENT '发布对象名',
  `instance_spec` varchar(64) NULL DEFAULT 'C1' COMMENT '实例规格C1/C2/C3 (根据CPU/MEM大小区分)',
  `port_count` int(8) NULL COMMENT '实例端口数',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  `insert_by` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name`),
  INDEX `idx_inserttime` (`insert_time`),
  INDEX `idx_updatetime` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='应用服务集群';

DROP TABLE IF EXISTS `instance`;
CREATE TABLE IF NOT EXISTS `instance`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(64) NOT NULL COMMENT '实例名',
  `env` varchar(64) DEFAULT NULL COMMENT '环境',
  `group_id` bigint(20) NOT NULL COMMENT '所属发布组ID',
  `app_id` varchar(64) DEFAULT NULL COMMENT '应用ID',
  `app_name` varchar(256) DEFAULT NULL COMMENT '应用名称',
  `slot_ip` varchar(256) DEFAULT NULL COMMENT 'SlotIp',
  `port` int(11) DEFAULT NULL COMMENT '端口',
  `env_vars` text COMMENT 'json格式的环境变量',
  `status` varchar(256) DEFAULT NULL COMMENT '实例状态',
  `has_pulled_in` tinyint(1) NOT NULL DEFAULT '1' COMMENT '是否发布系统已经拉入',
  `bitset` bigint(20) DEFAULT '0' COMMENT '用位表示的实例特性',
  `image` varchar(512) DEFAULT NULL COMMENT '实例镜像',
  `spec` varchar(64) DEFAULT NULL COMMENT '部署规格',
  `namespace` varchar(128) DEFAULT NULL COMMENT '对应k8s中的namespace',
  `zone` varchar(64) DEFAULT NULL COMMENT '部署区域',
  `release_time` timestamp NULL DEFAULT NULL COMMENT '实例发布时间',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  `insert_by` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `name_UNIQUE` (`name`),
  INDEX `idx_inserttime` (`insert_time`),
  INDEX `idx_updatetime` (`update_time`),
  INDEX `idx_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='应用服务instance实例';

DROP TABLE IF EXISTS `global_lock`;
CREATE TABLE IF NOT EXISTS `global_lock`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `lock_key` varchar(64) NOT NULL COMMENT '锁的键名',
  `expiration_time` bigint(20) NOT NULL COMMENT '锁的过期时间',
  `owner` varchar(128) NULL COMMENT '拥有者',
  `note` varchar(256) NULL COMMENT '备注',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  `insert_by` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `lock_key_UNIQUE` (`lock_key`),
  INDEX `idx_inserttime` (`insert_time`),
  INDEX `idx_updatetime` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='全局进程锁';


DROP TABLE IF EXISTS `audit_log`;
CREATE TABLE IF NOT EXISTS `audit_log`(
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_name` varchar(32) DEFAULT NULL COMMENT '发送请求者',
  `client_ip` varchar(32) DEFAULT NULL,
  `http_method` varchar(64) DEFAULT NULL COMMENT '请求方法：GET/POST/PUT/DELETE',
  `http_uri` varchar(256) DEFAULT NULL COMMENT '请求URI',
  `class_method` varchar(128) DEFAULT NULL COMMENT '调用方法',
  `class_method_args` varchar(1024) DEFAULT NULL COMMENT '调用方法参数',
  `class_method_return` varchar(1024) DEFAULT NULL COMMENT '调用方法返回值',
  `code` int(11) NOT NULL DEFAULT '0' COMMENT '返回码',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '插入时间',
  `insert_by` varchar(64) DEFAULT NULL,
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL,
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `idx_inserttime` (`insert_time`),
  INDEX `idx_updatetime` (`update_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='操作日志表';

DROP TABLE IF EXISTS `job`;
CREATE TABLE IF NOT EXISTS `job` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'job ID',
  `name` varchar(64) NOT NULL COMMENT 'job名称',
  `env` varchar(64) NOT NULL COMMENT '环境名称',
  `group_id` bigint(20) NOT NULL COMMENT '所属组ID',
  `app_id` varchar(64) NOT NULL COMMENT '所属appId',
  `app_name` varchar(64) NOT NULL COMMENT '所属app名称',
  `operation_type` varchar(45) NOT NULL COMMENT '操作类型',
  `assign_instance` varchar(45) DEFAULT NULL COMMENT '分配执行的实例',
  `thread_id` bigint(20) DEFAULT NULL COMMENT '执行线程的Id',
  `expire_time` int(11) DEFAULT NULL  COMMENT '超时时间',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `additional_info` text NULL COMMENT '更多信息',
  `data_map` text DEFAULT NULL COMMENT 'job 数据',
  `version` int DEFAULT 0 COMMENT '版本号',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `insert_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '最近修改者',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `group_id_idx` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='job表';

DROP TABLE IF EXISTS `task`;
CREATE TABLE IF NOT EXISTS `task` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT 'task ID',
  `name` varchar(64) NOT NULL COMMENT 'task名称',
  `description` varchar(128) NULL COMMENT 'task描述',
  `job_id` bigint(20) NOT NULL COMMENT '所属job ID',
  `instance_id` bigint(20) NULL DEFAULT NULL COMMENT '实例ID',
  `data_map` text DEFAULT NULL COMMENT 'task 数据',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `additional_info` text NULL COMMENT '更多信息',
  `step` int DEFAULT NULL  COMMENT '步骤',
  `expire_time` int(11) DEFAULT NULL  COMMENT '超时时间',
  `start_time` timestamp  NULL  COMMENT '开始时间',
  `end_time` timestamp NULL  COMMENT '结束时间',
  `insert_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `insert_by` varchar(64) DEFAULT NULL COMMENT '创建者',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '最近修改者',
  `is_active` tinyint(1) NOT NULL DEFAULT '1' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  INDEX `job_id_idx` (`job_id`),
  INDEX `idx_instance_id` (`instance_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='task表';

DROP TABLE IF EXISTS `ip`;
CREATE TABLE IF NOT EXISTS `ip` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `network` VARCHAR(64) NULL DEFAULT NULL COMMENT '网络',
  `network_segment` VARCHAR(64) NULL DEFAULT NULL COMMENT '网段',
  `ip` VARCHAR(64) NOT NULL COMMENT 'IP',
  `occupied` TINYINT(1) NOT NULL DEFAULT '0' COMMENT '是否占用',
  `insert_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '创建人',
  `insert_time` TIMESTAMP NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_active` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE INDEX `ip_UNIQUE` (`ip` ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='k8s集群ip池';

DROP TABLE IF EXISTS `resource`;
CREATE TABLE IF NOT EXISTS `resource` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `app_id` VARCHAR(64) NOT NULL COMMENT '应用ID',
  `app_name` VARCHAR(64) NULL DEFAULT NULL COMMENT '应用名',
  `env` VARCHAR(64) NULL DEFAULT NULL COMMENT '环境',
  `zone` VARCHAR(64) NULL DEFAULT NULL COMMENT '所属区域',
  `spec` VARCHAR(64) NULL DEFAULT NULL COMMENT '规格',
  `ip` VARCHAR(64) NOT NULL COMMENT 'IP',
  `pod_name` VARCHAR(64) NULL DEFAULT NULL COMMENT '实例名称',
  `is_static` TINYINT(1) NULL DEFAULT '0' COMMENT '是否为静态资源',
  `insert_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '创建人',
  `insert_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_active` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='应用资源';


DROP TABLE IF EXISTS `dns`;
CREATE TABLE IF NOT EXISTS `dns` (
  `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '自增ID',
  `name` VARCHAR(128) NOT NULL COMMENT '域名',
  `type` VARCHAR(64) NULL DEFAULT NULL COMMENT '类型',
  `content` VARCHAR(512) NULL DEFAULT NULL COMMENT '内容',
  `env_id` VARCHAR(16) NULL DEFAULT NULL COMMENT '环境标识',
  `ttl` INT(11) NULL DEFAULT NULL COMMENT 'timetolive',
  `insert_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '创建人',
  `insert_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` VARCHAR(64) NULL DEFAULT NULL COMMENT '更新人',
  `update_time` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `is_active` TINYINT(1) NOT NULL DEFAULT '1' COMMENT '逻辑删除',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='dns记录';
CREATE UNIQUE_INDEX `uniq_name_envid` ON `dns`(`name`, `env_id`);