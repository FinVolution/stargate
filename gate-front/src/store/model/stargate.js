import * as types from '../mutation-types'
import {api} from '../../api'
import localStorageUtil from '../../utils/localStorageUtil'

// initial state
const state = {
    apps: [],
    myApps: [],
    appList: [],
    appCount: 0,
    envList: [],
    envActiveList: [],
    orgList: [],
    imageList: [],
    imageCount: 0,
    auditLogList: [],
    auditLogCount: 0,
    currentSite: {},
    currentGroup: {},
    currentEnv: null,
    currentAppId: null,
    appInstanceList: [],
    releaseGroups: [],
    releaseGroupStatus: {
        latestJobInfo: {},
        instanceList: []
    },
    promptMessage: {
        code: null,
        details: null
    },
    releaseHistory: [],
    releaseHistoryCount: 0,
    releaseRecord: [],
    releaseRecordCount: 0,
    releaseLog: {},
    containerLog: {},
    releaseStatus: [],
    appQuotas: [],
    appQuotaStatus: [],
    applyList: [],
    applyCount: 0,
    userList: [],
    envZoneList: [],
    valuableImageList: [],
    ipList: [],
    ipCount: 0,
    resourceList: [],
    resourceCount: 0,
    resourceQuotaStatus: [],
    availableResources: [],
    releaseTypeList: [],
    hostInstanceList: [],
    cloudInstanceList: [],
    cloudInstanceCount: 0,
    cloudInstanceStatus: {},
    cloudInstanceLog: null,
    instanceCountInfo: [],
    appSetting: {},
    execCommandResult: {},
    dnsList: [],
    dnsCount: 0,
    hadoopConfigs: []
};

// getters
const getters = {
    getApps: state => state.apps,
    getMyApps: state => state.myApps,
    getAppList: state => state.appList,
    getAppCount: state => state.appCount,
    getEnvList: state => state.envList,
    getEnvActiveList: state => state.envActiveList,
    getCurrentSite: state => state.currentSite,
    getCurrentGroup: state => state.currentGroup,
    getCurrentEnv: state => state.currentEnv,
    getCurrentAppId: state => state.currentAppId,
    getOrgList: state => state.orgList,
    getImageList: state => state.imageList,
    getValuableImageList: state => state.valuableImageList,
    getImageCount: state => state.imageCount,
    getHadoopConfigs: state => state.hadoopConfigs,
    getAuditLogList: state => state.auditLogList,
    getAuditLogCount: state => state.auditLogCount,
    getAppInstanceList: state => state.appInstanceList,
    getReleaseGroups: state => state.releaseGroups,
    getReleaseGroupJobStatus: state => state.releaseGroupStatus.latestJobInfo,
    getReleaseGroupInstances: state => state.releaseGroupStatus.instanceList,
    getPromptMessage: state => state.promptMessage,
    getReleaseHistory: state => state.releaseHistory,
    getReleaseHistoryCount: state => state.releaseHistoryCount,
    getReleaseRecord: state => state.releaseRecord,
    getReleaseRecordCount: state => state.releaseRecordCount,
    getReleaseLog: state => state.releaseLog,
    getContainerLog: state => state.containerLog,
    getReleaseStatus: state => state.releaseStatus,
    getAppQuotaStatus: state => state.appQuotaStatus,
    getApplyList: state => state.applyList,
    getApplyCount: state => state.applyCount,
    getUserList: state => state.userList,
    getEnvZoneList: state => state.envZoneList,
    getIpList: state => state.ipList,
    getIpCount: state => state.ipCount,
    getResourceList: state => state.resourceList,
    getResourceCount: state => state.resourceCount,
    getResourceQuotaStatus: state => state.resourceQuotaStatus,
    getAvailableResources: state => state.availableResources,
    getReleaseTypeList: state => state.releaseTypeList,
    getHostInstanceList: state => state.hostInstanceList,
    getCloudInstanceList: state => state.cloudInstanceList,
    getCloudInstanceCount: state => state.cloudInstanceCount,
    getCloudInstanceStatus: state => state.cloudInstanceStatus,
    getCloudInstanceLog: state => state.cloudInstanceLog,
    getInstanceCountInfo: state => state.instanceCountInfo,
    getAppSetting: state => state.appSetting,
    getExecCommandResult: state => state.execCommandResult,
    getDnsList: state => state.dnsList,
    getDnsCount: state => state.dnsCount
};

