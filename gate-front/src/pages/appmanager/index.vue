<template>
    <div class="content-panel">
        <el-row>
            <div class="detail-box">
                <div class="detail-box-left">应用</div>
                <el-select v-model="queryForm.appId" placeholder="选择应用" filterable @change="onSearch" style="width: 300px">
                    <el-option v-for="item in apps" :key="item.id" :value="item.cmdbAppId"
                               :label="item.name + '/' + item.cmdbAppId"></el-option>
                </el-select>
            </div>

            <el-button @click="resetQueryForm">重置</el-button>
        </el-row>

        <br/>

        <el-table :data="appList" style="width: 100%" border fit>
            <el-table-column label="组织" prop="department" align="center" width="160"></el-table-column>
            <el-table-column label="应用名" prop="name" align="center"></el-table-column>
            <el-table-column label="应用ID" prop="cmdbAppId" align="center"></el-table-column>
            <el-table-column label="负责人" prop="owner" align="center" min-width="200"></el-table-column>
            <el-table-column label="创建时间" prop="insertTime" align="center" :formatter="dateFormatter" width="200"></el-table-column>
            <el-table-column label="操作" align="center" width="120px" v-if="isAdmin">
                <template scope="props">
                    <el-button @click="handleDelete(props.row)" size="small" type="danger" plain>删除<i class="el-icon-delete el-icon--right"></i></el-button>
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
    </div>
</template>

<script>
    import {mapGetters, mapActions} from 'vuex'

    export default {
        data: function () {
            return {
                queryForm: {
                    appId: null,
                    page: 1,
                    size: 10
                }
            }
        },
        computed: {
            ...mapGetters({
                apps: 'getApps',
                appList: 'getAppList',
                total: 'getAppCount',
                userRoles: 'getUserRoles'
            }),
            isAdmin: function() {
                return this.userRoles!=null && this.userRoles.includes('admin');
            }
        },
        created () {
            this.$store.dispatch('fetchAppList');
            this.refreshAppList();
        },
        methods: {
            dateFormatter(row, column, cellValue) {
                return new Date(cellValue).toLocaleString();
            },
            handleSizeChange(data) {
                this.queryForm.size = data;
                this.refreshAppList();
            },
            handleCurrentChange(data) {
                this.queryForm.page = data;
                this.refreshAppList();
            },
            onSearch() {
                this.queryForm.page = 1;
                this.refreshAppList();
            },
            refreshAppList() {
                this.$store.dispatch('fetchAppsByPage', this.queryForm);
            },
            resetQueryForm() {
                this.queryForm.appId = null;
                this.onSearch();
            },
            handleDelete(data) {
                this.$confirm('此操作将删除该应用和应用的所有集群, 是否继续？', '删除应用', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    this.$store.dispatch('removeApp', {
                        id: data.id,
                        queryForm: this.queryForm
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