<template>
    <div class="content-panel">
        <div>
            <span class="iframe-title">容器云平台使用说明</span>
            <el-button v-show="showTop" type="primary" class="back-to-top-btn" icon="el-icon-arrow-up" @click="toTop"
                       circle></el-button>
        </div>
    </div>
</template>

<script>
    export default {
        props: {
            scrollTop: Number
        },
        data() {
            return {
                dParams: 20,
                scrollState: 0
            }
        },
        computed: {
            showTop: function () {
                let value = this.scrollTop > 800 ? true : false;
                return value;
            }
        },
        methods: {
            toTop() {
                if (this.scrollState) {
                    return;
                }
                this.scrollState = 1;
                let _this = this;
                this.time = setInterval(function () {
                    _this.gotoTop(_this.scrollTop - _this.dParams)
                }, 10);
            },
            gotoTop(distance) {
                this.dParams += 20;
                distance = distance > 0 ? distance : 0;
                let documentView = document.getElementsByClassName("main-content")[0];
                documentView.scrollTop = distance;
                if (this.scrollTop < 10) {
                    clearInterval(this.time);
                    this.dParams = 20;
                    this.scrollState = 0;
                }
            }
        }
    }
</script>

<style>
    .iframe-title {
        color: #333;
        font-size: 26px;
        font-weight: bold;
        margin-left: 18px;
    }

    .back-to-top-btn {
        position: fixed;
        bottom: 20px;
        right: 30px;
        z-index: 99;
    }
</style>