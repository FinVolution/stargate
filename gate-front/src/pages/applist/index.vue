<template>
    <div class="content-panel">
        <el-row>
            <el-col>
                <el-breadcrumb separator="/">
                    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                    <el-breadcrumb-item>应用列表</el-breadcrumb-item>
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

            <el-button type="primary" style="float: right" @click="onApplyNewApp" v-if="isLogin">申请应用
            </el-button>
        </el-row>

        <br/>

        <el-table :data="myApps" style="width: 100%" border fit>
            <el-table-column type="expand">
                <template slot-scope="props">
                    <el-form v-for="(v,k) in props.row.envUrlMap" label-position="left" inline class="table-expand">
                        <el-form-item label="环境">
                            <span>{{ k }}</span>
                        </el-form-item>
                        <el-form-item label="域名" style="width: 75%">
                            <span>{{ v }}</span>
                        </el-form-item>
                    </el-form>
                </template>
            </el-table-column>
            <el-table-column label="组织" prop="department" align="center" width="160px"></el-table-column>
            <el-table-column label="应用名" prop="name" align="center"></el-table-column>
            <el-table-column label="应用ID" prop="cmdbAppId" align="center"></el-table-column>
            <el-table-column label="开发人员" prop="developerNames" align="center"></el-table-column>
            <el-table-column label="测试人员" prop="testerNames" align="center"></el-table-column>
            <el-table-column label="操作" align="center" width="300px" v-if="isLogin">
                <template scope="props">
                    <router-link :to="{name: 'groups', query: {env: currentEnv, appId: props.row.cmdbAppId}}">
                        <el-button size="small" type="primary" v-if="currentEnv">发布组<i class="el-icon-d-arrow-right el-icon--right"></i></el-button>
                    </router-link>
                    <router-link :to="{name: 'releasehistory', query: {env: currentEnv, appId: props.row.cmdbAppId }}">
                        <el-button size="small" type="primary" plain>历史<i class="el-icon-search el-icon--right"></i></el-button>
                    </router-link>
                    <el-button size="small" type="primary" plain @click="onUpdateAppMember(props.row)">
                        成员<i class="el-icon-edit-outline el-icon--right"></i>
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-dialog title="应用成员" :visible.sync="updateAppMemberVisible" width="1000px">
            <el-form label-width="70px" label-position="left">
                <el-form-item label="应用名">
                    <el-input v-model="updateAppMemberForm.appName" disabled></el-input>
                </el-form-item>
                <el-form-item label="开发人员">
                    <el-input v-model="updateAppMemberForm.developers" placeholder="请输入正确的域账号，用英文逗号分隔"></el-input>
                </el-form-item>
                <el-form-item label="测试人员">
                    <el-input v-model="updateAppMemberForm.testers" placeholder="请输入正确的域账号，用英文逗号分隔"></el-input>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseUpdateAppMember" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="onSubmitUpdateAppMember" style="float:right;margin:0 10px 0 0;">保存</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog title="申请应用" :visible.sync="applyNewAppDialogVisible" width="500px"
                   :before-close="onCloseApplyNewApp">
            <el-form label-width="80px" label-position="left" :model="applyNewApp" ref="inApplyNewAppForm"
                     :rules="inApplyNewAppRules">
                <el-form-item label="应用ID" prop="appId">
                    <el-input placeholder="请输入应用ID" v-model="applyNewApp.appId"></el-input>
                </el-form-item>
                <el-form-item label="生产域名" prop="domain">
                    <el-input placeholder="请输入生产域名" v-model="applyNewApp.domain"></el-input>
                </el-form-item>
                <el-form-item label="实例规格" prop="instanceSpec">
                    <el-select v-model="applyNewApp.instanceSpec" placeholder="请选择实例规格" style="width: 100%">
                        <el-option v-for="item in instanceSpecs" :key="item" :value="item"
                                   :label="item"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="实例个数" prop="instanceCount">
                    <el-input placeholder="请输入实例个数" v-model.number="applyNewApp.instanceCount"></el-input>
                </el-form-item>
                <el-form-item label="应用类型" prop="appType">
                    <el-select v-model="applyNewApp.appType" placeholder="请选择应用类型" style="width: 100%">
                        <el-option v-for="item in appTypes" :key="item" :value="item"
                                   :label="item"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="服务类型" prop="serviceType">
                    <el-select v-model="applyNewApp.serviceType" placeholder="请选择服务类型" style="width: 100%">
                        <el-option v-for="item in serviceTypes" :key="item" :value="item"
                                   :label="item"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="应用级别" prop="level">
                    <el-select v-model="applyNewApp.level" placeholder="请选择应用级别" style="width: 100%">
                        <el-option v-for="item in levels" :key="item" :value="item"
                                   :label="item"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="所属组织" prop="department">
                    <el-select v-model="applyNewApp.department" placeholder="请选择所属组织" style="width: 100%">
                        <el-option v-for="item in orgs"
                                :key="item.id"
                                :label="item.name"
                                :value="item.name">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseApplyNewApp" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="onSubmitApplyNewApp" style="float:right;margin:0 10px 0 0;">申请
                    </el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
    </div>
</template>

