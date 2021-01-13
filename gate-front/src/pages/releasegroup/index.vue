<template>
    <div class="content-panel">
        <el-row>
            <el-col>
                <el-breadcrumb separator="/">
                    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                    <el-breadcrumb-item :to="{ name: 'apps' }">应用列表</el-breadcrumb-item>
                    <el-breadcrumb-item>发布组列表</el-breadcrumb-item>
                </el-breadcrumb>
            </el-col>
        </el-row>

        <el-row class="detail-form">

            <div class="detail-box">
                <div class="detail-box-left">环境</div>
                <div class="detail-box-right">{{siteInView.environment}}</div>
            </div>

            <div class="detail-box">
                <div class="detail-box-left">应用</div>
                <div class="detail-box-right">{{siteInView.appName}}</div>
            </div>

            <div class="detail-box">
                <div class="detail-box-left">域名</div>
                <div class="detail-box-right">{{siteInView.domain}}</div>
            </div>

            <el-button type="primary" @click="onCreate" v-if="!isFlinkApp">新建发布组</el-button>
            <el-button type="primary" @click="onCreateFlink" v-if="isFlinkApp">新建FlinkJob</el-button>
        </el-row>

        <br/>

        <div id="release-card-pannel">
            <el-row v-if="showGroup">
                <el-col :span="cardLayout.span" v-for="(group, index) in groups" :key="group.id">
                    <el-card v-loading="isGroupBusy(group) || isGroupFailed(group)" :element-loading-text="getGroupLoadingText(group)"
                             class="release-group" :class="{'release-group-failed': isGroupFailed(group)}">
                        <div class="release-group-title">{{group.name}}</div>

                        <el-form label-width="70px" label-position="left">
                            <el-form-item label="实例规格">
                                <div class="release-group-item">{{group.instanceSpec}}</div>
                            </el-form-item>
                            <el-form-item label="实例总数">
                                <div class="release-group-item">{{group.instanceCount}}</div>
                            </el-form-item>
                            <el-form-item label="在线实例">
                                <div class="release-group-item">{{group.activeCount}}</div>
                            </el-form-item>
                            <el-form-item label="流量状态">
                                <el-progress :text-inside="true" :stroke-width="40"
                                             :percentage="group.instanceUpPercentage" status="success"></el-progress>
                            </el-form-item>
                        </el-form>

                        <el-row :gutter="20">
                            <el-col :span="12">
                                <router-link :to="{name: 'instancestatus', query: { env: group.environment, appId: group.appId, groupId: group.id }}">
                                    <el-button type="primary">实例操作</el-button>
                                </router-link>
                            </el-col>
                            <el-col :span="12">
                                <router-link
                                        :to="{name: 'releasehistory', query: { env: group.environment, appId: group.appId, groupId: group.id }}">
                                    <el-button type="primary">发布历史</el-button>
                                </router-link>
                            </el-col>
                        </el-row>
                        <el-row :gutter="20">
                            <el-col :span="24">
                                <el-button class="btn-removal" @click="removeGroup(group)">
                                    删除发布组
                                </el-button>
                            </el-col>
                        </el-row>

                        <div class="card-bottom">
                            <span class="insert-by">创建人：{{ group.insertBy }}</span>
                            <span class="create-time">创建时间：{{ formatTimestamp(group.insertTime) }}</span>
                        </div>

                        <div v-if="isGroupBusy(group) || isGroupFailed(group)" class="release-group-loading-details">
                            <router-link :to="{name: 'instancestatus', query: { env: group.environment, appId: group.appId, groupId: group.id }}">
                                <el-button>查看详情</el-button>
                            </router-link>
                            <el-button v-if="isGroupFailed(group)" @click="restartJob(group)" style="margin-left: 10px;">重新执行</el-button>
                            <el-button v-if="isGroupFailed(group)" class="btn-removal" @click="removeGroup(group)" style="margin-left: 10px;">删除发布组</el-button>
                        </div>
                    </el-card>
                </el-col>
            </el-row>
        </div>

        <el-dialog :title="'新建发布组（' + currentEnv + '）'" :visible.sync="dialogVisible" width="600px" :before-close="onClose">
            <el-form label-width="80px" label-position="left" :model="newGroup" :rules="newGroupRules" ref="create-group-form">
                <el-form-item label="应用镜像" prop="instanceTarget">
                    <el-select v-model="newGroup.instanceTarget" filterable placeholder="请选择应用镜像" style="width: 100%">
                        <el-option v-for="item in images" :key="item.id" :label="item.name"
                                   :value="item.name">
                            <span style="float: left">{{ item.name }}</span>
                            <span style="float: right; color: #8492a6; font-size: 13px">{{ formatImageDeployAt(item.deployAt) }}</span>
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="发布规格" prop="instanceSpec">
                    <el-select v-model="newGroup.instanceSpec" placeholder="请选择发布规格" @change="changeInstanceSpec" style="width: 100%">
                        <el-option v-for="item in appQuotaStatus" :key="item.spectypeName" :value="item.spectypeName"
                                   :label="item.spectypeName + '（可发实例数：' + item.freeCount + '）'"
                                   v-if="item.freeCount > 0"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="静态资源" prop="staticResources" v-if="siteInView.enableStaticResource && newGroup.instanceSpec">
                    <el-select v-model="newGroup.staticResources"
                               placeholder="请选择静态资源"
                               no-data-text="该规格的静态资源不足，请先添加"
                               multiple
                               filterable
                               style="width: 100%">
                        <el-option v-for="item in availableResources"
                                   :key="item.id"
                                   :label="item.zone + '@' + item.ip"
                                   :value="item.zone + '@' + item.ip">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="实例个数" prop="instanceCount" v-if="!siteInView.enableStaticResource">
                    <el-input v-model.number="newGroup.instanceCount" placeholder="请输入实例个数"></el-input>
                </el-form-item>
                <el-form-item label="启动端口" prop="instancePortCount">
                    <el-input v-model.number="newGroup.instancePortCount" placeholder="请输入应用启动端口"></el-input>
                </el-form-item>
                <el-form-item label="部署区域" prop="zone" v-if="!siteInView.enableStaticResource">
                    <el-checkbox-group v-model="newGroup.zone" style="height: 40px">
                        <el-checkbox v-for="item in siteInView.zones" :label="item" name="zone"></el-checkbox>
                    </el-checkbox-group>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onClose" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="createGroup()" style="float:right;margin:0 10px 0 0;">提交</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog :title="'新建FlinkJob发布组（' + siteInView.environment + '）'" :visible.sync="flinkDialogVisible" width="600px" :before-close="onCloseFlink">
            <el-form label-width="80px" label-position="left" :model="newFlinkGroup" :rules="newFlinkJobGroupRules" ref="create-flinkjob-group-form">
                <el-form-item label="应用镜像" prop="instanceImage">
                    <el-select v-model="newFlinkGroup.instanceImage" filterable value-key="id" placeholder="请选择应用镜像" style="width: 100%">
                        <el-option v-for="item in images" :key="item.id" :label="item.name"
                                   :value="item">
                            <span style="float: left">{{ item.name }}</span>
                            <span style="float: right; color: #8492a6; font-size: 13px">{{ formatImageDeployAt(item.deployAt) }}</span>
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="集群配置" prop="hadoopConfig">
                    <el-select v-model="newFlinkGroup.hadoopConfig" filterable value-key="id" placeholder="请选择hadoop配置" style="width: 100%">
                        <el-option v-for="item in hadoopConfigs" :key="item.id" :label="item.name"
                                   :value="item.name">
                            <span style="float: left">{{ item.name+' ('+item.description+')' }}</span>
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="发布规格" prop="instanceSpec">
                    <el-select v-model="newGroup.instanceSpec" placeholder="请选择发布规格" @change="changeInstanceSpec" style="width: 100%">
                        <el-option v-for="item in appQuotaStatus" :key="item.spectypeName" :value="item.spectypeName"
                                   :label="item.spectypeName + '（可发实例数：' + item.freeCount + '）'"
                                   v-if="item.freeCount > 0"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="任务个数" prop="instanceCount">
                    <el-input v-model.number="newFlinkGroup.instanceCount" placeholder="请输入任务个数(默认槽数为1)"></el-input>
                </el-form-item>
                <el-form-item label="启动参数" prop="cmd">
                    <el-input v-model.number="newFlinkGroup.cmd" placeholder="请输入启动参数"></el-input>
                </el-form-item>
                <el-form-item label="checkpoint" prop="checkpoint">
                    <el-input v-model.number="newFlinkGroup.checkpoint" placeholder="请输入checkpoint地址"></el-input>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseFlink" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="createFlinkGroup()" style="float:right;margin:0 10px 0 0;">提交</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
    </div>
