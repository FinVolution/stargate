<template>
    <div class="layout-wrapper">
        <vheader title="Stargate发布系统" icon="el-icon-upload"></vheader>
        <vsiderbar></vsiderbar>
        <el-menu :default-active="activeIndex" class="system-menu" mode="horizontal" router>
            <el-menu-item index="/system/envs">环境列表</el-menu-item>
            <el-menu-item index="/system/ips">IP列表</el-menu-item>
            <el-menu-item index="/system/resources">资源列表</el-menu-item>
            <el-menu-item index="/system/cloudinstance">私有云实例</el-menu-item>
            <el-menu-item index="/system/releaserecord">发布记录</el-menu-item>
            <el-menu-item index="/system/instancetransfer">实例迁移</el-menu-item>
            <el-menu-item index="/system/appmanager">应用管理</el-menu-item>
            <el-menu-item index="/system/instancecount">实例统计</el-menu-item>
            <el-menu-item index="/system/runningstatus">运行状态</el-menu-item>
            <el-menu-item index="/system/auditlogs">审计日志</el-menu-item>
        </el-menu>
        <div class="system-main-content">
            <transition name="move" mode="out-in">
                <router-view></router-view>
            </transition>
        </div>
    </div>
</template>

<script>
    import vheader from '../../components/Header.vue'
    import vsiderbar from '../../components/SiderBar.vue'
    import {mapGetters} from 'vuex'

    export default {
        components: {
            vheader, vsiderbar
        },
        computed: {
            ...mapGetters({
                promptMessage: 'getPromptMessage'
            }),
            activeIndex() {
                return this.$route.path;
            }
        },
        watch: {
            promptMessage: function (newMessage) {
                if (newMessage.code != null) {
                    if (newMessage.code >= 0) {
                        this.$message.success(newMessage.details);
                    } else {
                        this.$message({
                            type: "error",
                            message: newMessage.details,
                            showClose: true,
                            duration: 5000
                        });
                    }
                }
            }
        }
    }
</script>

<style>
    @import '../../assets/stargate.css';

    .system-menu {
        display: inline-flex;
        position: absolute;
        left: 140px;
        right: 0;
        top: 70px;
    }

    .system-menu > .el-menu-item {
        height: 40px;
        line-height: 40px;
    }

    .system-main-content {
        position: absolute;
        left: 140px;
        right: 0;
        top: 111px;
        bottom: 0;
        overflow-y: scroll;
    }
</style>
