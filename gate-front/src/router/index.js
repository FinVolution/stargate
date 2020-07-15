import Vue from 'vue'
import Router from 'vue-router'

Vue.use(Router);

import Layout from '../pages/Layout.vue'
import Login from 'pauth-vue-support/components/Login.vue'

import AppList from '../pages/applist/index.vue'
import ImageList from '../pages/imagelist/index.vue'
import EnvList from '../pages/envlist/index.vue'
import ReleaseGroup from '../pages/releasegroup/index.vue'
import InstanceStatus from '../pages/instancestatus/index.vue'
import InstanceList from '../pages/instancelist/index.vue'
import ReleaseHistory from '../pages/releasehistory/index.vue'
import ReleaseRecord from '../pages/releaserecord/index.vue'
import RunningStatus from '../pages/runningstatus/index.vue'
import AppQuota from '../pages/appQuota/index.vue'
import AuditLogList from '../pages/auditloglist/index.vue'
import ApplyList from '../pages/applylist/index.vue'
import Help from '../pages/help/index.vue'
import IpList from '../pages/iplist/index.vue'
import ResourceList from '../pages/resourcelist/index.vue'
import StaticResource from '../pages/staticresource/index.vue'
import InstanceTransfer from '../pages/instancetransfer/index.vue'
import CloudInstance from '../pages/cloudinstance/index.vue'
import InstanceCount from '../pages/instancecount/index.vue'
import AppManager from '../pages/appmanager/index.vue'
import DnsList from '../pages/dnslist/index.vue'
import SystemManagement from '../pages/systemmanagement/index.vue'

export default new Router({
    mode: 'hash', // mode option: 1. hash (default), 2. history
    routes: [{
        path: '/',
        name: 'base',
        component: Layout,
        redirect: 'apps',
        children: [{
            path: 'help',
            name: 'help',
            component: Help
        }, {
            path: 'apps',
            name: 'apps',
            component: AppList
        }, {
            path: 'images',
            name: 'images',
            component: ImageList
        }, {
            path: 'groups',
            name: 'groups',
            component: ReleaseGroup
        }, {
            path: 'instances',
            name: 'instances',
            component: InstanceList
        }, {
            path: 'instancestatus',
            name: 'instancestatus',
            component: InstanceStatus
        }, {
            path: 'releasehistory',
            name: 'releasehistory',
            component: ReleaseHistory
        }, {
            path: 'quotas',
            name: 'quotas',
            component: AppQuota
        }, {
            path: 'applies',
            name: 'applies',
            component: ApplyList
        }, {
            path: 'staticresources',
            name: 'staticresources',
            component: StaticResource
        }, {
            path: 'dnslist',
            name: 'dnslist',
            component: DnsList
        }]
    }, {
        path: '/system',
        name: 'system',
        component: SystemManagement,
        children: [{
            path: 'envs',
            name: 'envs',
            component: EnvList
        }, {
            path: 'releaserecord',
            name: 'releaserecord',
            component: ReleaseRecord
        }, {
            path: 'runningstatus',
            name: 'runningstatus',
            component: RunningStatus
        }, {
            path: 'auditlogs',
            name: 'auditlogs',
            component: AuditLogList
        }, {
            path: 'ips',
            name: 'ips',
            component: IpList
        }, {
            path: 'resources',
            name: 'resources',
            component: ResourceList
        }, {
            path: 'instancetransfer',
            name: 'instancetransfer',
            component: InstanceTransfer
        }, {
            path: 'cloudinstance',
            name: 'cloudinstance',
            component: CloudInstance
        }, {
            path: 'instancecount',
            name: 'instancecount',
            component: InstanceCount
        }, {
            path: 'appmanager',
            name: 'appmanager',
            component: AppManager
        }]
    }, {
        path: '/404',
        name: '404',
        component: Layout
    }, {
        path: '/login',
        name: 'Login',
        component: Login,
    }],
    linkActiveClass: 'active'
})
