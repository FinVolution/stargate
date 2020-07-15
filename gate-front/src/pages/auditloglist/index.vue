<template>
    <div class="content-panel">
        <el-table :data="auditLogs" style="width: 100%" border fit>
            <el-table-column type="expand">
                <template slot-scope="props">
                    <div class="table-expand-datamap">
                        <div>请求参数</div>
                        <span>{{ props.row.classMethodArgs }}</span>
                        <div>返回值</div>
                        <span>{{ props.row.classMethodReturn }}</span>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="用户名" prop="userName" align="center"></el-table-column>
            <el-table-column label="请求类型" prop="httpMethod" align="center"></el-table-column>
            <el-table-column label="请求路径" prop="httpUri" align="center"></el-table-column>
            <el-table-column label="请求方法" prop="classMethod" align="center" min-width="200px"></el-table-column>
            <el-table-column label="返回码" prop="code" align="center"></el-table-column>
            <el-table-column label="请求时间" prop="insertTime" align="center"
                             :formatter="dateFormatter"></el-table-column>
        </el-table>

        <div align="center" style="margin-top: 10px">
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
    import {mapGetters, mapActions} from 'vuex';

    export default {
        data: function () {
            return {
                queryForm: {
                    page: 1,
                    size: 10
                }
            }
        },
        created: function () {
            this.refreshAuditLogs();
        },
        computed: {
            ...mapGetters({
                auditLogs: 'getAuditLogList',
                total: 'getAuditLogCount'
            })
        },
        methods: {
            dateFormatter(row, column, cellValue) {
                return new Date(cellValue).toLocaleString();
            },
            handleSizeChange(data) {
                this.queryForm.size = data;
                this.refreshAuditLogs();
            },
            handleCurrentChange(data) {
                this.queryForm.page = data;
                this.refreshAuditLogs();
            },
            refreshAuditLogs() {
                this.$store.dispatch('fetchAuditLogsByPage', this.queryForm);
            }
        }
    }
</script>

<style>

</style>