<template>
    <div class="content-panel">
        <el-row>
            <el-col>
                <el-breadcrumb separator="/">
                    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                    <el-breadcrumb-item>DNS列表</el-breadcrumb-item>
                </el-breadcrumb>
            </el-col>
        </el-row>

        <el-row class="query-form">
            <div class="detail-box">
                <div class="detail-box-left">域名</div>
                <el-input v-model="queryForm.name" clearable placeholder="请输入域名" style="width: 217px"></el-input>
            </div>

            <div class="detail-box">
                <div class="detail-box-left">IP</div>
                <el-input v-model="queryForm.content" clearable placeholder="请输入IP" style="width: 217px"></el-input>
            </div>

            <div class="detail-box">
                <div class="detail-box-left">环境标识</div>
                <el-select
                        v-model="queryForm.envId"
                        filterable
                        allow-create
                        default-first-option
                        placeholder="请选择环境或输入IP">
                    <el-option
                            v-for="item in envList"
                            :key="item.name"
                            :label="item.name"
                            :value="item.name">
                    </el-option>
                </el-select>
            </div>

            <el-button type="primary" @click="onSearch">查询</el-button>
            <el-button @click="resetQueryForm">重置</el-button>

            <el-button @click="onExportDns()" type="primary" style="float: right">导出</el-button>
            <el-button @click="onCreateDnsBatch()" type="primary" style="float: right" v-if="isLogin">批量新建</el-button>
            <el-button @click="onCreateDns()" type="primary" style="float: right" v-if="isLogin">新建</el-button>
        </el-row>
        
        <br/>

        <el-table :data="records" style="width: 100%" border fit>
            <el-table-column label="域名" prop="name" align="center"></el-table-column>
            <el-table-column label="IP" prop="content" align="center"></el-table-column>
            <el-table-column label="环境标识" prop="envId" align="center"></el-table-column>
            <el-table-column label="类型" prop="type" align="center"></el-table-column>
            <el-table-column label="创建时间" prop="insertTime" align="center" :formatter="dateFormatter"></el-table-column>
            <el-table-column label="操作" align="center" v-if="isLogin">
                <template scope="props">
                    <el-button @click="onEditDns(props.row)" size="small" type="primary" plain>编辑</el-button>
                    <el-button @click="onDeleteDns(props.row)" size="small" type="danger" plain>删除</el-button>
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

        <el-dialog title="编辑DNS记录" :visible.sync="editDnsDialogVisible" width="600px" :before-close="onCloseEditDns">
            <el-form label-width="80px" label-position="left" :model="editDns" :rules="editDnsRules" ref="edit-dns-form">
                <el-form-item label="域名" prop="name">
                    <el-input placeholder="请输入域名" v-model="editDns.name"></el-input>
                </el-form-item>
                <el-form-item label="IP" prop="content">
                    <el-input placeholder="请输入IP" v-model="editDns.content"></el-input>
                </el-form-item>
                <el-form-item label="环境标识" prop="envId">
                    <el-select
                            v-model="editDns.envId"
                            filterable
                            allow-create
                            default-first-option
                            placeholder="请选择环境或输入IP">
                        <el-option
                                v-for="item in envList"
                                :key="item.name"
                                :label="item.name"
                                :value="item.name">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseEditDns" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="onSubmitEditDns" style="float:right;margin:0 10px 0 0;">更新</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog title="新建DNS记录" :visible.sync="createDnsDialogVisible" width="600px" :before-close="onCloseCreateDns">
            <el-form label-width="80px" label-position="left" :model="createDns" :rules="createDnsRules" ref="create-dns-form">
                <el-form-item label="域名" prop="name">
                    <el-input placeholder="请输入域名" v-model="createDns.name"></el-input>
                </el-form-item>
                <el-form-item label="IP" prop="content">
                    <el-input placeholder="请输入IP" v-model="createDns.content"></el-input>
                </el-form-item>
                <el-form-item label="环境标识" prop="envId">
                    <el-select
                            v-model="createDns.envId"
                            filterable
                            allow-create
                            default-first-option
                            placeholder="请选择环境或输入IP">
                        <el-option
                                v-for="item in envList"
                                :key="item.name"
                                :label="item.name"
                                :value="item.name">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseCreateDns" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="onSubmitCreateDns" style="float:right;margin:0 10px 0 0;">创建</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog title="批量新建DNS记录" :visible.sync="createDnsBatchDialogVisible" width="600px" :before-close="onCloseCreateDnsBatch">
            <el-form label-width="90px" label-position="left" :model="createDnsBatch" :rules="createDnsBatchRules" ref="create-dns-batch-form">
                <el-form-item label="环境标识" prop="envId">
                    <el-select
                            v-model="createDnsBatch.envId"
                            filterable
                            allow-create
                            default-first-option
                            placeholder="请选择环境或输入IP">
                        <el-option
                                v-for="item in envList"
                                :key="item.name"
                                :label="item.name"
                                :value="item.name">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="hosts列表" prop="content">
                    <el-input type="textarea" :rows="20" placeholder="请输入hosts列表 IP在前域名在后" v-model="createDnsBatch.content"></el-input>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseCreateDnsBatch" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="onSubmitCreateDnsBatch" style="float:right;margin:0 10px 0 0;">创建</el-button>
                </el-form-item>
            </el-form>
        </el-dialog>

        <el-dialog title="导出DNS记录" :visible.sync="exportDnsDialogVisible" width="600px" :before-close="onCloseExportDns">
            <el-form label-width="80px" label-position="left" :model="exportDns" :rules="exportDnsRules" ref="export-dns-form">
                <el-form-item label="环境标识" prop="envId">
                    <el-select
                            v-model="exportDns.envId"
                            filterable
                            allow-create
                            default-first-option
                            placeholder="请选择环境或输入IP">
                        <el-option
                                v-for="item in envList"
                                :key="item.name"
                                :label="item.name"
                                :value="item.name">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onCloseExportDns" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="onSubmitExportDns" style="float:right;margin:0 10px 0 0;">导出</el-button>
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
                queryForm: {
                    name: null,
                    envId: null,
                    content: null,
                    page: 1,
                    size: 10
                },
                editDnsDialogVisible: false,
                createDnsDialogVisible: false,
                createDnsBatchDialogVisible: false,
                exportDnsDialogVisible: false,
                editDns: {
                    id: null,
                    name: null,
                    envId: null,
                    content: null,
                    type: null
                },
                createDns: {
                    name: null,
                    envId: null,
                    content: null,
                    type: 'A'
                },
                createDnsBatch: {
                    envId: null,
                    content: null,
                    type: 'A'
                },
                exportDns: {
                    envId: null
                },
                editDnsRules: {
                    name: [
                        {required: true, message: '请输入域名', trigger: 'blur'}
                    ],
                    content: [
                        {required: true, message: '请输入IP', trigger: 'blur'}
                    ],
                    envId: [
                        {required: true, message: '请选择环境标识', trigger: 'change'}
                    ]
                },
                createDnsRules: {
                    name: [
                        {required: true, message: '请输入域名', trigger: 'blur'}
                    ],
                    content: [
                        {required: true, message: '请输入IP', trigger: 'blur'}
                    ],
                    envId: [
                        {required: true, message: '请选择环境标识', trigger: 'change'}
                    ]
                },
                createDnsBatchRules: {
                    content: [
                        {required: true, message: '请输入hosts列表', trigger: 'blur'}
                    ],
                    envId: [
                        {required: true, message: '请选择环境标识', trigger: 'change'}
                    ]
                },
                exportDnsRules: {
                    envId: [
                        {required: true, message: '请选择环境标识', trigger: 'change'}
                    ]
                }
            }
        },

        computed: {
            ...mapGetters({
                records: 'getDnsList',
                total: 'getDnsCount',
                envList: 'getEnvList',
                isLogin: 'getLoginState'
            })
        },

        created () {
            this.refreshDnsList();
            this.$store.dispatch('fetchEnvList');
        },

        methods: {
            dateFormatter(row, column, cellValue) {
                return new Date(cellValue).toLocaleString();
            },
            handleSizeChange(data) {
                this.queryForm.size = data;
                this.refreshDnsList();
            },
            handleCurrentChange(data) {
                this.queryForm.page = data;
                this.refreshDnsList();
            },
            onSearch() {
                this.queryForm.page = 1;
                this.refreshDnsList();
            },
            resetQueryForm() {
                this.queryForm.name = null;
                this.queryForm.content = null;
                this.queryForm.envId = null;
                this.queryForm.page = 1;
                this.refreshDnsList();
            },
            refreshDnsList() {
                this.$store.dispatch('fetchDnsByPage', this.queryForm);
            },
            onEditDns(recordRow) {
                this.editDns = this._.clone(recordRow);
                this.editDnsDialogVisible = true;
            },
            onCreateDns() {
                this.createDnsDialogVisible = true;
            },
            onCreateDnsBatch() {
                this.createDnsBatchDialogVisible = true;
            },
            onExportDns() {
                this.exportDnsDialogVisible = true;
            },
            onCloseEditDns() {
                this.editDnsDialogVisible = false;
                this.$refs["edit-dns-form"].resetFields();
            },
            onCloseCreateDns() {
                this.createDnsDialogVisible = false;
                this.$refs["create-dns-form"].resetFields();
            },
            onCloseCreateDnsBatch() {
                this.createDnsBatchDialogVisible = false;
                this.$refs["create-dns-batch-form"].resetFields();
            },
            onCloseExportDns() {
                this.exportDnsDialogVisible = false;
                this.$refs["export-dns-form"].resetFields();
            },
            onSubmitEditDns() {
                this.$refs["edit-dns-form"].validate((valid) => {
                    if (valid) {
                        this.$store.dispatch('updateDns', {
                            updateRequest: {
                                id: this.editDns.id,
                                name: this.editDns.name.trim(),
                                content: this.editDns.content.trim(),
                                envId: this.editDns.envId.trim(),
                                type: this.editDns.type
                            },
                            queryRequest: this.queryForm
                        });
                        this.onCloseEditDns();
                    } else {
                        return false;
                    }
                });
            },
            onSubmitCreateDns() {
                this.$refs["create-dns-form"].validate((valid) => {
                    if (valid) {
                        this.$store.dispatch('createDns', {
                            createRequest: {
                                records: [
                                    {
                                        name: this.createDns.name.trim(),
                                        content: this.createDns.content.trim(),
                                        envId: this.createDns.envId.trim(),
                                        type: this.createDns.type
                                    }
                                ]
                            },
                            queryRequest: this.queryForm
                        });
                        this.onCloseCreateDns();
                    } else {
                        return false;
                    }
                });
            },
            onSubmitCreateDnsBatch() {
                this.$refs["create-dns-batch-form"].validate((valid) => {
                    if (valid) {
                        let records = [];
                        let rows = this.createDnsBatch.content.split(/\r?\n/);
                        for (let i = 0; i < rows.length; ++i) {
                            let row = rows[i].trim();
                            if (row == "" || row.startsWith("#")) {
                                continue;
                            }
                            let fields = row.split(/\s+/);
                            if (fields.length >= 2) {
                                for (let j = 1; j < fields.length; ++j) {
                                    records.push({
                                        name: fields[j].trim(),
                                        content: fields[0].trim(),
                                        envId: this.createDnsBatch.envId.trim(),
                                        type: this.createDnsBatch.type
                                    })
                                }
                            }
                        }

                        this.$store.dispatch('createDns', {
                            createRequest: {
                                records: records
                            },
                            queryRequest: this.queryForm
                        });
                        this.onCloseCreateDnsBatch();
                    } else {
                        return false;
                    }
                });
            },
            onSubmitExportDns() {
                this.$refs["export-dns-form"].validate((valid) => {
                    if (valid) {
                        window.location.href = '/api/dns/export?envId=' + this.exportDns.envId;
                        this.onCloseExportDns();
                    } else {
                        return false;
                    }
                });
            },
            onDeleteDns(recordRow) {
                this.$confirm('此操作将删除该DNS记录, 是否继续？', '删除DNS记录', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    this.$store.dispatch('deleteDns', {
                        deleteRequest: {
                            id: recordRow.id,
                        },
                        queryRequest: this.queryForm
                    });
                }).catch(() => {
                    this.$message({
                        'type': 'info',
                        'message': '取消删除'
                    });
                });
            }
        }
    }
</script>

<style>

</style>