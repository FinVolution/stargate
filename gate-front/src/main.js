/**
 * Created by huangyinhuang on 6/15/2017.
 */
import Vue from 'vue'

import VueRouter from 'vue-router'
Vue.use(VueRouter);

import VueLodash from 'vue-lodash'
Vue.use(VueLodash)

import ElementUI from 'element-ui'
import "./assets/theme/babyblue/index.css";
Vue.use(ElementUI);
// import 'element-ui/lib/theme-chalk/index.css'

import 'jquery'

import axios from 'axios'
import router from './router'
import store from './store'

import pauthVueSupport from 'pauth-vue-support'

/**
 * enable axios ajax call in the vue component
 * please see the usage example in the ./pages/pages/demo/Ajax.vue
 * @type {AxiosStatic}
 */
Vue.prototype.$http = axios;

/**
 * enable the development mode
 * @type {boolean}
 */
Vue.config.devtools = process.env.NODE_ENV !== 'production';

/**
 * initialize the vue app with vuex store and vue router
 */
new Vue({
    store,
    router,
}).$mount('#app');