</template>

<script>
    import {mapGetters, mapActions} from 'vuex';

    export default {
        data: function () {
            return {
                dialogVisible: false,
                cardLayout: {
                    column: '',
                    span: ''
                },
                routeParam: {
                    env: null,
                    appId: null
                },
                siteInView: {
                    environment: null,
                    appId: null,
                    appName: null,
                    domain: null,
                    owner: null,
                    enableStaticResource: null,
                    zones: []
                },
                newGroup: {
                    instanceSpec: null,
                    instanceCount: null,
                    instancePortCount: null,
                    instanceTarget: null,
                    zone: [],
                    staticResources: []
                },
                newFlinkGroup: {
                    instanceSpec: null,
                    instanceCount: null,
                    instanceImage: null,
                    cmd: null,
                    hadoopConfig: null,
                    checkpoint: null
                },
            }
        },
        computed: {
            ...mapGetters({
                hadoopConfigs: 'getHadoopConfigs',
                myApps: 'getMyApps',
                groups: 'getReleaseGroups',
                images: 'getValuableImageList',
                username: 'getUserName',
                userRoles: 'getUserRoles',
                appQuotaStatus: 'getAppQuotaStatus',
                availableResources: 'getAvailableResources',
                currentSite: 'getCurrentSite',
                currentEnv: 'getCurrentEnv',
                currentAppId: 'getCurrentAppId'
            }),
            isAdmin: function () {
                return this.userRoles != null && this.userRoles.includes('admin');
            },
            showGroup: function () {
                return this.siteInView.environment && this.siteInView.appId;
            },
            isFlinkApp: function () {
                if (this.myApps != null && this.myApps[0] != null) {
                    let myApp = this.myApps[0];
                    return myApp.appType == "FlinkJob";
                }
            },
            newGroupRules: function () {
                return {
                    instanceTarget: [
                        {required: true, message: '请选择应用镜像', trigger: 'change'}
                    ],
                    instanceSpec: [
                        {required: true, message: '请选择发布规格', trigger: 'change'}
                    ],
                    instanceCount: [
                        {required: !this.siteInView.enableStaticResource, message: '请输入实例个数'},
                        {type: 'number', min: 1, max: 100, message: '请输入1-100之间的数字'}
                    ],
                    staticResources: [
                        {type: 'array', required: this.siteInView.enableStaticResource, message: '请至少选择一个静态资源', trigger: 'change'}
                    ],
                    instancePortCount: [
                        {required: true, message: '请输入应用启动端口'},
                        {type: 'number', min: 1, max: 65535, message: '请输入1-65535之间的数字'}
                    ],
                    zone: [
                        {type: 'array', required: true, message: '请至少选择一个部署区域', trigger: 'change'}
                    ]
                };
            }
        },
        watch: {
            currentEnv: function (newCurrentEnv) {
                this.$router.push({name: 'groups', query: {env: newCurrentEnv, appId: this.routeParam.appId}});
                this.routeParam.env = newCurrentEnv;

                this.$store.dispatch('fetchSiteByEnvAndAppId', {env: newCurrentEnv, appId: this.routeParam.appId});
                this.refreshReleseGroups();
            },
            currentSite: function(newCurrentSite) {
                this.siteInView = newCurrentSite;

                if (this.currentSite.owner) {
                    let owners = this.currentSite.owner.split(',');
                    if (!owners.includes(this.username) && !this.isAdmin) {
                        this.$message.warning('您没有该应用的权限');
                        this.$router.push({name: 'apps'});
                    }
                }

                if (this.currentSite.environment && this.currentSite.environment != this.currentEnv) {
                    this.$store.dispatch('refreshCurrentEnv', this.currentSite.environment);
                }

                if (this.currentSite.appId && this.currentSite.appId != this.currentAppId) {
                    this.$store.dispatch('refreshCurrentAppId', this.currentSite.appId);
                }
            }
        },
        created: function () {
            let query = this.$route.query;
            this.routeParam.env = query && query.env ? query.env : null;
            this.routeParam.appId = query && query.appId ? query.appId : null;

            this.$store.dispatch('fetchSiteByEnvAndAppId', {env: this.routeParam.env, appId: this.routeParam.appId});
            this.$store.dispatch('fetchApp', {appId: this.routeParam.appId});

            this.refreshReleseGroups();
        },
        mounted: function () {
            this.internalTimer = setInterval(this.onInterval.bind(this), 3000);

            window.addEventListener('resize', this.handleResize);
            this.handleResize();
        },
        beforeDestroy: function () {
            clearInterval(this.internalTimer);
            window.removeEventListener('resize', this.handleResize);
        },
        methods: {
            formatTimestamp(timestamp) {
                return new Date(timestamp).toLocaleString();
            },
            formatImageDeployAt(timestamp) {
                if (timestamp == null)
                    return timestamp;
                return new Date(timestamp).toLocaleString() + '发布';
            },
            isGroupBusy(group) {
                return (group.jobInfo != null && (group.jobInfo.status == "RUNNING" || group.jobInfo.status == "NEW"));
            },
            isGroupFailed(group) {
                return (group.jobInfo != null && (group.jobInfo.status == "FAIL" || group.jobInfo.status == "EXPIRED"));
            },
            getGroupLoadingText(group) {
                let msg = '';

                if (this.isGroupBusy(group)) {
                    msg = "任务执行中" + '\n\n';
                } else if (this.isGroupFailed(group)) {
                    msg = "任务执行失败" + '\n\n';
                }

                msg += group.jobInfo.operationTypeDesc + '\n';

                if (group.jobInfo != null && group.jobInfo.taskInfos != null) {
                    for(let index in group.jobInfo.taskInfos) {
                        let task = group.jobInfo.taskInfos[index];
                        let taskName = task.description;
                        if (task.dataMap && task.dataMap.zone) {
                            taskName += '(' + task.dataMap.zone + ')';
                        }
                        let order = parseInt(index) + 1;
                        msg += order + '. ' + taskName + '：' + task.status + '\n';
                    }
                }

                return msg;
            },
            handleResize: function () {
                let cardPanel = document.getElementById("release-card-pannel");

                if (cardPanel != null) {
                    this.cardLayout.column = parseInt(cardPanel.clientWidth / 440);
                    this.cardLayout.span = 24 / this.cardLayout.column;
                }
            },
            refreshReleseGroups() {
                this.$store.dispatch('fetchReleseGroupsByEnvAndAppId', {env: this.routeParam.env, appId: this.routeParam.appId});
            },
            onInterval: function () {
                if (this.routeParam.env && this.routeParam.appId) {
                    this.refreshReleseGroups();
                }
            },
            onCreate() {
                this.$store.dispatch('fetchValuableImageList', {env: this.siteInView.environment, appId: this.siteInView.appId, appName: this.siteInView.appName});
                this.$store.dispatch('fetchAppQuotaStatus', {
                    environment: this.siteInView.environment,
                    appId: this.siteInView.appId
                });
                this.newGroup.zone = this.siteInView.zones;
                this.dialogVisible = true;
            },
            onCreateFlink() {
                this.$store.dispatch('fetchValuableImageList', {env: this.siteInView.environment, appId: this.siteInView.appId, appName: this.siteInView.appName});
                this.$store.dispatch('fetchAppQuotaStatus', {
                    environment: this.siteInView.environment,
                    appId: this.siteInView.appId
                });
                this.$store.dispatch('fetchHadoopConfigs', {env:this.siteInView.environment,department: this.myApps[0].departmentCode});
                this.newFlinkGroup.zone = this.siteInView.zones;
                this.flinkDialogVisible = true;
            },
            onClose() {
                this.dialogVisible = false;
                this.$refs["create-group-form"].resetFields();
            },
            onCloseFlink() {
                this.flinkDialogVisible = false;
                this.$refs["create-flinkjob-group-form"].resetFields();
            },
            createFlinkGroup() {
                this.$refs["create-flinkjob-group-form"].validate((valid) => {
                    if (valid) {
                        this.$confirm('发布镜像为<strong>' + this.newFlinkGroup.instanceImage.name + '</strong>，是否继续？', '提示', {
                            confirmButtonText: '确定',
                            cancelButtonText: '取消',
                            type: 'warning',
                            dangerouslyUseHTMLString: true
                        }).then(() => {
                            let data = {
                                siteId: this.querySiteId,
                                group: {
                                    "id": null,
                                    "siteId": this.querySiteId,
                                    "appType": "SERVICE",
                                    "releaseTargetId": this.newFlinkGroup.instanceImage.id,
                                    "releaseTargetType": "IMAGE",
                                    "instanceSpec": this.newFlinkGroup.instanceSpec,
                                    "instanceCount": this.newFlinkGroup.instanceCount,
                                    "portCount": 8009,
                                    "keepAlive": true,
                                    "networkType": "MACVLAN",
                                    "zone": this.newFlinkGroup.zone.toString(),
                                    "cmd": this.newFlinkGroup.cmd,
                                    "hadoopConfig": this.newFlinkGroup.hadoopConfig,
                                    "checkpoint" : this.newFlinkGroup.checkpoint
                                }
                            };
                            this.$store.dispatch('createFlinkGroup', data);
                            this.onCloseFlink();
                        });
                    } else {
                        return false;
                    }
                });
            },
            changeInstanceSpec() {
                if (this.siteInView.enableStaticResource) {
                    this.$store.dispatch('fetchAvailableResources', {
                        appId: this.siteInView.appId,
                        env: this.siteInView.environment,
                        spec: this.newGroup.instanceSpec
                    });
                    this.newGroup.staticResources = [];
                }
            },
            createGroup() {
                this.$refs["create-group-form"].validate((valid) => {
                    if (valid) {
                        let data = {
                            env: this.siteInView.environment,
                            appId: this.siteInView.appId,
                            appName: this.siteInView.appName,
                            releaseTarget: this.newGroup.instanceTarget,
                            instanceSpec: this.newGroup.instanceSpec,
                            instanceCount: this.siteInView.enableStaticResource ? this.newGroup.staticResources.length : this.newGroup.instanceCount,
                            staticResources: this.siteInView.enableStaticResource ? this.newGroup.staticResources.toString() : null,
                            portCount: this.newGroup.instancePortCount,
                            zone: this.newGroup.zone.toString()
                        };
                        this.$store.dispatch('createReleaseGroup', data);
                        this.onClose();
                    } else {
                        return false;
                    }
                });
            },
            removeGroup(group) {
                if (group.instanceUpPercentage == 0) {
                    this.$confirm('此操作将删除该发布组下的所有实例，请确认这些实例没有流量接入。是否继续？', '删除发布组（' + this.currentEnv + '）', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    }).then(() => {
                        let data = {
                            env: this.siteInView.environment,
                            appId: this.siteInView.appId,
                            groupId: group.id
                        };
                        this.$store.dispatch('removeReleaseGroup', data);
                    }).catch(() => {
                        this.$message({
                            type: 'info',
                            message: '取消删除'
                        });
                    });
                } else {
                    this.$message({
                        type: 'warning',
                        message: '删除发布组前请将所有实例的流量拉出'
                    });
                }
            },
            restartJob(group) {
                this.$store.dispatch("restartJob", {jobId: group.jobInfo.id});
            },
            changeFlinkJobSpec() {
                this.$store.dispatch('fetchAvailableResources', {
                    appId: this.currentSite.appId,
                    env: this.currentSite.environment,
                    spec: this.newFlinkGroup.instanceSpec
                });
                this.newGroup.staticResources = [];
            }
        }
    }
