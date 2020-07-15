import Vue from 'vue'
import Vuex from 'vuex'
import stargate from './model/stargate'
import pauth from 'pauth-vue-support/store/pauth'

Vue.use(Vuex);

/**
 * detect current environment and set the debug configuration for vue store
 */
const debug_mode = process.env.NODE_ENV !== 'production';

/**
 * initialize the vuex store with actions/getters/modules
 */
export default new Vuex.Store({
    modules: {
        stargate,
        pauth
    },
    strict: debug_mode
})
