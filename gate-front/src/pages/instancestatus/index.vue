<template>
    <div class="content-panel">
        <el-row>
            <el-col>
                <el-breadcrumb separator="/">
                    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                    <el-breadcrumb-item :to="{ name: 'apps' }">应用列表</el-breadcrumb-item>
                    <el-breadcrumb-item :to="{ name: 'groups', query: { env: routeParam.env, appId: routeParam.appId }}">发布组列表</el-breadcrumb-item>
                    <el-breadcrumb-item>实例操作</el-breadcrumb-item>
                </el-breadcrumb>
            </el-col>
        </el-row>

        <el-row class="detail-form">

            <div class="detail-box">
                <div class="detail-box-left">环境</div>
                <div class="detail-box-right">{{groupInView.environment}}</div>
            </div>

            <div class="detail-box">
                <div class="detail-box-left">应用</div>
                <div class="detail-box-right">{{groupInView.appName}}</div>
            </div>

            <div class="detail-box">
                <div class="detail-box-left">域名</div>
                <div class="detail-box-right">{{groupInView.domain}}</div>
            </div>

            <div class="detail-box">
                <div class="detail-box-left">发布组</div>
                <div class="detail-box-right">{{groupInView.groupName}}</div>
            </div>

            <el-button type="primary" @click="displayReleaseLog">发布日志</el-button>
        </el-row>

        <br/>
        <br/>

        <el-row class="job-status" v-if="releaseJob.status != null">
            <el-steps :align-center="true" :active="stepsActive">
                <el-step v-for="task in releaseJob.taskInfos" :key="task.id" :title="task.name"
                         :status="task.status"></el-step>
            </el-steps>
            <div v-if="releaseJob.additionalInfo != null" style="text-align: center; margin-top: 10px">
                <el-tag type="warning">{{releaseJob.additionalInfo}}</el-tag>
            </div>
            <div v-if="isReleaseJobFailed" style="text-align: center; margin-top: 10px">
                <el-button @click="restartJob" type="warning" size="small">重新执行</el-button>
            </div>
        </el-row>

        <el-row class="query-form instance-operation">
            <el-button type="primary" @click="batchPullInInstance">拉入流量</el-button>
            <el-button type="primary" @click="batchPullOutInstance">拉出流量</el-button>
            <el-button type="primary" @click="onBatchUpdateInstance" style="margin-left: 40px">更新实例</el-button>
            <el-button type="primary" @click="onBatchRestartInstance">重启实例</el-button>
            <el-button type="primary" @click="onAddInstance" style="margin-left: 40px">添加实例</el-button>
            <el-button type="danger" @click="batchDeleteInstance">删除实例</el-button>
        </el-row>

        <br/>

        <el-table :data="instances" style="width: 100%" :row-key="getRowKeys"
                  ref="multipleTable" :default-sort="{prop: 'id', order: 'ascending'}">
            <el-table-column type="selection" :reserve-selection="true" width="50" align="center"></el-table-column>
            <el-table-column prop="id" label="序号" width="50" align="center"></el-table-column>
            <el-table-column prop="name" label="实例名" align="center" width="260"></el-table-column>
            <el-table-column prop="zone" label="部署区域" align="center"></el-table-column>
            <el-table-column prop="releaseVersion" label="镜像版本" align="center"></el-table-column>
            <el-table-column prop="ip" label="实例IP" align="center"></el-table-column>
            <el-table-column prop="port" label="端口" align="center" width="80"></el-table-column>
            <el-table-column prop="agentHost" label="物理机IP" align="center" v-if="isAdmin"></el-table-column>
            <el-table-column prop="releaseTime" label="发布时间" align="center" :formatter="dateFormatter"></el-table-column>
            <el-table-column label="资源类型" align="center" width="90">
                <template scope="props">
                    <el-tag :type="props.row.isStatic ? 'success' : 'info'"
                            :disable-transitions="true">{{props.row.isStatic ? '静态' : '动态'}}</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="slbSiteServerId" label="内存超配" align="center" width="90" v-if="isAdmin">
                <template scope="props">
                    <el-tag v-if="props.row.slbSiteServerId == null" type="danger" :disable-transitions="true">否</el-tag>
                    <el-tag v-else-if="(props.row.slbSiteServerId & 1) == 1" type="success" :disable-transitions="true">是</el-tag>
                    <el-tag v-else-if="(props.row.slbSiteServerId & 1) == 0" type="danger" :disable-transitions="true">否</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="容器状态" align="center" width="100" v-if="isAdmin">
                <template scope="props">
                    <el-tag :type="containerStatusTagType(props.row.containerStatus)"
                            v-if="props.row.containerStatus != null"
                            :disable-transitions="true">{{props.row.containerStatus}}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="健康检查" align="center" width="100">
                <template scope="props">
                    <el-tag type="success" v-if="props.row.ready != null && props.row.ready"
                            :disable-transitions="true">Ready</el-tag>
                    <el-tag type="danger" v-if="props.row.ready != null && !props.row.ready" :disable-transitions="true" class="unready-tag"
                            @click.native="displayContainerLog(props.row.name)">Unready</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="opsPulledIn" label="流量状态" align="center" width="90">
                <template scope="props">
                    <el-tag v-if="props.row.opsPulledIn == 1" type="success" :disable-transitions="true">Up</el-tag>
                    <el-tag v-else-if="props.row.opsPulledIn == 0" type="danger" :disable-transitions="true">Down</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="容器操作" align="center" width="130">
                <template scope="props">
                    <el-tooltip effect="dark" content="日志" placement="bottom">
                        <el-button type="primary" icon="el-icon-tickets" circle style="padding: 8px" @click="displayContainerLog(props.row.name)"></el-button>
                    </el-tooltip>
                    <el-tooltip effect="dark" content="终端" placement="bottom">
                        <el-button type="primary" icon="el-icon-mobile-phone" circle style="padding: 8px" @click="displayContainerConsole(props.row)" :disabled="!props.row.containerConsoleUrl"></el-button>
                    </el-tooltip>
                    <el-tooltip effect="dark" content="工具" placement="bottom">
                        <el-button type="primary" icon="el-icon-phone-outline" circle style="padding: 8px" @click="displayContainerTool(props.row)"></el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
        </el-table>

        <el-dialog :title="'添加实例（' + groupInView.environment + '）'" :visible.sync="addInstanceVisible" width="600px" :before-close="onCloseAddInstance">
            <el-form label-width="80px" label-position="left" :model="addInstanceRequest" :rules="addInstanceRules" ref="addInstanceForm">
                <el-form-item label="应用镜像" prop="image">
                    <el-select v-model="addInstanceRequest.image" filterable placeholder="请选择应用镜像" style="width: 100%">
                        <el-option v-for="item in images" :key="item.id" :label="item.name"
                                   :value="item.name">
                            <span style="float: left">{{ item.name }}</span>
                            <span style="float: right; color: #8492a6; font-size: 13px">{{ formatImageDeployAt(item.deployAt) }}</span>
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="发布规格" required>
                    <el-input :value="groupInView.instanceSpec + '（可发实例数：' + instanceSpecFreeCount + '）'" disabled></el-input>
                </el-form-item>
                <el-form-item label="静态资源" prop="staticResources" v-if="groupInView.enableStaticResource">
                    <el-select v-model="addInstanceRequest.staticResources"
                               placeholder="请选择静态资源"
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
                <el-form-item label="实例个数" prop="instanceCount" v-if="!groupInView.enableStaticResource">
                    <el-input v-model.number="addInstanceRequest.instanceCount" placeholder="请输入实例个数"></el-input>
                </el-form-item>
                <el-form-item label="部署区域" prop="zone" v-if="!groupInView.enableStaticResource">
                    <el-checkbox-group v-model="addInstanceRequest.zone" style="height: 40px">
                        <el-checkbox v-for="item in groupInView.zones" :label="item" name="zone"></el-checkbox>
                    </el-checkbox-group>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseAddInstance" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="addInstance" style="float:right;margin:0 10px 0 0;">添加</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog :title="'更新实例（' + groupInView.environment + '）'" :visible.sync="updateInstanceVisible" width="550px"
                   :before-close="onCloseUpdateInstance">
            <el-form label-width="80px" label-position="left" :model="updateInstanceRequest" :rules="updateInstanceRules" ref="updateInstanceForm">
                <el-form-item label="应用镜像" prop="image">
                    <el-select v-model="updateInstanceRequest.image" filterable placeholder="请选择应用镜像" style="width: 100%">
                        <el-option v-for="item in images" :key="item.id" :label="item.name" :value="item.name">
                            <span style="float: left">{{ item.name }}</span>
                            <span style="float: right; color: #8492a6; font-size: 13px">{{ formatImageDeployAt(item.deployAt) }}</span>
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseUpdateInstance" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="batchUpdateInstance" style="float:right;margin:0 10px 0 0;">更新</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog :title="'重启实例（' + groupInView.environment + '）'" :visible.sync="restartInstanceVisible" width="550px"
                   :before-close="onRestartUpdateInstance">
            <el-form label-width="80px" label-position="left" :model="restartInstanceRequest">
                <el-form-item>
                    <span>此操作会将所有选中的实例重启。是否继续？</span>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseRestartInstance" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="batchRestartInstance" style="float:right;margin:0 10px 0 0;">重启</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog title="发布日志" :visible.sync="logVisible" top="40px" width="90%" custom-class="log-dialog">
            <i @click="displayReleaseLog" class="el-icon-refresh refresh-log-button" style="right: 50px" title="刷新日志"></i>
            <div class="release-log" :style="{height: logHeight + 'px'}">{{releaseLog.logs}}</div>
        </el-dialog>

        <el-dialog title="容器日志" :visible.sync="containerLogVisible" top="40px" width="90%" custom-class="log-dialog" :before-close="onCloseContainerLog">
            <i @click="refreshContainerLog" class="el-icon-refresh refresh-log-button" title="刷新日志" v-if="!autoRefreshContainerLog"></i>
            <el-switch v-model="autoRefreshContainerLog" active-color="#13ce66" inactive-color="#ff4949" @change="changeAutoRefresh"
                       class="auto-refresh-switch" :title="autoRefreshContainerLog ? '关闭自动刷新' : '开启自动刷新'"></el-switch>
            <div class="container-log" :style="{height: logHeight + 'px'}">{{containerLog.logs}}</div>
        </el-dialog>

        <el-dialog :visible.sync="containerConsoleVisible" top="40px" custom-class="console-dialog" :before-close="onCloseContainerConsole" v-if="selectedInstanceConsoleUrl">
            <iframe :src="selectedInstanceConsoleUrl" :width="consoleWidth + 'px'" :height="consoleHeight + 'px'" frameborder="0" scrolling="no"
                    style="position: absolute; top: -177px; left: -258px;">
            </iframe>
        </el-dialog>

        <el-dialog title="容器工具" :visible.sync="containerToolVisible" top="80px" width="1000px" custom-class="log-dialog container-tool" :before-close="onCloseContainerTool">
            <el-input placeholder="请选择实例" v-model="execCommandRequest.instance" readonly>
                <template slot="prepend">实例名</template>
            </el-input>
            <el-input placeholder="请输入内容" v-model="execCommandRequest.args" style="margin: 10px 0;">
                <el-select v-model="execCommandRequest.type" slot="prepend" placeholder="请选择命令">
                    <el-option label="curl" value="curl"></el-option>
                    <el-option label="ping" value="ping"></el-option>
                </el-select>
                <el-button slot="append" icon="el-icon-search" @click="execCommand" :disabled="execCommandDisable"></el-button>
            </el-input>
            <div class="command-log" style="height: 600px">{{execCommandResult.stdout}}</div>
        </el-dialog>
    </div>