</script>

<style>
    .release-group {
        padding: 20px 30px;
        margin: 20px;
        position: relative;
    }

    .release-group .el-row, .release-group .el-form-item {
        margin: 15px 0px;
    }

    .release-group-title {
        text-align: center;
        font-size: 1.6em;
        padding-bottom: 10px;
        word-break: break-word;
    }

    .release-group-item {
        box-shadow: 0 1px 5px 0 rgba(0,0,0,.1);
        padding: 0 15px;
        color: #606266;
    }

    .release-group-label {
        font-size: 15px;
        color: rgb(72, 87, 106);
        line-height: 2em;
        float: left;
        margin-right: 10px;
    }

    .release-group .el-select {
        width: 100%;
    }

    .release-group button {
        width: 100%;
    }

    .release-group > .el-card__body {
        padding: 0;
        position: relative;
    }

    #release-card-pannel .release-group-failed {
        box-shadow: 0 2px 40px 0 #e63c3c;
    }

    .release-group-loading-details {
        position: absolute;
        top: 80%;
        width: 100%;
        z-index: 2000;
        text-align: center;
    }

    .release-group-loading-details button {
        width: unset;
    }

    .release-group .btn-removal:hover,
    .release-group-loading-details .btn-removal:hover {
        color: #fff;
        background-color: #ff4949;
        border-color: #ff4949;
    }

    .el-input-group__prepend {
        padding: 0 10px;
    }

    .group-size {
        position: absolute;
        right: 5px;
        margin-top: 3px;
    }

    .card-bottom {
        margin-top: 13px;
        line-height: 12px;
    }

    .card-bottom .insert-by {
        font-size: 12px;
        color: #999;
        display: inline-block;
    }

    .card-bottom .create-time {
        font-size: 12px;
        color: #999;
        display: inline-block;
        float: right;
    }

    .el-loading-mask {
        z-index: 1999;
    }

    .el-loading-spinner {
        top: 30%;
    }

    .el-loading-spinner .el-loading-text {
        white-space: pre;
    }

    .release-group .el-tag {
        font-size: 14px;
    }

</style>