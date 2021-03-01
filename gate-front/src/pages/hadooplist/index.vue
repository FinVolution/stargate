<template>
    <div class="content-panel">
        <el-row>
            <el-button type="primary" @click="onCreate" style="float: right">添加集群配置</el-button>
        </el-row>
        <br/>

        <el-table :data="hadoopConfigs" style="width: 100%" border fit>
            <el-table-column label="名称" prop="name" align="center"></el-table-column>
            <el-table-column label="savepoint" prop="savepoint" align="center"></el-table-column>
            <el-table-column label="coreSite" align="center">
                <template scope="props">
                    <el-button type="primary" @click="onCoreSiteConfig(props.row.coreSite)" plain>查看</el-button>
                </template>
            </el-table-column>
            <el-table-column label="hdfsSite" align="center" :formatter="dateFormatter">
                <template scope="props">
                    <el-button @click="onHdfsSiteConfig(props.row.hdfsSite)" type="primary" plain>查看</el-button>
                </template>
            </el-table-column>
            <el-table-column label="描述" prop="description" align="center"></el-table-column>
            <el-table-column label="修改者" prop="updateBy" align="center"></el-table-column>
            <el-table-column label="部门" prop="department" align="center" :formatter="departmentFormatter">
                <!--                <template scope="props">-->
                <!--                    -->
                <!--                </template>-->
            </el-table-column>
            <el-table-column label="创建时间" prop="updateTime" align="center" :formatter="dateFormatter"></el-table-column>
            <el-table-column label="操作" align="center" width="250">
                <template scope="props">
                    <el-button v-if="isAdmin" size="small" type="primary" @click="editHadoop(props.row)" plain>编辑<i
                            class="el-icon-edit el-icon--left"></i>
                    </el-button>
                    <el-button v-if="isAdmin" size="small" type="danger" @click="deleteHadoop(props.row.id)" plain>删除<i
                            class="el-icon-delete el-icon--right"></i>
                    </el-button>
                </template>
            </el-table-column>
        </el-table>

        <el-dialog title="配置" :visible.sync="hadoopConfigDialogTableVisible" width="50%" :before-close="onCloseConfig">
            <el-input
                    type="textarea"
                    autosize
                    placeholder="请输入内容"
                    v-model="configContent">
            </el-input>

        </el-dialog>
        <el-dialog title="添加hadoop配置" :visible.sync="addHadoopConfig" width="500px" :before-close="onClose">
            <el-form label-width="100px" label-position="left" :model="hadoopConfig" ref="hadoopConfig"
                     :rules="addResourceRules">
                <el-form-item label="集群名称" prop="hadoopName">
                    <el-input placeholder="请输入集群名称" v-model="hadoopConfig.hadoopName"></el-input>
                </el-form-item>
                <el-form-item label="savepoint" prop="savepoint">
                    <el-input placeholder="请输入savepoint" v-model="hadoopConfig.savepoint"></el-input>
                </el-form-item>
                <el-form-item label="所属组织" prop="department">
                    <el-select v-model="hadoopConfig.department" placeholder="请选择所属组织" style="width: 100%">
                        <el-option v-for="item in orgs"
                                   :key="item.id"
                                   :label="item.name"
                                   :value="item.orgCode">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="core配置" prop="env">
                    <el-upload
                            :on-change="onCoreSite"
                            class="upload-core"
                            action=""
                            :on-preview="handlePreview"
                            :on-remove="handleRemove"
                            :before-remove="beforeRemove"
                            multiple
                            :limit="1"
                            :auto-upload="false"
                            :file-list="coreSite">
                        <el-button size="small" type="primary">点击上传</el-button>
                        <div slot="tip" class="el-upload__tip">请上传core-site.xml</div>
                    </el-upload>
                </el-form-item>
                <el-form-item label="hdfs配置" prop="zone">
                    <el-upload
                            class="upload-hdfs"
                            action=""
                            :on-change="onHdfsSite"
                            :on-preview="handlePreview"
                            :on-remove="handleRemove"
                            :before-remove="beforeRemove"
                            :auto-upload="false"
                            multiple
                            :limit="1"
                            :file-list="hdfsSite">
                        <el-button size="small" type="primary">点击上传</el-button>
                        <div slot="tip" class="el-upload__tip">请上传hdfs-site.xml</div>
                    </el-upload>
                </el-form-item>
                <el-form-item label="描述" prop="description">
                    <el-input
                            type="textarea"
                            :limit="4"
                            placeholder="请输入内容"
                            v-model="hadoopConfig.description">
                    </el-input>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onClose" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="onCreateHadoop" style="float:right;margin:0 10px 0 0;">创建
                    </el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
        <el-dialog title="修改hadoop配置" :visible.sync="updateHadoopConfig" width="500px" :before-close="onUpdateClose">
            <el-form label-width="100px" label-position="left" :model="hadoopConfig" ref="hadoopConfig"
                     :rules="addResourceRules">
                <el-form-item label="集群名称" prop="hadoopName">
                    <el-input disabled placeholder="请输入集群名称" v-model="hadoopConfig.hadoopName"></el-input>
                </el-form-item>
                <el-form-item label="所属组织" prop="department">
                    <el-select v-model="hadoopConfig.department" placeholder="请选择所属组织" style="width: 100%">
                        <el-option v-for="item in orgs"
                                   :key="item.id"
                                   :label="item.name"
                                   :value="item.orgCode">
                        </el-option>
                    </el-select>
                </el-form-item>
                <el-form-item label="savepoint" prop="savepoint">
                    <el-input placeholder="请输入savepoint" v-model="hadoopConfig.savepoint"></el-input>
                </el-form-item>
                <el-form-item label="描述" prop="description">
                    <el-input
                            type="textarea"
                            :limit="4"
                            placeholder="请输入内容"
                            v-model="hadoopConfig.description">
                    </el-input>
                </el-form-item>
                <el-form-item style="margin-bottom: 0">
                    <el-button @click="onUpdateClose" style="float: right">关闭</el-button>
                    <el-button type="primary" @click="onUpdateHadoop" style="float:right;margin:0 10px 0 0;">修改
                    </el-button>
                </el-form-item>
            </el-form>
        </el-dialog>
    </div>