</template>

<script>
    import {mapGetters, mapActions} from 'vuex'

    export default {
        data: function () {
            return {
                routeParam: {
                    env: null,
                    appId: null,
                    groupId: null
                },
                groupInView: {
                    environment: null,
                    appId: null,
                    appName: null,
                    domain: null,
                    owner: null,
                    groupId: null,
                    groupName: null,
                    releaseVersion: null,
                    releaseTarget: null,
                    instanceSpec: null,
                    enableStaticResource: null,
                    zones: []
                },
                releaseJob: {},
                addInstanceRequest: {
                    image: null,
                    instanceCount: null,
                    zone: [],
                    staticResources: []
                },
                updateInstanceRequest: {
                    groupId: null,
                    instanceNames: null,
                    image: null,
                    appName: null
                },
                updateInstanceRules: {
                    image: [
                        {required: true, message: '请选择应用镜像', trigger: 'change'}
                    ]
                },
                restartInstanceRequest: {},
                execCommandRequest: {
                    type: null,
                    args: null,
                    instance: null
                },
                execCommandDisable: false,
                addInstanceVisible: false,
                updateInstanceVisible: false,
                restartInstanceVisible: false,
                logVisible: false,
                containerLogVisible: false,
                containerConsoleVisible: false,
                containerToolVisible: false,
                selectedInstanceName: null,
                selectedInstanceConsoleUrl: null,
                logHeight: 0,
                consoleWidth: 0,
                consoleHeight: 0,
                autoRefreshContainerLog: true
            }
        },
        computed: {
            ...mapGetters({
                instances: 'getReleaseGroupInstances',
                images: 'getValuableImageList',
                currentGroup: 'getCurrentGroup',
                jobStatus: 'getReleaseGroupJobStatus',
                appQuotaStatus: 'getAppQuotaStatus',
                releaseLog: 'getReleaseLog',
                containerLog: 'getContainerLog',
                availableResources: 'getAvailableResources',
                execCommandResult: 'getExecCommandResult',
                currentEnv: 'getCurrentEnv',
                currentAppId: 'getCurrentAppId',
                username: 'getUserName',
                userRoles: 'getUserRoles'
            }),
            addInstanceRules: function () {
                return {
                    image: [
                        {required: true, message: '请选择应用镜像', trigger: 'change'}
                    ],
                    instanceCount: [
                        {required: !this.groupInView.enableStaticResource, message: '请输入实例个数'},
                        {type: 'number', min: 1, max: 100, message: '请输入1-100之间的数字'}
                    ],
                    staticResources: [
                        {type: 'array', required: this.groupInView.enableStaticResource, message: '请至少选择一个静态资源', trigger: 'change'}
                    ],
                    zone: [
                        {type: 'array', required: true, message: '请至少选择一个部署区域', trigger: 'change'}
                    ]
                };
            },
            isAdmin: function () {
                return this.userRoles != null && this.userRoles.includes('admin');
            },
            isReleaseJobFailed() {
                return (this.releaseJob.status != null && (this.releaseJob.status == "FAIL" || this.releaseJob.status == "EXPIRED"));
            },
            instanceSpecFreeCount() {
                let instanceSpecFreeCount = 0;
                for (let specQuota of this.appQuotaStatus) {
                    if (specQuota.spectypeName == this.groupInView.instanceSpec) {
                        instanceSpecFreeCount = specQuota.freeCount;
                        break;
                    }
                }
                return instanceSpecFreeCount;
            },
            selectedInstances() {
                let instances = [];
                for (let instance of this.$refs.multipleTable.selection) {
                    if (instance.groupId == this.groupInView.groupId) {
                        instances.push(instance);
                    }
                }
                return instances;
            },
            selectedInstanceNames() {
                let instanceNames = [];
                for (let instance of this.selectedInstances) {
                    instanceNames.push(instance.name);
                }
                return instanceNames.toString();
            },
            stepsActive() {
                let active = 0;
                if (this.releaseJob && this.releaseJob.taskInfos) {
                    for (let taskInfo of this.releaseJob.taskInfos) {
                        if (taskInfo.status == 'success') {
                            active++;
                        }
                    }
                }
                return active;
            }
        },
        watch: {
            currentEnv: function (newCurrentEnv) {
                this.$router.push({name: 'groups', query: {env: newCurrentEnv, appId: this.routeParam.appId}});
            },
            currentGroup: function(newCurrentGroup) {
                this.groupInView = newCurrentGroup;

                if (this.currentGroup.owner) {
                    let owners = this.currentGroup.owner.split(',');
                    if (!owners.includes(this.username) && !this.isAdmin) {
                        this.$message.warning('您没有该应用的权限');
                        this.$router.push({name: 'apps'});
                    }
                }

                if (this.currentGroup.environment && this.currentGroup.environment != this.currentEnv) {
                    this.$store.dispatch('refreshCurrentEnv', this.currentGroup.environment);
                }

                if (this.currentGroup.appId && this.currentGroup.appId != this.currentAppId) {
                    this.$store.dispatch('refreshCurrentAppId', this.currentGroup.appId);
                }
            },
            jobStatus: function (newJobStatus) {
                let jobInfo = newJobStatus;
                this.releaseJob = {
                    id: jobInfo.id,
                    name: jobInfo.name,
                    operationTypeDesc: jobInfo.operationTypeDesc,
                    status: jobInfo.status,
                    additionalInfo: jobInfo.additionalInfo,
                    taskInfos: []
                };

                let firstTaskStatus = this.getFirstTaskStatus(jobInfo);
                this.releaseJob.taskInfos.push({
                    name: this.releaseJob.operationTypeDesc + "开始",
                    status: firstTaskStatus
                });

                for (let index in jobInfo.taskInfos) {
                    let task = jobInfo.taskInfos[index];
                    let taskStatus = this.getStepTaskStatus(task.status);
                    let taskName = task.description;
                    if (task.dataMap && task.dataMap.zone) {
                        taskName += '(' + task.dataMap.zone + ')';
                    }
                    let taskInfo = {
                        name: taskName,
                        status: taskStatus
                    };
                    this.releaseJob.taskInfos.push(taskInfo);
                }

                let lastTaskStatus = this.getLastTaskStatus(jobInfo);
                this.releaseJob.taskInfos.push({
                    name: this.releaseJob.operationTypeDesc + "结束",
                    status: lastTaskStatus
                });
            },
            releaseLog: function (newLog) {
                let documentView = document.getElementsByClassName("release-log")[0];
                if (documentView != null) {
                    documentView.scrollTop = documentView.scrollHeight;
                }
            },
            execCommandResult: function (data) {
                this.execCommandDisable = false;
            }
            // containerLog: function (newLog) {
            //     let documentView = document.getElementsByClassName("container-log")[0];
            //     if (documentView != null) {
            //         documentView.scrollTop = documentView.scrollHeight;
            //     }
            // }
        },
        created () {
            let query = this.$route.query;
            this.routeParam.env = query && query.env ? query.env : null;
            this.routeParam.appId = query && query.appId ? query.appId : null;
            this.routeParam.groupId = query && query.groupId ? query.groupId : null;

            if (this.routeParam.groupId) {
                this.$store.dispatch('fetchGroupById', {groupId: this.routeParam.groupId});
                this.refreshReleaseStatus();
            } else {
                this.$message('请选择要访问的发布组');
            }
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
            dateFormatter(row, column, cellValue) {
                return new Date(cellValue).toLocaleString();
            },
            getRowKeys(row) {
                return row.name;
            },
            formatImageDeployAt(timestamp) {
                if (timestamp == null)
                    return timestamp;
                return new Date(timestamp).toLocaleString() + '发布';
            },
            getStepTaskStatus(status){
                let stepStatus = "wait";
                if (status == "NEW") {
                    stepStatus = "wait";
                } else if (status == "RUNNING") {
                    stepStatus = "process";
                } else if (status == "FAIL" || status == "EXPIRED") {
                    stepStatus = "error";
                } else if (status == "SUCCESS") {
                    stepStatus = "success"
                }
                return stepStatus;
            },
            getFirstTaskStatus: function (jobInfo) {
                let taskStatus = "wait";

                // 当Job为新建状态时first task显示process，其它时候都显示成功
                if (jobInfo.status == "NEW") {
                    taskStatus = "process";
                } else if (jobInfo.status == "RUNNING") {
                    taskStatus = "success";
                } else if (jobInfo.status == "FAIL" || jobInfo.status == "EXPIRED") {
                    taskStatus = "success";
                } else if (jobInfo.status == "SUCCESS") {
                    taskStatus = "success"
                }

                return taskStatus;
            },
            getLastTaskStatus: function (jobInfo) {
                let taskStatus = "wait";

                if (jobInfo.status == "NEW") {
                    taskStatus = "wait";
                } else if (jobInfo.status == "RUNNING") {
                    taskStatus = "wait";
                } else if (jobInfo.status == "FAIL" || jobInfo.status == "EXPIRED") {
                    taskStatus = "error";
                } else if (jobInfo.status == "SUCCESS") {
                    taskStatus = "success"
                }

                return taskStatus;
            },
            containerStatusTagType(containerStatus) {
                let tagType = 'info';
                if (containerStatus == 'Running') {
                    tagType = 'success';
                } else if (containerStatus == 'Failed') {
                    tagType = 'danger';
                } else if (containerStatus == 'Unknown') {
                    tagType = 'warning';
                }
                return tagType;
            },
            handleResize: function () {
                let app = document.getElementById("app");
                this.consoleWidth = app.clientWidth * 0.8 + 258;
                if (this.consoleWidth < 1048) {
                    this.consoleWidth = 1048;
                }
                this.consoleHeight = app.clientHeight * 0.9 + 193;
                this.logHeight = app.clientHeight * 0.9 - 74;
            },
            onInterval: function () {
                this.refreshReleaseStatus();
            },
            refreshReleaseStatus() {
                if (this.routeParam.groupId) {
                    this.$store.dispatch('fetchReleaseGroupStatus', {groupId: this.routeParam.groupId});
                    this.$store.dispatch('fetchInstancesByGroupId', {groupId: this.routeParam.groupId});
                }
            },
            restartJob() {
                this.$store.dispatch('restartJob', {jobId: this.releaseJob.id});
            },
            displayReleaseLog() {
                if (this.groupInView.groupId && this.releaseJob && this.releaseJob.id) {
                    this.$store.dispatch('fetchReleaseLog', {
                        groupId: this.groupInView.groupId,
                        groupName: this.releaseJob.name,
                        jobId: this.releaseJob.id
                    });
                    this.logVisible = true;
                } else {
                    this.$message.warning("无法查看日志");
                }
            },
            displayContainerLog(instanceName) {
                if (instanceName) {
                    this.selectedInstanceName = instanceName;
                    this.containerLogVisible = true;

                    this.refreshContainerLog();

                    if (this.autoRefreshContainerLog) {
                        this.containerLogTimer = setInterval(this.refreshContainerLog.bind(this), 3000);
                    }
                } else {
                    this.$message.warning("无法查看日志");
                }
            },
            refreshContainerLog() {
                this.$store.dispatch('fetchContainerLog', {
                    instanceName: this.selectedInstanceName
                });
            },
            onCloseContainerLog() {
                this.containerLogVisible = false;
                clearInterval(this.containerLogTimer);
            },
            changeAutoRefresh() {
                if (this.autoRefreshContainerLog) {
                    this.refreshContainerLog();
                    this.containerLogTimer = setInterval(this.refreshContainerLog.bind(this), 3000);
                } else {
                    clearInterval(this.containerLogTimer);
                }
            },
            displayContainerConsole(instance) {
                this.containerConsoleVisible = true;
                this.selectedInstanceConsoleUrl = instance.containerConsoleUrl;
            },
            onCloseContainerConsole() {
                this.$confirm('此操作将关闭终端, 是否继续？', '关闭终端', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    this.containerConsoleVisible = false;
                    this.selectedInstanceConsoleUrl = null;
                });
            },
            displayContainerTool(instance) {
                this.containerToolVisible = true;
                if (instance.name != this.execCommandRequest.instance) {
                    this.$store.dispatch('clearExecCommandResult');
                }
                this.execCommandRequest.instance = instance.name;
            },
            onCloseContainerTool() {
                this.containerToolVisible = false;
            },
            execCommand() {
                if (this.execCommandRequest.type && this.execCommandRequest.args) {
                    this.$store.dispatch('execCommand', this.execCommandRequest);
                    this.execCommandDisable = true;
                } else {
                    this.$message.warning('请选择命令并输入内容');
                }
            },
            batchPullInInstance() {
                if(this.selectedInstanceNames) {
                    let allowed = true;
                    for (let instance of this.selectedInstances) {
                        if (!instance.ready) {
                            allowed = false;
                            this.$message.warning("拉入流量前请保证实例健康检查Ready");
                            break;
                        }
                    }

                    if (allowed) {
                        this.$confirm('此操作将为实例接入流量，请先确认所有选中的实例已经启动成功。是否继续？', '拉入流量（' + this.groupInView.environment + '）', {
                            confirmButtonText: '确定',
                            cancelButtonText: '取消',
                            type: 'warning'
                        }).then(() => {
                            let data = {
                                groupId: this.groupInView.groupId,
                                operatorType: 'PULL_IN',
                                instanceNames: this.selectedInstanceNames
                            };
                            this.$store.dispatch('pullReleaseGroupInstance', data);
                        });
                    }
                } else {
                    this.$message.warning('请选择实例');
                }
            },
            batchPullOutInstance() {
                if(this.selectedInstanceNames) {
                    this.$confirm('此操作会将所有选中实例的流量拉出。是否继续？', '拉出流量（' + this.groupInView.environment + '）', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    }).then(() => {
                        let data = {
                            groupId: this.groupInView.groupId,
                            operatorType: 'PULL_OUT',
                            instanceNames: this.selectedInstanceNames
                        };
                        this.$store.dispatch('pullReleaseGroupInstance', data);
                    });
                } else {
                    this.$message.warning('请选择实例');
                }
            },
            batchDeleteInstance() {
                if(this.selectedInstanceNames) {
                    let allowed = true;
                    for (let instance of this.selectedInstances) {
                        if (instance.status == 1) {
                            allowed = false;
                            this.$message.warning("删除实例前请将实例流量拉出");
                            break;
                        }
                    }

                    if (allowed) {
                        if (this.instances.length == this.selectedInstances.length) {
                            this.$message.warning("发布组容量须大于0，请尝试直接删除发布组。");
                        } else {
                            this.$confirm('此操作会将所有选中的实例删除。是否继续？', '删除实例（' + this.groupInView.environment + '）', {
                                confirmButtonText: '确定',
                                cancelButtonText: '取消',
                                type: 'warning'
                            }).then(() => {
                                let data = {
                                    env: this.groupInView.environment,
                                    appId: this.groupInView.appId,
                                    appName: this.groupInView.appName,
                                    groupId: this.groupInView.groupId,
                                    operatorType: 'REDUCE_GROUP',
                                    instanceCount: this.selectedInstances.length,
                                    instanceNames: this.selectedInstanceNames
                                };
                                this.$store.dispatch('resizeReleaseGroup', data);
                                this.$refs.multipleTable.clearSelection();
                            });
                        }
                    }
                } else {
                    this.$message.warning('请选择实例');
                }
            },
            onBatchUpdateInstance() {
                if(this.selectedInstanceNames) {
                    let allowed = true;
                    for (let instance of this.selectedInstances) {
                        if (instance.status == 1) {
                            allowed = false;
                            this.$message.warning("更新实例前请将实例流量拉出");
                            break;
                        }
                    }

                    if (allowed) {
                        this.$store.dispatch('fetchValuableImageList', {env: this.groupInView.environment, appId: this.groupInView.appId, appName: this.groupInView.appName});
                        this.updateInstanceVisible = true;
                    }
                } else {
                    this.$message.warning('请选择实例');
                }
            },
            onCloseUpdateInstance() {
                this.updateInstanceVisible = false;
                this.$refs["updateInstanceForm"].resetFields();
            },
            batchUpdateInstance() {
                this.$refs["updateInstanceForm"].validate((valid) => {
                    if (valid) {
                        let allowed = true;
                        for (let instance of this.selectedInstances) {
                            if (instance.releaseTarget == this.updateInstanceRequest.image) {
                                allowed = false;
                                this.$message.warning("更新实例不能选择原镜像");
                                break;
                            }
                        }

                        if (allowed) {
                            this.updateInstanceRequest.appName = this.groupInView.appName;
                            this.updateInstanceRequest.groupId = this.groupInView.groupId;
                            this.updateInstanceRequest.instanceNames = this.selectedInstanceNames;
                            this.$store.dispatch('updateReleaseGroupInstance', this.updateInstanceRequest);

                            this.onCloseUpdateInstance();
                        }
                    } else {
                        return false;
                    }
                });
            },
            onBatchRestartInstance() {
                if(this.selectedInstanceNames) {
                    let allowed = true;
                    for (let instance of this.selectedInstances) {
                        if (instance.status == 1) {
                            allowed = false;
                            this.$message.warning("更新实例前请将实例流量拉出");
                            break;
                        }
                    }

                    if (allowed) {
                        this.restartInstanceVisible = true;
                    }
                } else {
                    this.$message.warning('请选择实例');
                }
            },
            onCloseRestartInstance() {
                this.restartInstanceVisible = false;
            },
            batchRestartInstance() {
                let data = {
                    groupId: this.groupInView.groupId,
                    instanceNames: this.selectedInstanceNames,

                };
                this.$store.dispatch('restartReleaseGroupInstance', data);
                this.onCloseRestartInstance();
            },
            onAddInstance() {
                this.$store.dispatch('fetchAppQuotaStatus', {
                    appId: this.groupInView.appId,
                    environment: this.groupInView.environment
                });
                this.$store.dispatch('fetchValuableImageList', {env: this.groupInView.environment, appId: this.groupInView.appId, appName: this.groupInView.appName});
                if (this.groupInView.enableStaticResource) {
                    this.$store.dispatch('fetchAvailableResources', {
                        appId: this.groupInView.appId,
                        env: this.groupInView.environment,
                        spec: this.groupInView.instanceSpec
                    });
                }
                this.addInstanceRequest.image = this.groupInView.releaseTarget;
                this.addInstanceRequest.zone = this.groupInView.zones;
                this.addInstanceVisible = true;
            },
            onCloseAddInstance() {
                this.addInstanceVisible = false;
                this.$refs["addInstanceForm"].resetFields();
            },
            addInstance() {
                this.$refs["addInstanceForm"].validate((valid) => {
                    if (valid) {
                        let data = {
                            env: this.groupInView.environment,
                            appId: this.groupInView.appId,
                            appName: this.groupInView.appName,
                            groupId: this.groupInView.groupId,
                            operatorType: 'EXPAND_GROUP',
                            instanceCount: this.groupInView.enableStaticResource ? this.addInstanceRequest.staticResources.length : this.addInstanceRequest.instanceCount,
                            staticResources: this.groupInView.enableStaticResource ? this.addInstanceRequest.staticResources.toString() : null,
                            image: this.addInstanceRequest.image,
                            zone: this.addInstanceRequest.zone.toString(),
                        };
                        this.$store.dispatch('resizeReleaseGroup', data);
                        this.onCloseAddInstance();
                    } else {
                        return false;
                    }
                });
            }
        }
    }

</script>

<style>
    .job-status {
        padding: 3em 0;
    }
    .refresh-log-button {
        position: absolute;
        right: 100px;
        top: 18px;
        font-size: 20px;
    }
    .refresh-log-button:hover {
        color: #16B6D7;
        cursor: pointer;
    }
    .auto-refresh-switch {
        position: absolute;
        right: 50px;
        top: 18px;
    }
    .console-dialog {
        overflow: hidden;
        width: 80%;
        height: 90%;
        border: 10px solid #fff;
        border-radius: 2px;
        box-shadow: 0 1px 3px rgba(0,0,0,.3);
    }
    .console-dialog .el-dialog__body {
        padding: 20px 20px 0;
    }
    .instance-operation {
        text-align: center;
    }
    .instance-operation .el-button+.el-button {
        margin-left: 0px;
    }
    .unready-tag {
        cursor: pointer;
    }
    .unready-tag:hover {
        background-color: #f56c6c;
        color: #ffffff;
    }
    .container-tool .el-input-group__prepend {
        padding: 0 20px;
        width: 100px;
    }
</style>