<script>
    import {mapGetters, mapActions} from 'vuex'

    export default {
        data: function () {
            return {
                updateAppMemberVisible: false,
                updateAppMemberForm: {
                    appName: '',
                    appId: '',
                    developers: '',
                    testers: ''
                },
                inApplyNewAppRules: {
                    appId: [
                        {required: true, message: '请输入应用ID', trigger: 'blur'}
                    ],
                    appName: [
                        {required: true, message: '请输入应用名', trigger: 'blur'}
                    ],
                    instanceCount: [
                        {required: true, message: '请输入实例个数'},
                        {type: 'number', min: 1, max: 100, message: '请输入1-100之间的数字'}
                    ],
                    instanceSpec: [
                        {required: true, message: '请选择实例规格', trigger: 'change'},
                    ],
                    domain: [
                        {required: true, message: '请输入生产域名', trigger: 'blur'}
                    ],
                    appType: [
                        {required: true, message: '请选择应用类型', trigger: 'change'},
                    ],
                    serviceType: [
                        {required: true, message: '请选择服务类型', trigger: 'change'},
                    ],
                    level: [
                        {required: true, message: '请选择应用级别', trigger: 'change'},
                    ],
                    department: [
                        {required: true, message: '请选择所属组织', trigger: 'change'}
                    ]
                },
                applyNewAppDialogVisible: false,
                serviceTypes: ['服务提供', '服务消费', 'Job', '其他'],
                appTypes: ['SpringBoot', 'Tomcat', 'Django', '其他'],
                levels: ['一般业务', '重要业务', '核心业务'],
                instanceSpecs: ['C-2C4G', 'D-4C8G'],
                applyNewApp: {
                    appId: '',
                    appName: '',
                    instanceCount: '',
                    instanceSpec: '',
                    domain: '',
                    serviceType: '',
                    appType: '',
                    level: '',
                    department: ''
                },
                queryForm: {
                    appId: ''
                }
            }
        },
        computed: {
            ...mapGetters({
                apps: 'getApps',
                myApps: 'getMyApps',
                appSetting: 'getAppSetting',
                users: 'getUserList',
                username: 'getUserName',
                userOrg: 'getUserOrg',
                userRoles: 'getUserRoles',
                orgs: 'getOrgList',
                isLogin: 'getLoginState',
                currentEnv: 'getCurrentEnv',
                currentAppId: 'getCurrentAppId'
            }),
            isAdmin: function() {
                return this.userRoles!=null && this.userRoles.includes('admin');
            }
        },
        created () {
            if (this.currentAppId) {
                this.queryForm.appId = this.currentAppId;
                this.$store.dispatch('fetchApp', {appId: this.queryForm.appId});
            }

            if (this.isAdmin) {
                this.$store.dispatch('fetchAppList');
            } else if (this.username) {
                this.$store.dispatch('fetchAppsByUsername', {username: this.username});
            }
        },
        methods: {
            onSearch() {
                this.$store.dispatch('fetchApp', {appId: this.queryForm.appId});
                if (this.queryForm.appId && this.queryForm.appId != this.currentAppId) {
                    this.$store.dispatch('refreshCurrentAppId', this.queryForm.appId);
                }
            },
            onApplyNewApp() {
                if (this.userOrg) {
                    this.applyNewApp.department = this.userOrg;
                }
                this.applyNewAppDialogVisible = true;
                this.$store.dispatch('fetchAllOrgs');
            },
            onSubmitApplyNewApp() {
                this.$refs["inApplyNewAppForm"].validate((valid) => {
                    if (valid) {
                        this.$store.dispatch('applyNewApp', {
                            appId: this.applyNewApp.appId,
                            appName: this.applyNewApp.domain,
                            instanceCount: this.applyNewApp.instanceCount,
                            instanceSpec: this.applyNewApp.instanceSpec,
                            domain: this.applyNewApp.domain,
                            appType: this.applyNewApp.appType,
                            serviceType: this.applyNewApp.serviceType,
                            level: this.applyNewApp.level,
                            department: this.applyNewApp.department
                        });
                        this.onCloseApplyNewApp();
                    }
                });
            },
            onCloseApplyNewApp() {
                this.applyNewAppDialogVisible = false;
                this.$refs["inApplyNewAppForm"].resetFields();
            },
            onUpdateAppMember(app) {
                this.updateAppMemberForm.appName = app.name;
                this.updateAppMemberForm.appId = app.cmdbAppId;
                this.updateAppMemberForm.developers = app.developer;
                this.updateAppMemberForm.testers = app.tester;
                this.updateAppMemberVisible = true;
            },
            onCloseUpdateAppMember() {
                this.updateAppMemberVisible = false;
            },
            onSubmitUpdateAppMember() {
                if (this.updateAppMemberForm.developers.trim() == '') {
                    this.$message.warning('开发人员不能为空');
                } else {
                    let data = {
                        queryRequest: {
                            appId: this.queryForm.appId
                        },
                        updateAppMemberRequest: {
                            appId: this.updateAppMemberForm.appId,
                            developers: this.updateAppMemberForm.developers.trim(),
                            testers: this.updateAppMemberForm.testers.trim()
                        }
                    };
                    this.$store.dispatch('updateAppMember', data);
                    this.onCloseUpdateAppMember();
                }
            }
        }
    }
</script>

<style>

</style>