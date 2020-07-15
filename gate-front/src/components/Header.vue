<template>
    <div class="header">
        <div class="title" @click="returnHome"><i :class="icon"></i>{{title}}</div>

        <div class="environment-info">
            <el-tooltip effect="dark" content="请选择环境" placement="right" :disabled="!showTips" :value="showTips" :manual="showTips">
                <el-radio-group v-model="currentEnvironment" fill="#39d0a0" size="small" @change="onEnvChange">
                    <template v-for="env in orderedEnvs">
                        <el-tooltip effect="dark" :content="env.description" placement="bottom">
                            <el-radio-button :key="env.name" :label="env.name" name="description"></el-radio-button>
                        </el-tooltip>
                    </template>
                </el-radio-group>
            </el-tooltip>
        </div>

        <div class="user-info">
            <el-dropdown @command="handleCommand">
                <div class="el-dropdown-link">
                    <img v-if="username != null" :src="imgUrl" class="user-logo">
                    {{(username != null) ? username : "您好，请登录"}}
                </div>
                <el-dropdown-menu slot="dropdown" class="dropdown-menu">
                    <el-dropdown-item command="logout" v-if='isLogin'>退出</el-dropdown-item>
                    <el-dropdown-item command="login" v-else>登录</el-dropdown-item>
                </el-dropdown-menu>
            </el-dropdown>
            <div v-if="isPromptExpire" class="expire-prompt">
                <div>您的当前登录会话已过期，请重新登录。</div>
                <div>
                    <el-button type="primary" @click="login">重新登录</el-button>
                </div>
            </div>
        </div>

        <router-link :to="{name: 'help'}">
            <div class="help-link"><i class="el-icon-question"></i><span>帮助</span></div>
        </router-link>
    </div>
</template>

<script>
    import {mapGetters, mapActions} from 'vuex'
    import localStroageUtil from '../utils/localStorageUtil'
    import crypto from 'crypto'
    import Identicon from 'identicon.js'

    export default {
        props: {
            title: String,
            icon: String
        },
        data: function () {
            return {
                isLogoutOnStartup: false,
                currentEnvironment: null,
                lastEnvironment: null
            }
        },
        computed: {
            ...mapGetters({
                isLogin: 'getLoginState',
                username: 'getUserName',
                userRoles: 'getUserRoles',
                isExpired: 'getExpireState',
                promptMsg: 'getPromptMsg',
                activeEnvs: 'getEnvActiveList',
                currentEnv: 'getCurrentEnv'
            }),
            isPromptExpire: function () {
                return this.isExpired && this.isLogin;
            },
            showTips: function () {
                return this.activeEnvs.length != 0 && this.currentEnvironment == null;
            },
            orderedEnvs: function () {
                let orderedEnvs = this._.clone(this.activeEnvs);
                orderedEnvs.sort((a, b) => a.name.localeCompare(b.name));
                return orderedEnvs;
            },
            imgUrl: function () {
                let hash = crypto.createHash('md5');
                hash.update(this.username);
                let imgData = new Identicon(hash.digest('hex'), 40).toString();
                let imgUrl = 'data:image/png;base64,' + imgData;
                return imgUrl;
            }
        },
        methods: {
            ...mapActions(['logout', 'checkExpired', 'refreshToken']),
            handleCommand(command) {
                if (command == 'logout') {
                    this.logout();
                } else if (command == 'login') {
                    this.login();
                }
            },
            returnHome() {
                this.$router.push('/');
            },
            onInterval: function () {
                if (!this.isLogin) {
                    // 一旦用户登出，则可以关闭定时器
                    clearInterval(this.internalTimer);
                    clearInterval(this.refreshTokenTimer);

                    // 若用户手动点击登出按钮，提示用户已经成功登出
                    if (!this.isLogoutOnStartup) {
                        this.$message.success("登出成功");
                    }
                } else {
                    // 定期检查登录状态是否过期
                    this.$store.dispatch('checkExpired');
                }
            },
            onEnvChange: function (data) {
                this.$confirm('此操作将切换至' + data + '环境, 是否继续？', '环境切换', {
                    confirmButtonText: '确定',
                    cancelButtonText: '取消',
                    type: 'warning',
                }).then(() => {
                    this.$store.dispatch('refreshCurrentEnv', this.currentEnvironment);
                    this.lastEnvironment = this.currentEnvironment;

                    this.$message('环境切换至：' + this.currentEnvironment);
                }).catch(() => {
                    this.currentEnvironment = this.lastEnvironment;
                });
            },
            refreshToken: function () {
                this.$store.dispatch('refreshToken');
            },
            logout(){
                // 登出时注销并清空所有token
                this.$store.dispatch('revokeToken');
            },
            login(){
                this.$store.dispatch('saveLastVisit', this.$route.fullPath);
                this.$store.dispatch('login');
            }
        },
        watch: {
            promptMsg: function (newMessage) {
                if (newMessage.code != null) {
                    if (newMessage.code >= 0) {
                        this.$message.success(newMessage.details);
                    } else {
                        this.$message.error(newMessage.details);
                    }
                }
            },
            currentEnv: function (newCurrentEnv) {
                this.currentEnvironment = newCurrentEnv;
            }
        },
        created () {
            this.$store.dispatch('readLoginInfo');
            this.$store.dispatch('fetchActiveEnvList');
            this.refreshToken();
        },
        beforeMount: function () {
            // 若发现没有登录，提示用户登录
            if (!this.isLogin) {
                this.$message.warning("您好，请先登录");
                this.isLogoutOnStartup = true;
            }

            // 获取前一次保存的环境选择
            let environment = localStroageUtil.readEnvironment();
            if (environment != null) {
                this.currentEnvironment = environment;
                this.lastEnvironment = environment;
                this.$store.dispatch('refreshCurrentEnv', environment);
            }

            // 获取前一次保存的应用选择
            let appId = localStroageUtil.readAppId();
            if (appId != null) {
                this.$store.dispatch('refreshCurrentAppId', appId);
            }
        },
        mounted: function () {
            // 定时器，每隔500ms触发一次定期检查
            this.internalTimer = setInterval(this.onInterval.bind(this), 500);
            // 每隔5小时触发一次access token刷新
            this.refreshTokenTimer = setInterval(this.refreshToken.bind(this), 5 * 60 * 60 * 1000);
        },
        beforeDestroy: function () {
            //清理定时器
            clearInterval(this.internalTimer);
            clearInterval(this.refreshTokenTimer);
        }
    };
