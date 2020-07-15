<template>
    <div class="content-panel">
        <el-row>
            <el-col>
                <el-breadcrumb separator="/">
                    <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
                    <el-breadcrumb-item>镜像列表</el-breadcrumb-item>
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
        </el-row>

        <br/>

        <el-table :data="images" style="width: 100%" border fit>
            <el-table-column label="组织" prop="orgName" align="center" width="160"></el-table-column>
            <el-table-column label="应用名" prop="appName" align="center"></el-table-column>
            <el-table-column label="镜像名" align="center" :min-width="120">
                <template slot-scope="props">{{props.row.name}}</template>
            </el-table-column>
            <el-table-column label="仓库" prop="repoName" align="center"></el-table-column>
            <el-table-column label="版本" prop="version" align="center"></el-table-column>
            <el-table-column label="创建时间" prop="createdAt" align="center" :formatter="dateFormatter" width="200"></el-table-column>
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

    export default{
        data() {
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
                images: 'getImageList',
                total: 'getImageCount',
                apps: 'getApps',
                username: 'getUserName',
                userOrg: 'getUserOrg',
                userRoles: 'getUserRoles',
                currentAppId: 'getCurrentAppId'
            }),
            isAdmin: function() {
                return this.userRoles!=null && this.userRoles.includes('admin');
            }
        },
        created() {
            if (this.currentAppId) {
                this.queryForm.appId = this.currentAppId;
            }

            if (this.isAdmin) {
                this.$store.dispatch('fetchAppList');
            } else if (this.username) {
                this.$store.dispatch('fetchAppsByUsername', {username: this.username});
            }

            this.refreshImages();
        },
        methods: {
            dateFormatter(row, column, cellValue) {
                return new Date(cellValue).toLocaleString();
            },
            handleSizeChange(data) {
                this.queryForm.size = data;
                this.refreshImages();
            },
            handleCurrentChange(data) {
                this.queryForm.page = data;
                this.refreshImages();
            },
            onSearch() {
                this.queryForm.page = 1;
                this.refreshImages();

                if (this.queryForm.appId && this.queryForm.appId != this.currentAppId) {
                    this.$store.dispatch('refreshCurrentAppId', this.queryForm.appId);
                }
            },
            refreshImages() {
                if (this.queryForm.appId) {
                    this.$store.dispatch('fetchImagesByPage', this.queryForm);
                }
            }
        }
    }
</script>

<style>

</style>