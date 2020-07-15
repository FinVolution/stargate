<template>

    <div class="content-panel">

        <div>
            <el-row>
                <el-col>
                    <el-breadcrumb separator="/">
                        <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                        <el-breadcrumb-item>应用配额</el-breadcrumb-item>
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

                <el-button type="primary" @click="changeAppQuota" style="float: right" v-if="showApplyBtn">修改配额</el-button>
            </el-row>
        </div>

        <br/>

        <div v-for="item in appQuotaStatusList" :key="item.spectypeName" class="release-status">
            <div class="release-status-item">
                <div class="release-status-item-env">{{item.spectypeName}}</div>
            </div>
            <div class="release-status-item">
                <i class="el-icon-circle-plus" style="color: #E6A23C"></i>
                <p>总数：{{item.total}}</p>
            </div>
            <div class="release-status-item">
                <i class="el-icon-circle-close" style="color: #F56C6C"></i>
                <p>已用：{{item.usedCount}}</p>
            </div>
            <div class="release-status-item">
                <i class="el-icon-circle-check" style="color: #67C23A"></i>
                <p>可用：{{item.freeCount}}</p>
            </div>
        </div>

        <el-dialog title="申请修改配额" :visible.sync="dialogVisible" width="500px" :before-close="onClose">
            <el-form label-width="80px" label-position="left" :model="appQuota" ref="inChangeAppQuotaForm" :rules="inChangeAppQuotaRules">
                <el-form-item label="应用" prop="appId" required>
                    <el-select v-model="appQuota.appId" disabled style="width: 100%">
                        <el-option v-for="item in apps" :key="item.id" :value="item.cmdbAppId"
                                   :label="item.name"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="环境" prop="env" required>
                    <el-input v-model="appQuota.env" disabled></el-input>
                </el-form-item>
                <el-form-item label="实例规格" prop="spec" required>
                    <el-select v-model="appQuota.spec" placeholder="请选择实例规格" style="width: 100%">
                        <el-option v-for="item in appQuotaStatusList" :key="item.spectypeName" :value="item.spectypeName"
                                   :label="item.spectypeName + '（当前配额：' + item.total + '）'"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="目标配额" prop="number">
                    <el-input v-model.number="appQuota.number" placeholder="请输入目标配额" style="width: 100%"></el-input>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onClose" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="onSubmit" style="float:right;margin:0 10px 0 0;">申请</el-button>
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
                queryForm: {
                    appId: ''
                },
                dialogVisible: false,
                appQuota: {
                    appId: null,
                    env: null,
                    spec: null,
                    number: null
                },
                specs:['C-2C4G', 'D-4C8G'],
                inChangeAppQuotaRules: {
                    spec: [
                        {required: true, message: '请选择实例规格', trigger: 'change'}
                    ],
                    number: [
                        {required: true, message: '请输入目标配额'},
                        {type: 'number', min: 0, max: 100, message: '请输入0-100之间的数字'}
                    ]
                }
            }
        },

        computed: {
            ...mapGetters({
                apps: 'getApps',
                username: 'getUserName',
                userOrg: 'getUserOrg',
                userRoles: 'getUserRoles',
                currentEnv: 'getCurrentEnv',
                currentAppId: 'getCurrentAppId',
                appQuotaStatus: 'getAppQuotaStatus',
                isLogin: 'getLoginState'
            }),
            isAdmin: function() {
                return this.userRoles!=null && this.userRoles.includes('admin');
            },
            appQuotaStatusList: function () {
                return this._.sortBy(this.appQuotaStatus, 'spectypeName');
            },
            showApplyBtn: function () {
                return this.currentEnv && this.currentAppId && this.isLogin;
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

            this.refreshAppQuotaStatus();
        },

        watch: {
            currentEnv: function (newCurrentEnv) {
                this.refreshAppQuotaStatus();
            }
        },

        methods: {
            changeAppQuota() {
                this.appQuota.appId = this.queryForm.appId;
                this.appQuota.env = this.currentEnv;
                this.dialogVisible = true;
            },
            onSubmit() {
                this.$refs["inChangeAppQuotaForm"].validate((valid) => {
                    if (valid) {
                        this.$store.dispatch('changeAppQuota', this.appQuota);
                        this.onClose();
                    }
                });

            },
            onClose() {
                this.dialogVisible = false;
                this.$refs["inChangeAppQuotaForm"].resetFields();
            },
            onSearch() {
                this.refreshAppQuotaStatus();

                if (this.queryForm.appId && this.queryForm.appId != this.currentAppId) {
                    this.$store.dispatch('refreshCurrentAppId', this.queryForm.appId);
                }
            },
            refreshAppQuotaStatus() {
                this.$store.dispatch('fetchAppQuotaStatus', {
                    appId: this.queryForm.appId,
                    environment: this.currentEnv
                });
            }
        }

    }

</script>

<style>

</style>
