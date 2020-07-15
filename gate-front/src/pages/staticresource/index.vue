<template>
    <div class="content-panel">
        <el-row>
            <el-col>
                <el-breadcrumb separator="/">
                    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                    <el-breadcrumb-item>静态资源</el-breadcrumb-item>
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
                <div class="detail-box-left">类型</div>
                <el-select v-model="queryForm.isStatic" placeholder="选择资源类型" @change="onSearch" style="width: 200px">
                    <el-option v-for="item in resourceTypes" :key="item.value" :label="item.label" :value="item.value"></el-option>
                </el-select>
            </div>

            <el-button type="primary" @click="onCreate" style="float: right; margin-left: 10px;" v-if="showActionBtn && queryForm.isStatic">添加静态资源</el-button>
            <el-button type="primary" @click="changeType" style="float: right" v-if="showActionBtn && queryForm.isStatic">静态转动态</el-button>
            <el-button type="primary" @click="changeType" style="float: right" v-if="showActionBtn && !queryForm.isStatic">动态转静态</el-button>
        </el-row>

        <br/>

        <el-table :data="resources" style="width: 100%" border fit>
            <el-table-column label="应用" prop="appName" align="center"></el-table-column>
            <el-table-column label="环境" prop="env" align="center"></el-table-column>
            <el-table-column label="部署区域" prop="zone" align="center"></el-table-column>
            <el-table-column label="规格" prop="spec" align="center"></el-table-column>
            <el-table-column label="IP" prop="ip" align="center"></el-table-column>
            <el-table-column label="实例" prop="podName" align="center" width="240"></el-table-column>
            <el-table-column label="资源类型" align="center" width="120">
                <template scope="props">
                    <el-tag v-if="props.row.isStatic" type="success">静态</el-tag>
                    <el-tag v-else type="info">动态</el-tag>
                </template>
            </el-table-column>
            <!--                <el-table-column label="创建时间" prop="insertTime" align="center" :formatter="dateFormatter"></el-table-column>-->
            <el-table-column label="操作" align="center" width="120" v-if="isLogin">
                <template scope="props">
                    <el-button @click="handleDelete(props.row.id)" size="small" type="danger" plain>删除<i class="el-icon-delete el-icon--right"></i></el-button>
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

        <el-dialog title="添加静态资源" :visible.sync="dialogVisible" width="500px" :before-close="onClose">
            <el-form label-width="55px" label-position="right" :model="addResourceRequest" ref="addResourceForm" :rules="addResourceRules">
                <el-form-item label="应用" prop="appId">
                    <el-select v-model="addResourceRequest.appId" placeholder="请选择应用" filterable disabled style="width: 100%">
                        <el-option v-for="item in apps" :key="item.id" :label="item.name" :value="item.cmdbAppId"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="环境" prop="env">
                    <el-input v-model="addResourceRequest.env" disabled></el-input>
                </el-form-item>
                <el-form-item label="规格" prop="spec">
                    <el-select v-model="addResourceRequest.spec" placeholder="请选择规格" style="width: 100%">
                        <el-option v-for="item in resourceQuotaStatusList"
                                   :key="item.spectypeName"
                                   :label="item.spectypeName + '（可添加资源数：' + item.freeCount + '）'"
                                   :value="item.spectypeName"
                                   v-if="item.freeCount > 0"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="个数" prop="number">
                    <el-input v-model.number="addResourceRequest.number" placeholder="请输入个数"></el-input>
                </el-form-item>
                <el-form-item label="区域" prop="zone">
                    <el-select v-model="addResourceRequest.zone" placeholder="请选择部署区域，不选择自动分配" style="width: 100%">
                        <el-option v-for="item in zones" :key="item" :label="item" :value="item"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onClose" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="onSubmit" style="float:right;margin:0 10px 0 0;">提交</el-button>
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
                    page: 1,
                    size: 10,
                    appId: null,
                    env: null,
                    isStatic: true
                },
                resourceTypes: [
                    {label: '静态', value: true},
                    {label: '动态', value: false}
                ],
                dialogVisible: false,
                addResourceRequest: {
                    appId: null,
                    env: null,
                    zone: null,
                    spec: null,
                    number: null
                },
                addResourceRules: {
                    appId: [
                        {required: true, message: '请选择应用', trigger: 'change'}
                    ],
                    env: [
                        {required: true, message: '请选择环境', trigger: 'change'}
                    ],
                    spec: [
                        {required: true, message: '请选择规格', trigger: 'change'}
                    ],
                    number: [
                        {required: true, message: '请输入个数'},
                        {type: 'number', min: 1, max: 100, message: '请输入1-100之间的数字'}
                    ]
                }
            }
        },

        computed: {
            ...mapGetters({
                resources: 'getResourceList',
                total: 'getResourceCount',
                apps: 'getApps',
                resourceQuotaStatus: 'getResourceQuotaStatus',
                zones: 'getEnvZoneList',
                currentEnv: 'getCurrentEnv',
                currentAppId: 'getCurrentAppId',
                username: 'getUserName',
                userRoles: 'getUserRoles',
                isLogin: 'getLoginState'
            }),
            isAdmin: function() {
                return this.userRoles!=null && this.userRoles.includes('admin');
            },
            resourceQuotaStatusList: function () {
                return this._.sortBy(this.resourceQuotaStatus, 'spectypeName');
            },
            showActionBtn: function () {
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

            this.refreshResourceList();
        },

        watch: {
            currentEnv: function (newCurrentEnv) {
                this.refreshResourceList();
            }
        },

        methods: {
            onSubmit() {
                this.$refs["addResourceForm"].validate((valid) => {
                    if (valid) {
                        if (this.zones.length == 1) {
                            this.addResourceRequest.zone = this.zones[0];
                        }
                        this.$store.dispatch('createResources', {
                            addRequest: this.addResourceRequest,
                            queryForm: this.queryForm
                        });
                        this.onClose();
                    }
                });
                this.queryForm.isStatic = true;
            },
            onClose() {
                this.dialogVisible = false;
                this.$refs["addResourceForm"].resetFields();
            },
            onCreate() {
                if (this.currentEnv && this.queryForm.appId) {
                    this.$store.dispatch('fetchEnvZones', {env: this.currentEnv});
                }
                this.$store.dispatch('fetchResourceQuotaStatus', {
                    appId: this.queryForm.appId,
                    env: this.currentEnv
                });
                this.addResourceRequest.appId = this.queryForm.appId;
                this.addResourceRequest.env = this.currentEnv;
                this.dialogVisible = true;
            },
            onSearch() {
                this.queryForm.page = 1;
                this.refreshResourceList();

                if (this.queryForm.appId && this.queryForm.appId != this.currentAppId) {
                    this.$store.dispatch('refreshCurrentAppId', this.queryForm.appId);
                }
            },
            dateFormatter(row, column, cellValue) {
                return new Date(cellValue).toLocaleString();
            },
            handleDelete(resourceId) {
                this.$confirm('此操作将删除该静态资源, 是否继续？', '删除静态资源', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    this.$store.dispatch('deleteResourceById', {
                        id: resourceId,
                        queryForm: this.queryForm
                    });
                }).catch(() => {
                    this.$message({
                        'type': 'info',
                        'message': '取消删除'
                    });
                });
            },
            handleSizeChange(data) {
                this.queryForm.size = data;
                this.refreshResourceList();
            },
            handleCurrentChange(data) {
                this.queryForm.page = data;
                this.refreshResourceList();
            },
            refreshResourceList() {
                if (this.queryForm.appId) {
                    this.queryForm.env = this.currentEnv;
                    this.$store.dispatch('fetchResourcesByPage', this.queryForm);
                }
            },
            changeType() {
                let title;
                if (this.queryForm.isStatic) {
                    title = '静态资源转为动态资源';
                } else {
                    title = '动态资源转为静态资源';
                }
                this.$confirm('此操作会将该环境下应用的所有' + title + ', 是否继续？', title, {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    let data = {
                        changeRequest: {
                            appId: this.queryForm.appId,
                            env: this.currentEnv,
                            isStatic: this.queryForm.isStatic
                        },
                        queryForm: this.queryForm
                    };
                    this.$store.dispatch('changeTypeForAllResources', data);
                    this.queryForm.isStatic = !this.queryForm.isStatic;
                });
            }
        }
    }

</script>

<style>

</style>