// actions
const actions = {

    createHadoopConfig({dispatch}, data) {
        api.gateService.createHadoopConfig(data.formData).then(function (resp) {
            console.log("------");
            console.log(resp);
            dispatch('fetchHadoopConfigs', data.env);
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },
    fetchHadoopConfigs({commit, dispatch}, data) {
        api.gateService.getHadoopConfigsByEnv(data).then(function (resp) {
            console.log(resp.data.details);
            commit(types.REFRESH_HADOOPCONFIG_LIST, resp.data.details);
            console.log("----====---=-=")
        }.bind(this)).catch(function (err) {
            console.log("error");
            console.error(err);
            // dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 用户切换环境时，将状态保存到store和localStorage
     * @param commit    store state更新提交者
     * @param data      环境名
     */
    refreshCurrentEnv({commit}, data) {
        commit(types.REFRESH_CURRENT_ENV, data);
        localStorageUtil.saveEnvironment(data);
    },

    /**
     * 用户切换应用时，将状态保存到store和localStorage
     * @param commit    store state更新提交者
     * @param data      appId
     */
    refreshCurrentAppId({commit}, data) {
        commit(types.REFRESH_CURRENT_APP_ID, data);
        localStorageUtil.saveAppId(data);
    },

    /**
     * 发送请求到后端服务，获取app列表
     * @param commit     store state更新提交者
     * @param dispatch  store action分发者
     */
    fetchAppList ({commit, dispatch}) {
        api.gateService.getAppList().then(function (resp) {
            if (Array.isArray(resp.data.details)) {
                commit(types.REFRESH_APP_LIST, resp.data.details);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 发送请求到后端服务，获取environment列表
     * @param commit     store state更新提交者
     * @param dispatch  store action分发者
     */
    fetchEnvList ({commit, dispatch}) {
        api.gateService.getEnvList().then(function (resp) {
            if (Array.isArray(resp.data.details)) {
                commit(types.REFRESH_ENV_LIST, resp.data.details);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 发送请求到后端服务，获取激活的environment列表
     * @param commit     store state更新提交者
     * @param dispatch  store action分发者
     */
    fetchActiveEnvList ({commit, dispatch}) {
        api.gateService.getActivateEnvList().then(function (resp) {
            if (Array.isArray(resp.data.details)) {
                commit(types.REFRESH_ENV_ACTIVE_LIST, resp.data.details);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 发送请求到后端服务，激活environment列表
     *
     * @param dispatch  store action分发者
     * @param data      附带应用信息的数据对象，data格式为 {envId: x, activated: y}
     */
    activateEnv ({dispatch}, data) {
        api.gateService.activateEnv(data).then(function (resp) {
            dispatch("fetchEnvList");
            dispatch('fetchActiveEnvList');
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    enableHa ({dispatch}, data) {
        api.gateService.enableHa(data).then(function (resp) {
            dispatch("fetchEnvList");
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    disableHa ({dispatch}, data) {
        api.gateService.disableHa(data).then(function (resp) {
            dispatch("fetchEnvList");
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 发送请求到后端服务，获取指定发布组的instances列表
     * @param commit     store state更新提交者
     * @param dispatch  store action分发者
     * @param data      附带应用信息的数据对象，data格式为 {groupId: x}，其中x为指定发布组id
     */
    fetchInstancesByGroupId ({commit, dispatch}, data) {
        api.gateService.getInstancesByGroupId(data).then(function (resp) {
            if (resp.data.code >= 0 && Array.isArray(resp.data.details)) {
                commit(types.REFRESH_GROUP_INSTANCE_LIST, resp.data.details);
            } else {
                commit(types.REFRESH_GROUP_INSTANCE_LIST, []);
                dispatch("displayPromptByResponseMsg", resp);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchInstancesByCondition({commit, dispatch}, data) {
        api.gateService.getInstancesByCondition(data).then(function (resp) {
            commit(types.REFRESH_APP_INSTANCE_LIST, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 发送请求到后端服务，根据环境和appId获取相应站点发布组列表
     */
    fetchReleseGroupsByEnvAndAppId({commit, dispatch}, data){
        api.gateService.getGroupsByEnvAndAppId(data).then(function (resp) {
            if (resp.data.code >= 0 && Array.isArray(resp.data.details)) {
                commit(types.REFRESH_RELEASE_GROUP_LIST, resp.data.details);
            } else {
                commit(types.REFRESH_RELEASE_GROUP_LIST, []);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 发送请求到后端服务，根据GroupID获取相应发布组状态
     * @param commit     store state更新提交者
     * @param dispatch  store action分发者
     * @param data      附带应用信息的数据对象，data格式为 {groupId: x}，其中x为发布组ID
     */
    fetchReleaseGroupStatus({commit, dispatch}, data){
        api.gateService.getGroupStatus(data).then(function (resp) {
            if (resp.data.code >= 0) {
                commit(types.REFRESH_GROUP_JOB_STATUS, resp.data.details);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 发送请求到后端服务，根据环境和appId创建新发布组
     */
    createReleaseGroup({dispatch}, data){
        api.gateService.createReleaseGroup(data).then(function (resp) {
            dispatch("fetchReleseGroupsByEnvAndAppId", {env: data.env, appId: data.appId});
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 发送请求到后端服务，删除指定发布组
     */
    removeReleaseGroup({dispatch}, data){
        api.gateService.removeReleaseGroup(data).then(function (resp) {
            dispatch("fetchReleseGroupsByEnvAndAppId", {env: data.env, appId: data.appId});
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 发送请求到后端服务，指定发布组的新容量大小，对指定站点发布组进行扩容、缩容
     * @param dispatch  store action分发者
     * @param data      附带应用信息的数据对象，data格式为 {groupId: x, operatorType: y, newSize: z, instanceNames: w}，其中x为发布组ID, y为操作类型，z为新容量大小, w为实例名列表
     */
    resizeReleaseGroup({dispatch}, data){
        api.gateService.resizeReleaseGroup(data).then(function (resp) {
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 发送请求到后端服务，对指定发布组进行实例流量调节
     * @param dispatch  store action分发者
     * @param data      附带应用信息的数据对象，data格式为 {groupId: x, operatorType: y, instanceNames: z}，其中x为发布组ID, y为操作类型，z为实例名列表
     */
    pullReleaseGroupInstance({dispatch}, data){
        api.gateService.pullReleaseGroupInstance(data).then(function (resp) {
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 发送请求到后端服务，对指定发布组的目标实例进行版本更新
     * @param dispatch  store action分发者
     * @param data      附带应用信息的数据对象，data格式为 {groupId: x, instanceNames: y, image: z}，其中x为发布组ID, y为实例名列表，z为目标镜像
     */
    updateReleaseGroupInstance({dispatch}, data){
        api.gateService.updateReleaseGroupInstance(data).then(function (resp) {
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 发送请求到后端服务，对指定发布组的目标实例进行重启
     * @param dispatch  store action分发者
     * @param data      附带应用信息的数据对象，data格式为 {groupId: x, instanceNames: y}，其中x为发布组ID, y为实例名列表
     */
    restartReleaseGroupInstance({dispatch}, data){
        api.gateService.restartReleaseGroupInstance(data).then(function (resp) {
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 根据后端服务返回的响应消息，更新提示消息，使得在UI上显示
     * @param commit     store state更新提交者
     * @param response  后端服务返回的响应消息
     */
    displayPromptByResponseMsg({commit}, response){
        let details = response.data.details;
        if (details == null) {
            details = response.data.msg;
        }
        if (response != null && response.status != null && response.status == 200) {
            commit(types.REFRESH_PROMPT_MESSAGE, {code: response.data.code, details: details});
        } else {
            let errorMsg = "请求失败，";
            if (response == null) {
                errorMsg += "访问后端服务返回异常。";
            } else if (response.status != null && details != null) {
                // 发生后端处理过的错误
                errorMsg += "返回码：" + response.status + "，返回消息：" + details;
            } else if (response.status != null && response.status >= 400 && response.status < 500) {
                // 发生4XX错误
                errorMsg += "返回码：" + response.status + "，返回消息：" + response.statusText;
            } else if (response.status != null && response.status >= 500 && response.status < 600) {
                // 发生5XX错误
                errorMsg += "请检查后端服务是否工作正常。";
                errorMsg += "返回码：" + response.status + "，返回消息：" + response.statusText;
            } else if (response.status != null) {
                errorMsg += "返回码：" + response.status + "，返回消息：" + response.statusText;
            } else {
                errorMsg += "请检查后端服务是否工作正常。";
                errorMsg += "消息：" + response;
            }

            commit(types.REFRESH_PROMPT_MESSAGE, {code: -1, details: errorMsg});
        }
    },

    /**
     *发送请求到后端服务，根据环境和appId获取站点信息
     */
    fetchSiteByEnvAndAppId({commit, dispatch}, data){
        api.gateService.getSiteByEnvAndAppId(data).then(function (resp) {
            if (resp.data && resp.data.code >= 0) {
                commit(types.REFRESH_CURRENT_SITE, resp.data.details);
            } else {
                commit(types.REFRESH_CURRENT_SITE, {});
                dispatch("displayPromptByResponseMsg", resp);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     *发送请求到后端服务，根据groupId获取发布组信息
     */
    fetchGroupById({commit, dispatch}, data){
        api.gateService.getGroupById(data).then(function (resp) {
            if (resp.data && resp.data.code >= 0) {
                commit(types.REFRESH_CURRENT_GROUP, resp.data.details);
            } else {
                commit(types.REFRESH_CURRENT_GROUP, {});
                dispatch("displayPromptByResponseMsg", resp);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 发送请求到后端服务，获取指定应用的镜像列表
     *
     * @param commit    store state更新提交者
     * @param dispatch  store action分发者
     * @param data      附带镜像查询信息的数据对象，data格式为{ appId:x }，其中x为查询的指定应用名字
     */
    fetchImageList({commit, dispatch}, data){
        api.gateService.getImageList(data).then(function (resp) {
            if (resp.data.code >= 0 && Array.isArray(resp.data.details)) {
                commit(types.REFRESH_IMAGE_LIST, resp.data.details);
            } else {
                // 清空镜像列表，并显示提示信息，告知用户错误
                commit(types.REFRESH_IMAGE_LIST, []);
                dispatch("displayPromptByResponseMsg", resp);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchValuableImageList({commit, dispatch}, data){
        api.gateService.getValuableImageList(data).then(function (resp) {
            if (resp.data.code >= 0 && Array.isArray(resp.data.details)) {
                commit(types.REFRESH_VALUABLE_IMAGE_LIST, resp.data.details);
            } else {
                // 清空镜像列表，并显示提示信息，告知用户错误
                commit(types.REFRESH_VALUABLE_IMAGE_LIST, []);
                dispatch("displayPromptByResponseMsg", resp);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchAllOrgs({commit, dispatch}) {
        api.gateService.getAllOrgs().then(function (resp) {
            if (resp.data.code >= 0 && Array.isArray(resp.data.details)) {
                commit(types.REFRESH_ORG_LIST, resp.data.details);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchImagesByPage({commit, dispatch}, data) {
        api.gateService.getImagesByPage(data).then(function (resp) {
            commit(types.REFRESH_IMAGES_BY_PAGE, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchAppsByPage({commit, dispatch}, data) {
        api.gateService.getAppsByPage(data).then(function (resp) {
            commit(types.REFRESH_APPS_BY_PAGE, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 获取某个应用或发布组的发布历史分页列表
     */
    fetchReleaseHistoryByPage({commit, dispatch}, data) {
        api.gateService.getReleaseRecordByPage(data).then(function (resp) {
            commit(types.REFRESH_RELEASE_HISTORY_BY_PAGE, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 根据条件获取发布记录分页列表
     */
    fetchReleaseRecordByPage({commit, dispatch}, data) {
        api.gateService.getReleaseRecordByPage(data).then(function (resp) {
            commit(types.REFRESH_RELEASE_RECORD_BY_PAGE, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchReleaseTypes({commit, dispatch}, data) {
        api.gateService.getReleaseTypes().then(function (resp) {
            commit(types.REFRESH_RELEASE_TYPE_LIST, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 根据组织获取应用列表
     * @param commit    store state更新提交者
     * @param dispatch  store action分发者
     * @param data      data格式为 {org: x}
     */
    fetchAppsByOrg({commit, dispatch}, data) {
        api.gateService.getAppsByOrg(data).then(function (resp) {
            commit(types.REFRESH_APP_LIST, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 根据用户名获取应用列表
     * @param commit    store state更新提交者
     * @param dispatch  store action分发者
     * @param data      data格式为 {username: x}
     */
    fetchAppsByUsername({commit, dispatch}, data) {
        api.gateService.getAppsByUsername(data).then(function (resp) {
            commit(types.REFRESH_APP_LIST, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchApp({commit, dispatch}, data) {
        api.gateService.getApp(data).then(function (resp) {
            commit(types.REFRESH_MY_APP_LIST, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchContainerLog({commit, dispatch}, data) {
        api.gateService.getContainerLog(data).then(function (resp) {
            commit(types.REFRESH_CONTAINER_LOG, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    /**
     * 根据发布组Id、发布组名和jobId获取发布日志
     * @param commit    store state更新提交者
     * @param dispatch  store action分发者
     * @param data      data格式为 {groupId: x, groupName: y, jobId: z}
     */
    fetchReleaseLog({commit, dispatch}, data) {
        api.gateService.getReleaseLog(data).then(function (resp) {
            commit(types.REFRESH_RELEASE_LOG, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchReleaseStatus({commit, dispatch}) {
        api.gateService.getReleaseStatus().then(function (resp) {
            commit(types.REFRESH_RELEASE_STATUS, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    restartJob({commit, dispatch}, data) {
        api.gateService.restartJob(data).then(function (resp) {
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchAppQuotaStatus({commit, dispatch}, data) {
        if (data.appId && data.environment) {
            api.gateService.getAppQuotaStatus(data).then(function (resp) {
                if (resp.data.code >= 0) {
                    commit(types.REFRESH_APP_QUOTA_STATUS, resp.data.details);
                } else {
                    dispatch("displayPromptByResponseMsg", resp);
                }
            }.bind(this)).catch(function (err) {
                dispatch("displayPromptByResponseMsg", err.response);
            }.bind(this));
        } else {
            commit(types.REFRESH_APP_QUOTA_STATUS, []);
        }
    },

    fetchAuditLogsByPage({commit, dispatch}, data) {
        api.gateService.getAuditLogsByPage(data).then(function (resp) {
            commit(types.REFRESH_AUDIT_LOGS_BY_PAGE, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    applyNewApp({commit, dispatch}, data) {
        api.gateService.applyNewApp(data).then(function (resp) {
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchAppliesByPage({commit, dispatch}, data) {
        api.gateService.queryAppliesByPage(data).then(function (resp) {
            if (resp.data.code >= 0) {
                commit(types.REFRESH_APPLY_LIST, resp.data.details);
            } else {
                dispatch("displayPromptByResponseMsg", resp);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    changeAppQuota({commit, dispatch}, data) {
        api.gateService.changeAppQuota(data).then(function (resp) {
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    searchUsersByUserName({commit, dispatch}, data) {
        api.gateService.findUsersByUserName(data).then(function (resp) {
            if (Array.isArray(resp.data.details)) {
                commit(types.REFRESH_USER_LIST, resp.data.details);
            } else {
                commit(types.REFRESH_USER_LIST, []);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    updateAppMember({commit, dispatch}, data) {
        api.gateService.updateAppMember(data.updateAppMemberRequest).then(function (resp) {
            dispatch("fetchApp", data.queryRequest);
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchEnvZones({commit, dispatch}, data) {
        api.gateService.fetchZonesByEnv(data).then(function (resp) {
            if (resp.data.code >= 0) {
                commit(types.REFRESH_ENV_ZONES, resp.data.details);
            } else {
                dispatch("displayPromptByResponseMsg", resp);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchIpsByPage({commit, dispatch}, data) {
        api.gateService.getIpsByPage(data).then(function (resp) {
            commit(types.REFRESH_IP_LIST, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    createIps({commit, dispatch}, data) {
        api.gateService.createIps(data.addRequest).then(function (resp) {
            dispatch("fetchIpsByPage", data.queryForm);
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    deleteIpById({commit, dispatch}, data) {
        api.gateService.deleteIp(data).then(function (resp) {
            dispatch("fetchIpsByPage", data.queryForm);
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    syncIpStatusByEnv({commit, dispatch}, data) {
        api.gateService.syncIpStatus(data).then(function (resp) {
            dispatch("fetchIpsByPage", data.queryForm);
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchResourcesByPage({commit, dispatch}, data) {
        api.gateService.getResourcesByPage(data).then(function (resp) {
            commit(types.REFRESH_RESOURCE_LIST, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    createResources({commit, dispatch}, data) {
        api.gateService.createResources(data.addRequest).then(function (resp) {
            dispatch("fetchResourcesByPage", data.queryForm);
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    deleteResourceById({commit, dispatch}, data) {
        api.gateService.deleteResource(data).then(function (resp) {
            dispatch("fetchResourcesByPage", data.queryForm);
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchResourceQuotaStatus({commit, dispatch}, data) {
        if (data.appId && data.env) {
            api.gateService.getResourceQuotaStatus(data).then(function (resp) {
                if (resp.data.code >= 0) {
                    commit(types.REFRESH_RESOURCE_QUOTA_STATUS, resp.data.details);
                } else {
                    dispatch("displayPromptByResponseMsg", resp);
                }
            }.bind(this)).catch(function (err) {
                dispatch("displayPromptByResponseMsg", err.response);
            }.bind(this));
        } else {
            commit(types.REFRESH_RESOURCE_QUOTA_STATUS, []);
        }
    },

    fetchAvailableResources({commit, dispatch}, data) {
        api.gateService.getAvailableResources(data).then(function (resp) {
            commit(types.REFRESH_AVAILABLE_RESOURCES, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    changeTypeForAllResources({commit, dispatch}, data) {
        api.gateService.changeTypeForAllResources(data.changeRequest).then(function (resp) {
            dispatch("fetchResourcesByPage", data.queryForm);
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchInstancesByHost({commit, dispatch}, data) {
        api.gateService.getInstancesByHost(data).then(function (resp) {
            commit(types.REFRESH_HOST_INSTANCE_LIST, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    transferInstances({commit, dispatch}, data) {
        api.gateService.transferInstances(data).then(function (resp) {
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchCloudInstances({commit, dispatch}, data) {
        api.gateService.getCloudInstancesByPage(data).then(function (resp) {
            commit(types.REFRESH_CLOUD_INSTANCES_BY_PAGE, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchCloudInstanceStatus({commit, dispatch}, data) {
        api.gateService.getCloudInstanceStatus(data).then(function (resp) {
            if (resp.data.code == 0) {
                commit(types.REFRESH_CLOUD_INSTANCE_STATUS, resp.data.instances[0]);
            } else {
                commit(types.REFRESH_CLOUD_INSTANCE_STATUS, {});
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchCloudInstanceLog({commit, dispatch}, data) {
        api.gateService.getCloudInstanceLog(data).then(function (resp) {
            if (resp.data.code == 0) {
                commit(types.REFRESH_CLOUD_INSTANCE_LOG, resp.data.log);
            }
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    pullInCloudInstance({commit, dispatch}, data) {
        api.gateService.pullInCloudInstance(data).then(function (resp) {
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    pullOutCloudInstance({commit, dispatch}, data) {
        api.gateService.pullOutCloudInstance(data).then(function (resp) {
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    restartCloudInstance({commit, dispatch}, data) {
        api.gateService.restartCloudInstance(data).then(function (resp) {
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    deleteCloudInstance({commit, dispatch}, data) {
        api.gateService.deleteCloudInstance(data).then(function (resp) {
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchInstanceCountInfo({commit, dispatch}) {
        api.gateService.getInstanceCountList().then(function (resp) {
            commit(types.REFRESH_INSTANCE_COUNT_INFO, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    fetchAppSetting({commit, dispatch}, data) {
        api.gateService.getAppSettingByAppId(data).then(function (resp) {
            commit(types.REFRESH_APP_SETTING, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    updateAppSetting({commit, dispatch}, data) {
        api.gateService.updateAppSetting(data).then(function (resp) {
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    removeApp({commit, dispatch}, data) {
        api.gateService.deleteAppById(data).then(function (resp) {
            dispatch("fetchAppsByPage", data.queryForm);
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    execCommand({commit, dispatch}, data) {
        api.gateService.execCommand(data).then(function (resp) {
            commit(types.REFRESH_EXEC_COMMAND_RESULT, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    clearExecCommandResult({commit}) {
        commit(types.REFRESH_EXEC_COMMAND_RESULT, {});
    },

    fetchDnsByPage({commit, dispatch}, data) {
        api.gateService.queryDnsByPage(data).then(function (resp) {
            commit(types.REFRESH_DNS_LIST, resp.data.details);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    updateDns({commit, dispatch}, data) {
        api.gateService.updateDns(data.updateRequest).then(function (resp) {
            dispatch("fetchDnsByPage", data.queryRequest);
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    createDns({commit, dispatch}, data) {
        api.gateService.addDns(data.createRequest).then(function (resp) {
            dispatch("fetchDnsByPage", data.queryRequest);
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },

    deleteDns({commit, dispatch}, data) {
        api.gateService.deleteDnsById(data.deleteRequest).then(function (resp) {
            dispatch("fetchDnsByPage", data.queryRequest);
            dispatch("displayPromptByResponseMsg", resp);
        }.bind(this)).catch(function (err) {
            dispatch("displayPromptByResponseMsg", err.response);
        }.bind(this));
    },
};

// mutations
const mutations = {
    [types.REFRESH_APP_LIST] (state, data) {
        state.apps = data;
    },
    [types.REFRESH_MY_APP_LIST] (state, data) {
        state.myApps = data;
    },
    [types.REFRESH_ENV_LIST] (state, data) {
        state.envList = data;
    },
    [types.REFRESH_ENV_ACTIVE_LIST] (state, data) {
        state.envActiveList = data;
    },
    [types.REFRESH_GROUP_INSTANCE_LIST] (state, data) {
        state.releaseGroupStatus.instanceList = data;
    },
    [types.REFRESH_APP_INSTANCE_LIST] (state, data) {
        state.appInstanceList = data;
    },
    [types.REFRESH_RELEASE_GROUP_LIST] (state, data) {
        state.releaseGroups = data;
    },
    [types.REFRESH_GROUP_JOB_STATUS] (state, data){
        state.releaseGroupStatus.latestJobInfo = data;
    },
    [types.REFRESH_PROMPT_MESSAGE] (state, data){
        state.promptMessage = data;
    },
    [types.REFRESH_IMAGE_LIST] (state, data) {
        state.imageList = data;
    },
    [types.REFRESH_VALUABLE_IMAGE_LIST] (state, data) {
        state.valuableImageList = data;
    },
    [types.REFRESH_CURRENT_SITE](state, data){
        state.currentSite = data;
    },
    [types.REFRESH_CURRENT_GROUP](state, data){
        state.currentGroup = data;
    },
    [types.REFRESH_CURRENT_ENV] (state, data) {
        state.currentEnv = data;
    },
    [types.REFRESH_CURRENT_APP_ID] (state, data) {
        state.currentAppId = data;
    },
    [types.REFRESH_ORG_LIST] (state, data) {
        state.orgList = data;
    },
    [types.REFRESH_IMAGES_BY_PAGE] (state, data) {
        state.imageList = data.content;
        state.imageCount = data.totalElements;
    },
    [types.REFRESH_APPS_BY_PAGE] (state, data) {
        state.appList = data.content;
        state.appCount = data.totalElements;
    },
    [types.REFRESH_RELEASE_HISTORY_BY_PAGE] (state, data) {
        state.releaseHistory = data.content;
        state.releaseHistoryCount = data.totalElements;
    },
    [types.REFRESH_RELEASE_RECORD_BY_PAGE] (state, data) {
        state.releaseRecord = data.content;
        state.releaseRecordCount = data.totalElements;
    },
    [types.REFRESH_RELEASE_TYPE_LIST] (state, data) {
        state.releaseTypeList = data;
    },
    [types.REFRESH_RELEASE_LOG] (state, data) {
        state.releaseLog = data;
    },
    [types.REFRESH_CONTAINER_LOG] (state, data) {
        state.containerLog = data;
    },
    [types.REFRESH_RELEASE_STATUS] (state, data) {
        state.releaseStatus = data;
    },
    [types.REFRESH_APP_QUOTAS] (state, data) {
        state.appQuotas = data;
    },
    [types.REFRESH_APP_QUOTA_STATUS] (state, data) {
        state.appQuotaStatus = data;
    },
    [types.REFRESH_AUDIT_LOGS_BY_PAGE] (state, data) {
        state.auditLogList = data.content;
        state.auditLogCount = data.totalElements;
    },
    [types.REFRESH_APPLY_LIST] (state, data) {
        state.applyList = data.content;
        state.applyCount = data.totalElements;
    },
    [types.REFRESH_USER_LIST] (state, data) {
        state.userList = data;
    },
    [types.REFRESH_ENV_ZONES] (state, data) {
        state.envZoneList = data;
    },
    [types.REFRESH_IP_LIST] (state, data) {
        state.ipList = data.content;
        state.ipCount = data.totalElements;
    },
    [types.REFRESH_RESOURCE_LIST] (state, data) {
        state.resourceList = data.content;
        state.resourceCount = data.totalElements;
    },
    [types.REFRESH_RESOURCE_QUOTA_STATUS] (state, data) {
        state.resourceQuotaStatus = data;
    },
    [types.REFRESH_AVAILABLE_RESOURCES] (state, data) {
        state.availableResources = data;
    },
    [types.REFRESH_HOST_INSTANCE_LIST] (state, data) {
        state.hostInstanceList = data;
    },
    [types.REFRESH_CLOUD_INSTANCES_BY_PAGE] (state, data) {
        state.cloudInstanceList = data.content;
        state.cloudInstanceCount = data.totalElements;
    },
    [types.REFRESH_CLOUD_INSTANCE_STATUS] (state, data) {
        state.cloudInstanceStatus = data;
    },
    [types.REFRESH_HADOOPCONFIG_LIST] (state, data) {
        state.hadoopConfigs = data;
    },
    [types.REFRESH_CLOUD_INSTANCE_LOG] (state, data) {
        state.cloudInstanceLog = data;
    },
    [types.REFRESH_INSTANCE_COUNT_INFO] (state, data) {
        state.instanceCountInfo = data;
    },
    [types.REFRESH_APP_SETTING] (state, data) {
        state.appSetting = data;
    },
    [types.REFRESH_EXEC_COMMAND_RESULT] (state, data) {
        state.execCommandResult = data;
    },
    [types.REFRESH_DNS_LIST] (state, data) {
        state.dnsList = data.content;
        state.dnsCount = data.totalElements;
    },
};

export default {
    state,
    getters,
    actions,
    mutations
}