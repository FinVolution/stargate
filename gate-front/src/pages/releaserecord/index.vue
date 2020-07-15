<template>
    <div class="content-panel">
        <el-row>
            <el-select v-model="queryForm.env" placeholder="选择环境" style="width: 150px; margin-right: 40px;" @change="onSearch">
                <el-option
                        v-for="item in envs"
                        :key="item.id"
                        :label="item.name"
                        :value="item.name">
                </el-option>
            </el-select>
            <el-select v-model="queryForm.appId" placeholder="选择应用" filterable style="width: 300px; margin-right: 40px;" @change="onSearch">
                <el-option v-for="item in apps" :key="item.id" :value="item.cmdbAppId"
                           :label="item.name + '/' + item.cmdbAppId"></el-option>
            </el-select>
            <el-select v-model="queryForm.operationType" placeholder="选择任务类型" style="width: 150px; margin-right: 40px;" @change="onSearch">
                <el-option v-for="item in types" :key="item.name" :label="item.description" :value="item.name"></el-option>
            </el-select>
            <el-select v-model="queryForm.status" placeholder="选择任务状态" style="width: 150px; margin-right: 40px;" @change="onSearch">
                <el-option v-for="item in jobStatus" :key="item" :label="item" :value="item"></el-option>
            </el-select>

            <el-date-picker
                    v-model="queryForm.date"
                    type="daterange"
                    range-separator="至"
                    start-placeholder="开始日期"
                    end-placeholder="结束日期"
                    value-format="timestamp"
                    :default-time="['00:00:00', '23:59:59']"
                    :picker-options="pickerOptions"
                    style="margin-right: 40px"
                    @change="onSearch">
            </el-date-picker>

            <el-button type="primary" @click="onSearch">查询</el-button>
            <el-button @click="resetQueryForm">重置</el-button>
        </el-row>

        <br/>

        <el-table :data="releaseRecord" style="width: 100%" border fit>
            <el-table-column type="expand">
                <template slot-scope="props">
                    <el-form v-for="task in props.row.taskList" :key="task.id" label-position="left" inline class="table-expand">
                        <el-form-item :label="'任务'+task.step">
                            <span>{{ getTaskName(task) }}</span>
                        </el-form-item>
                        <el-form-item label="状态">
                            <span>{{ task.status }}</span>
                        </el-form-item>
                        <el-form-item v-if="task.additionalInfo" label="附加信息" style="width:50%;">
                            <span>{{ task.additionalInfo }}</span>
                        </el-form-item>
                    </el-form>

                    <div class="table-expand-datamap">
                        <div>任务信息</div>
                        <vue-json-pretty :path="'res'" :data="JSON.parse(props.row.dataMap)" class="vue-json-pretty"></vue-json-pretty>
                    </div>
                </template>
            </el-table-column>
            <el-table-column label="环境" prop="env" align="center" width="80"></el-table-column>
            <el-table-column label="应用名" prop="appName" align="center"></el-table-column>
            <el-table-column label="发布组 | 实例" prop="name" align="center"></el-table-column>
            <el-table-column label="任务类型" prop="operationTypeDesc" align="center"></el-table-column>
            <el-table-column label="操作者" prop="insertBy" align="center"></el-table-column>
            <el-table-column label="操作时间" prop="insertTime" align="center" :formatter="dateFormatter"></el-table-column>
            <el-table-column label="任务状态" align="center" width="120">
                <template slot-scope="props">
                    <el-tag v-if="props.row.status == 'SUCCESS'" type="success">{{props.row.status}}</el-tag>
                    <el-tag v-else-if="props.row.status == 'FAIL'" type="danger">{{props.row.status}}</el-tag>
                    <el-tag v-else-if="props.row.status == 'EXPIRED'" type="warning">{{props.row.status}}</el-tag>
                    <el-tag v-else-if="props.row.status == 'RUNNING'">{{props.row.status}}</el-tag>
                    <el-tag v-else type="info">{{props.row.status}}</el-tag>
                </template>
            </el-table-column>
            <el-table-column label="操作" align="center" width="140">
                <template scope="props">
                    <el-button size="small" type="primary" plain @click="restartJob(props.row.id)" v-if="props.row.status == 'FAIL' || props.row.status == 'EXPIRED'">重试</el-button>
                    <el-button size="small" type="primary" plain @click="displayReleaseLog(props.row.groupId, props.row.name, props.row.id)">日志</el-button>
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

        <el-dialog title="发布日志" :visible.sync="logVisible" top="40px" width="90%" custom-class="log-dialog">
            <div class="release-log">{{releaseLog.logs}}</div>
        </el-dialog>
    </div>
