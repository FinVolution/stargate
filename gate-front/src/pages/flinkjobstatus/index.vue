<template>
    <div class="content-panel">
        <el-row>
            <el-col>
                <el-breadcrumb separator="/">
                    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                    <el-breadcrumb-item :to="{ path: '/sites' }">站点列表</el-breadcrumb-item>
                    <el-breadcrumb-item :to="{ path: '/groups', query: { siteId: querySiteId }}">发布组列表
                    </el-breadcrumb-item>
                    <el-breadcrumb-item>FlinkJob操作</el-breadcrumb-item>
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
                <div class="detail-box-left">集群</div>
                <div class="detail-box-right">{{groupInView.siteName}}</div>
            </div>

            <div class="detail-box">
                <div class="detail-box-left">发布组</div>
                <div class="detail-box-right">{{groupInView.groupName}}</div>
            </div>

            <el-button type="primary" @click="displayReleaseLog">发布日志</el-button>
        </el-row>

        <br/>
        <br/>

        <el-row class="job-status">
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

        <br/>

        <el-table :data="flinkJobs" style="width: 100%" :default-sort="{prop: 'id', order: 'ascending'}">
            <el-table-column prop="name" label="Job名称" align="center"></el-table-column>
            <el-table-column prop="hadoopName" label="集群名称" align="center"></el-table-column>
            <el-table-column prop="version" label="版本" align="center"></el-table-column>
            <el-table-column prop="runningTaskTotal" label="运行任务数" align="center"></el-table-column>
            <el-table-column prop="taskTotal" label="总任务数" align="center"></el-table-column>
            <el-table-column prop="status" label="任务状态" align="center"></el-table-column>

            <el-table-column label="操作" align="center" width="500">
                <template scope="props">
                    <el-button v-if="props.row.status === 'NOTRUNNING'" type="primary" @click="startFlinkJob">启动
                    </el-button>
                    <el-button v-if="props.row.status !== 'NOTRUNNING'" type="primary" disabled @click="startFlinkJob">
                        启动
                    </el-button>
                    <el-button type="primary" @click="stopFlinkJob">停止</el-button>
                    <!--                    <el-button v-if="props.row.status !== 'RUNNING'" type="primary" disabled @click="stopFlinkJob">停止-->
                    <!--                    </el-button>-->
                    <el-button type="primary" @click="onUpdateFlinkJob">更新</el-button>
                    <el-button type="primary" @click="restartFlinkJob">重启</el-button>
                    <el-button type="primary" @click="displayContainerLog(props.row.instanceName)">日志</el-button>
                    <el-button v-if="props.row.status === 'RUNNING'" type="primary" @click="openDashboard(props.row)">
                        ui
                    </el-button>

                    <el-button v-if="props.row.status !== 'RUNNING'" disabled type="primary"
                               @click="openDashboard(props.row)">ui
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-dialog :title="'启动FlinkJob（' + groupInView.environment + '）'" :visible.sync="runFlinkJobVisible"
                   width="550px"
                   :before-close="onCloseRunFlinkJob">
            <el-form label-width="120px" label-position="left">
                <el-form-item label="savepoint启用">
                    <el-switch
                            v-model="savepointSwitch"
                            active-color="#13ce66"
                            inactive-color="#ff4949"
                            @change="changeSavepointSwitch">
                    </el-switch>
                </el-form-item>
                <el-form-item label="默认路径">
                    <!--                    <el-tag type="info">{{defaultSavepointPath}}</el-tag>-->
                    <el-input
                            disabled
                            type="textarea"
                            :rows="2"
                            placeholder="请输入内容"
                            v-model="defaultSavepointPath">
                    </el-input>
                </el-form-item>


                <el-form-item label="savepointPath">
                    <el-input
                            type="textarea"
                            :rows="2"
                            placeholder="请输入savepointPath"
                            v-model="savepointPath">
                    </el-input>
                </el-form-item>
                <el-form-item label="启动参数">
                    <el-input
                            type="textarea"
                            :rows="2"
                            placeholder="请输入启动参数"
                            v-model="cmd">
                    </el-input>
                </el-form-item>
                <el-form-item label="变量参数">
                    <el-input
                            type="textarea"
                            :rows="2"
                            placeholder="请输入变量参数"
                            v-model="variable">
                    </el-input>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseRunFlinkJob" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="runFlinkJob" style="float:right;margin:0 10px 0 0;">启动</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
        <el-dialog :title="'更新FlinkJob（' + groupInView.environment + '）'" :visible.sync="updateFlinkJobVisible"
                   width="550px"
                   :before-close="onCloseUpdateFlinkJob">
            <el-form label-width="120px" label-position="left" :model="updateFlinkJobRequest"
                     :rules="updateFlinkJobRules" ref="updateFlinkJobForm">
                <el-form-item label="应用镜像" prop="image">
                    <el-select v-model="updateFlinkJobRequest.image" filterable placeholder="请选择应用镜像"
                               style="width: 100%">
                        <el-option v-for="item in images" :key="item.id" :label="item.name" :value="item.name">
                            <span style="float: left">{{ item.name }}</span>
                            <span style="float: right; color: #8492a6; font-size: 13px">{{ formatImageDeployAt(item.deployAt) }}</span>
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="启动参数">
                    <el-input
                            type="textarea"
                            :rows="2"
                            placeholder="请输入启动参数"
                            v-model="cmd">
                    </el-input>
                </el-form-item>
                <el-form-item label="变量参数">
                    <el-input
                            type="textarea"
                            :rows="2"
                            placeholder="请输入启动参数"
                            v-model="variable">
                    </el-input>
                </el-form-item>
                <el-form-item label="默认路径">
                    <el-input
                            disabled
                            type="textarea"
                            :rows="2"
                            placeholder="job停止后生成的savepoint路径"
                            v-model="defaultSavepointPath">
                    </el-input>
                </el-form-item>
                <el-form-item label="savepointPath" prop="savepointPath">
                    <el-input filterable
                              type="textarea"
                              :rows="2"
                              placeholder="请输入savepointPath"
                              v-model="updateFlinkJobRequest.savepointPath">
                    </el-input>
                </el-form-item>
                <el-form-item label="savepoint启用">
                    <el-switch
                            v-model="updateFlinkJobRequest.savepointSwitch"
                            active-color="#13ce66"
                            inactive-color="#ff4949"
                            @change="changeSavepointSwitch">
                    </el-switch>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseUpdateFlinkJob" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="updateFlinkJob" style="float:right;margin:0 10px 0 0;">更新
                    </el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog title="发布日志" :visible.sync="logVisible" top="40px" width="90%" custom-class="log-dialog">
            <i @click="displayReleaseLog" class="el-icon-refresh refresh-log-button" style="right: 50px"
               title="刷新日志"></i>
            <div class="release-log">{{releaseLog.logs}}</div>
        </el-dialog>
        <el-dialog title="停止flink job" :visible.sync="stopFlinkVisible" width="30%"
                   custom-class="log-dialog">
            <el-form>
                <el-form-item label="强制停止">
                    <el-switch
                            v-model="destroy"
                            active-color="#13ce66"
                            inactive-color="#ff4949">
                    </el-switch>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseStopFlinkJob" style="float: right">取消</el-button>
                    <el-button type="primary" @click="canFlinkJob" style="float:right;margin:0 10px 0 0;">停止
                    </el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
        <el-dialog title="容器日志" :visible.sync="containerLogVisible" top="40px" width="90%" custom-class="log-dialog"
                   :before-close="onCloseContainerLog">
            <i @click="refreshContainerLog" class="el-icon-refresh refresh-log-button" title="刷新日志"
               v-if="!autoRefreshContainerLog"></i>
            <el-switch v-model="autoRefreshContainerLog" active-color="#13ce66" inactive-color="#ff4949"
                       @change="changeAutoRefresh"
                       class="auto-refresh-switch" :title="autoRefreshContainerLog ? '关闭自动刷新' : '开启自动刷新'"></el-switch>
            <div class="container-log" :style="{height: logHeight + 'px'}" v-html="containerLog.logs"></div>
        </el-dialog>
    </div>
