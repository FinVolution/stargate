export default {

    saveEnvironment(environment) {
        localStorage.setItem("stargate-environment", environment);
    },

    readEnvironment() {
        return localStorage.getItem("stargate-environment");
    },

    removeEnvironment() {
        localStorage.removeItem("stargate-environment");
    },

    saveAppId(appId) {
        localStorage.setItem("stargate-app-id", appId);
    },

    readAppId() {
        return localStorage.getItem("stargate-app-id");
    },

    removeAppId() {
        localStorage.removeItem("stargate-app-id");
    }

}