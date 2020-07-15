<template>
    <div class="sidebar">
        <el-menu :default-active="activeIndex" router>
            <el-menu-item index="/apps"><i class="el-icon-star-on"></i>应用列表</el-menu-item>
            <el-menu-item index="/instances"><i class="el-icon-menu"></i>实例列表</el-menu-item>
            <el-menu-item index="/images"><i class="el-icon-picture"></i>镜像列表</el-menu-item>
            <el-menu-item index="/quotas"><i class="el-icon-goods"></i>应用配额</el-menu-item>
            <el-menu-item index="/staticresources"><i class="el-icon-location-outline"></i>静态资源</el-menu-item>
            <el-menu-item index="/dnslist"><i class="el-icon-document"></i>DNS列表</el-menu-item>
            <el-menu-item index="/applies"><i class="el-icon-message"></i>申请历史</el-menu-item>
            <el-menu-item index="/help"><i class="el-icon-question"></i>帮助文档</el-menu-item>
            <el-menu-item index="/system/releaserecord" v-if="isAdmin"><i class="el-icon-setting"></i>系统管理</el-menu-item>
        </el-menu>
    </div>
</template>

<script>
    import {mapGetters} from 'vuex'

    export default {
        computed: {
            ...mapGetters({
                userRoles: 'getUserRoles'
            }),
            isAdmin: function() {
                return this.userRoles!=null && this.userRoles.includes('admin');
            },
            activeIndex() {
                if (this.$route.path.startsWith('/system')) {
                    return '/system/releaserecord';
                } else {
                    return this.$route.path;
                }
            }
        }
    }
</script>

<style scoped>

    .sidebar {
        display: block;
        position: absolute;
        width: 140px;
        left: 0;
        top: 70px;
        bottom: 0;
        background: #2E363F;
    }

    .sidebar > ul {
        height: 100%;
    }

    .sidebar li i {
        font-size: 16px;
        vertical-align: baseline;
    }

    .el-submenu .el-menu-item {
        min-width: unset;
    }

    .sidebar .el-menu-item.is-active {
        outline: 0;
        background-color: rgb(232, 248, 251);
    }

</style>
