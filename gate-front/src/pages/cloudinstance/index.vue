<template>
    <div class="content-panel">
        <el-row>
            <div class="detail-box">
                <div class="detail-box-left">环境</div>
                <el-select v-model="queryForm.env" placeholder="选择环境" @change="onSearch" style="width: 200px">
                    <el-option v-for="item in envs" :key="item.id" :label="item.name" :value="item.name"></el-option>
                </el-select>
            </div>

            <div class="detail-box">
                <div class="detail-box-left">应用</div>
                <el-select v-model="queryForm.appId" placeholder="选择应用" filterable @change="onSearch" style="width: 300px">
                    <el-option v-for="item in apps" :key="item.id" :value="item.cmdbAppId"
                               :label="item.name + '/' + item.cmdbAppId"></el-option>
                </el-select>
            </div>

            <el-input v-model="queryForm.name" clearable placeholder="输入实例名"
                      class="query-box" prefix-icon="el-icon-search"></el-input>

            <el-input v-model="queryForm.ip" clearable placeholder="输入实例IP"
                      class="query-box" prefix-icon="el-icon-search"></el-input>

            <el-button type="primary" @click="onSearch">查询</el-button>
            <el-button @click="resetQueryForm">重置</el-button>
        </el-row>

        <br/>

        <el-table :data="cloudInstances" style="width: 100%" border fit>
            <el-table-column prop="env" label="环境" width="60" align="center"></el-table-column>
            <el-table-column prop="appId" label="应用ID" align="center"></el-table-column>
            <el-table-column prop="name" label="实例名" align="center"></el-table-column>
            <el-table-column prop="image" label="镜像" align="center"></el-table-column>
            <el-table-column prop="slotIp" label="实例IP" align="center"></el-table-column>
            <el-table-column prop="port" label="端口" align="center" width="80"></el-table-column>
            <el-table-column prop="zone" label="部署区域" align="center"></el-table-column>
            <el-table-column prop="spec" label="规格" align="center" width="100"></el-table-column>
            <el-table-column prop="hasPulledIn" label="流量状态" align="center" width="100">
                <template scope="props">
                    <el-tag v-if="props.row.hasPulledIn == 1" type="success" :disable-transitions="true">Up</el-tag>
                    <el-tag v-else-if="props.row.hasPulledIn == 0" type="danger" :disable-transitions="true">Down</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="releaseTime" label="发布时间" align="center" :formatter="dateFormatter"></el-table-column>
            <el-table-column label="操作" align="center" width="240">
                <template scope="props">
                    <el-tooltip effect="dark" content="详情" placement="bottom">
                        <el-button type="primary" icon="el-icon-view" circle style="padding: 8px" @click="refreshCloudInstanceStatus(props.row.name)"></el-button>
                    </el-tooltip>
                    <el-tooltip effect="dark" content="日志" placement="bottom">
                        <el-button type="primary" icon="el-icon-tickets" circle style="padding: 8px" @click="refreshCloudInstanceLog(props.row.name)"></el-button>
                    </el-tooltip>
                    <el-tooltip effect="dark" content="拉入" placement="bottom">
                        <el-button type="success" icon="el-icon-plus" circle style="padding: 8px" @click="pullInInstance(props.row.name)"></el-button>
                    </el-tooltip>
                    <el-tooltip effect="dark" content="拉出" placement="bottom">
                        <el-button type="success" icon="el-icon-minus" circle style="padding: 8px" @click="pullOutInstance(props.row.name)"></el-button>
                    </el-tooltip>
                    <el-tooltip effect="dark" content="重启" placement="bottom">
                        <el-button type="warning" icon="el-icon-refresh" circle style="padding: 8px" @click="restartInstance(props.row.name)"></el-button>
                    </el-tooltip>
                    <el-tooltip effect="dark" content="删除" placement="bottom">
                        <el-button type="danger" icon="el-icon-delete" circle style="padding: 8px" @click="deleteInstance(props.row.name)"></el-button>
                    </el-tooltip>
                </template>
            </el-table-column>
        </el-table>

        <div align='center' style="margin-top: 10px">
            <el-pagination
                    @size-change="handleSizeChange"
                    @current-change="handleCurrentChange"
                    :current-page="queryForm.page"
                    :page-sizes="[10, 30, 50, 100]"
                    :page-size="queryForm.size"
                    layout="total, sizes, prev, pager, next, jumper"
                    :total="total">
            </el-pagination>
        </div>

        <el-dialog title="实例详情" :visible.sync="detailDialogVisible" width="700px" top="80px" class="instance-detail-dialog">
            <el-form label-position="left" label-width="140px">
                <el-form-item label="应用名">
                    <span>{{ instanceStatus.appName }}</span>
                </el-form-item>
                <el-form-item label="实例名">
                    <span>{{ instanceStatus.name }}</span>
                </el-form-item>
                <el-form-item label="环境变量">
                    <span>{{ instanceStatus.envVars }}</span>
                </el-form-item>
                <el-form-item label="物理机IP">
                    <span>{{ instanceStatus.hostIp }}</span>
                </el-form-item>
                <el-form-item label="实例IP">
                    <span>{{ instanceStatus.instanceIp }}</span>
                </el-form-item>
                <el-form-item label="容器ID">
                    <span>{{ instanceStatus.containerId }}</span>
                </el-form-item>
                <el-form-item label="资源类型">
                    <el-tag :type="instanceStatus.isStatic ? 'success' : 'info'" :disable-transitions="true">{{instanceStatus.isStatic ? '静态' : '动态'}}</el-tag>
                </el-form-item>
                <el-form-item label="容器状态">
                    <el-tag :type="containerStatusTagType(instanceStatus.containerStatus)" v-if="instanceStatus.containerStatus != null"
                            :disable-transitions="true">{{instanceStatus.containerStatus}}</el-tag>
                </el-form-item>
                <el-form-item label="健康检查">
                    <el-tag :type="instanceStatus.ready ? 'success' : 'danger'" v-if="instanceStatus.ready != null"
                            :disable-transitions="true">{{instanceStatus.ready ? 'Ready' : 'Unready'}}</el-tag>
                </el-form-item>
                <el-form-item label="流量状态">
                    <el-tag v-if="instanceStatus.opsPulledIn == 1" type="success" :disable-transitions="true">Up</el-tag>
                    <el-tag v-else-if="instanceStatus.opsPulledIn == 0" type="danger" :disable-transitions="true">Down</el-tag>
                </el-form-item>
                <el-form-item label="容器终端">
                    <a :href="instanceStatus.containerConsoleUrl" target="_blank" style="color: #16B6D7">在新标签页中打开终端</a>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog title="容器日志" :visible.sync="containerLogVisible" top="40px" width="90%" custom-class="log-dialog">
            <div class="container-log">{{instanceLog}}</div>
        </el-dialog>
    </div>
