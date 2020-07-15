<template>
    <div class="content-panel">
        <el-row>
            <div class="detail-box">
                <div class="detail-box-left">环境</div>
                <el-select v-model="queryForm.env" placeholder="选择环境" style="width: 200px">
                    <el-option v-for="item in envs" :key="item.id" :label="item.name" :value="item.name"></el-option>
                </el-select>
            </div>

            <el-input v-model="queryForm.hostIp" clearable placeholder="输入物理机IP" class="query-box" prefix-icon="el-icon-search"></el-input>

            <el-button type="primary" @click="onSearch">查询</el-button>
            <el-button @click="resetQueryForm">重置</el-button>
            <el-button type="primary" @click="onTransfer" style="float: right">迁移</el-button>
            <el-button type="primary" @click="onSubmitExportRecord" style="float: right">导出</el-button>
        </el-row>

        <br/>

        <el-table :data="instances" ref="multipleTable" style="width: 100%">
            <el-table-column type="selection" width="50" align="center"></el-table-column>
            <el-table-column prop="name" label="实例名" sortable align="center"></el-table-column>
            <el-table-column prop="slotIp" label="实例IP" align="center"></el-table-column>
            <el-table-column prop="image" label="镜像" align="center"></el-table-column>
            <el-table-column prop="zone" label="部署区域" align="center"></el-table-column>
            <el-table-column prop="releaseTime" sortable label="发布时间" align="center" :formatter="dateFormatter"></el-table-column>
            <el-table-column prop="hasPulledIn" sortable label="流量状态" align="center">
                <template scope="props">
                    <el-tag v-if="props.row.hasPulledIn == 1" type="success" :disable-transitions="true">Up</el-tag>
                    <el-tag v-else-if="props.row.hasPulledIn == 0" type="danger" :disable-transitions="true">Down</el-tag>
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
                    env: '',
                    hostIp: ''
                }
            }
        },

        computed: {
            ...mapGetters({
                envs: 'getEnvList',
                instances: 'getHostInstanceList'
            }),
            selectedInstances() {
                return this.$refs.multipleTable.selection;
            },
            selectedInstanceIds() {
                let instanceIds = [];
                for (let instance of this.selectedInstances) {
                    instanceIds.push(instance.id);
                }
                return instanceIds;
            }
        },

        methods: {
            dateFormatter(row, column, cellValue) {
                return new Date(cellValue).toLocaleString();
            },
            onSearch() {
                this.refreshHostInstances();
            },
            resetQueryForm() {
                this.queryForm.env = '';
                this.queryForm.hostIp = '';
                this.refreshHostInstances();
            },
            refreshHostInstances() {
                this.$store.dispatch('fetchInstancesByHost', this.queryForm);
            },
            onTransfer() {
                let self = this;
                if (this.selectedInstanceIds.length > 0) {
                    this.$confirm('此操作将迁移所有选中的实例。是否继续？', '迁移实例', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    }).then(() => {
                        let data = {
                            env: self.queryForm.env,
                            hostIp: self.queryForm.hostIp,
                            instanceIds: self.selectedInstanceIds
                        };
                        this.$store.dispatch('transferInstances', data);
                    });
                } else {
                    this.$message.warning('请选择实例');
                }
            },
            onSubmitExportRecord() {
                if (this.queryForm.env != '' && this.queryForm.hostIp != '') {
                    this.$confirm('此操作将导出所有实例的信息。是否继续？', '导出实例', {
                        confirmButtonText: '确定',
                        cancelButtonText: '取消',
                        type: 'warning'
                    }).then(() => {
                        window.location.href = 'api/instances/export?env=' + this.queryForm.env + '&hostIp=' + this.queryForm.hostIp
                    });
                } else {
                    this.$message.warning('请选择环境并输入物理机IP');
                }
            },
        },
        created () {
            this.$store.dispatch('fetchEnvList');
            this.refreshHostInstances();
        }
    }

</script>

<style>

</style>
