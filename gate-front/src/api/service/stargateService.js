import restApi from '../restApi'

export default {

    getAppList(request = {}) {
        let url = '/api/apps';
        return restApi.doGetRequest(url);
    },

    getAppsByOrg(request = {}) {
        let url = 'api/apps?department=' + request.org;
        return restApi.doGetRequest(url);
    },

    getAppsByUsername(request = {}) {
        let url = 'api/apps?username=' + request.username;
        return restApi.doGetRequest(url);
    },

    getApp(request = {}) {
        let url = 'api/apps?appId=' + request.appId;
        return restApi.doGetRequest(url);
    },

    getInstancesByGroupId(request = {}) {
        let url = '/api/instances?groupId=' + request.groupId;
        return restApi.doGetRequest(url);
    },

    getInstancesByCondition(request = {}) {
        let url = '/api/instances/condition';
        return restApi.doGetRequest(url, request);
    },

    getGroupsByEnvAndAppId(request = {}) {
        let url = 'api/groups';
        return restApi.doGetRequest(url, request);
    },

    getGroupById(request = {}) {
        let url = 'api/groups/' + request.groupId;
        return restApi.doGetRequest(url);
    },

    getGroupStatus(request = {}) {
        let url = 'api/groups/status?groupId=' + request.groupId;
        return restApi.doGetRequest(url);
    },

    createReleaseGroup(request = {}) {
        let url = 'api/groups';
        return restApi.doPostRequest(url, request);
    },

    removeReleaseGroup(request = {}) {
        let url = 'api/groups/' + request.groupId;
        return restApi.doDeleteRequest(url);
    },

    resizeReleaseGroup(request = {}) {
        let url = 'api/groups/resize';
        return restApi.doPostRequest(url, request);
    },

    pullReleaseGroupInstance(request = {}) {
        let url = 'api/groups/pull_instance';
        return restApi.doPostRequest(url, request);
    },

    updateReleaseGroupInstance(request = {}) {
        let url = 'api/groups/update_instance';
        return restApi.doPostRequest(url, request);
    },

    restartReleaseGroupInstance(request = {}) {
        let url = 'api/groups/restart_instance';
        return restApi.doPostRequest(url, request);
    },

    getReleaseLog(request = {}) {
        let url = 'api/groups/releaselog';
        return restApi.doGetRequest(url, request);
    },

    getContainerLog(request = {}) {
        let url = 'api/instances/containerlog?instanceName=' + request.instanceName;
        return restApi.doGetRequest(url);
    },

    getReleaseRecordByPage(request = {}) {
        let url = 'api/releases/condition';
        return restApi.doGetRequest(url, request);
    },

    getReleaseTypes(request = {}) {
        let url = 'api/releases/types';
        return restApi.doGetRequest(url);
    },

    getReleaseStatus() {
        let url = 'api/releases/status';
        return restApi.doGetRequest(url);
    },

    getSiteByEnvAndAppId(request = {}) {
        let url = 'api/apps/site';
        return restApi.doGetRequest(url, request);
    },

    getEnvList(request = {}) {
        let url = 'api/envs';
        return restApi.doGetRequest(url);
    },

    getActivateEnvList(request = {}) {
        let url = 'api/envs/activate';
        return restApi.doGetRequest(url);
    },

    activateEnv(request = {}) {
        let url = 'api/envs/activate?envId=' + request.envId + "&activated=" + request.activated;
        return restApi.doPostRequest(url);
    },

    enableHa(request = {}) {
        let url = 'api/envs/enableHa?envId=' + request.envId;
        return restApi.doPostRequest(url);
    },

    disableHa(request = {}) {
        let url = 'api/envs/disableHa?envId=' + request.envId;
        return restApi.doPostRequest(url);
    },

    getAllOrgs() {
        let url = 'api/orgs';
        return restApi.doGetRequest(url);
    },

    getImagesByPage(request = {}) {
        let url = 'api/images/condition';
        return restApi.doGetRequest(url, request);
    },

    getAppsByPage(request = {}) {
        let url = 'api/apps/condition';
        return restApi.doGetRequest(url, request);
    },

    getImageList(request = {}) {
        let url = 'api/images?appName=' + request.appName;
        return restApi.doGetRequest(url);
    },

    getValuableImageList(request = {}) {
        let url = 'api/images/valuableImages';
        return restApi.doGetRequest(url, request);
    },

    restartJob(request = {}) {
        let url = 'api/groups/restartJob?jobId=' + request.jobId;
        return restApi.doPostRequest(url);
    },

    getAppQuotaStatus(request = {}) {
        let url = 'api/apps/quotas/status';
        return restApi.doGetRequest(url, request);
    },

    getAuditLogsByPage(request = {}) {
        let url = '/api/auditlogs';
        return restApi.doGetRequest(url, request);
    },

    applyNewApp(request = {}) {
        let url = 'api/applies/newApp';
        return restApi.doPostRequest(url, request);
    },

    queryAppliesByPage(request = {}) {
        let url = 'api/applies/queryAppliesByPage';
        return restApi.doGetRequest(url, request);
    },

    changeAppQuota(request = {}) {
        let url = 'api/applies/changeAppQuota';
        return restApi.doPostRequest(url, request);
    },

    findUsersByUserName(request = {}) {
        let url = 'api/users?name=' + request.userName;
        return restApi.doGetRequest(url);
    },

    updateAppMember(request = {}) {
        let url = 'api/apps/member';
        return restApi.doPostRequest(url, request);
    },

    fetchZonesByEnv(request = {}) {
        let url = 'api/zones';
        return restApi.doGetRequest(url, request);
    },

    getIpsByPage(request = {}) {
        let url = 'api/ips/page';
        return restApi.doGetRequest(url, request);
    },

    createIps(request = {}) {
        let url = 'api/ips/add';
        return restApi.doPostRequest(url, request);
    },

    deleteIp(request = {}) {
        let url = 'api/ips/' + request.id;
        return restApi.doDeleteRequest(url);
    },

    syncIpStatus(request = {}) {
        let url = 'api/ips/syncstatus?env=' + request.env;
        return restApi.doPostRequest(url);
    },

    getResourcesByPage(request = {}) {
        let url = 'api/resources/page';
        return restApi.doGetRequest(url, request);
    },

    createResources(request = {}) {
        let url = 'api/resources/add';
        return restApi.doPostRequest(url, request);
    },

    deleteResource(request = {}) {
        let url = 'api/resources/' + request.id;
        return restApi.doDeleteRequest(url);
    },

    getResourceQuotaStatus(request = {}) {
        let url = 'api/resources/quotas/status';
        return restApi.doGetRequest(url, request);
    },

    getAvailableResources(request = {}) {
        let url = 'api/resources/available';
        return restApi.doGetRequest(url, request);
    },

    changeTypeForAllResources(request = {}) {
        let url = 'api/resources/changeType?appId=' + request.appId + '&env=' + request.env + '&isStatic=' + request.isStatic;
        return restApi.doPostRequest(url);
    },

    getInstancesByHost(request = {}) {
        let url = 'api/instances/host';
        return restApi.doGetRequest(url, request);
    },

    transferInstances(request = {}) {
        let url = 'api/instances/transfer';
        return restApi.doPostRequest(url, request);
    },

    getCloudInstancesByPage(request = {}) {
        let url = 'api/instances/cloud';
        return restApi.doGetRequest(url, request);
    },

    getCloudInstanceStatus(request = {}) {
        let url = 'api/cloud/instance/get';
        return restApi.doPostRequest(url, request);
    },

    getCloudInstanceLog(request = {}) {
        let url = 'api/cloud/instance/log';
        return restApi.doPostRequest(url, request);
    },

    pullInCloudInstance(request = {}) {
        let url = 'api/cloud/instance/up';
        return restApi.doPostRequest(url, request);
    },

    pullOutCloudInstance(request = {}) {
        let url = 'api/cloud/instance/down';
        return restApi.doPostRequest(url, request);
    },

    restartCloudInstance(request = {}) {
        let url = 'api/cloud/instance/restart';
        return restApi.doPostRequest(url, request);
    },

    deleteCloudInstance(request = {}) {
        let url = 'api/cloud/instance/destroy';
        return restApi.doPostRequest(url, request);
    },

    getInstanceCountList() {
        let url = 'api/instances/count';
        return restApi.doGetRequest(url);
    },

    getAppSettingByAppId(request = {}) {
        let url = 'api/apps/setting';
        return restApi.doGetRequest(url, request);
    },

    updateAppSetting(request = {}) {
        let url = 'api/apps/setting';
        return restApi.doPostRequest(url, request);
    },

    deleteAppById(request = {}) {
        let url = 'api/apps/' + request.id;
        return restApi.doDeleteRequest(url);
    },

    execCommand(request = {}) {
        let url = 'api/instances/exec';
        return restApi.doPostRequest(url, request);
    },

    queryDnsByPage(request = {}) {
        let url = '/api/dns/queryByPage';
        return restApi.doGetRequest(url, request);
    },

    exportDns(request = {}) {
        let url = '/api/dns/export?envId=' + request.envId;
        return restApi.doGetRequest(url);
    },

    updateDns(request = {}) {
        let url = '/api/dns/update';
        return restApi.doPostRequest(url, request);
    },

    addDns(request = {}) {
        let url = '/api/dns/add';
        return restApi.doPostRequest(url, request);
    },

    deleteDnsById(request = {}) {
        let url = '/api/dns/deleteById';
        return restApi.doPostRequest(url, request);
    }
}