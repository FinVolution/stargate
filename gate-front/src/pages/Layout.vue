<template>
    <div class="layout-wrapper">
        <vheader title="Stargate发布系统" icon="el-icon-upload"></vheader>
        <vsiderbar></vsiderbar>
        <div class="main-content" @scroll.passive="onScroll()">
            <transition name="move" mode="out-in">
                <router-view :scrollTop="scrollTop"></router-view>
            </transition>
            <!--<vfooter></vfooter>-->
        </div>
    </div>
</template>

<script>
//    import vheader from 'pauth-vue-support/components/Header.vue';
    import vheader from '../components/Header.vue'
    import vsiderbar from '../components/SiderBar.vue';
    import vfooter from '../components/Footer.vue';
    import {mapGetters} from 'vuex'

    export default {
        data: function () {
            return {
                scrollTop: 0
            }
        },
        components: {
            vheader, vsiderbar, vfooter
        },
        computed: {
            ...mapGetters({
                promptMessage: 'getPromptMessage'
            })
        },
        methods: {
            onScroll: function () {
                let documentView = document.getElementsByClassName("main-content")[0];
                if (documentView != null) {
                    this.scrollTop = documentView.scrollTop;
                }
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

    @import '../assets/stargate.css';

    .main-content {
        position: absolute;
        left: 140px;
        right: 0;
        top: 70px;
        bottom: 0;
        overflow-y: scroll;
    }

</style>