</template>

<script>
    import {mapGetters, mapActions} from 'vuex'
    import VueJsonPretty from 'vue-json-pretty'

    export default {
        components: {
            VueJsonPretty
        },
        data: function() {
            return {
                queryForm: {
                    env: null,
                    appId: null,
                    operationType: null,
                    status: null,
                    date: null,
                    page: 1,
                    size: 10
                },
                jobStatus: ['NEW', 'RUNNING', 'SUCCESS', 'FAIL', 'EXPIRED'],
                logVisible: false,
                pickerOptions: {
                    disabledDate(time) {
                        return time.getTime() > Date.now();
                    }
                }
            }
        },
        computed: {
            ...mapGetters({
                releaseRecord: 'getReleaseRecord',
                total: 'getReleaseRecordCount',
                releaseLog: 'getReleaseLog',
                envs: 'getEnvList',
                apps: 'getApps',
                types: 'getReleaseTypeList'
            })
        },
        created() {
            this.refreshReleaseRecord();
            this.$store.dispatch('fetchEnvList');
            this.$store.dispatch('fetchAppList');
            this.$store.dispatch('fetchReleaseTypes');
        },
        methods: {
            dateFormatter(row, column, cellValue) {
                return new Date(cellValue).toLocaleString();
            },
            onSearch() {
                this.queryForm.page = 1;
                this.refreshReleaseRecord();
            },
            handleSizeChange(data) {
                this.queryForm.size = data;
                this.refreshReleaseRecord();
            },
            handleCurrentChange(data) {
                this.queryForm.page = data;
                this.refreshReleaseRecord();
            },
            resetQueryForm() {
                this.queryForm.env = null;
                this.queryForm.appId = null;
                this.queryForm.operationType = null;
                this.queryForm.status = null;
                this.queryForm.date = null;
                this.queryForm.page = 1;
                this.refreshReleaseRecord();
            },
            refreshReleaseRecord() {
                let sTime = null;
                let eTime = null;
                if (Array.isArray(this.queryForm.date) && this.queryForm.date.length == 2) {
                    sTime = this.queryForm.date[0];
                    eTime = this.queryForm.date[1];
                }
                this.$store.dispatch('fetchReleaseRecordByPage', {
                    env: this.queryForm.env,
                    appId: this.queryForm.appId,
                    operationType: this.queryForm.operationType,
                    status: this.queryForm.status,
                    page: this.queryForm.page,
                    size: this.queryForm.size,
                    startTime: sTime,
                    endTime: eTime
                });
            },
            displayReleaseLog(groupId, groupName, jobId) {
                this.$store.dispatch('fetchReleaseLog', {
                    groupId: groupId,
                    groupName: groupName,
                    jobId: jobId
                });
                this.logVisible = true;
            },
            getTaskName(task) {
                let taskName = task.description;
                if (task.dataMap && task.dataMap.zone) {
                    taskName += '(' + task.dataMap.zone + ')';
                }
                return taskName;
            },
            restartJob(jobId) {
                this.$store.dispatch('restartJob', {jobId: jobId});
            }
        }
    }

</script>

<style>

</style>