</script>

<style>

    .header {
        background-color: #16B6D7;
        position: relative;
        box-sizing: border-box;
        width: 100%;
        height: 70px;
        font-size: 22px;
        line-height: 70px;
        color: #fff;
    }

    .header .title {
        cursor: pointer;
        margin-left: 20px;
        float: left;
        font-size: 24px;
    }

    .header .title:hover {
        color: #036879;
    }

    .header .title i {
        margin-right: 5px;
        font-size: 28px;
    }

    .environment-info {
        float: left;
        margin-left: 50px;
    }

    .environment-info .el-radio-group {
        vertical-align: super;
    }

    .environment-info .el-radio-group .el-radio-button__inner {
        width: 56px;
    }

    .el-radio-button--small .el-radio-button__inner {
        padding: 10px 5px;
    }

    .environment-info .el-radio-group .el-radio-button.is-active span {
        font-size: 16px;
    }

    .help-link {
        float: right;
        font-size: 16px;
        margin-right: 50px;
        color: white;
    }

    .help-link:hover {
        color: #036879;
    }

    .help-link i {
        margin-right: 3px;
    }

    .user-info {
        float: right;
        margin-right: 40px;
        font-size: 16px;
        color: #fff;
    }

    .user-info .el-dropdown {
        display: block;
    }

    .user-info .el-dropdown-link {
        position: relative;
        padding-left: 50px;
        color: #fff;
        cursor: pointer;
    }

    .user-info .user-logo {
        position: absolute;
        left: 0;
        top: 15px;
        width: 40px;
        height: 40px;
        border-radius: 50%;
    }

    .el-dropdown-menu__item {
        text-align: center;
    }

    .expire-prompt {
        position: absolute;
        right: 0px;
        background: #fff;
        color: black;
        min-width: 150px;
        border-radius: 2px;
        border: 1px solid rgb(209, 219, 229);
        padding: 20px;
        z-index: 2000;
    }

    .expire-prompt div {
        text-align: center;
        margin: 0;
        line-height: 4em;
    }

    .dropdown-menu {
        min-width: 100px;
    }

</style>