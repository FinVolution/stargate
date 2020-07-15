<template>
    <div class="content-panel">
        <el-row>
            <el-col>
                <el-breadcrumb separator="/">
                    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                    <el-breadcrumb-item>申请历史</el-breadcrumb-item>
                </el-breadcrumb>
            </el-col>
        </el-row>

        <br/>

        <el-table :data="applies" style="width: 100%" border fit>
            <el-table-column type="expand">
                <template slot-scope="props">
                    <table class="job-expand-table" cellpadding="10px" cellspacing="0" border="0">
                        <tr>
                            <td width="20%" align="center">
                                <div>申请参数</div>
                            </td>
                            <td width="80%" >
                                <vue-json-pretty :path="'res'" :data="JSON.parse(props.row.request)"></vue-json-pretty>
                            </td>
                        </tr>
                        <tr>
                            <td width="20%" align="center">
                                <div>申请结果</div>
                            </td>
                            <td width="80%" >
                                <vue-json-pretty :path="'res'" :data="JSON.parse(props.row.result)"></vue-json-pretty>
                            </td>
                        </tr>

                    </table>
                </template>
            </el-table-column>
            <el-table-column label="申请类型" prop="type" align="center">
                <template scope="props">
                    <el-tag v-if="props.row.type == 'NEW_APP'" type="info">申请应用</el-tag>
                    <el-tag v-else-if="props.row.type == 'CHANGE_QUOTA'" type="info">修改配额</el-tag>
                    <el-tag v-else type="warning">未知类型</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="申请状态" prop="status" align="center">
                <template scope="props">
                    <el-tag v-if="props.row.status == 'NEW'" type="success">待处理</el-tag>
                    <el-tag v-else-if="props.row.status == 'ACCEPTED'">处理中</el-tag>
                    <el-tag v-else-if="props.row.status == 'REJECTED'" type="danger">拒绝</el-tag>
                    <el-tag v-else-if="props.row.status == 'DONE'" type="info">完成</el-tag>
                    <el-tag v-else type="warning">未知状态</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="组织" prop="applyDepartment" align="center"></el-table-column>
            <el-table-column label="应用名" prop="request" align="center" :formatter="appNameFormatter"></el-table-column>
            <el-table-column label="申请人" prop="applyUser" align="center"></el-table-column>
            <el-table-column label="操作人" prop="opUser" align="center"></el-table-column>
            <el-table-column label="申请时间" prop="applyTime" align="center" :formatter="dateFormatter"></el-table-column>
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
    </div>
</template>

<script>
    import {mapGetters, mapActions} from 'vuex'
    import VueJsonPretty from 'vue-json-pretty'

    export default {
        components: {
            VueJsonPretty
        },
        data: function () {
            return {
                queryForm: {
                    applyUser: '',
                    status: '',
                    page: 1,
                    size: 10
                }
            }
        },

        computed: {
            ...mapGetters({
                applies: 'getApplyList',
                total: 'getApplyCount',
                username: 'getUserName'
            })
        },

        created () {
            this.refreshApplyList();
        },

        methods: {
            dateFormatter(row, column, cellValue) {
                return new Date(cellValue).toLocaleString();
            },
            appNameFormatter(row, column, cellValue) {
                let request = JSON.parse(cellValue);
                return request ? request.appName : '';
            },
            handleSizeChange(data) {
                this.queryForm.size = data;
                this.refreshApplyList();
            },
            handleCurrentChange(data) {
                this.queryForm.page = data;
                this.refreshApplyList();
            },
            refreshApplyList() {
                if (this.username) {
                    this.queryForm.applyUser = this.username;
                    this.$store.dispatch('fetchAppliesByPage', this.queryForm);
                }
            }
        }
    }
</script>

<style>
    .job-expand-table {
        border-left: 1px solid #ebeef5;
        border-top: 1px solid #ebeef5;
        width: 100%;
        word-break: break-all;
    }

    .job-expand-table td div {
        padding: 0 10px;
        /*white-space: pre-wrap;*/
    }
</style>