</template>

<script>
    import {mapGetters} from 'vuex'
    import localStroageUtil from '../../utils/localStorageUtil'

    export default {
        data: function () {
            return {
                destroy: false,
                stopFlinkVisible: false,
                containerLogTimer: null,
                selectedInstanceName: null,
                containerLogVisible: false,
                cmd: null,
                variable: null,
                savepointSwitch: true,
                savepointPath: null,
                defaultSavepointPath: null,
                runFlinkJobVisible: false,
                querySiteId: null,
                queryGroupId: null,
                groupInView: {
                    environment: "",
                    appName: "",
                    siteName: "",
                    groupName: ""
                },
                releaseJob: {},
                updateFlinkJobRequest: {
                    groupId: null,
                    image: null,
                    savepointSwitch: true,
                    savepointPath: null,
                    cmd: null,
                    variable: null
                },
                updateFlinkJobRules: {
                    image: [
                        {required: true, message: '请选择应用镜像', trigger: 'change'}
                    ],
                    savepointPath: [
                        {required: false, message: '请选择应用镜像', trigger: 'change'}
                    ]
                },
                updateFlinkJobVisible: false,
                logVisible: false,
                autoRefreshContainerLog: true,
            }
        },
        computed: {
            ...mapGetters({
                flinkJobs: 'getReleaseFlinkJobs',
                images: 'getValuableImageList',
                currentGroup: 'getCurrentGroup',
                jobStatus: 'getReleaseGroupJobStatus',
                releaseLog: 'getReleaseLog',
                currentEnv: 'getCurrentEnv',
                username: 'getUserName',
                userRoles: 'getUserRoles',
                flinkGroup: 'getFlinkJobGroup',
                containerLog: 'getFlinkContainerLog',
            }),
            isAdmin: function () {
                return this.userRoles != null && this.userRoles.includes('admin');
            },
            isReleaseJobFailed() {
                return (this.releaseJob.status != null && (this.releaseJob.status == "FAIL" || this.releaseJob.status == "EXPIRED"));
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
            flinkGroup: function (newFlinkGroup) {
                this.cmd = newFlinkGroup.cmd;
                this.variable = newFlinkGroup.variable;
            },
            flinkJobs: function (newFlinkJobs) {
                if (newFlinkJobs != null && newFlinkJobs.length > 0 && newFlinkJobs[0].status === 'RUNNING') {
                    this.defaultSavepointPath = null;
                } else {
                    this.defaultSavepointPath = newFlinkJobs[0].savepointPath;
                }
            },
            currentGroup: function (newCurrentGroup) {
                this.groupInView = newCurrentGroup;

                if (this.currentGroup.owner) {
                    let owners = this.currentGroup.owner.split(',');
                    if (!owners.includes(this.username) && !this.isAdmin) {
                        this.$message.warning('您没有该应用的权限');
                        this.$router.push({name: 'apps'});
                    }
                }

                if (this.currentGroup.environment != this.currentEnv) {
                    localStroageUtil.saveEnvironment(this.currentGroup.environment);
                    this.$store.dispatch('refreshCurrentEnv', this.currentGroup.environment);
                }

                if (this.currentGroup.appId) {
                    localStroageUtil.saveLastQueryAppId(this.currentGroup.appId);
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
            }
        },
        created() {
            let query = this.$route.query;
            this.querySiteId = query && query.siteId ? query.siteId : null;
            this.queryGroupId = query && query.groupId ? query.groupId : null;

            // 若没有查询的siteId或groupId，则自动切换到上次访问的发布组
            if (this.querySiteId == null || this.queryGroupId == null) {
                let lastQuerySiteId = localStorage.getItem("last-query-site-id");
                let lastQueryGroupId = localStorage.getItem("last-query-group-id");
                if (lastQuerySiteId != null && lastQueryGroupId != null) {
                    this.$router.push({path: '/flinkjobstatus?siteId=' + lastQuerySiteId + '&groupId=' + lastQueryGroupId});
                    this.$message('自动为您切换到上次访问的发布组，id=' + lastQueryGroupId);
                    this.querySiteId = lastQuerySiteId;
                    this.queryGroupId = lastQueryGroupId;
                }
            } else {
                localStorage.setItem("last-query-site-id", this.querySiteId);
                localStorage.setItem("last-query-group-id", this.queryGroupId);
            }

            if (this.queryGroupId != null) {
                this.$store.dispatch('fetchGroupById', {groupId: this.queryGroupId});
                this.refreshReleaseStatus();
            } else {
                this.$message('请选择访问的发布组。');
            }
        },
        mounted: function () {
            this.internalTimer = setInterval(this.onInterval.bind(this), 8000);
        },
        beforeDestroy: function () {
            clearInterval(this.internalTimer);
            window.removeEventListener('resize', this.handleResize);
        },
        methods: {
            onCloseContainerLog() {
                this.containerLogVisible = false;
                clearInterval(this.containerLogTimer);
            },
            changeAutoRefresh() {
                if (this.autoRefreshContainerLog) {
                    this.refreshContainerLog();
                    this.containerLogTimer = setInterval(this.refreshContainerLog.bind(this), 8000);
                } else {
                    clearInterval(this.containerLogTimer);
                }
            },
            displayContainerLog(instanceName) {
                if (instanceName) {
                    this.selectedInstanceName = instanceName;
                    this.containerLogVisible = true;

                    this.refreshContainerLog();

                    if (this.autoRefreshContainerLog) {
                        this.containerLogTimer = setInterval(this.refreshContainerLog.bind(this), 8000);
                    }
                } else {
                    this.$message.warning("无法查看日志");
                }
            },
            refreshContainerLog() {
                this.$store.dispatch('fetchFlinkContainerLog', {instanceName: this.selectedInstanceName});
            },
            changeSavepointSwitch() {
                if (this.savepointSwitch) {
                } else {
                    this.savepointPath = null;
                }
            },
            openDashboard(row) {
                window.open(row.dashboardAddress, '_blank');
            },
            formatImageDeployAt(timestamp) {
                if (timestamp == null)
                    return timestamp;
                return new Date(timestamp).toLocaleString() + '发布';
            },
            getStepTaskStatus(status) {
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
            onInterval: function () {
                if (document.visibilityState == 'visible') {
                    this.refreshReleaseStatus();
                }
            },
            refreshReleaseStatus() {
                if (this.queryGroupId != null) {
                    this.$store.dispatch('fetchReleaseGroupStatus', {"groupId": this.queryGroupId});
                    this.$store.dispatch('fetchFlinkJobByGroupId', {"groupId": this.queryGroupId});
                }
            },
            displayReleaseLog() {
                if (this.queryGroupId && this.releaseJob && this.releaseJob.id) {
                    this.$store.dispatch('fetchReleaseLog', {
                        groupId: this.queryGroupId,
                        groupName: this.releaseJob.name,
                        jobId: this.releaseJob.id
                    });
                    this.logVisible = true;
                } else {
                    this.$message.warning("无法查看日志");
                }
            },
            restartJob() {
                this.$store.dispatch("restartJob", {jobId: this.releaseJob.id});
            },
            startFlinkJob() {
                this.runFlinkJobVisible = true;
                this.$store.dispatch('fetchReleaseGroup', {groupId: this.queryGroupId});
            },
            runFlinkJob() {
                let data = {
                    groupId: this.queryGroupId,
                    savepointSwitch: this.savepointSwitch,
                    savepointPath: this.savepointPath,
                    cmd: this.cmd,
                    variable: this.variable
                };
                this.$store.dispatch('startFlinkJob', data);
                this.runFlinkJobVisible = false;
                this.savepointSwitch = true;
                this.$store.dispatch('fetchReleaseGroup', {groupId: this.queryGroupId});
            },
            onCloseStopFlinkJob() {
                this.stopFlinkVisible = false;
                this.destroy = false;
            },
            canFlinkJob() {
                let data = {
                    groupId: this.queryGroupId,
                    destroy: this.destroy
                };
                this.$store.dispatch('stopFlinkJob', data);
                this.stopFlinkVisible = false;
                this.destroy = false;
            },
            stopFlinkJob() {
                this.stopFlinkVisible = true;
            },
            onUpdateFlinkJob() {
                this.$store.dispatch('fetchValuableImageList', {
                    "appName": this.groupInView.appName,
                    "siteId": this.querySiteId
                });
                this.$store.dispatch('fetchReleaseGroup', {groupId: this.queryGroupId});
                this.updateFlinkJobVisible = true;
            },
            updateFlinkJob() {
                this.$refs["updateFlinkJobForm"].validate((valid) => {
                    if (valid) {
                        this.$confirm('发布镜像为<strong>' + this.updateFlinkJobRequest.image + '</strong>，是否继续？', '提示', {
                            confirmButtonText: '确定',
                            cancelButtonText: '取消',
                            type: 'warning',
                            dangerouslyUseHTMLString: true
                        }).then(() => {
                            this.updateFlinkJobRequest.groupId = this.queryGroupId;
                            this.updateFlinkJobRequest.cmd = this.cmd;
                            this.updateFlinkJobRequest.variable = this.variable;
                            this.$store.dispatch('updateFlinkJob', this.updateFlinkJobRequest);

                            this.onCloseUpdateFlinkJob();
                            this.$store.dispatch('fetchReleaseGroup', {groupId: this.queryGroupId});

                        });
                    } else {
                        return false;
                    }
                });
            },
            restartFlinkJob() {
                this.$confirm('此操作将会重启FlinkJob，是否继续？', '重启FlinkJob（' + this.groupInView.environment + '）', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    let data = {
                        groupId: this.queryGroupId,
                    };
                    this.$store.dispatch('restartFlinkJob', data);
                });
            },
            onCloseUpdateFlinkJob() {
                this.updateFlinkJobVisible = false;
                // this.$refs["updateFlinkJobForm"].resetFields();
                this.updateFlinkJobRequest.groupId = null;
                this.updateFlinkJobRequest.image = null;
                this.updateFlinkJobRequest.savepointSwitch = true;
                this.updateFlinkJobRequest.savepointPath = null;

            },
            onCloseRunFlinkJob() {
                this.runFlinkJobVisible = false;
                this.$store.dispatch('fetchReleaseGroup', {groupId: this.queryGroupId});
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
</style>