</template>

<script>
    import {mapGetters} from 'vuex';

    export default {

        data: function () {
            return {
                updateHadoopConfig: false,
                savepoint: null,
                description: null,
                configContent: '',
                hadoopConfigDialogTableVisible: false,
                hadoopConfig: {
                    department: null,
                    id: null,
                    hadoopName: null,
                    savepoint: "",
                    description: ""
                },
                hdfsSite: [],
                coreSite: [],
                addHadoopConfig: false
            }
        },
        created() {
            this.$store.dispatch('fetchHadoopConfigs', this.currentEnv);
            this.$store.dispatch('fetchAllOrgs');
        },
        computed: {
            ...mapGetters({
                hadoopConfigs: 'getHadoopConfigs',
                currentEnv: 'getCurrentEnv',
                userRoles: 'getUserRoles',
                orgs: 'getOrgList'
            }),
            isAdmin: function () {
                return this.userRoles != null && this.userRoles.includes('admin');
            }
        },
        methods: {
            onUpdateClose() {
                this.updateHadoopConfig = false;
                this.$refs["hadoopConfig"].resetFields();
            },
            onUpdateHadoop() {
                let data = {
                    id: this.hadoopConfig.id,
                    description: this.hadoopConfig.description,
                    savepoint: this.hadoopConfig.savepoint,
                    env: this.hadoopConfig.env,
                    department: this.hadoopConfig.department
                };
                this.$store.dispatch('updateHadoopConfig', data);
                this.onUpdateClose();
            },
            editHadoop(row) {
                this.hadoopConfig.id = row.id;
                this.hadoopConfig.savepoint = row.savepoint;
                this.hadoopConfig.hadoopName = row.name;
                this.hadoopConfig.description = row.description;
                this.hadoopConfig.env = row.env;
                this.hadoopConfig.department = row.department;
                this.updateHadoopConfig = true;
            },
            deleteHadoop(id) {
                this.$confirm('此操作将删除hadoop配置，会影响到使用该配置的flink job, 是否继续？', '删除hadoop配置', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    this.$store.dispatch('deleteHadoopById', {
                        id: id,
                        env: this.currentEnv
                    });
                }).catch(e => {
                    this.$message({
                        'type': 'info',
                        'message': '取消删除'
                    });
                });
            },
            departmentFormatter(row, column, cellValue) {
                for (let x of this.orgs) { // 遍历Array
                    if (x.orgCode === cellValue) {
                        return x.name;
                    }
                }
                return "";
            },
            dateFormatter(row, column, cellValue) {
                return new Date(cellValue).toLocaleString();
            },
            onCloseConfig() {
                this.hadoopConfigDialogTableVisible = false;
            },
            onHdfsSiteConfig(hdfsSite) {
                this.hadoopConfigDialogTableVisible = true;
                this.configContent = hdfsSite;
            },
            onCoreSiteConfig(coreSite) {
                this.hadoopConfigDialogTableVisible = true;
                this.configContent = coreSite;
            },
            onCreate() {
                this.hadoopConfig={
                    id: null,
                    hadoopName: null,
                    savepoint: "",
                    description: "",
                    department: null,
                };
                this.addHadoopConfig = true;
            },
            onCoreSite(file, fileList) {
                this.coreSite = fileList;
            },
            onHdfsSite(file, fileList) {
                this.hdfsSite = fileList;
            },
            onCreateHadoop() {
                let form = this.$refs['hadoopConfig'].$el;
                let formData = new FormData(form);
                formData.delete("file");
                formData.append('hadoopName', this.hadoopConfig.hadoopName);
                formData.append('coreSite', this.coreSite[0] ? this.coreSite[0].raw : '');
                formData.append('hdfsSite', this.hdfsSite[0] ? this.hdfsSite[0].raw : '');
                if (this.hadoopConfig.description != null) {
                    formData.append('description', this.hadoopConfig.description);
                }
                if (this.hadoopConfig.savepoint != null) {
                    formData.append('savepoint', this.hadoopConfig.savepoint);
                }
                if (this.hadoopConfig.department != null) {
                    formData.append('department', this.hadoopConfig.department);
                }
                formData.append('env', this.currentEnv);
                let data = {
                    formData: formData,
                    env: this.currentEnv
                };
                this.$store.dispatch('createHadoopConfig', data);
                this.addHadoopConfig = false;
            },
            onClose() {
                this.addHadoopConfig = false;
                this.$refs["hadoopConfig"].resetFields();
            },
        }
    }

</script>

<style>

</style>
