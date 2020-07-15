<template>
    <div class="content-panel">
        <el-row>
            <el-col>
                <el-breadcrumb separator="/">
                    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                    <el-breadcrumb-item>实例列表</el-breadcrumb-item>
                </el-breadcrumb>
            </el-col>
        </el-row>

        <el-row class="query-form">
            <div class="detail-box">
                <div class="detail-box-left">应用</div>
                <el-select v-model="queryForm.appId" placeholder="选择应用" filterable @change="onSearch" style="width: 300px">
                    <el-option v-for="item in apps" :key="item.id" :value="item.cmdbAppId"
                               :label="item.name + '/' + item.cmdbAppId"></el-option>
                </el-select>
            </div>

            <div class="detail-box">
                <div class="detail-box-left">实例IP</div>
                <el-input v-model="queryForm.ip" clearable placeholder="输入任意实例IP" style="width: 217px"></el-input>
            </div>

            <el-button type="primary" @click="onSearch">查询</el-button>
            <el-button @click="resetQueryForm">重置</el-button>
        </el-row>

        <br/>

        <el-table :data="instances" v-if="isLogin" class="instance-table" style="width: 100%"
                  :default-sort="{prop: 'releaseTime', order: 'descending'}">
            <el-table-column type="expand" width="40">
                <template slot-scope="props">
                    <el-form label-position="left" label-width="140px" class="table-expand">
                        <el-form-item label="发布组">
                            <span>{{ props.row.groupName ? props.row.groupName : '无' }}</span>
                        </el-form-item>
                        <el-form-item label="实例规格">
                            <span>{{ props.row.instanceSpec }}</span>
                        </el-form-item>
                        <el-form-item label="部署区域">
                            <span>{{ props.row.zone }}</span>
                        </el-form-item>
                        <el-form-item label="容器ID" v-if="isAdmin">
                            <span>{{ props.row.containerId }}</span>
                        </el-form-item>
                        <el-form-item label="容器状态" v-if="isAdmin">
                            <el-tag :type="containerStatusTagType(props.row.containerStatus)" v-if="props.row.containerStatus != null"
                                    :disable-transitions="true">{{props.row.containerStatus}}</el-tag>
                        </el-form-item>
                        <el-form-item label="资源类型">
                            <el-tag :type="props.row.isStatic ? 'success' : 'info'"
                                    :disable-transitions="true">{{props.row.isStatic ? '静态' : '动态'}}</el-tag>
                        </el-form-item>
                    </el-form>
                </template>
            </el-table-column>
            <el-table-column prop="env" label="环境" align="center" width="60"></el-table-column>
            <el-table-column prop="appName" label="应用" align="center"></el-table-column>
            <el-table-column prop="name" label="实例名" align="center" width="260"></el-table-column>
            <el-table-column prop="releaseTarget" label="镜像" align="center"></el-table-column>
            <el-table-column prop="ip" label="实例IP" align="center"></el-table-column>
            <el-table-column prop="port" label="端口" align="center" width="80"></el-table-column>
            <el-table-column prop="agentHost" label="物理机IP" align="center" v-if="isAdmin"></el-table-column>
            <el-table-column prop="releaseTime" sortable label="发布时间" align="center" :formatter="dateFormatter"></el-table-column>
            <el-table-column prop="ready" label="健康检查" sortable align="center" width="100">
                <template scope="props">
                    <el-tag :type="props.row.ready ? 'success' : 'danger'" v-if="props.row.ready != null"
                            :disable-transitions="true">{{props.row.ready ? 'Ready' : 'Unready'}}</el-tag>
                </template>
            </el-table-column>
            <el-table-column prop="opsPulledIn" sortable label="流量状态" align="center" width="100">
                <template scope="props">
                    <el-tag v-if="props.row.opsPulledIn == 1" type="success" :disable-transitions="true">Up</el-tag>
                    <el-tag v-else-if="props.row.opsPulledIn == 0" type="danger" :disable-transitions="true">Down</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="实例操作" align="center" width="100">
                <template scope="props">
                    <el-button size="small" type="primary" @click="gotoInstanceStatus(props.row)" :disabled="props.row.groupId == 0">
                        操作<i class="el-icon-d-arrow-right el-icon--right"></i>
                    </el-button>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>

<script>
    import {mapGetters, mapActions} from 'vuex';

    export default {

        data: function () {
            return {
                queryForm: {
                    appId: null,
                    ip: null
                }
            }
        },

        computed: {
            ...mapGetters({
                apps: 'getApps',
                instances: 'getAppInstanceList',
                containerLog: 'getContainerLog',
                username: 'getUserName',
                userOrg: 'getUserOrg',
                userRoles: 'getUserRoles',
                currentEnv: 'getCurrentEnv',
                currentAppId: 'getCurrentAppId',
                isLogin: 'getLoginState'
            }),
            isAdmin: function() {
                return this.userRoles!=null && this.userRoles.includes('admin');
            }
        },

        methods: {
            dateFormatter(row, column, cellValue) {
                return new Date(cellValue).toLocaleString();
            },
            onSearch() {
                this.refreshAppInstances();

                if (this.queryForm.appId && this.queryForm.appId != this.currentAppId) {
                    this.$store.dispatch('refreshCurrentAppId', this.queryForm.appId);
                }
            },
            resetQueryForm() {
                this.queryForm.ip = null;
                this.refreshAppInstances();
            },
            refreshAppInstances() {
                this.$store.dispatch('fetchInstancesByCondition', {
                    appId: this.queryForm.appId,
                    ip: this.queryForm.ip,
                    env: this.currentEnv
                });
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
            gotoInstanceStatus(instance) {
                this.$router.push({name: 'instancestatus', query: {env: instance.env, appId: instance.appId, groupId: instance.groupId}});
            }
        },
        created () {
            if (this.currentAppId) {
                this.queryForm.appId = this.currentAppId;
            }

            if (this.isAdmin) {
                this.$store.dispatch('fetchAppList');
            } else if (this.username) {
                this.$store.dispatch('fetchAppsByUsername', {username: this.username});
            }

            this.refreshAppInstances();
        },
        watch: {
            currentEnv: function (newCurrentEnv) {
                this.refreshAppInstances();
            }
        }

    }

</script>

<style>
    .instance-table a {
        color: inherit;
    }
    .instance-table a:active {
        color: #ff0000;
    }
    .instance-table .el-form-item {
        width: 100%;
    }
</style>
