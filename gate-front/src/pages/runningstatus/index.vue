<template>
    <div class="content-panel">
        <div v-for="item in releaseStatus" :key="item.environment" class="release-status">
            <div class="release-status-item">
                <div class="release-status-item-env">{{item.environment}}</div>
            </div>
            <div class="release-status-item">
                <i class="el-icon-loading" style="color: #E6A23C"></i>
                <p>Running：{{item.runningCount}}</p>
            </div>
            <div class="release-status-item">
                <i class="el-icon-success" style="color: #67C23A"></i>
                <p>Success：{{item.successCount}}</p>
            </div>
            <div class="release-status-item">
                <i class="el-icon-error" style="color: #F56C6C"></i>
                <p>Fail：{{item.failCount}}</p>
            </div>
        </div>
    </div>
</template>

<script>
    import {mapGetters, mapActions} from 'vuex';

    export default {
        data: function() {
            return {
            }
        },
        computed: {
            ...mapGetters({
                releaseStatus: 'getReleaseStatus'
            })
        },
        created: function() {
            this.$store.dispatch('fetchReleaseStatus');
        },
        mounted: function () {
            this.internalTimer = setInterval(this.onInterval.bind(this), 3000);
        },
        beforeDestroy: function () {
            clearInterval(this.internalTimer);
        },
        methods: {
            onInterval: function () {
                this.$store.dispatch('fetchReleaseStatus');
            }
        }
    }
</script>

<style>
    .release-status {
        display: flex;
        padding: 30px;
        margin-bottom: 20px;
        box-shadow: 0 2px 12px 0 rgba(0,0,0,.1);
    }
    .release-status .release-status-item {
        width: 25%;
        text-align: center;
        display: inline-block;
        border-right: 1px solid #eeeff1;
    }
    .release-status .release-status-item:last-child {
        border-right: none;
    }
    .release-status .release-status-item .release-status-item-env {
        padding: 15px 0;
        font-size: 26px;
        color: #16B6D7;
    }
    .release-status .release-status-item i {
        margin-top: 5px;
        font-size: 24px;
    }
    .release-status .release-status-item p {
        font-size: 14px;
        color: #99a9c0;
        margin: 1em 0 0 0;
    }

</style>