</template>

<script>
    import {mapGetters, mapActions} from 'vuex';

    export default {

        data: function () {
            return {
                queryForm: {
                    page: 1,
                    size: 10,
                    appId: null,
                    env: null,
                    name: null,
                    ip: null
                },
                detailDialogVisible: false,
                containerLogVisible: false
            }
        },

        computed: {
            ...mapGetters({
                cloudInstances: 'getCloudInstanceList',
                total: 'getCloudInstanceCount',
                envs: 'getEnvList',
                apps: 'getApps',
                instanceStatus: 'getCloudInstanceStatus',
                instanceLog: 'getCloudInstanceLog'
            })
        },

        created () {
            this.$store.dispatch('fetchEnvList');
            this.$store.dispatch('fetchAppList');
            this.refreshCloudInstanceList();
        },

        methods: {
            onSearch() {
                this.queryForm.page = 1;
                this.refreshCloudInstanceList();
            },
            dateFormatter(row, column, cellValue) {
                return new Date(cellValue).toLocaleString();
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
            resetQueryForm() {
                this.queryForm.appId = null;
                this.queryForm.env = null;
                this.queryForm.ip = null;
                this.queryForm.name = null;
                this.queryForm.page = 1;
                this.refreshCloudInstanceList();
            },
            handleSizeChange(data) {
                this.queryForm.size = data;
                this.refreshCloudInstanceList();
            },
            handleCurrentChange(data) {
                this.queryForm.page = data;
                this.refreshCloudInstanceList();
            },
            refreshCloudInstanceList() {
                this.$store.dispatch('fetchCloudInstances', this.queryForm);
            },
            refreshCloudInstanceStatus(instanceName) {
                let data = {name: instanceName};
                this.$store.dispatch('fetchCloudInstanceStatus', data);
                this.detailDialogVisible = true;
            },
            refreshCloudInstanceLog(instanceName) {
                let data = {name: instanceName};
                this.$store.dispatch('fetchCloudInstanceLog', data);
                this.containerLogVisible = true;
            },
            pullInInstance(instanceName) {
                this.$confirm('此操作将为实例拉入流量, 是否继续？', '拉入流量', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    let data = {name: instanceName};
                    this.$store.dispatch('pullInCloudInstance', data);
                });
            },
            pullOutInstance(instanceName) {
                this.$confirm('此操作将为实例拉出流量, 是否继续？', '拉出流量', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    let data = {name: instanceName};
                    this.$store.dispatch('pullOutCloudInstance', data);
                });
            },
            restartInstance(instanceName) {
                this.$confirm('此操作将重启实例, 是否继续？', '重启实例', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    let data = {name: instanceName};
                    this.$store.dispatch('restartCloudInstance', data);
                });
            },
            deleteInstance(instanceName) {
                this.$confirm('此操作将删除实例, 是否继续？', '删除实例', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    let data = {name: instanceName};
                    this.$store.dispatch('deleteCloudInstance', data);
                });
            }
        }
    }

</script>

<style>
 .instance-detail-dialog .el-form-item {
     margin-bottom: 0;
 }
 .instance-detail-dialog .el-dialog__header {
     padding-bottom: 0;
 }
</style>
