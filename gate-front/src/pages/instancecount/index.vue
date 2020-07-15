<template>
    <div class="content-panel">
        <el-card class="box-card">
            <div slot="header">
                <span>实例总数：{{getTotalInstanceCount()}}</span>
            </div>
            <el-collapse accordion>
                <el-collapse-item v-for="env in sortedEnvList" :title="'【' + env.name + '】实例数：' + getInstanceCountByEnv(env.name)">
                    <div v-for="item in sortedInstanceCountInfo" v-if="item.env == env.name">{{'〖' + item.zone + "〗实例数：" + item.instanceCount}}</div>
                </el-collapse-item>
            </el-collapse>
        </el-card>
    </div>
</template>

<script>
    import {mapGetters, mapActions} from 'vuex';

    export default {

        data: function () {
            return {}
        },

        computed: {
            ...mapGetters({
                envList: 'getEnvList',
                instanceCountInfo: 'getInstanceCountInfo'
            }),
            sortedEnvList() {
                return this._.sortBy(this.envList, 'name');
            },
            sortedInstanceCountInfo() {
                return this._.sortBy(this.instanceCountInfo, ['env', 'zone']);
            }
        },

        created () {
            this.$store.dispatch('fetchEnvList');
            this.$store.dispatch('fetchInstanceCountInfo');
        },

        methods: {
            getInstanceCountByEnv(env) {
                let count = 0;
                for (let item of this.instanceCountInfo) {
                    if (item.env == env) {
                        count += item.instanceCount;
                    }
                }
                return count;
            },
            getTotalInstanceCount() {
                let count = 0;
                for (let item of this.instanceCountInfo) {
                    count += item.instanceCount;
                }
                return count;
            }
        }
    }

</script>

<style>

</style>
