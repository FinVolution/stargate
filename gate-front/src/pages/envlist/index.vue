<template>
    <div class="content-panel">
        <el-table :data="envsInView" border fit style="width: 100%">
            <el-table-column prop="name" label="环境名" align="center"></el-table-column>
            <el-table-column prop="description" label="环境描述" align="center"></el-table-column>
            <el-table-column prop="consul" label="consul地址" align="center"></el-table-column>
            <el-table-column prop="nginx" label="nginx地址" align="center"></el-table-column>
            <el-table-column prop="dns" label="dns地址" align="center"></el-table-column>
            <el-table-column prop="dockeryard" label="镜像仓库地址" align="center"></el-table-column>
            <el-table-column label="启用高可用" align="center" v-if="isAdmin">
                <template scope="props">
                    <el-switch
                            v-model="props.row.enableHa"
                            active-color="#13ce66"
                            inactive-color="#ff4949"
                            @change="confirmHaChange(props.row)">
                    </el-switch>
                </template>
            </el-table-column>
            <el-table-column label="激活" align="center" v-if="isAdmin">
                <template scope="props">
                    <el-switch
                            v-model="props.row.isInUse"
                            active-color="#13ce66"
                            inactive-color="#ff4949"
                            @change="confirmChange(props.row)">
                    </el-switch>
                </template>
            </el-table-column>
        </el-table>
    </div>
</template>

<script>
    import {mapGetters, mapActions} from 'vuex'
    import localStroageUtil from '../../utils/localStorageUtil'

    export default {
        data: function () {
            return {
                envsInView: []
            }
        },
        computed: {
            ...mapGetters({
                envlist: 'getEnvList',
                userRoles: 'getUserRoles'
            }),
            isAdmin: function() {
                return this.userRoles!=null && this.userRoles.includes('admin');
            }
        },
        created () {
            this.$store.dispatch('fetchEnvList');
        },
        watch: {
            envlist: function (envList) {
                this.envsInView = [];
                for (let index in envList) {
                    let env = Object.assign({}, envList[index]);
                    this.envsInView.push(env);
                }
            }
        },
        methods: {
            confirmChange(env){
                this.$confirm('此操作需谨慎，将影响用户可以发布的环境，是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    center: true
                }).then(() => {
                    this.$store.dispatch('activateEnv', {
                        envId: env.id,
                        activated: env.isInUse
                    });

                    let environment = localStroageUtil.readEnvironment();
                    if (!env.isInUse && env.name == environment) {
                        this.$store.dispatch('refreshCurrentEnv', null);
                    }
                }).catch(() => {
                    env.isInUse = !env.isInUse;
                });
            },
            confirmHaChange(env){
                this.$confirm('此操作需谨慎，将影响容器云环境的高可用，是否继续?', '提示', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                    center: true
                }).then(() => {
                    if (env.enableHa) {
                        this.$store.dispatch('enableHa', {
                            envId: env.id
                        });
                    } else {
                        this.$store.dispatch('disableHa', {
                            envId: env.id
                        });
                    }
                }).catch(() => {
                    env.enableHa = !env.enableHa;
                });
            }
        }
    }
</script>

<style>

</style>