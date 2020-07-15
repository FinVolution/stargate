<template>
    <div class="content-panel">
        <el-row>
            <el-input v-model="queryForm.network" clearable placeholder="输入网络"
                      class="query-box" prefix-icon="el-icon-search"></el-input>

            <el-input v-model="queryForm.networkSegment" clearable placeholder="输入网段"
                      class="query-box" prefix-icon="el-icon-search"></el-input>

            <el-input v-model="queryForm.ip" clearable placeholder="输入IP"
                      class="query-box" prefix-icon="el-icon-search"></el-input>

            <div class="detail-box">
                <div class="detail-box-left">占用</div>
                <el-select v-model="queryForm.occupied" @change="onSearch" style="width: 200px">
                    <el-option key="true" :value="true" label="是"></el-option>
                    <el-option key="false" :value="false" label="否"></el-option>
                </el-select>
            </div>

            <el-button type="primary" @click="onSearch">查询</el-button>
            <el-button @click="resetQueryForm">重置</el-button>

            <el-button type="primary" @click="onCreate" style="float: right; margin-left: 10px;">添加IP</el-button>
            <el-button type="primary" @click="onSync" style="float: right">同步状态</el-button>
        </el-row>

        <br/>

        <el-table :data="ips" style="width: 100%" border fit>
            <el-table-column label="网络" prop="network" align="center"></el-table-column>
            <el-table-column label="网段" prop="networkSegment" align="center"></el-table-column>
            <el-table-column label="IP" prop="ip" align="center"></el-table-column>
            <el-table-column label="占用" align="center">
                <template scope="props">
                    <el-tag type="success" v-if="props.row.occupied">是</el-tag>
                    <el-tag type="info" v-else>否</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="创建时间" prop="insertTime" align="center" :formatter="dateFormatter"></el-table-column>
            <el-table-column label="操作" align="center" width="160">
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

        <el-dialog title="添加IP" :visible.sync="dialogVisible" width="500px" :before-close="onClose">
            <el-form label-width="75px" label-position="left" :model="addIpRequest" ref="addIpForm" :rules="addIpRules">
                <el-form-item label="网络" prop="network">
                    <el-input v-model="addIpRequest.network" placeholder="请输入网络"></el-input>
                </el-form-item>
                <el-form-item label="网段" prop="networkSegment">
                    <el-input v-model="addIpRequest.networkSegment" placeholder="请输入网段 格式为x.x.x.0"></el-input>
                </el-form-item>
                <el-form-item label="最小IP" prop="minIp">
                    <el-input v-model.number="addIpRequest.minIp" placeholder="请输入最小IP的末位"></el-input>
                </el-form-item>
                <el-form-item label="最大IP" prop="maxIp">
                    <el-input v-model.number="addIpRequest.maxIp" placeholder="请输入最大IP的末位"></el-input>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onClose" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="onSubmit" style="float:right;margin:0 10px 0 0;">提交</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog title="同步IP状态" :visible.sync="syncDialogVisible" width="500px" :before-close="onCloseSync">
            <el-form label-width="75px" label-position="left" :model="syncIpRequest" ref="syncIpForm" :rules="syncIpRules">
                <el-form-item label="环境" prop="env">
                    <el-select v-model="syncIpRequest.env" placeholder="请选择环境" style="width: 100%">
                        <el-option v-for="item in envs" :key="item.id" :value="item.name" :label="item.name"></el-option>
                    </el-select>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseSync" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="syncIpStatus" style="float:right;margin:0 10px 0 0;">同步</el-button>
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
                syncDialogVisible: false,
                queryForm: {
                    page: 1,
                    size: 10,
                    network: null,
                    networkSegment: null,
                    ip: null,
                    occupied: null
                },
                addIpRequest: {
                    network: null,
                    networkSegment: null,
                    minIp: null,
                    maxIp: null
                },
                addIpRules: {
                    network: [
                        {required: true, message: '请输入网络', trigger: 'blur'}
                    ],
                    networkSegment: [
                        {required: true, message: '请输入网段', trigger: 'blur'},
                    ],
                    minIp: [
                        {required: true, message: '请输入最小IP'},
                        {type: 'number', min: 0, max: 255, message: '请输入0-255之间的数字'}
                    ],
                    maxIp: [
                        {required: true, message: '请输入最大IP'},
                        {type: 'number', min: 0, max: 255, message: '请输入0-255之间的数字'}
                    ]
                },
                syncIpRequest: {
                    env: null
                },
                syncIpRules: {
                    env: [
                        {required: true, message: '请选择环境', trigger: 'change'}
                    ]
                }
            }
        },

        computed: {
            ...mapGetters({
                ips: 'getIpList',
                total: 'getIpCount',
                envs: 'getEnvList'
            })
        },

        created () {
            this.refreshIpList();
            this.$store.dispatch('fetchEnvList');
        },

        methods: {
            onSubmit() {
                this.$refs["addIpForm"].validate((valid) => {
                    if (valid) {
                        this.$store.dispatch('createIps', {
                            addRequest: this.addIpRequest,
                            queryForm: this.queryForm
                        });
                        this.onClose();
                    }
                });
            },
            onClose() {
                this.dialogVisible = false;
                this.$refs["addIpForm"].resetFields();
            },
            onCreate() {
                this.dialogVisible = true;
            },
            onSearch() {
                this.queryForm.page = 1;
                this.refreshIpList();
            },
            dateFormatter(row, column, cellValue) {
                return new Date(cellValue).toLocaleString();
            },
            handleDelete(ipId) {
                this.$confirm('此操作将删除该IP, 是否继续？', '删除IP', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    this.$store.dispatch('deleteIpById', {
                        id: ipId,
                        queryForm: this.queryForm
                    });
                }).catch(() => {
                    this.$message({
                        'type': 'info',
                        'message': '取消删除'
                    });
                });
            },
            resetQueryForm() {
                this.queryForm.network = null;
                this.queryForm.networkSegment = null;
                this.queryForm.ip = null;
                this.queryForm.occupied = null;
                this.queryForm.page = 1;
                this.refreshIpList();
            },
            handleSizeChange(data) {
                this.queryForm.size = data;
                this.refreshIpList();
            },
            handleCurrentChange(data) {
                this.queryForm.page = data;
                this.refreshIpList();
            },
            refreshIpList() {
                this.$store.dispatch('fetchIpsByPage', this.queryForm);
            },
            onSync() {
                this.syncDialogVisible = true;
            },
            onCloseSync() {
                this.syncDialogVisible = false;
                this.$refs["syncIpForm"].resetFields();
            },
            syncIpStatus() {
                this.$refs["syncIpForm"].validate((valid) => {
                    if (valid) {
                        this.$store.dispatch('syncIpStatusByEnv', {
                            env: this.syncIpRequest.env,
                            queryForm: this.queryForm
                        });
                    }
                });
            }
        }
    }

</script>

<style>

</style>
